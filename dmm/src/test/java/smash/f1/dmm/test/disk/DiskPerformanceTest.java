package smash.f1.dmm.test.disk;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class DiskPerformanceTest 
{
	private static long StartTime;
	private static int TotalNoOfTests;
	private static AtomicInteger FinishCounter = new AtomicInteger();
	
	private static class Test implements Runnable
	{
		private DataWriter dataWriter;
		private ByteBuffer data;
		private int noOfRecords;
		private boolean flush;
		private double sizeInMB;
		
		private Test( DataWriter aDataWriter, int aSizeOfRecord, int aNoOfRecords, boolean shouldFlush )
		{
			sizeInMB = (double)(aSizeOfRecord * aNoOfRecords)/(double)1e+6;
			data = ByteBuffer.allocateDirect(aSizeOfRecord);
			for(int count=0; count<aSizeOfRecord; count++)
			{
				data.put((byte)count);
			}
			data.flip();
			dataWriter = aDataWriter;
			noOfRecords = aNoOfRecords;
		}
		
		public void run() 
		{
			try
			{
				for(int count=0; count<noOfRecords; count++)
				{
					dataWriter.write(data, flush);
					data.position(0);
				}
				dataWriter.close();
				if (FinishCounter.addAndGet(1)==TotalNoOfTests)
				{
					long time = System.nanoTime() - StartTime;
					double throughput = (sizeInMB*(double)TotalNoOfTests)/((double)time/(double)1e+9);
					System.out.println("Throughput " + throughput + "MBps");
				}
			}
			catch( Throwable t )
			{
				t.printStackTrace();
			}
		}
		
	}
	
	/**
	 * write data to disk 
	 * @param args
	 */
	public static void main( String[] args )
	{
		try
		{
			ArrayList<Test> tests = new ArrayList<Test>();
			String writerType = args[0];
			int sizeOfRecords = Integer.parseInt( args[1] );
			int noOfRecords = Integer.parseInt( args[2] );
			boolean flush = Boolean.getBoolean( args[3] );
			int totalSize = sizeOfRecords * noOfRecords;
			for(int count=4; count<args.length; count++)
			{
				DataWriter writer = null;
				if ("MemoryMapped".equals( writerType ))
				{
					writer = new MappedFileChannelDataWriter();
				}
				else
				{
					writer = new FileChannelDataWriter();
				}
				File file = new File( args[count] );
				if ( file.exists() )
				{
					file.delete();
				}
				writer.initialize( args[count], totalSize );
				tests.add( new Test( writer, sizeOfRecords, noOfRecords, flush ) );
			}
			StartTime = System.nanoTime();
			TotalNoOfTests = tests.size();
			for( Test test: tests )
			{
				Thread thread = new Thread(test);
				thread.start();
			}
		}
		catch( Throwable t )
		{
			t.printStackTrace();
		}
	}
}
