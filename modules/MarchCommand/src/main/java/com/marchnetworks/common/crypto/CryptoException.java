package com.marchnetworks.common.crypto;

public class CryptoException extends Exception
{
	public CryptoException( String msg )
	{
		super( msg );
	}

	public CryptoException( String msg, Exception cause )
	{
		super( msg, cause );
	}
}
