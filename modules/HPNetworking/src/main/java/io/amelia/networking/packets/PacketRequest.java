package io.amelia.networking.packets;

import com.sun.istack.internal.NotNull;
import io.amelia.support.Objs;

import java.util.function.Supplier;

/**
 * @param <T> The Packet subclass
 * @param <R> The PacketResponse type
 */
@SuppressWarnings( "unchecked" )
public abstract class PacketRequest<T extends PacketRequest, R> extends RawPacket
{
	private final Supplier<R> responsePacketSupplier;

	// 15 Second Response Timeout
	private long timeout = 15;

	public PacketRequest( @NotNull Supplier<R> responsePacketSupplier )
	{
		Objs.notNull( responsePacketSupplier );
		this.responsePacketSupplier = responsePacketSupplier;
	}

	public R getResponsePacket()
	{
		return responsePacketSupplier.get();
	}

	public long getTimeout()
	{
		return timeout;
	}

	public T setTimeout( long timeout )
	{
		if ( timeout > 300 )
			throw new IllegalArgumentException( "Timeout has a max timeout of 5 minutes! (300 seconds)" );
		this.timeout = timeout;
		return ( T ) this;
	}
}
