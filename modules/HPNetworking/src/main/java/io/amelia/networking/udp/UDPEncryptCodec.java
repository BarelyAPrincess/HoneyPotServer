/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.networking.udp;

import io.amelia.lang.NetworkException;
import io.amelia.networking.NetworkLoader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.util.List;

/**
 * Translates the UDP Packet for network encryption.
 * Bypasses encryption if KeyPair is null.
 */
public class UDPEncryptCodec extends MessageToMessageCodec<ByteBuf, ByteBuf>
{
	private static final String ALGORITHM = "RSA";

	private final KeyPair keyPair;

	UDPEncryptCodec() throws NetworkException
	{
		keyPair = NetworkLoader.UDP().getRSA();
	}

	@Override
	protected void encode( ChannelHandlerContext ctx, ByteBuf msg, List<Object> out ) throws Exception
	{
		if ( keyPair == null )
			out.add( msg );
		else
		{
			byte[] dest = new byte[msg.readableBytes()];
			msg.readBytes( dest );

			final Cipher cipher = Cipher.getInstance( ALGORITHM );
			cipher.init( Cipher.ENCRYPT_MODE, keyPair.getPublic() );
			dest = cipher.doFinal( dest );

			out.add( Unpooled.wrappedBuffer( dest ) );
		}
	}

	@Override
	protected void decode( ChannelHandlerContext ctx, ByteBuf msg, List<Object> out ) throws Exception
	{
		if ( keyPair == null )
			out.add( msg );
		else
		{
			byte[] src = new byte[msg.readableBytes()];
			msg.readBytes( src );


			final Cipher cipher = Cipher.getInstance( ALGORITHM );
			cipher.init( Cipher.DECRYPT_MODE, keyPair.getPrivate() );
			src = cipher.doFinal( src );

			out.add( Unpooled.wrappedBuffer( src ) );
		}
	}
}
