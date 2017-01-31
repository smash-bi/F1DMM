package smash.f1.dmm.api;

import io.aeron.Publication;

public class F1DMMConstants 
{
	public final static long	APPEND_FAILED_BACK_PRESSURED
								= Publication.BACK_PRESSURED;
	public final static long	APPEND_FAILED_NOT_CONNECTED
								= Publication.NOT_CONNECTED;
	public final static long	APPEND_FAILED_ADMIN_ACTION
								= Publication.ADMIN_ACTION;
	public final static long	APPEND_FAILED_CLOSED
								= Publication.CLOSED;
								
}
