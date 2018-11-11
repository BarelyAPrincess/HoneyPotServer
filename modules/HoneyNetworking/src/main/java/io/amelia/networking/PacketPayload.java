/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.networking;

import com.google.common.base.Charsets;
import io.amelia.support.Strs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;

public class PacketPayload extends ByteBuf
{
	/**
	 * Calculates the number of bytes required to fit the supplied int (0-5) if it were to be read/written using
	 * readVarIntFromBuffer or writeVarIntToBuffer
	 */
	public static int getVarIntSize( int p_150790_0_ )
	{
		return ( p_150790_0_ & -128 ) == 0 ? 1 : ( ( p_150790_0_ & -16384 ) == 0 ? 2 : ( ( p_150790_0_ & -2097152 ) == 0 ? 3 : ( ( p_150790_0_ & -268435456 ) == 0 ? 4 : 5 ) ) );
	}

	private final ByteBuf byteBuf;

	public PacketPayload( ByteBuf byteBuf )
	{
		this.byteBuf = byteBuf;
	}

	@Override
	public int capacity()
	{
		return byteBuf.capacity();
	}

	@Override
	public ByteBuf capacity( int p_capacity_1_ )
	{
		return byteBuf.capacity( p_capacity_1_ );
	}

	@Override
	public int maxCapacity()
	{
		return byteBuf.maxCapacity();
	}

	@Override
	public ByteBufAllocator alloc()
	{
		return byteBuf.alloc();
	}

	@Override
	public ByteOrder order()
	{
		return byteBuf.order();
	}

	@Override
	public ByteBuf order( ByteOrder p_order_1_ )
	{
		return byteBuf.order( p_order_1_ );
	}

	@Override
	public ByteBuf unwrap()
	{
		return byteBuf.unwrap();
	}

	@Override
	public boolean isDirect()
	{
		return byteBuf.isDirect();
	}

	@Override
	public int readerIndex()
	{
		return byteBuf.readerIndex();
	}

	@Override
	public ByteBuf readerIndex( int p_readerIndex_1_ )
	{
		return byteBuf.readerIndex( p_readerIndex_1_ );
	}

	@Override
	public int writerIndex()
	{
		return byteBuf.writerIndex();
	}

	@Override
	public ByteBuf writerIndex( int p_writerIndex_1_ )
	{
		return byteBuf.writerIndex( p_writerIndex_1_ );
	}

	@Override
	public ByteBuf setIndex( int p_setIndex_1_, int p_setIndex_2_ )
	{
		return byteBuf.setIndex( p_setIndex_1_, p_setIndex_2_ );
	}

	@Override
	public int readableBytes()
	{
		return byteBuf.readableBytes();
	}

	@Override
	public int writableBytes()
	{
		return byteBuf.writableBytes();
	}

	@Override
	public int maxWritableBytes()
	{
		return byteBuf.maxWritableBytes();
	}

	@Override
	public boolean isReadable()
	{
		return byteBuf.isReadable();
	}

	@Override
	public boolean isReadable( int p_isReadable_1_ )
	{
		return byteBuf.isReadable( p_isReadable_1_ );
	}

	@Override
	public boolean isWritable()
	{
		return byteBuf.isWritable();
	}

	@Override
	public boolean isWritable( int p_isWritable_1_ )
	{
		return byteBuf.isWritable( p_isWritable_1_ );
	}

	@Override
	public ByteBuf clear()
	{
		return byteBuf.clear();
	}

	@Override
	public ByteBuf markReaderIndex()
	{
		return byteBuf.markReaderIndex();
	}

	@Override
	public ByteBuf resetReaderIndex()
	{
		return byteBuf.resetReaderIndex();
	}

	@Override
	public ByteBuf markWriterIndex()
	{
		return byteBuf.markWriterIndex();
	}

	@Override
	public ByteBuf resetWriterIndex()
	{
		return byteBuf.resetWriterIndex();
	}

	@Override
	public ByteBuf discardReadBytes()
	{
		return byteBuf.discardReadBytes();
	}

