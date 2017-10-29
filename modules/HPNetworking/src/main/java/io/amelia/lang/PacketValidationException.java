package io.amelia.lang;

import com.sun.istack.internal.NotNull;
import io.amelia.networking.packets.RawPacket;

public class PacketValidationException extends NetworkException
{
	private final RawPacket packet;

	public PacketValidationException( @NotNull RawPacket packet, @NotNull String message )
	{
		super( message );
		this.packet = packet;
	}

	public PacketValidationException( @NotNull RawPacket packet, @NotNull String message, @NotNull Throwable cause )
	{
		super( message, cause );
		this.packet = packet;
	}

	public PacketValidationException( @NotNull RawPacket packet, @NotNull Throwable cause )
	{
		super( cause );
		this.packet = packet;
	}

	public RawPacket getPacket()
	{
		return packet;
	}
}
