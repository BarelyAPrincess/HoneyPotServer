/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class UtilFiles
{
	private UtilFiles()
	{

	}

	public static String getStringFromFile( String filePath ) throws IOException
	{
		return getStringFromFile( new File( filePath ) );
	}

	public static String getStringFromFile( File file ) throws IOException
	{
		return getStringFromFile( file, null );
	}

	public static String getStringFromFile( File file, String def ) throws IOException
	{
		try
		{
			FileInputStream fin = new FileInputStream( file );
			String ret = LibIO.inputStream2String( fin );
			fin.close();
			return ret;
		}
		catch ( FileNotFoundException e )
		{
			if ( def == null )
				throw e;
			return def;
		}
	}

	public static boolean putStringToFile( File file, String str ) throws IOException
	{
		return putStringToFile( file, str, false );
	}

	public static boolean putStringToFile( File file, String str, boolean append ) throws IOException
	{
		FileOutputStream fos = null;
		try
		{
			if ( !append && file.exists() )
				file.delete();

			fos = new FileOutputStream( file );
			fos.write( str.getBytes() );
		}
		finally
		{
			try
			{
				if ( fos != null )
					fos.close();
			}
			catch ( IOException ignore )
			{

			}
		}

		return true;
	}
}