	@Override
	public ByteBuf discardSomeReadBytes()
	{
		return byteBuf.discardSomeReadBytes();
	}

	@Override
	public ByteBuf ensureWritable( int p_ensureWritable_1_ )
	{
		return byteBuf.ensureWritable( p_ensureWritable_1_ );
	}

	@Override
	public int ensureWritable( int p_ensureWritable_1_, boolean p_ensureWritable_2_ )
	{
		return byteBuf.ensureWritable( p_ensureWritable_1_, p_ensureWritable_2_ );
	}

	@Override
	public boolean getBoolean( int p_getBoolean_1_ )
	{
		return byteBuf.getBoolean( p_getBoolean_1_ );
	}

	@Override
	public byte getByte( int p_getByte_1_ )
	{
		return byteBuf.getByte( p_getByte_1_ );
	}

	@Override
	public short getUnsignedByte( int p_getUnsignedByte_1_ )
	{
		return byteBuf.getUnsignedByte( p_getUnsignedByte_1_ );
	}

	@Override
	public short getShort( int p_getShort_1_ )
	{
		return byteBuf.getShort( p_getShort_1_ );
	}

	@Override
	public int getUnsignedShort( int p_getUnsignedShort_1_ )
	{
		return byteBuf.getUnsignedShort( p_getUnsignedShort_1_ );
	}

	@Override
	public int getMedium( int p_getMedium_1_ )
	{
		return byteBuf.getMedium( p_getMedium_1_ );
	}

	@Override
	public int getUnsignedMedium( int p_getUnsignedMedium_1_ )
	{
		return byteBuf.getUnsignedMedium( p_getUnsignedMedium_1_ );
	}

	@Override
	public int getInt( int p_getInt_1_ )
	{
		return byteBuf.getInt( p_getInt_1_ );
	}

	@Override
	public long getUnsignedInt( int p_getUnsignedInt_1_ )
	{
		return byteBuf.getUnsignedInt( p_getUnsignedInt_1_ );
	}

	@Override
	public long getLong( int p_getLong_1_ )
	{
		return byteBuf.getLong( p_getLong_1_ );
	}

	@Override
	public char getChar( int p_getChar_1_ )
	{
		return byteBuf.getChar( p_getChar_1_ );
	}

	@Override
	public float getFloat( int p_getFloat_1_ )
	{
		return byteBuf.getFloat( p_getFloat_1_ );
	}

	@Override
	public double getDouble( int p_getDouble_1_ )
	{
		return byteBuf.getDouble( p_getDouble_1_ );
	}

	@Override
	public ByteBuf getBytes( int p_getBytes_1_, ByteBuf p_getBytes_2_ )
	{
		return byteBuf.getBytes( p_getBytes_1_, p_getBytes_2_ );
	}

	@Override
	public ByteBuf getBytes( int p_getBytes_1_, ByteBuf p_getBytes_2_, int p_getBytes_3_ )
	{
		return byteBuf.getBytes( p_getBytes_1_, p_getBytes_2_, p_getBytes_3_ );
	}

	@Override
	public ByteBuf getBytes( int p_getBytes_1_, ByteBuf p_getBytes_2_, int p_getBytes_3_, int p_getBytes_4_ )
	{
		return byteBuf.getBytes( p_getBytes_1_, p_getBytes_2_, p_getBytes_3_, p_getBytes_4_ );
	}

	@Override
	public ByteBuf getBytes( int p_getBytes_1_, byte[] p_getBytes_2_ )
	{
		return byteBuf.getBytes( p_getBytes_1_, p_getBytes_2_ );
	}

	@Override
	public ByteBuf getBytes( int p_getBytes_1_, byte[] p_getBytes_2_, int p_getBytes_3_, int p_getBytes_4_ )
	{
		return byteBuf.getBytes( p_getBytes_1_, p_getBytes_2_, p_getBytes_3_, p_getBytes_4_ );
	}

