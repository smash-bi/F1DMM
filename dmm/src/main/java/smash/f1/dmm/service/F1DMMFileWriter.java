package smash.f1.dmm.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.agrona.DirectBuffer;

public class F1DMMFileWriter 
{
    private static final int INITIAL_MAP_SIZE = 10_000;
    private static final String CURRENT_FILE_NAME_MAPPING_PROPERTY_NAME = "current.properties";
    private final Path fileStore;
    private final HashMap<UUID, FileChannel> fileBlockChannelByFileIdLookup;
    private final HashMap<UUID, AtomicLong> fileBlockSizeByFileIdLookup;
    private final long maxFileSize;
    private final HashMap<UUID, UUID> currentFileBlockByFileIdLookup = new HashMap<>();
    
    public F1DMMFileWriter(Path aFileStore, long aMaxFileSize ) 
    {
        fileStore = aFileStore;
        fileBlockChannelByFileIdLookup = new HashMap<>(INITIAL_MAP_SIZE);
        fileBlockSizeByFileIdLookup = new HashMap<>(INITIAL_MAP_SIZE);

        maxFileSize = aMaxFileSize;
    }

    public boolean write(final long aFileHighUUID, final long aFileLowUUID, final DirectBuffer aMessage, final int anOffset, final int aLength) 
    {
    	return write( new UUID(aFileHighUUID,aFileLowUUID), aMessage, anOffset, aLength );
    }
    
    public boolean write(final UUID aFileUUID, final DirectBuffer aMessage, final int anOffset, final int aLength) 
    {    	
        FileChannel fileChannel = null;
        try 
        {
        	fileChannel = getCurrentFileBlock(aFileUUID);
        } 
        catch (IOException io) 
        {
            return false;
        }
        if (fileChannel == null) 
        {
            return false;
        }
        AtomicLong fileSize = fileBlockSizeByFileIdLookup.get(aFileUUID);
        long currentSize = fileSize.addAndGet(aLength);
        if (currentSize == aLength) 
        {
            handleFirstRecord(aFileUUID);
        }
        try 
        {
        	// if new data size addition will cause the current file size to exceed max size
            if (currentSize >= maxFileSize) 
            {
                // close the old file channel
                fileChannel.close();
                UUID currentFileBlockName = currentFileBlockByFileIdLookup.get(aFileUUID);
                createFileBlockPath(aFileUUID, currentFileBlockName);

                currentFileBlockByFileIdLookup.remove(aFileUUID);
                Files.delete(Paths.get(fileStore.toString(), aFileUUID.toString(), CURRENT_FILE_NAME_MAPPING_PROPERTY_NAME));

                // roll file and notify
                createFileBlock(aFileUUID);
                
                // update file size information
                fileSize = fileBlockSizeByFileIdLookup.get(aFileUUID);
            }
            
            ByteBuffer bytesToWrite = aMessage.byteBuffer();
			bytesToWrite.limit(anOffset + aLength);
			bytesToWrite.position(anOffset);
            int byteswritten = fileChannel.write(bytesToWrite);
        } 
        catch (IOException io) 
        {
            return false;
        }
        return true;
    }

    private void handleFirstRecord(UUID anUUID) 
    {
        // it means this is the first record
        UUID currentFileBlockId = currentFileBlockByFileIdLookup.get(anUUID);
        createFileBlockPath(anUUID, currentFileBlockId);
    }

    private final FileChannel getCurrentFileBlock(UUID aFileUUID) throws IOException 
    {
        if (!fileBlockChannelByFileIdLookup.containsKey(aFileUUID)) 
        {
        	// register the profile
            return createFileBlock(aFileUUID);
        }
        else
        {
        	return fileBlockChannelByFileIdLookup.get(aFileUUID);
        }
    }

    private final FileChannel createFileBlock(UUID aFileId) throws IOException 
    {
        UUID currentFileBlockId = currentFileBlockByFileIdLookup.get(aFileId);
        if (currentFileBlockId == null) 
        {
            Path currentFileTracker = Paths.get(fileStore.toString(), aFileId.toString(), CURRENT_FILE_NAME_MAPPING_PROPERTY_NAME);
            if (Files.exists(currentFileTracker, LinkOption.NOFOLLOW_LINKS)) 
            {
                currentFileBlockId = UUID.fromString(new String(Files.readAllBytes(currentFileTracker)));
            }
            else 
            {
                currentFileBlockId = UUID.randomUUID();
                currentFileBlockByFileIdLookup.put(aFileId, currentFileBlockId);

                Files.createDirectories(currentFileTracker.getParent());
                // write new Block file to current tracker           
                Files.write(currentFileTracker, currentFileBlockId.toString().getBytes(), StandardOpenOption.CREATE);
            }
        }
        Path fileBlockPath = createFileBlockPath(aFileId, currentFileBlockId);

        boolean fileExists = Files.exists(fileBlockPath, LinkOption.NOFOLLOW_LINKS);
        long fileSize = 0L;
        if (fileExists) 
        {
            fileSize = Files.size(fileBlockPath);
        } 
        else 
        {
            Files.createDirectories(fileBlockPath.getParent());
            fileBlockPath = Files.createFile(fileBlockPath);
        }
        FileChannel fileChannel = FileChannel.open(fileBlockPath, StandardOpenOption.APPEND);
        fileBlockChannelByFileIdLookup.put(aFileId, fileChannel);

        // reset file size tracker
        if (!fileBlockSizeByFileIdLookup.containsKey(aFileId)) 
        {
            fileBlockSizeByFileIdLookup.put(aFileId, new AtomicLong(fileSize));
        } 
        else 
        {
            fileBlockSizeByFileIdLookup.get(aFileId).set(fileSize);
        }

        return fileChannel;
    }

    private final Path createFileBlockPath(UUID datasetId, UUID blockId) 
    {
        return Paths.get(fileStore.toString(), datasetId.toString(), blockId.toString());
    }

    public void close() 
    {
        for (Map.Entry<UUID, FileChannel> entry : fileBlockChannelByFileIdLookup.entrySet()) 
        {
            try 
            {
                entry.getValue().close();
            } 
            catch (Throwable t) 
            {
            }
        }
    }
}