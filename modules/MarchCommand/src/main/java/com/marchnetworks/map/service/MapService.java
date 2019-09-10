package com.marchnetworks.map.service;

public abstract interface MapService
{
	public abstract Long create( byte[] paramArrayOfByte ) throws MapException;

	public abstract Long update( Long paramLong1, Long paramLong2, byte[] paramArrayOfByte ) throws MapException;

	public abstract byte[] getMapData( Long paramLong ) throws MapException;

	public abstract String getMapTag( Long paramLong ) throws MapException;

	public abstract void addReference( Long paramLong1, Long paramLong2 );

	public abstract void removeReference( Long paramLong1, Long paramLong2 );
}