	@Override
	public ByteBuf getBytes( int p_getBytes_1_, ByteBuffer p_getBytes_2_ )
	{
		return byteBuf.getBytes( p_getBytes_1_, p_getBytes_2_ );
	}

	@Override
	public ByteBuf getBytes( int p_getBytes_1_, OutputStream p_getBytes_2_, int p_getBytes_3_ ) throws IOException
	{
		return byteBuf.getBytes( p_getBytes_1_, p_getBytes_2_, p_getBytes_3_ );
	}

	@Override
	public int getBytes( int p_getBytes_1_, GatheringByteChannel p_getBytes_2_, int p_getBytes_3_ ) throws IOException
	{
		return byteBuf.getBytes( p_getBytes_1_, p_getBytes_2_, p_getBytes_3_ );
	}

	@Override
	public ByteBuf setBoolean( int p_setBoolean_1_, boolean p_setBoolean_2_ )
	{
		return byteBuf.setBoolean( p_setBoolean_1_, p_setBoolean_2_ );
	}

	@Override
	public ByteBuf setByte( int p_setByte_1_, int p_setByte_2_ )
	{
		return byteBuf.setByte( p_setByte_1_, p_setByte_2_ );
	}

	@Override
	public ByteBuf setShort( int p_setShort_1_, int p_setShort_2_ )
	{
		return byteBuf.setShort( p_setShort_1_, p_setShort_2_ );
	}

	@Override
	public ByteBuf setMedium( int p_setMedium_1_, int p_setMedium_2_ )
	{
		return byteBuf.setMedium( p_setMedium_1_, p_setMedium_2_ );
	}

	@Override
	public ByteBuf setInt( int p_setInt_1_, int p_setInt_2_ )
	{
		return byteBuf.setInt( p_setInt_1_, p_setInt_2_ );
	}

	@Override
	public ByteBuf setLong( int p_setLong_1_, long p_setLong_2_ )
	{
		return byteBuf.setLong( p_setLong_1_, p_setLong_2_ );
	}

	@Override
	public ByteBuf setChar( int p_setChar_1_, int p_setChar_2_ )
	{
		return byteBuf.setChar( p_setChar_1_, p_setChar_2_ );
	}

	@Override
	public ByteBuf setFloat( int p_setFloat_1_, float p_setFloat_2_ )
	{
		return byteBuf.setFloat( p_setFloat_1_, p_setFloat_2_ );
	}

	@Override
	public ByteBuf setDouble( int p_setDouble_1_, double p_setDouble_2_ )
	{
		return byteBuf.setDouble( p_setDouble_1_, p_setDouble_2_ );
	}

	@Override
	public ByteBuf setBytes( int p_setBytes_1_, ByteBuf p_setBytes_2_ )
	{
		return byteBuf.setBytes( p_setBytes_1_, p_setBytes_2_ );
	}

	@Override
	public ByteBuf setBytes( int p_setBytes_1_, ByteBuf p_setBytes_2_, int p_setBytes_3_ )
	{
		return byteBuf.setBytes( p_setBytes_1_, p_setBytes_2_, p_setBytes_3_ );
	}

	@Override
	public ByteBuf setBytes( int p_setBytes_1_, ByteBuf p_setBytes_2_, int p_setBytes_3_, int p_setBytes_4_ )
	{
		return byteBuf.setBytes( p_setBytes_1_, p_setBytes_2_, p_setBytes_3_, p_setBytes_4_ );
	}

	@Override
	public ByteBuf setBytes( int p_setBytes_1_, byte[] p_setBytes_2_ )
	{
		return byteBuf.setBytes( p_setBytes_1_, p_setBytes_2_ );
	}

	@Override
	public ByteBuf setBytes( int p_setBytes_1_, byte[] p_setBytes_2_, int p_setBytes_3_, int p_setBytes_4_ )
	{
		return byteBuf.setBytes( p_setBytes_1_, p_setBytes_2_, p_setBytes_3_, p_setBytes_4_ );
	}

