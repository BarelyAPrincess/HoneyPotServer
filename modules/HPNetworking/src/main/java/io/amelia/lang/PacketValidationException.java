package io.amelia.lang;

import com.sun.istack.internal.NotNull;
import io.amelia.networking.packets.Packet;

public class PacketValidationException extends NetworkException
{
	private final Packet packet;

	public PacketValidationException( @NotNull String message, @NotNull Packet packet )
	{
		super( message );
		this.packet = packet;
	}

	public PacketValidationException( @NotNull String message, @NotNull Throwable cause, @NotNull Packet packet )
	{
		super( message, cause );
		this.packet = packet;
	}

	public PacketValidationException( @NotNull Throwable cause, @NotNull Packet packet )
	{
		super( cause );
		this.packet = packet;
	}

	public Packet getPacket()
	{
		return packet;
	}
}
