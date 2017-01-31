package smash.f1.dmm.test.disk;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface DataWriter 
{
	public void initialize( String aFileName, int aTotalSize ) throws IOException;
	
	public void write( ByteBuffer aBuffer, boolean shouldFlush ) throws IOException;
	
	public void close() throws IOException;
}