	@Override
	public ByteBuf setBytes( int p_setBytes_1_, ByteBuffer p_setBytes_2_ )
	{
		return byteBuf.setBytes( p_setBytes_1_, p_setBytes_2_ );
	}

	@Override
	public int setBytes( int p_setBytes_1_, InputStream p_setBytes_2_, int p_setBytes_3_ ) throws IOException
	{
		return byteBuf.setBytes( p_setBytes_1_, p_setBytes_2_, p_setBytes_3_ );
	}

	@Override
	public int setBytes( int p_setBytes_1_, ScatteringByteChannel p_setBytes_2_, int p_setBytes_3_ ) throws IOException
	{
		return byteBuf.setBytes( p_setBytes_1_, p_setBytes_2_, p_setBytes_3_ );
	}

	@Override
	public ByteBuf setZero( int p_setZero_1_, int p_setZero_2_ )
	{
		return byteBuf.setZero( p_setZero_1_, p_setZero_2_ );
	}

	@Override
	public boolean readBoolean()
	{
		return byteBuf.readBoolean();
	}

	@Override
	public byte readByte()
	{
		return byteBuf.readByte();
	}

	@Override
	public short readUnsignedByte()
	{
		return byteBuf.readUnsignedByte();
	}

	@Override
	public short readShort()
	{
		return byteBuf.readShort();
	}

	@Override
	public int readUnsignedShort()
	{
		return byteBuf.readUnsignedShort();
	}

	@Override
	public int readMedium()
	{
		return byteBuf.readMedium();
	}

	@Override
	public int readUnsignedMedium()
	{
		return byteBuf.readUnsignedMedium();
	}

	@Override
	public int readInt()
	{
		return byteBuf.readInt();
	}

	@Override
	public long readUnsignedInt()
	{
		return byteBuf.readUnsignedInt();
	}

	@Override
	public long readLong()
	{
		return byteBuf.readLong();
	}

	@Override
	public char readChar()
	{
		return byteBuf.readChar();
	}

	@Override
	public float readFloat()
	{
		return byteBuf.readFloat();
	}

	@Override
	public double readDouble()
	{
		return byteBuf.readDouble();
	}

	@Override
	public ByteBuf readBytes( int p_readBytes_1_ )
	{
		return byteBuf.readBytes( p_readBytes_1_ );
	}

	@Override
	public ByteBuf readSlice( int p_readSlice_1_ )
	{
		return byteBuf.readSlice( p_readSlice_1_ );
	}

	@Override
	public ByteBuf readBytes( ByteBuf p_readBytes_1_ )
	{
		return byteBuf.readBytes( p_readBytes_1_ );
	}

	@Override
	public ByteBuf readBytes( ByteBuf p_readBytes_1_, int p_readBytes_2_ )
	{
		return byteBuf.readBytes( p_readBytes_1_, p_readBytes_2_ );
	}

	@Override
	public ByteBuf readBytes( ByteBuf p_readBytes_1_, int p_readBytes_2_, int p_readBytes_3_ )
	{
		return byteBuf.readBytes( p_readBytes_1_, p_readBytes_2_, p_readBytes_3_ );
	}

	@Override
	public ByteBuf readBytes( byte[] p_readBytes_1_ )
	{
		return byteBuf.readBytes( p_readBytes_1_ );
	}

	@Override
	public ByteBuf readBytes( byte[] p_readBytes_1_, int p_readBytes_2_, int p_readBytes_3_ )
	{
		return byteBuf.readBytes( p_readBytes_1_, p_readBytes_2_, p_readBytes_3_ );
	}

	@Override
	public ByteBuf readBytes( ByteBuffer p_readBytes_1_ )
	{
		return byteBuf.readBytes( p_readBytes_1_ );
	}

	@Override
	public ByteBuf readBytes( OutputStream p_readBytes_1_, int p_readBytes_2_ ) throws IOException
	{
		return byteBuf.readBytes( p_readBytes_1_, p_readBytes_2_ );
	}

