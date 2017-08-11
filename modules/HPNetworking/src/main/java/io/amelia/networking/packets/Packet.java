package io.amelia.networking.packets;

import io.amelia.lang.PacketValidationException;
import io.amelia.support.LibEncrypt;
import io.amelia.support.Objs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.lang.reflect.Field;

public abstract class Packet
{
	public static byte[] readBlob( ByteBuf buf )
	{
		short length = buf.readShort();
		Objs.notNegative( length, "Key is smaller than nothing. Strange!" );
		byte[] bytes = new byte[length];
		buf.readBytes( bytes );
		return bytes;
	}

	public static void writeBlob( ByteBuf buf, byte[] bytes )
	{
		buf.writeShort( bytes.length );
		buf.writeBytes( bytes );
	}

	public String packetId;
	public ByteBuf payload;
	public boolean sent = false;
	public long sentTime;

	public Packet()
	{
		packetId = LibEncrypt.uuid();
	}

	protected abstract void decode( ByteBuf in );

	protected abstract void encode( ByteBuf out );

	public void encode()
	{
		payload = Unpooled.buffer();
		encode( payload );
	}

	protected Object getField( String field ) throws PacketValidationException
	{
		try
		{
			Field varField = getClass().getDeclaredField( field );
			varField.setAccessible( true );
			return varField.get( this );
		}
		catch ( Throwable e )
		{
			throw new PacketValidationException( this, "Packet validation failed for the following reason.", e );
		}
	}

	public String getPacketId()
	{
		return packetId;
	}

	public long getSentTime()
	{
		return sentTime;
	}

	/**
	 * If true, the network manager will process the packet immediately when received, otherwise it will queue it for
	 * processing. Currently true for: Disconnect, LoginSuccess, KeepAlive, ServerQuery/Info, Ping/Pong
	 */
	public boolean hasPriority()
	{
		return false;
	}

	protected void notEmpty( String field ) throws PacketValidationException
	{
		if ( Objs.isEmpty( getField( field ) ) )
			throw new PacketValidationException( this, "Packet validation failed! The variable '" + field + "' must not be empty." );
	}

	protected void notNull( String field ) throws PacketValidationException
	{
		if ( Objs.isNull( getField( field ) ) )
			throw new PacketValidationException( this, "Packet validation failed! The variable '" + field + "' must not be empty." );
	}

	/**
	 * Returns a string formatted as comma separated [field]=[value] values. Used by Minecraft for logging purposes.
	 */
	public String serialize()
	{
		return "";
	}

	public String toString()
	{
		return this.getClass().getSimpleName();
	}

	public abstract void validate() throws PacketValidationException;
}
