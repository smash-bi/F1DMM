package smash.f1.dmm.test;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.agrona.concurrent.UnsafeBuffer;

import smash.f1.dmm.service.F1DMMFileWriter;

public class F1DMMFileWriterTest 
{
	private final static String	TEST_DATA 
								= "This is the test data for ";
	
	public void test( Path aFileStorePath, long aMaxFileSize, int aNoOfRecords, int aNoOfFiles )
	{
		F1DMMFileWriter writer = new F1DMMFileWriter( aFileStorePath, aMaxFileSize );
		UUID[] fileUUIDs = new UUID[aNoOfFiles];
		UnsafeBuffer[] dataBuffers = new UnsafeBuffer[aNoOfFiles];
		for( int fileCount=0; fileCount<aNoOfFiles; fileCount++ )
		{
			fileUUIDs[fileCount] = UUID.randomUUID();
			String data = TEST_DATA + fileCount + "\n";
			dataBuffers[fileCount] = new UnsafeBuffer( ByteBuffer.wrap( data.getBytes() ) );
		}
		for( int count=0; count<aNoOfRecords; count++ )
		{
			for( int fileCount=0; fileCount<fileUUIDs.length; fileCount++ )
			{
				UnsafeBuffer dataBuffer = dataBuffers[fileCount];
				writer.write(fileUUIDs[fileCount], dataBuffer, 0, dataBuffer.capacity());
			}
		}
		writer.close();
	}
	
	public static void main( String[] args )
	{
		F1DMMFileWriterTest test = new F1DMMFileWriterTest();
		test.test( Paths.get(args[0] ), 
				Long.parseLong(args[1]), 
				Integer.parseInt(args[2]), Integer.parseInt(args[3]));
	}
}
