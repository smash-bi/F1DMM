package smash.f1.dmm.test.disk;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelDataWriter implements DataWriter
{
	private FileChannel fileChannel;
	private RandomAccessFile file;
	
	public void initialize( String aFileName, int aTotalSize ) throws IOException 
	{
		file = new RandomAccessFile(aFileName, "rw");
		fileChannel = file.getChannel();
	}

	public void write(ByteBuffer aBuffer, boolean shouldFlush) throws IOException
	{
		fileChannel.write(aBuffer);
		if (shouldFlush)
		{
			fileChannel.force(false);
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
