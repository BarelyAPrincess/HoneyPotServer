package io.amelia.lang;

import com.sun.istack.internal.NotNull;
import io.amelia.networking.packets.Packet;

public class PacketValidationException extends NetworkException
{
	private final Packet packet;

	public PacketValidationException( @NotNull Packet packet, @NotNull String message )
	{
		super( message );
		this.packet = packet;
	}

	public PacketValidationException( @NotNull Packet packet, @NotNull String message, @NotNull Throwable cause )
	{
		super( message, cause );
		this.packet = packet;
	}

	public PacketValidationException( @NotNull Packet packet, @NotNull Throwable cause )
	{
		super( cause );
		this.packet = packet;
	}

	public Packet getPacket()
	{
		return packet;
	}
}
