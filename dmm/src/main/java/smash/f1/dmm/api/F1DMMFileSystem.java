package smash.f1.dmm.api;

import io.aeron.Aeron;
import io.aeron.Publication;

import static smash.f1.dmm.api.F1DMMConstants.*;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.agrona.ExpandableArrayBuffer;

/**
 * F1DMMFileSystem is a client API interfacing into F1 Data Memory Management File System
 * to create file, append data to file, etc.
 */
public final class F1DMMFileSystem 
{
	private Aeron aeron;
	private Publication publication;
	private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();
	private final Aeron.Context aeronContext;
	private final AtomicBoolean disposed = new AtomicBoolean( false );
	private final String channel;
	private final int streamId;
	
	/**
	 * create F1 DMM File System
	 * @param aChannel channel to communicate with the file system service
	 * @param aStreamId stream to communicate with the file system service
	 */
	public F1DMMFileSystem( final String aChannel, final int aStreamId )
	{
		channel = aChannel;
		streamId = aStreamId;
		aeronContext = new Aeron.Context();
		aeron = Aeron.connect( aeronContext );
		publication = aeron.addPublication( channel, streamId );
	}
	
	/**
	 * append data from the given byte buffer to the given file. if the service is back pressured
	 * then this method will keep trying until it is successful
	 */
	public boolean append( final long anUUIDHigh, final long anUUIDLow, 
			final ByteBuffer aBuffer, final int anOffset, final int aLength )
	{
		buffer.putLong(0, anUUIDHigh);
		buffer.putLong(8, anUUIDLow);
		buffer.putBytes(16, aBuffer, anOffset, aLength);
		while( !disposed.get() )
		{
			long status = publication.offer(buffer, 0, 16+aLength);
			if ( status >= 0 )
			{
				return true;
			}
			else if ( status == APPEND_FAILED_CLOSED )
			{
				if ( !disposed.get() )
				{
					aeron.close();
					publication.close();
					aeron = Aeron.connect( aeronContext );
					publication = aeron.addPublication( channel, streamId );
				}
			}
			else if ( status == APPEND_FAILED_NOT_CONNECTED )
			{
				return false;
			}
			else if ( status == APPEND_FAILED_BACK_PRESSURED )
			{
				System.out.println( "BACK PRESSURED" );
			}
		}
		return false;
	}
	
	/**
	 * dispose the client API
	 */
	public void dispose()
	{
		disposed.set(true);
		publication.close();
		aeron.close();
	}
}
