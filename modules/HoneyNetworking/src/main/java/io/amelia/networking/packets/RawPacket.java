/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.networking.packets;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import io.amelia.lang.ApplicationException;
import io.amelia.lang.NetworkException;
import io.amelia.lang.ReportingLevel;
import io.amelia.networking.NetworkLoader;
import io.amelia.networking.udp.UDPHandler;
import io.amelia.networking.udp.UDPPacketHandler;
import io.amelia.support.Encrypt;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Provides a raw byte-based packet
 */
public abstract class RawPacket
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

	public boolean sent = false;
	public long sentTime;
	private String classId;
	private String packetId;
	private ByteBuf payload;

	private PacketPayload state;
	private Map<PacketPayload, ByteBuf> statePayloads = new HashMap<>();

	RawPacket( String packetId )
	{
		this.packetId = packetId;
	}

	public RawPacket()
	{
		NetworkLoader.registerPacket( this );
		packetId = Encrypt.hash();
	}

	public boolean checkUDPState( UDPHandler.ClusterRole clusterRole )
	{
		return true;
	}

	protected abstract void decode( ByteBuf in );

	/**
	 * Signals the subclass to deserialize the payload.
	 */
	public void decode()
	{
		if ( payload != null )
		{
			decode( payload );
			payload.readerIndex( 0 );
		}
	}

	protected abstract void encode( ByteBuf out );

	/**
	 * Signals the subclass to serialize the packet.
	 */
	public void encode()
	{
		payload = Unpooled.directBuffer();
		String packetClassIdentifier = getClassId();
		if ( packetClassIdentifier.length() != 32 )
			throw new ApplicationException.Runtime( ReportingLevel.E_STRICT, "We expected the Packet Class Identifier to be 32-bytes but it was " + packetClassIdentifier.length() );
		payload.writeBytes( Strs.stringToBytesUTF( packetClassIdentifier ) );
		if ( packetId.length() != 32 )
			throw new ApplicationException.Runtime( ReportingLevel.E_STRICT, "We expected the Unique Packet Identifier to be 32-bytes but it was " + packetId.length() );
		payload.writeBytes( Strs.stringToBytesUTF( packetId ) );
		encode( payload );
	}

	public String getClassId()
	{
		if ( classId == null )
			classId = Encrypt.md5Hex( getClass().getName() );
		return classId;
	}

	protected Object getField( String field ) throws NetworkException.PacketValidation
	{
		try
		{
			Field varField = getClass().getDeclaredField( field );
			varField.setAccessible( true );
			return varField.get( this );
		}
		catch ( Throwable e )
		{
			throw new NetworkException.PacketValidation( this, "Packet validation failed for the following reason.", e );
		}
	}

	public String getPacketId()
	{
		return packetId;
	}

	public ByteBuf getPayload()
	{
		return payload;
	}

	public void setPayload( @Nonnull ByteBuf payload )
	{
		this.payload = payload == null ? null : payload.slice();
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

	protected void notEmpty( String field ) throws NetworkException.PacketValidation
	{
		if ( Objs.isEmpty( getField( field ) ) )
			throw new NetworkException.PacketValidation( this, "Packet validation failed! The variable '" + field + "' must not be empty." );
	}

	protected void notNull( String field ) throws NetworkException.PacketValidation
	{
		if ( Objs.isNull( getField( field ) ) )
			throw new NetworkException.PacketValidation( this, "Packet validation failed! The variable '" + field + "' must not be empty." );
	}

	public abstract void processPacket( UDPPacketHandler packetHandler );

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

	public abstract void validate() throws NetworkException.PacketValidation;

	public enum PacketPayload
	{
		/**
		 * Packet payload is a REQUEST and will be/has been SENT. (Origin)
		 */
		REQUEST_OUT,
		/**
		 * Packet payload is a REQUEST and has been RECEIVED. (Receiver)
		 */
		REQUEST_IN,
		/**
		 * Packet payload is a RESPONSE and will be/has been SENT. (Receiver)
		 */
		RESPONSE_OUT,
		/**
		 * Packet payload is a RESPONSE and has been RECEIVED. (Origin)
		 */
		RESPONSE_IN
	}
}