	@Override
	public int readBytes( GatheringByteChannel p_readBytes_1_, int p_readBytes_2_ ) throws IOException
	{
		return byteBuf.readBytes( p_readBytes_1_, p_readBytes_2_ );
	}

	@Override
	public ByteBuf skipBytes( int p_skipBytes_1_ )
	{
		return byteBuf.skipBytes( p_skipBytes_1_ );
	}

	@Override
	public ByteBuf writeBoolean( boolean p_writeBoolean_1_ )
	{
		return byteBuf.writeBoolean( p_writeBoolean_1_ );
	}

	@Override
	public ByteBuf writeByte( int p_writeByte_1_ )
	{
		return byteBuf.writeByte( p_writeByte_1_ );
	}

	@Override
	public ByteBuf writeShort( int p_writeShort_1_ )
	{
		return byteBuf.writeShort( p_writeShort_1_ );
	}

	@Override
	public ByteBuf writeMedium( int p_writeMedium_1_ )
	{
		return byteBuf.writeMedium( p_writeMedium_1_ );
	}

	@Override
	public ByteBuf writeInt( int p_writeInt_1_ )
	{
		return byteBuf.writeInt( p_writeInt_1_ );
	}

	@Override
	public ByteBuf writeLong( long p_writeLong_1_ )
	{
		return byteBuf.writeLong( p_writeLong_1_ );
	}

	@Override
	public ByteBuf writeChar( int p_writeChar_1_ )
	{
		return byteBuf.writeChar( p_writeChar_1_ );
	}

	@Override
	public ByteBuf writeFloat( float p_writeFloat_1_ )
	{
		return byteBuf.writeFloat( p_writeFloat_1_ );
	}

	@Override
	public ByteBuf writeDouble( double p_writeDouble_1_ )
	{
		return byteBuf.writeDouble( p_writeDouble_1_ );
	}

	@Override
	public ByteBuf writeBytes( ByteBuf p_writeBytes_1_ )
	{
		return byteBuf.writeBytes( p_writeBytes_1_ );
	}

	@Override
	public ByteBuf writeBytes( ByteBuf p_writeBytes_1_, int p_writeBytes_2_ )
	{
		return byteBuf.writeBytes( p_writeBytes_1_, p_writeBytes_2_ );
	}

	@Override
	public ByteBuf writeBytes( ByteBuf p_writeBytes_1_, int p_writeBytes_2_, int p_writeBytes_3_ )
	{
		return byteBuf.writeBytes( p_writeBytes_1_, p_writeBytes_2_, p_writeBytes_3_ );
	}

	@Override
	public ByteBuf writeBytes( byte[] p_writeBytes_1_ )
	{
		return byteBuf.writeBytes( p_writeBytes_1_ );
	}

	@Override
	public ByteBuf writeBytes( byte[] p_writeBytes_1_, int p_writeBytes_2_, int p_writeBytes_3_ )
	{
		return byteBuf.writeBytes( p_writeBytes_1_, p_writeBytes_2_, p_writeBytes_3_ );
	}

	@Override
	public ByteBuf writeBytes( ByteBuffer p_writeBytes_1_ )
	{
		return byteBuf.writeBytes( p_writeBytes_1_ );
	}

	@Override
	public int writeBytes( InputStream p_writeBytes_1_, int p_writeBytes_2_ ) throws IOException
	{
		return byteBuf.writeBytes( p_writeBytes_1_, p_writeBytes_2_ );
	}

	@Override
	public int writeBytes( ScatteringByteChannel p_writeBytes_1_, int p_writeBytes_2_ ) throws IOException
	{
		return byteBuf.writeBytes( p_writeBytes_1_, p_writeBytes_2_ );
	}

	@Override
	public ByteBuf writeZero( int p_writeZero_1_ )
	{
		return byteBuf.writeZero( p_writeZero_1_ );
	}

	@Override
	public int indexOf( int p_indexOf_1_, int p_indexOf_2_, byte p_indexOf_3_ )
	{
		return byteBuf.indexOf( p_indexOf_1_, p_indexOf_2_, p_indexOf_3_ );
	}

