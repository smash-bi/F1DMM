package smash.f1.dmm.test;

import java.nio.ByteBuffer;

import smash.f1.dmm.api.F1DMMConstants;
import smash.f1.dmm.api.F1DMMFileSystem;

public class F1DMMPerformanceTest 
{
	public static void main( String[] args )
	{
		try
		{
			F1DMMFileSystem fileSystem = new F1DMMFileSystem( args[0], Integer.valueOf( args[1] ) );
			ByteBuffer data = ByteBuffer.allocateDirect( 128 );
			for( long count=0; count<16; count++ )
			{
				data.putLong( count );
			}
			long time = System.nanoTime();
			for( int count=0; count<10000000; count++ )
			{
				data.rewind();
				boolean status = fileSystem.append(count, count, data, 0, 128);
				/*
				if ( result != F1DMMConstants.APPEND_SUCCESS )
				{
					System.out.println( "FAILED " + result );
				}
				*/
			}
			time = System.nanoTime() - time;
			System.out.println( "Nano time taken " + time );
		}
		catch( Throwable t )
		{
			t.printStackTrace();
		}
	}
}
