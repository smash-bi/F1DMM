package smash.f1.dmm.test;

import java.nio.ByteBuffer;
import java.util.UUID;

import smash.f1.dmm.api.F1DMMFileSystem;

public class F1DMMFileSystemAPITest 
{
	private final static String	TEST_DATA 
								= "This is the test data for ";
	
	public void test( String aChannel, int aStreamId, int aNoOfFiles, int aNoOfRecords )
	{
		F1DMMFileSystem api = new F1DMMFileSystem( aChannel, aStreamId );
		UUID[] fileUUIDs = new UUID[aNoOfFiles];
		ByteBuffer[] dataBuffers = new ByteBuffer[aNoOfFiles];
		for( int fileCount=0; fileCount<aNoOfFiles; fileCount++ )
		{
			fileUUIDs[fileCount] = UUID.randomUUID();
			String data = TEST_DATA + fileCount + "\n";
			dataBuffers[fileCount] = ByteBuffer.wrap( data.getBytes() );
		}
		for( int count=0; count<aNoOfRecords; count++ )
		{
			for( int fileCount=0; fileCount<fileUUIDs.length; fileCount++ )
			{
				ByteBuffer dataBuffer = dataBuffers[fileCount];
				UUID uuid = fileUUIDs[fileCount];
				api.append(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(),
						dataBuffer, 0, dataBuffer.capacity());
				dataBuffer.flip();
			}
		}
		api.dispose();
	}
	
	public static void main( String[] args )
	{
		F1DMMFileSystemAPITest test = new F1DMMFileSystemAPITest();
		test.test( args[0], // channel
				Integer.parseInt(args[1]), // stream id
				Integer.parseInt(args[2]), // no of files
				Integer.parseInt(args[3])); // no of records
	}
}