	@Override
	public int bytesBefore( byte p_bytesBefore_1_ )
	{
		return byteBuf.bytesBefore( p_bytesBefore_1_ );
	}

	@Override
	public int bytesBefore( int p_bytesBefore_1_, byte p_bytesBefore_2_ )
	{
		return byteBuf.bytesBefore( p_bytesBefore_1_, p_bytesBefore_2_ );
	}

	@Override
	public int bytesBefore( int p_bytesBefore_1_, int p_bytesBefore_2_, byte p_bytesBefore_3_ )
	{
		return byteBuf.bytesBefore( p_bytesBefore_1_, p_bytesBefore_2_, p_bytesBefore_3_ );
	}

	@Override
	public int forEachByte( ByteBufProcessor p_forEachByte_1_ )
	{
		return byteBuf.forEachByte( p_forEachByte_1_ );
	}

	@Override
	public int forEachByte( int p_forEachByte_1_, int p_forEachByte_2_, ByteBufProcessor p_forEachByte_3_ )
	{
		return byteBuf.forEachByte( p_forEachByte_1_, p_forEachByte_2_, p_forEachByte_3_ );
	}

	@Override
	public int forEachByteDesc( ByteBufProcessor p_forEachByteDesc_1_ )
	{
		return byteBuf.forEachByteDesc( p_forEachByteDesc_1_ );
	}

	@Override
	public int forEachByteDesc( int p_forEachByteDesc_1_, int p_forEachByteDesc_2_, ByteBufProcessor p_forEachByteDesc_3_ )
	{
		return byteBuf.forEachByteDesc( p_forEachByteDesc_1_, p_forEachByteDesc_2_, p_forEachByteDesc_3_ );
	}

	@Override
	public ByteBuf copy()
	{
		return byteBuf.copy();
	}

	@Override
	public ByteBuf copy( int p_copy_1_, int p_copy_2_ )
	{
		return byteBuf.copy( p_copy_1_, p_copy_2_ );
	}

	@Override
	public ByteBuf slice()
	{
		return byteBuf.slice();
	}

	@Override
	public ByteBuf slice( int p_slice_1_, int p_slice_2_ )
	{
		return byteBuf.slice( p_slice_1_, p_slice_2_ );
	}

	@Override
	public ByteBuf duplicate()
	{
		return byteBuf.duplicate();
	}

	@Override
	public int nioBufferCount()
	{
		return byteBuf.nioBufferCount();
	}

	@Override
	public ByteBuffer nioBuffer()
	{
		return byteBuf.nioBuffer();
	}

	@Override
	public ByteBuffer nioBuffer( int p_nioBuffer_1_, int p_nioBuffer_2_ )
	{
		return byteBuf.nioBuffer( p_nioBuffer_1_, p_nioBuffer_2_ );
	}

	@Override
	public ByteBuffer internalNioBuffer( int p_internalNioBuffer_1_, int p_internalNioBuffer_2_ )
	{
		return byteBuf.internalNioBuffer( p_internalNioBuffer_1_, p_internalNioBuffer_2_ );
	}

	@Override
	public ByteBuffer[] nioBuffers()
	{
		return byteBuf.nioBuffers();
	}

	@Override
	public ByteBuffer[] nioBuffers( int p_nioBuffers_1_, int p_nioBuffers_2_ )
	{
		return byteBuf.nioBuffers( p_nioBuffers_1_, p_nioBuffers_2_ );
	}

	@Override
	public boolean hasArray()
	{
		return byteBuf.hasArray();
	}

	@Override
	public byte[] array()
	{
		return byteBuf.array();
	}

	@Override
	public int arrayOffset()
	{
		return byteBuf.arrayOffset();
	}

	@Override
	public boolean hasMemoryAddress()
	{
		return byteBuf.hasMemoryAddress();
	}

	@Override
	public long memoryAddress()
	{
		return byteBuf.memoryAddress();
	}

