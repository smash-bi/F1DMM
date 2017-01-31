package smash.f1.dmm.service;

import io.aeron.Aeron;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;
import static io.aeron.logbuffer.FrameDescriptor.*;

import java.nio.file.FileSystems;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SigInt;

public class F1DMMService 
{
	private final static int UUID_BITS_PART_NO_OF_BYTES = Long.SIZE/Byte.SIZE;
	private final static int UUID_NO_OF_BYTES = UUID_BITS_PART_NO_OF_BYTES*2;
	private final F1DMMFileWriter fileWriter;
	private final Aeron aeron;
	private final Subscription subscription;
	private final F1DMMFragmentHandler fragmentHandler = new F1DMMFragmentHandler();
	private final AtomicBoolean disposed = new AtomicBoolean( false );
	
	/**
	 * create F1DMMService 
	 * @param aFileStorePath path where data files are stored 
	 * @param aMaxFileSize maximum file size for the data files
	 * @param aChannel channel to communicate with the file system service
	 * @param aStreamId stream to communicate with the file system service
	 */
	public	F1DMMService( String aFileStorePath, long aMaxFileSize, String aChannel, int aStreamId )
	{
		SigInt.register( ()-> { dispose(); } );
		fileWriter = new F1DMMFileWriter( FileSystems.getDefault().getPath( aFileStorePath ), aMaxFileSize );
		aeron = Aeron.connect( new Aeron.Context() );
		subscription = aeron.addSubscription(aChannel, aStreamId);
	}
	
	/**
	 * start
	 */
	public void	start()
	{
		final IdleStrategy idleStrategy = new BackoffIdleStrategy(
			    100, 10, TimeUnit.MICROSECONDS.toNanos(1), TimeUnit.MICROSECONDS.toNanos(100));
		while (!disposed.get())
		{
		    final int fragmentsRead = subscription.poll(fragmentHandler, 10);
		    idleStrategy.idle(fragmentsRead);
		}
	}
	
	/**
	 * dispose
	 */
	public void dispose()
	{
		disposed.set( true );
	}
	
	/**
	 * F1DMMFragmentHandler handles F1 DMM incoming data
	 */
	private class F1DMMFragmentHandler implements FragmentHandler
	{
		private long currentUUIDHigh;
		private long currentUUIDLow;
		
		@Override
		public void onFragment(DirectBuffer aBuffer, int anOffset, int aLength,
				Header aHeader) 
		{
			byte flags = aHeader.flags();
			int offset = anOffset;
			int length = aLength;
			if( (flags & BEGIN_FRAG_FLAG) == BEGIN_FRAG_FLAG )
			{
				currentUUIDHigh = aBuffer.getLong(offset);
				offset+=UUID_BITS_PART_NO_OF_BYTES;
				currentUUIDLow = aBuffer.getLong(offset);
				offset+=UUID_BITS_PART_NO_OF_BYTES;
				length = length - UUID_NO_OF_BYTES;
			}
			fileWriter.write(currentUUIDHigh, currentUUIDLow, aBuffer, offset, length );
		}
	}
	
	public static void main( String[] args )
	{
		F1DMMService dmmService = new F1DMMService( args[0], Long.parseLong( args[1] ), args[2], Integer.parseInt( args[3] ) );
		dmmService.start();
	}
}
