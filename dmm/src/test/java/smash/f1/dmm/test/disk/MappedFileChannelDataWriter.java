package smash.f1.dmm.test.disk;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MappedFileChannelDataWriter implements DataWriter
{
	private FileChannel fileChannel;
	private RandomAccessFile file;
	private MappedByteBuffer mappedBuffer;
	
	public void initialize( String aFileName, int aTotalSize ) throws IOException 
	{
		file = new RandomAccessFile(aFileName, "rw");
		fileChannel = file.getChannel();
		mappedBuffer = fileChannel.map( FileChannel.MapMode.READ_WRITE, 0, aTotalSize );
	}

	public void write(ByteBuffer aBuffer, boolean shouldFlush) throws IOException
	{
		mappedBuffer.put(aBuffer);
		if (shouldFlush)
		{
			mappedBuffer.force();
		}
	}

	public void close() throws IOException 
	{
		try
		{
			fileChannel.close();
		}
		catch(Throwable t)
		{}
		try
		{
			file.close();
		}
		catch( Throwable t)
		{}
	}

}