	@Override
	public String toString( Charset p_toString_1_ )
	{
		return byteBuf.toString( p_toString_1_ );
	}

	@Override
	public String toString( int p_toString_1_, int p_toString_2_, Charset p_toString_3_ )
	{
		return byteBuf.toString( p_toString_1_, p_toString_2_, p_toString_3_ );
	}

	public int hashCode()
	{
		return byteBuf.hashCode();
	}

	public boolean equals( Object p_equals_1_ )
	{
		return byteBuf.equals( p_equals_1_ );
	}

	@Override
	public int compareTo( ByteBuf p_compareTo_1_ )
	{
		return byteBuf.compareTo( p_compareTo_1_ );
	}

	public String toString()
	{
		return byteBuf.toString();
	}

	@Override
	public ByteBuf retain( int p_retain_1_ )
	{
		return byteBuf.retain( p_retain_1_ );
	}

	@Override
	public ByteBuf retain()
	{
		return byteBuf.retain();
	}

	@Override
	public ByteBuf touch()
	{
		return byteBuf.touch();
	}

	@Override
	public ByteBuf touch( Object hint )
	{
		return byteBuf.touch( hint );
	}

	/**
	 * Reads a string from buffer. Expected parameter is maximum allowed string length. Will throw IOException if
	 * string length exceeds value!
	 */
	public String readString( int max ) throws IOException
	{
		int var2 = readVarIntFromBuffer();

		if ( var2 > max * 4 )
		{
			throw new IOException( "The received encoded string buffer length is longer than maximum allowed (" + var2 + " > " + max * 4 + ")" );
		}
		else if ( var2 < 0 )
		{
			throw new IOException( "The received encoded string buffer length is less than zero! Weird string!" );
		}
		else
		{
			String var3 = new String( readBytes( var2 ).array(), Charsets.UTF_8 );

			if ( var3.length() > max )
			{
				throw new IOException( "The received string length is longer than maximum allowed (" + var2 + " > " + max + ")" );
			}
			else
			{
				return var3;
			}
		}
	}

	/**
	 * Reads a compressed int from the buffer. To do so it maximally reads 5 byte-sized chunks whose most significant
	 * bit dictates whether another byte should be read.
	 */
	public int readVarIntFromBuffer()
	{
		int var1 = 0;
		int var2 = 0;
		byte var3;

		do
		{
			var3 = readByte();
			var1 |= ( var3 & 127 ) << var2++ * 7;

			if ( var2 > 5 )
			{
				throw new RuntimeException( "VarInt too big" );
			}
		}
		while ( ( var3 & 128 ) == 128 );

		return var1;
	}

	@Override
	public int refCnt()
	{
		return byteBuf.refCnt();
	}

	@Override
	public boolean release()
	{
		return byteBuf.release();
	}

	@Override
	public boolean release( int p_release_1_ )
	{
		return byteBuf.release( p_release_1_ );
	}

	/**
	 * Writes a (UTF-8 encoded) String to buffer. Will throw IOException if String length exceeds 32767 bytes
	 */
	public void writeString( String str ) throws IOException
	{
		byte[] bytes = Strs.decodeUtf8( str );

		if ( bytes.length > 32767 )
			throw new IOException( "String too big (was " + str.length() + " bytes encoded, max " + 32767 + ")" );
		else
		{
			writeVarIntToBuffer( bytes.length );
			writeBytes( bytes );
		}
	}

	/**
	 * Writes a compressed int to the buffer. The smallest number of bytes to fit the passed int will be written. Of
	 * each such byte only 7 bits will be used to describe the actual value since its most significant bit dictates
	 * whether the next byte is part of that same int. Micro-optimization for int values that are expected to have
	 * values below 128.
	 */
	public void writeVarIntToBuffer( int value )
	{
		while ( ( value & -128 ) != 0 )
		{
			writeByte( value & 127 | 128 );
			value >>>= 7;
		}

		writeByte( value );
	}
}
