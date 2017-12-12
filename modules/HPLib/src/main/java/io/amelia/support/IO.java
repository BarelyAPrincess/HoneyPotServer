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

import com.google.common.base.Strings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

import io.amelia.foundation.Kernel;
import io.amelia.injection.Libraries;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.UncaughtException;
import io.netty.buffer.ByteBuf;

public class IO
{
	public static final String PATH_SEPERATOR = File.separator;
	private static final char[] BYTE2CHAR = new char[256];
	private static final String[] BYTE2HEX = new String[256];
	private static final String[] BYTEPADDING = new String[16];
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	private static final int EOF = -1;
	private static final String[] HEXDUMP_ROWPREFIXES = new String[65536 >>> 4];
	private static final String[] HEXPADDING = new String[16];
	private static final Kernel.Logger L = Kernel.getLogger( IO.class );
	private static final String NEWLINE = "\n";

	static
	{
		int i;

		// Generate the lookup table for byte-to-hex-dump conversion
		for ( i = 0; i < BYTE2HEX.length; i++ )
			BYTE2HEX[i] = String.format( " %02X", i );

		// Generate the lookup table for hex dump paddings
		for ( i = 0; i < HEXPADDING.length; i++ )
		{
			int padding = HEXPADDING.length - i;
			StringBuilder buf = new StringBuilder( padding * 3 );
			for ( int j = 0; j < padding; j++ )
				buf.append( "   " );
			HEXPADDING[i] = buf.toString();
		}

		// Generate the lookup table for byte dump paddings
		for ( i = 0; i < BYTEPADDING.length; i++ )
		{
			int padding = BYTEPADDING.length - i;
			StringBuilder buf = new StringBuilder( padding );
			for ( int j = 0; j < padding; j++ )
				buf.append( ' ' );
			BYTEPADDING[i] = buf.toString();
		}

		// Generate the lookup table for byte-to-char conversion
		for ( i = 0; i < BYTE2CHAR.length; i++ )
			if ( i <= 0x1f || i >= 0x7f )
				BYTE2CHAR[i] = '.';
			else
				BYTE2CHAR[i] = ( char ) i;

		// Generate the lookup table for the start-offset header in each row (up to 64KiB).
		for ( i = 0; i < HEXDUMP_ROWPREFIXES.length; i++ )
		{
			StringBuilder buf = new StringBuilder( 12 );
			buf.append( NEWLINE );
			buf.append( Long.toHexString( i << 4 & 0xFFFFFFFFL | 0x100000000L ) );
			buf.setCharAt( buf.length() - 9, '|' );
			buf.append( '|' );
			HEXDUMP_ROWPREFIXES[i] = buf.toString();
		}
	}

	/**
	 * Appends the prefix of each hex dump row. Uses the look-up table for the buffer <= 64 KiB.
	 */
	private static void appendHexDumpRowPrefix( StringBuilder dump, int row, int rowStartIndex )
	{
		if ( row < HEXDUMP_ROWPREFIXES.length )
			dump.append( HEXDUMP_ROWPREFIXES[row] );
		else
		{
			dump.append( NEWLINE );
			dump.append( Long.toHexString( rowStartIndex & 0xFFFFFFFFL | 0x100000000L ) );
			dump.setCharAt( dump.length() - 9, '|' );
			dump.append( '|' );
		}
	}

	public static File buildFile( boolean absolute, String... args )
	{
		return new File( ( absolute ? PATH_SEPERATOR : "" ) + buildPath( args ) );
	}

	public static File buildFile( File file, String... args )
	{
		return new File( file, buildPath( args ) );
	}

	public static File buildFile( String... args )
	{
		return buildFile( false, args );
	}

	public static String buildPath( @Nonnull String... paths )
	{
		return Arrays.stream( paths ).filter( n -> !Objs.isEmpty( n ) ).map( n -> Strs.trimAll( n, '/' ) ).collect( Collectors.joining( PATH_SEPERATOR ) );
	}

	public static Byte[] bytesToBytes( byte[] bytes )
	{
		Byte[] newBytes = new Byte[bytes.length];
		for ( int i = 0; i < bytes.length; i++ )
			newBytes[i] = bytes[i];
		return newBytes;
	}

	public static String bytesToStringUTFNIO( byte[] bytes )
	{
		if ( bytes == null )
			return null;

		CharBuffer cBuffer = ByteBuffer.wrap( bytes ).asCharBuffer();
		return cBuffer.toString();
	}

	public static boolean checkMd5( File file, String expectedMd5 ) throws IOException
	{
		if ( expectedMd5 == null || file == null || !file.exists() )
			return false;

		String md5 = Encrypt.md5Hex( new FileInputStream( file ) );
		return md5 != null && md5.equals( expectedMd5 );
	}

	public static void closeQuietly( Closeable closeable )
	{
		try
		{
			if ( closeable != null )
				closeable.close();
		}
		catch ( IOException ioe )
		{
			// ignore
		}
	}

	public static int copy( InputStream input, OutputStream output ) throws IOException
	{
		long count = copyLarge( input, output );
		if ( count > Integer.MAX_VALUE )
			return -1;
		return ( int ) count;
	}

	/**
	 * This method copies one file to another location
	 *
	 * @param inFile  the source filename
	 * @param outFile the target filename
	 * @return true on success
	 */
	@SuppressWarnings( "resource" )
	public static boolean copy( File inFile, File outFile )
	{
		if ( !inFile.exists() )
			return false;

		if ( inFile.isDirectory() )
		{
			outFile.mkdirs();
			for ( File file : inFile.listFiles() )
				copy( file, new File( outFile, file.getName() ) );
		}
		else
		{
			FileChannel in = null;
			FileChannel out = null;

			try
			{
				in = new FileInputStream( inFile ).getChannel();
				out = new FileOutputStream( outFile ).getChannel();

				long pos = 0;
				long size = in.size();

				while ( pos < size )
					pos += in.transferTo( pos, 10 * 1024 * 1024, out );
			}
			catch ( IOException ioe )
			{
				return false;
			}
			finally
			{
				try
				{
					if ( in != null )
						in.close();
					if ( out != null )
						out.close();
				}
				catch ( IOException ioe )
				{
					return false;
				}
			}
		}

		return true;
	}

	public static long copyLarge( InputStream input, OutputStream output ) throws IOException
	{
		return copyLarge( input, output, new byte[DEFAULT_BUFFER_SIZE] );
	}

	public static long copyLarge( InputStream input, OutputStream output, byte[] buffer ) throws IOException
	{
		long count = 0;
		int n = 0;
		while ( EOF != ( n = input.read( buffer ) ) )
		{
			output.write( buffer, 0, n );
			count += n;
		}
		return count;
	}

	private static long copyToFile( InputStream inputStream, File file ) throws IOException
	{
		return copy( inputStream, new FileOutputStream( file ) );
	}

	public static String dirname( File path )
	{
		return dirname( path, 1 );
	}

	public static String dirname( @Nonnull File path, @Nonnull int levels )
	{
		Objs.notNull( path );
		Objs.notFalse( levels > 0 );

		path = path.getAbsoluteFile();

		while ( levels > 0 && path.getParent() != null )
		{
			levels--;
			path = path.getParentFile();
		}

		return path.getName();
	}

	/**
	 * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
	 * The returned array will be double the length of the passed array, as it takes two characters to represent any
	 * given byte.
	 *
	 * @param data a byte[] to convert to Hex characters
	 * @return A char[] containing hexadecimal characters
	 */
	public static char[] encodeHex( final byte[] data )
	{
		char[] chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

		final int l = data.length;
		final char[] out = new char[l << 1];
		// two characters form the hex value.
		for ( int i = 0, j = 0; i < l; i++ )
		{
			out[j++] = chars[( 0xF0 & data[i] ) >>> 4];
			out[j++] = chars[0x0F & data[i]];
		}
		return out;
	}

	public static String encodeHexPretty( final byte... data )
	{
		return Arrays.stream( encodeHexStringArray( data ) ).map( c -> "0x" + c ).collect( Collectors.joining( " " ) );
	}

	/**
	 * Converts an array of bytes into a String representing the hexadecimal values of each byte in order. The returned
	 * String will be double the length of the passed array, as it takes two characters to represent any given byte.
	 *
	 * @param data a byte[] to convert to Hex characters
	 * @return A String containing hexadecimal characters
	 */
	public static String encodeHexString( final byte[] data )
	{
		return new String( encodeHex( data ) );
	}

	public static String[] encodeHexStringArray( final byte... data )
	{
		char[] chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

		final int l = data.length;
		final String[] out = new String[l << 1];
		// two characters form the hex value.
		for ( int i = 0, j = 0; i < l; i++ )
			out[j++] = chars[( 0xF0 & data[i] ) >>> 4] + "" + chars[0x0F & data[i]];
		return out;
	}

	public static Set<String> entriesToSet( String archivePath, Enumeration<? extends ZipEntry> entries )
	{
		Set<String> result = new HashSet<>(); // avoid duplicates in case it is a subdirectory
		while ( entries.hasMoreElements() )
		{
			String name = entries.nextElement().getName();
			if ( name.startsWith( archivePath ) )
			{ // filter according to the path
				String entry = name.substring( archivePath.length() );
				int checkSubdir = entry.indexOf( "/" );
				if ( checkSubdir >= 0 )
					// if it is a subdirectory, we just return the directory name
					entry = entry.substring( 0, checkSubdir );
				result.add( entry );
			}
		}
		return result;
	}

	public static void extractLibraries( File jarFile, File baseDir )
	{
		try
		{
			baseDir = new File( baseDir, "libraries" );
			// FileFunc.directoryHealthCheck( baseDir );

			if ( jarFile == null || !jarFile.exists() || !jarFile.getName().endsWith( ".jar" ) )
				L.severe( "There was a problem with the provided jar file, it was either null, not existent or did not end with jar." );

			JarFile jar = new JarFile( jarFile );

			try
			{
				ZipEntry libDir = jar.getEntry( "libraries" );

				if ( libDir != null ) // && libDir.isDirectory() )
				{
					Enumeration<JarEntry> entries = jar.entries();
					while ( entries.hasMoreElements() )
					{
						JarEntry entry = entries.nextElement();
						if ( entry.getName().startsWith( libDir.getName() ) && !entry.isDirectory() && entry.getName().endsWith( ".jar" ) )
						{
							File lib = new File( baseDir, entry.getName().substring( libDir.getName().length() + 1 ) );

							if ( !lib.exists() )
							{
								lib.getParentFile().mkdirs();
								L.info( EnumColor.GOLD + "Extracting bundled library '" + entry.getName() + "' to '" + lib.getAbsolutePath() + "'." );
								InputStream is = jar.getInputStream( entry );
								FileOutputStream out = new FileOutputStream( lib );
								copy( is, out );
								is.close();
								out.close();
							}

							Libraries.loadLibrary( lib );
						}
					}
				}
			}
			finally
			{
				jar.close();
			}
		}
		catch ( Throwable t )
		{
			L.severe( "We had a problem extracting bundled libraries from jar file '" + jarFile.getAbsolutePath() + "'", t );
		}
	}

	public static boolean extractNatives( File libFile, File baseDir ) throws IOException
	{
		List<String> nativesExtracted = new ArrayList<>();
		boolean foundArchMatchingNative = false;

		baseDir = new File( baseDir, "natives" );
		// FileFunc.directoryHealthCheck( baseDir );

		if ( libFile == null || !libFile.exists() || !libFile.getName().endsWith( ".jar" ) )
			throw new IOException( "There was a problem with the provided jar file, it was either null, not existent or did not end with jar." );

		JarFile jar = new JarFile( libFile );
		Enumeration<JarEntry> entries = jar.entries();

		while ( entries.hasMoreElements() )
		{
			JarEntry entry = entries.nextElement();

			if ( !entry.isDirectory() && ( entry.getName().endsWith( ".so" ) || entry.getName().endsWith( ".dll" ) || entry.getName().endsWith( ".jnilib" ) || entry.getName().endsWith( ".dylib" ) ) )
				try
				{
					File internal = new File( entry.getName() );
					String newName = internal.getName();

					String os = System.getProperty( "os.name" );
					if ( os.contains( " " ) )
						os = os.substring( 0, os.indexOf( " " ) );
					os = os.replaceAll( "\\W", "" );
					os = os.toLowerCase();

					String parent = internal.getParentFile().getName();

					if ( parent.startsWith( os ) || parent.startsWith( "windows" ) || parent.startsWith( "linux" ) || parent.startsWith( "darwin" ) || parent.startsWith( "osx" ) || parent.startsWith( "solaris" ) || parent.startsWith( "cygwin" ) || parent.startsWith( "mingw" ) || parent.startsWith( "msys" ) )
						newName = parent + "/" + newName;

					if ( Arrays.asList( OSInfo.NATIVE_SEARCH_PATHS ).contains( parent ) )
						foundArchMatchingNative = true;

					File lib = new File( baseDir, newName );

					if ( lib.exists() && nativesExtracted.contains( lib.getAbsolutePath() ) )
						L.warning( EnumColor.GOLD + "We detected more than one file with the destination '" + lib.getAbsolutePath() + "', if these files from for different architectures, you might need to separate them into their separate folders, i.e., windows, linux-x86, linux-x86_64, etc." );

					if ( !lib.exists() )
					{
						lib.getParentFile().mkdirs();
						L.info( EnumColor.GOLD + "Extracting native library '" + entry.getName() + "' to '" + lib.getAbsolutePath() + "'." );
						InputStream is = jar.getInputStream( entry );
						FileOutputStream out = new FileOutputStream( lib );
						byte[] buf = new byte[0x1000];
						while ( true )
						{
							int r = is.read( buf );
							if ( r == -1 )
								break;
							out.write( buf, 0, r );
						}
						is.close();
						out.close();
					}

					if ( !nativesExtracted.contains( lib.getAbsolutePath() ) )
						nativesExtracted.add( lib.getAbsolutePath() );
				}
				catch ( FileNotFoundException e )
				{
					jar.close();
					throw new IOException( "We had a problem extracting native library '" + entry.getName() + "' from jar file '" + libFile.getAbsolutePath() + "'", e );
				}
		}

		jar.close();

		if ( nativesExtracted.size() > 0 )
		{
			if ( !foundArchMatchingNative )
				L.warning( EnumColor.DARK_GRAY + "We found native libraries contained within jar '" + libFile.getAbsolutePath() + "' but according to conventions none of them had the required architecture, the dependency may fail to load the required native if our theory is correct." );

			String path = baseDir.getAbsolutePath().contains( " " ) ? "\"" + baseDir.getAbsolutePath() + "\"" : baseDir.getAbsolutePath();
			System.setProperty( "java.library.path", System.getProperty( "java.library.path" ) + ":" + path );

			try
			{
				Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
				fieldSysPath.setAccessible( true );
				fieldSysPath.set( null, null );
			}
			catch ( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e )
			{
				L.severe( "We could not force the ClassAppLoader to reinitalize the LD_LIBRARY_PATH variable. You may need to set '-Djava.library.path=" + baseDir.getAbsolutePath() + "' on next load because one or more dependencies may fail to load their native libraries.", e );
			}
		}

		return nativesExtracted.size() > 0;
	}

	public static boolean extractNatives( Map<String, List<String>> natives, File libFile, File baseDir ) throws IOException
	{
		if ( !Objs.containsKeys( natives, Arrays.asList( OSInfo.NATIVE_SEARCH_PATHS ) ) )
			L.warning( String.format( "%sWe were unable to locate any natives libraries that match architectures '%s' within plugin '%s'.", EnumColor.DARK_GRAY, Strs.join( OSInfo.NATIVE_SEARCH_PATHS ), libFile.getAbsolutePath() ) );

		List<String> nativesExtracted = new ArrayList<>();
		baseDir = new File( baseDir, "natives" );
		// FileFunc.directoryHealthCheck( baseDir );

		if ( libFile == null || !libFile.exists() || !libFile.getName().endsWith( ".jar" ) )
			throw new IOException( "There was a problem with the provided jar file, it was either null, not existent or did not end with jar." );

		JarFile jar = new JarFile( libFile );

		for ( String arch : OSInfo.NATIVE_SEARCH_PATHS )
		{
			List<String> libs = natives.get( arch.toLowerCase() );
			if ( libs != null && !libs.isEmpty() )
				for ( String lib : libs )
					try
					{
						ZipEntry entry = jar.getEntry( lib );

						if ( entry == null || entry.isDirectory() )
						{
							entry = jar.getEntry( "natives/" + lib );

							if ( entry == null || entry.isDirectory() )
								L.warning( String.format( "We were unable to load the native lib '%s' for arch '%s' for it was non-existent (or it's a directory) within plugin '%s'.", lib, arch, libFile ) );
						}

						if ( entry != null && !entry.isDirectory() )
						{
							if ( !entry.getName().endsWith( ".so" ) && !entry.getName().endsWith( ".dll" ) && !entry.getName().endsWith( ".jnilib" ) && !entry.getName().endsWith( ".dylib" ) )
								L.warning( String.format( "We found native lib '%s' for arch '%s' within plugin '%s', but it did not end with a known native library ext. We will extract it anyways but you may have problems.", lib, arch, libFile ) );

							File newNative = new File( baseDir + "/" + arch + "/" + new File( entry.getName() ).getName() );

							if ( !newNative.exists() )
							{
								newNative.getParentFile().mkdirs();
								L.info( String.format( "%sExtracting native library '%s' to '%s'.", EnumColor.GOLD, entry.getName(), newNative.getAbsolutePath() ) );
								InputStream is = jar.getInputStream( entry );
								FileOutputStream out = new FileOutputStream( newNative );
								copy( is, out );
								is.close();
								out.close();
							}

							nativesExtracted.add( entry.getName() );
							// L.severe( String.format( "We were unable to load the native lib '%s' for arch '%s' within plugin '%s' for an unknown reason.", lib, arch, libFile ) );
						}
					}
					catch ( FileNotFoundException e )
					{
						jar.close();
						throw new IOException( String.format( "We had a problem extracting native library '%s' from jar file '%s'", lib, libFile.getAbsolutePath() ), e );
					}
		}

		// Enumeration<JarEntry> entries = jar.entries();
		jar.close();

		if ( nativesExtracted.size() > 0 )
		{
			LibraryPath path = new LibraryPath();
			path.add( baseDir.getAbsolutePath() );
			for ( String arch : OSInfo.NATIVE_SEARCH_PATHS )
				path.add( baseDir.getAbsolutePath() + "/" + arch );
			path.set();

			try
			{
				Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
				fieldSysPath.setAccessible( true );
				fieldSysPath.set( null, null );
			}
			catch ( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e )
			{
				L.severe( "We could not force the ClassAppLoader to reinitialize the LD_LIBRARY_PATH variable. You may need to set '-Djava.library.path=" + baseDir.getAbsolutePath() + "' on next load because one or more dependencies may fail to load their native libraries.", e );
			}
		}

		return nativesExtracted.size() > 0;
	}

	public static void extractResourceDirectory( String path, File dest ) throws IOException
	{
		extractResourceDirectory( path, dest, IO.class );
	}

	public static void extractResourceDirectory( @Nonnull String path, @Nonnull File dest, @Nonnull Class<?> clazz ) throws IOException
	{
		Objs.notEmpty( path );
		Objs.notNull( dest );
		Objs.notNull( clazz );

		dest = dest.getAbsoluteFile();

		if ( !dest.isDirectory() )
			throw new IOException( "Specified dest '" + IO.relPath( dest ) + "' is not a directory or does not exist." );

		final File jarFile = new File( clazz.getProtectionDomain().getCodeSource().getLocation().getPath() );

		if ( jarFile.isFile() )
		{  // Run with JAR file
			final JarFile jar = new JarFile( jarFile );
			final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
			while ( entries.hasMoreElements() )
			{
				final JarEntry entry = entries.nextElement();
				if ( entry.getName().startsWith( path + "/" ) )
				{ //filter according to the path
					File save = new File( dest, entry.getName() );
					if ( entry.isDirectory() )
						save.mkdirs();
					else
					{
						save.getParentFile().mkdirs();
						copyToFile( jar.getInputStream( entry ), save );
					}
				}
			}
			jar.close();
		}
		else
		{ // Run with IDE
			final URL url = clazz.getResource( "/" + path );
			if ( url != null )
			{
				try
				{
					final File dir = new File( url.toURI() );

					if ( !dir.isDirectory() )
						throw new IOException( "Specified resource path '" + IO.relPath( dir ) + "' is not a directory." );

					for ( File file : dir.listFiles() )
						copy( file, new File( dest, file.getName() ) );
				}
				catch ( URISyntaxException ex )
				{
					// never happens
				}
			}
		}
	}

	public static boolean extractResourceZip( String path, File dest ) throws IOException
	{
		return extractResourceZip( path, dest, IO.class );
	}

	public static boolean extractResourceZip( String path, File dest, Class<?> clz ) throws IOException
	{
		File cache = Kernel.getPath( Kernel.PATH_CACHE );
		if ( !cache.exists() )
			cache.mkdirs();
		File temp = new File( cache, "temp.zip" );
		putResource( clz, path, temp );

		ZipFile zip = new ZipFile( temp );

		try
		{
			Enumeration<? extends ZipEntry> entries = zip.entries();

			while ( entries.hasMoreElements() )
			{
				ZipEntry entry = entries.nextElement();
				File save = new File( dest, entry.getName() );
				if ( entry.isDirectory() )
					save.mkdirs();
				else if ( save.getParentFile() != null )
				{
					save.getParentFile().mkdirs();
					copyToFile( zip.getInputStream( entry ), save );
				}
			}
		}
		finally
		{
			zip.close();
			temp.delete();
		}

		return true;
	}

	public static String fileExtension( File file )
	{
		return fileExtension( file.getName() );
	}

	public static String fileExtension( String fileName )
	{
		return Strs.regexCapture( fileName, ".*\\.(.*)$" );
	}

	public static String getFileName( String path )
	{
		path = path.replace( "\\/", "/" );
		if ( path.contains( File.pathSeparator ) )
			return path.substring( path.lastIndexOf( File.pathSeparator ) + 1 );
		if ( path.contains( "/" ) )
			return path.substring( path.lastIndexOf( "/" ) + 1 );
		if ( path.contains( "\\" ) )
			return path.substring( path.lastIndexOf( "\\" ) + 1 );
		return path;
	}

	public static String getFileNameWithoutExtension( String path )
	{
		path = getFileName( path );
		if ( path.contains( "." ) )
			path = path.substring( 0, path.lastIndexOf( "." ) );
		return path;
	}

	/**
	 * List directory contents for a resource folder. Not recursive.
	 * This is basically a brute-force implementation.
	 * Works for regular files and also JARs.
	 *
	 * @param clazz Any java class that lives in the same place as the resources you want.
	 * @param path  Should end with "/", but not start with one.
	 * @return Just the name of each member item, not the full paths.
	 * @throws URISyntaxException
	 * @throws IOException
	 * @author Greg Briggs
	 */
	public static String[] getResourceListing( Class<?> clazz, String path ) throws URISyntaxException, IOException
	{
		URL dirURL = clazz.getClassLoader().getResource( path );

		if ( dirURL == null )
		{
			/*
			 * In case of a jar file, we can't actually find a directory.
			 * Have to assume the same jar as clazz.
			 */
			String me = clazz.getName().replace( ".", "/" ) + ".class";
			dirURL = clazz.getClassLoader().getResource( me );
		}

		if ( dirURL.getProtocol().equals( "file" ) )
			/* A file path: easy enough */
			return new File( dirURL.toURI() ).list();

		if ( dirURL.getProtocol().equals( "jar" ) || dirURL.getProtocol().equals( "zip" ) )
		{
			/* A JAR or ZIP path */
			String archivePath = dirURL.getPath().substring( 5, dirURL.getPath().indexOf( "!" ) ); // strip out only the archive file
			Set<String> result;
			if ( dirURL.getProtocol().equals( "jar" ) )
			{
				JarFile jar = new JarFile( URLDecoder.decode( archivePath, "UTF-8" ) );
				result = entriesToSet( archivePath, jar.entries() );
				jar.close();
			}
			else// if ( dirURL.getProtocol().equals( "zip" ) )
			{
				ZipFile zip = new ZipFile( URLDecoder.decode( archivePath, "UTF-8" ) );
				result = entriesToSet( archivePath, zip.entries() );
				zip.close();
			}
			return result.toArray( new String[result.size()] );
		}

		throw new UnsupportedOperationException( "Cannot list files for URL " + dirURL );
	}

	public static void gzFile( File source ) throws IOException
	{
		gzFile( source, new File( source + ".gz" ) );
	}

	public static void gzFile( File source, File dest ) throws IOException
	{
		byte[] buffer = new byte[1024];

		GZIPOutputStream gzos = new GZIPOutputStream( new FileOutputStream( dest ) );

		FileInputStream in = new FileInputStream( source );

		int len;
		while ( ( len = in.read( buffer ) ) > 0 )
			gzos.write( buffer, 0, len );

		in.close();

		gzos.finish();
		gzos.close();
	}

	public static String hex2Readable( byte... elements )
	{
		if ( elements == null )
			return "";

		// TODO Char Dump
		String result = "";
		char[] chars = encodeHex( elements );
		for ( int i = 0; i < chars.length; i = i + 2 )
			result += " " + chars[i] + chars[i + 1];

		if ( result.length() > 0 )
			result = result.substring( 1 );

		return result;
	}

	public static String hex2Readable( int... elements )
	{
		byte[] e2 = new byte[elements.length];
		for ( int i = 0; i < elements.length; i++ )
			e2[i] = ( byte ) elements[i];
		return hex2Readable( e2 );
	}

	public static String hexDump( ByteBuf buf )
	{
		return hexDump( buf, buf.readerIndex() );
	}

	public static String hexDump( ByteBuf buf, int highlightIndex )
	{
		if ( buf == null )
			return "Buffer: null!";

		if ( buf.capacity() < 1 )
			return "Buffer: 0B!";

		StringBuilder dump = new StringBuilder();

		final int startIndex = 0;
		final int endIndex = buf.capacity();
		final int length = endIndex - startIndex;
		final int fullRows = length >>> 4;
		final int remainder = length & 0xF;

		int highlightRow = -1;

		dump.append( NEWLINE + "         +-------------------------------------------------+" + NEWLINE + "         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |" + NEWLINE + "+--------+-------------------------------------------------+----------------+" );

		if ( highlightIndex > 0 )
		{
			highlightRow = highlightIndex >>> 4;
			highlightIndex = highlightIndex - ( 16 * highlightRow ) - 1;

			dump.append( NEWLINE + "|        |" + Strings.repeat( "   ", highlightIndex ) + " $$" + Strings.repeat( "   ", 15 - highlightIndex ) );
			dump.append( " |" + Strings.repeat( " ", highlightIndex ) + "$" + Strings.repeat( " ", 15 - highlightIndex ) + "|" );
		}

		// Dump the rows which have 16 bytes.
		for ( int row = 0; row < fullRows; row++ )
		{
			int rowStartIndex = row << 4;

			// Per-row prefix.
			appendHexDumpRowPrefix( dump, row, rowStartIndex );

			// Hex dump
			int rowEndIndex = rowStartIndex + 16;
			for ( int j = rowStartIndex; j < rowEndIndex; j++ )
				dump.append( BYTE2HEX[buf.getUnsignedByte( j )] );
			dump.append( " |" );

			// ASCII dump
			for ( int j = rowStartIndex; j < rowEndIndex; j++ )
				dump.append( BYTE2CHAR[buf.getUnsignedByte( j )] );
			dump.append( '|' );

			if ( highlightIndex > 0 && highlightRow == row + 1 )
				dump.append( " <--" );
		}

		// Dump the last row which has less than 16 bytes.
		if ( remainder != 0 )
		{
			int rowStartIndex = fullRows << 4;
			appendHexDumpRowPrefix( dump, fullRows, rowStartIndex );

			// Hex dump
			int rowEndIndex = rowStartIndex + remainder;
			for ( int j = rowStartIndex; j < rowEndIndex; j++ )
				dump.append( BYTE2HEX[buf.getUnsignedByte( j )] );
			dump.append( HEXPADDING[remainder] );
			dump.append( " |" );

			// Ascii dump
			for ( int j = rowStartIndex; j < rowEndIndex; j++ )
				dump.append( BYTE2CHAR[buf.getUnsignedByte( j )] );
			dump.append( BYTEPADDING[remainder] );
			dump.append( '|' );

			if ( highlightIndex > 0 && highlightRow > fullRows + 1 )
				dump.append( " <--" );
		}

		dump.append( NEWLINE + "+--------+-------------------------------------------------+----------------+" );

		return dump.toString();
	}

	public static boolean isAbsolute( String dir )
	{
		return dir.startsWith( "/" ) || dir.startsWith( ":\\", 1 );
	}

	public static boolean isDirectoryEmpty( File file )
	{
		Objs.notNull( file );
		if ( file.isDirectory() )
		{
			String[] lst = file.list();
			return lst != null && lst.length == 0;
		}
		return false;
	}

	public static Stream<File> listFiles( File dir )
	{
		File[] files = dir.listFiles();
		return files == null ? Stream.empty() : Arrays.stream( files );
	}

	public static Map<String, List<File>> mapExtensions( File[] files )
	{
		Map<String, List<File>> result = new TreeMap<>();
		for ( File f : files )
			result.compute( fileExtension( f ).toLowerCase(), ( k, l ) -> l == null ? new ArrayList<>() : l ).add( f );
		return result;
	}

	public static Map<Long, List<File>> mapLastModified( File[] files )
	{
		Map<Long, List<File>> result = new TreeMap<>();
		for ( File f : files )
			result.compute( f.lastModified(), ( k, l ) -> l == null ? new ArrayList<>() : l ).add( f );
		return result;
	}

	public static void putResource( Class<?> clz, String resource, File file ) throws IOException
	{
		try
		{
			InputStream is = clz.getClassLoader().getResourceAsStream( resource );
			if ( is == null )
				throw new IOException( String.format( "The resource %s does not exist.", resource ) );
			FileOutputStream os = new FileOutputStream( file );
			copy( is, os );
			is.close();
			os.close();
		}
		catch ( FileNotFoundException e )
		{
			throw new IOException( e );
		}
	}

	public static void putResource( String resource, File file ) throws IOException
	{
		putResource( IO.class, resource, file );
	}

	public static List<String> readFileToLines( @Nonnull File file, @Nonnull String ignorePrefix ) throws FileNotFoundException
	{
		return readFileToStream( file, ignorePrefix ).collect( Collectors.toList() );
	}

	public static List<String> readFileToLines( @Nonnull File file ) throws FileNotFoundException
	{
		return readFileToStream( file ).collect( Collectors.toList() );
	}

	public static Stream<String> readFileToStream( @Nonnull File file, @Nonnull String ignorePrefix ) throws FileNotFoundException
	{
		Objs.notNull( file );
		Objs.notNull( ignorePrefix );

		return new BufferedReader( new InputStreamReader( new FileInputStream( file ) ) ).lines().filter( s -> !s.toLowerCase().startsWith( ignorePrefix.toLowerCase() ) );
	}

	public static Stream<String> readFileToStream( @Nonnull File file ) throws FileNotFoundException
	{
		Objs.notNull( file );

		return new BufferedReader( new InputStreamReader( new FileInputStream( file ) ) ).lines();
	}

	public static String readFileToString( @Nonnull File file ) throws IOException
	{
		InputStream in = null;
		try
		{
			in = new FileInputStream( file );

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ( ( nRead = in.read( data, 0, data.length ) ) != -1 )
				buffer.write( data, 0, nRead );

			return new String( buffer.toByteArray(), Charset.defaultCharset() );
		}
		finally
		{
			closeQuietly( in );
		}
	}

	public static ByteArrayOutputStream readStreamToByteArray( InputStream is ) throws IOException
	{
		try
		{
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ( ( nRead = is.read( data, 0, data.length ) ) != -1 )
				buffer.write( data, 0, nRead );

			buffer.flush();
			return buffer;
		}
		finally
		{
			closeQuietly( is );
		}
	}

	public static byte[] readStreamToBytes( InputStream is ) throws IOException
	{
		return readStreamToByteArray( is ).toByteArray();
	}

	public static List<String> readStreamToLines( @Nonnull InputStream is, @Nonnull String ignorePrefix )
	{
		return readStreamToStream( is, ignorePrefix ).collect( Collectors.toList() );
	}

	public static Stream<String> readStreamToStream( @Nonnull InputStream is, @Nonnull String ignorePrefix )
	{
		Objs.notNull( is );
		Objs.notNull( ignorePrefix );

		return new BufferedReader( new InputStreamReader( is ) ).lines().filter( s -> !s.toLowerCase().startsWith( ignorePrefix.toLowerCase() ) );
	}

	public static Stream<String> readStreamToStream( @Nonnull InputStream is ) throws FileNotFoundException
	{
		Objs.notNull( is );

		return new BufferedReader( new InputStreamReader( is ) ).lines();
	}

	public static String readStreamToString( @Nonnull InputStream is ) throws IOException
	{
		return Strs.encodeDefault( readStreamToByteArray( is ).toByteArray() );
	}

	public static List<File> recursiveFiles( final File dir )
	{
		return recursiveFiles( dir, 9999 );
	}

	private static List<File> recursiveFiles( final File start, final File current, final int depth, final int maxDepth, final String regexPattern )
	{
		final List<File> files = new ArrayList<>();

		current.list( ( dir, name ) -> {
			dir = new File( dir, name );

			if ( dir.isDirectory() && depth < maxDepth )
				files.addAll( recursiveFiles( start, dir, depth + 1, maxDepth, regexPattern ) );

			if ( dir.isFile() )
			{
				String filename = dir.getAbsolutePath();
				filename = filename.substring( start.getAbsolutePath().length() + 1 );
				if ( regexPattern == null || filename.matches( regexPattern ) )
					files.add( dir );
			}

			return false;
		} );

		return files;
	}

	public static List<File> recursiveFiles( final File dir, final int maxDepth )
	{
		return recursiveFiles( dir, maxDepth, null );
	}

	public static List<File> recursiveFiles( final File dir, final int maxDepth, final String regexPattern )
	{
		return recursiveFiles( dir, dir, 0, maxDepth, regexPattern );
	}

	/**
	 * Constructs a relative path from the server root
	 *
	 * @param file The file you wish to get relative to
	 * @return The relative path to the file, will return absolute if file is not relative to server root
	 */
	public static String relPath( File file )
	{
		return relPath( file, Kernel.getPath() );
	}

	public static String relPath( File file, File relTo )
	{
		if ( file == null || relTo == null )
			return null;
		String root = relTo.getAbsolutePath();
		if ( file.getAbsolutePath().startsWith( root ) )
			return file.getAbsolutePath().substring( root.length() + 1 );
		else
			return file.getAbsolutePath();
	}

	public static String resourceToString( String resource ) throws IOException
	{
		return resourceToString( resource, IO.class );
	}

	public static String resourceToString( String resource, Class<?> clz ) throws IOException
	{
		InputStream is = clz.getClassLoader().getResourceAsStream( resource );

		if ( is == null )
			return null;

		return new String( readStreamToBytes( is ), "UTF-8" );
	}

	public static boolean setDirectoryAccess( File file )
	{
		if ( file.exists() && file.isDirectory() && file.canRead() && file.canWrite() )
			L.finest( "This application has read and write access to directory \"" + relPath( file ) + "\"!" );
		else
			try
			{
				if ( file.exists() && file.isFile() )
					Objs.notFalse( file.delete(), "failed to delete directory!" );
				Objs.notFalse( file.mkdirs(), "failed to create directory!" );
				Objs.notFalse( file.setWritable( true ), "failed to set directory writable!" );
				Objs.notFalse( file.setReadable( true ), "failed to set directory readable!" );

				L.fine( "Setting read and write access for directory \"" + relPath( file ) + "\" was successful!" );
			}
			catch ( IllegalArgumentException e )
			{
				L.severe( "Exception encountered while handling access to path '" + relPath( file ) + "' with message '" + e.getMessage() + "'" );
				return false;
			}
		return true;
	}

	public static void setDirectoryAccessWithException( File file )
	{
		if ( !setDirectoryAccess( file ) )
			throw new UncaughtException( ReportingLevel.E_ERROR, "Experienced a problem setting read and write access to directory \"" + relPath( file ) + "\"!" );
	}

	public static void writeStringToFile( File file, String data ) throws IOException
	{
		writeStringToFile( file, data, false );
	}

	public static void writeStringToFile( File file, String data, boolean append ) throws IOException
	{
		BufferedWriter out = null;
		try
		{
			out = new BufferedWriter( new FileWriter( file, append ) );
			out.write( data );
		}
		finally
		{
			if ( out != null )
				out.close();
		}
	}

	public static void zipDir( File src, File dest ) throws IOException
	{
		if ( dest.isDirectory() )
			dest = new File( dest, "temp.zip" );

		ZipOutputStream out = new ZipOutputStream( new FileOutputStream( dest ) );
		try
		{
			zipDirRecursive( src, src, out );
		}
		finally
		{
			out.close();
		}
	}

	private static void zipDirRecursive( File origPath, File dirObj, ZipOutputStream out ) throws IOException
	{
		File[] files = dirObj.listFiles();
		byte[] tmpBuf = new byte[1024];

		for ( int i = 0; i < files.length; i++ )
		{
			if ( files[i].isDirectory() )
			{
				zipDirRecursive( origPath, files[i], out );
				continue;
			}
			FileInputStream in = new FileInputStream( files[i].getAbsolutePath() );
			out.putNextEntry( new ZipEntry( relPath( files[i], origPath ) ) );
			int len;
			while ( ( len = in.read( tmpBuf ) ) > 0 )
				out.write( tmpBuf, 0, len );
			out.closeEntry();
			in.close();
		}
	}

	private IO()
	{

	}

	static class LibraryPath
	{
		private List<String> libPath = new ArrayList<>();

		LibraryPath()
		{
			read();
		}

		void add( String path )
		{
			if ( path.contains( " " ) )
				path = "\"" + path + "\"";
			if ( libPath.contains( path ) )
				return;
			libPath.add( path );
		}

		void read()
		{
			String prop = System.getProperty( "java.library.path" );
			if ( !Objs.isNull( prop ) )
				libPath.addAll( Arrays.asList( prop.split( ":" ) ) );
		}

		void set()
		{
			System.setProperty( "java.library.path", Strs.join( libPath, ":" ) );
		}
	}

	/**
	 * Separate class for native platform ID which is only loaded when native libs are loaded.
	 */
	public static class OSInfo
	{
		public static final String ARCH_NAME;
		public static final String CPU_ID;
		public static final String[] NATIVE_SEARCH_PATHS;
		public static final String OS_ID;

		static
		{
			final Object[] strings = AccessController.doPrivileged( new PrivilegedAction<Object[]>()
			{
				@Override
				public Object[] run()
				{
					// First, identify the operating system.
					boolean knownOs = true;
					String osName;
					// let the user override it.
					osName = System.getProperty( "chiori.os-name" );
					if ( osName == null )
					{
						String sysOs = System.getProperty( "os.name" );
						if ( sysOs == null )
						{
							osName = "unknown";
							knownOs = false;
						}
						else
						{
							sysOs = sysOs.toUpperCase( Locale.US );
							if ( sysOs.startsWith( "LINUX" ) )
								osName = "linux";
							else if ( sysOs.startsWith( "MAC OS" ) )
								osName = "macosx";
							else if ( sysOs.startsWith( "WINDOWS" ) )
								osName = "win";
							else if ( sysOs.startsWith( "OS/2" ) )
								osName = "os2";
							else if ( sysOs.startsWith( "SOLARIS" ) || sysOs.startsWith( "SUNOS" ) )
								osName = "solaris";
							else if ( sysOs.startsWith( "MPE/IX" ) )
								osName = "mpeix";
							else if ( sysOs.startsWith( "HP-UX" ) )
								osName = "hpux";
							else if ( sysOs.startsWith( "AIX" ) )
								osName = "aix";
							else if ( sysOs.startsWith( "OS/390" ) )
								osName = "os390";
							else if ( sysOs.startsWith( "OS/400" ) )
								osName = "os400";
							else if ( sysOs.startsWith( "FREEBSD" ) )
								osName = "freebsd";
							else if ( sysOs.startsWith( "OPENBSD" ) )
								osName = "openbsd";
							else if ( sysOs.startsWith( "NETBSD" ) )
								osName = "netbsd";
							else if ( sysOs.startsWith( "IRIX" ) )
								osName = "irix";
							else if ( sysOs.startsWith( "DIGITAL UNIX" ) )
								osName = "digitalunix";
							else if ( sysOs.startsWith( "OSF1" ) )
								osName = "osf1";
							else if ( sysOs.startsWith( "OPENVMS" ) )
								osName = "openvms";
							else if ( sysOs.startsWith( "IOS" ) )
								osName = "iOS";
							else
							{
								osName = "unknown";
								knownOs = false;
							}
						}
					}
					// Next, our CPU ID and its compatible variants.
					boolean knownCpu = true;
					ArrayList<String> cpuNames = new ArrayList<>();

					String cpuName = System.getProperty( "jboss.modules.cpu-name" );
					if ( cpuName == null )
					{
						String sysArch = System.getProperty( "os.arch" );
						if ( sysArch == null )
						{
							cpuName = "unknown";
							knownCpu = false;
						}
						else
						{
							boolean hasEndian = false;
							boolean hasHardFloatABI = false;
							sysArch = sysArch.toUpperCase( Locale.US );
							if ( sysArch.startsWith( "SPARCV9" ) || sysArch.startsWith( "SPARC64" ) )
								cpuName = "sparcv9";
							else if ( sysArch.startsWith( "SPARC" ) )
								cpuName = "sparc";
							else if ( sysArch.startsWith( "X86_64" ) || sysArch.startsWith( "AMD64" ) )
								cpuName = "x86_64";
							else if ( sysArch.startsWith( "I386" ) )
								cpuName = "i386";
							else if ( sysArch.startsWith( "I486" ) )
								cpuName = "i486";
							else if ( sysArch.startsWith( "I586" ) )
								cpuName = "i586";
							else if ( sysArch.startsWith( "I686" ) || sysArch.startsWith( "X86" ) || sysArch.contains( "IA32" ) )
								cpuName = "i686";
							else if ( sysArch.startsWith( "X32" ) )
								cpuName = "x32";
							else if ( sysArch.startsWith( "PPC64" ) )
								cpuName = "ppc64";
							else if ( sysArch.startsWith( "PPC" ) || sysArch.startsWith( "POWER" ) )
								cpuName = "ppc";
							else if ( sysArch.startsWith( "ARMV7A" ) || sysArch.contains( "AARCH32" ) )
							{
								hasEndian = true;
								hasHardFloatABI = true;
								cpuName = "armv7a";
							}
							else if ( sysArch.startsWith( "AARCH64" ) || sysArch.startsWith( "ARM64" ) || sysArch.startsWith( "ARMV8" ) || sysArch.startsWith( "PXA9" ) || sysArch.startsWith( "PXA10" ) )
							{
								hasEndian = true;
								cpuName = "aarch64";
							}
							else if ( sysArch.startsWith( "PXA27" ) )
							{
								hasEndian = true;
								cpuName = "armv5t-iwmmx";
							}
							else if ( sysArch.startsWith( "PXA3" ) )
							{
								hasEndian = true;
								cpuName = "armv5t-iwmmx2";
							}
							else if ( sysArch.startsWith( "ARMV4T" ) || sysArch.startsWith( "EP93" ) )
							{
								hasEndian = true;
								cpuName = "armv4t";
							}
							else if ( sysArch.startsWith( "ARMV4" ) || sysArch.startsWith( "EP73" ) )
							{
								hasEndian = true;
								cpuName = "armv4";
							}
							else if ( sysArch.startsWith( "ARMV5T" ) || sysArch.startsWith( "PXA" ) || sysArch.startsWith( "IXC" ) || sysArch.startsWith( "IOP" ) || sysArch.startsWith( "IXP" ) || sysArch.startsWith( "CE" ) )
							{
								hasEndian = true;
								String isaList = System.getProperty( "sun.arch.isalist" );
								if ( isaList != null )
								{
									if ( isaList.toUpperCase( Locale.US ).contains( "MMX2" ) )
										cpuName = "armv5t-iwmmx2";
									else if ( isaList.toUpperCase( Locale.US ).contains( "MMX" ) )
										cpuName = "armv5t-iwmmx";
									else
										cpuName = "armv5t";
								}
								else
									cpuName = "armv5t";
							}
							else if ( sysArch.startsWith( "ARMV5" ) )
							{
								hasEndian = true;
								cpuName = "armv5";
							}
							else if ( sysArch.startsWith( "ARMV6" ) )
							{
								hasEndian = true;
								hasHardFloatABI = true;
								cpuName = "armv6";
							}
							else if ( sysArch.startsWith( "PA_RISC2.0W" ) )
								cpuName = "parisc64";
							else if ( sysArch.startsWith( "PA_RISC" ) || sysArch.startsWith( "PA-RISC" ) )
								cpuName = "parisc";
							else if ( sysArch.startsWith( "IA64" ) )
								// HP-UX reports IA64W for 64-bit Itanium and IA64N when running
								// in 32-bit mode.
								cpuName = sysArch.toLowerCase( Locale.US );
							else if ( sysArch.startsWith( "ALPHA" ) )
								cpuName = "alpha";
							else if ( sysArch.startsWith( "MIPS" ) )
								cpuName = "mips";
							else
							{
								knownCpu = false;
								cpuName = "unknown";
							}

							boolean be = false;
							boolean hf = false;

							if ( knownCpu && hasEndian && "big".equals( System.getProperty( "sun.cpu.endian", "little" ) ) )
								be = true;

							if ( knownCpu && hasHardFloatABI )
							{
								String archAbi = System.getProperty( "sun.arch.abi" );
								if ( archAbi != null )
								{
									if ( archAbi.toUpperCase( Locale.US ).contains( "HF" ) )
										hf = true;
								}
								else
								{
									String libPath = System.getProperty( "java.library.path" );
									if ( libPath != null && libPath.toUpperCase( Locale.US ).contains( "GNUEABIHF" ) )
										hf = true;
								}
								if ( hf )
									cpuName += "-hf";
							}

							if ( knownCpu )
							{
								switch ( cpuName )
								{
									case "i686":
										cpuNames.add( "i686" );
									case "i586":
										cpuNames.add( "i586" );
									case "i486":
										cpuNames.add( "i486" );
									case "i386":
										cpuNames.add( "i386" );
										break;
									case "armv7a":
										cpuNames.add( "armv7a" );
										if ( hf )
											break;
									case "armv6":
										cpuNames.add( "armv6" );
										if ( hf )
											break;
									case "armv5t":
										cpuNames.add( "armv5t" );
									case "armv5":
										cpuNames.add( "armv5" );
									case "armv4t":
										cpuNames.add( "armv4t" );
									case "armv4":
										cpuNames.add( "armv4" );
										break;
									case "armv5t-iwmmx2":
										cpuNames.add( "armv5t-iwmmx2" );
									case "armv5t-iwmmx":
										cpuNames.add( "armv5t-iwmmx" );
										cpuNames.add( "armv5t" );
										cpuNames.add( "armv5" );
										cpuNames.add( "armv4t" );
										cpuNames.add( "armv4" );
										break;
									default:
										cpuNames.add( cpuName );
										break;
								}
								if ( hf || be )
									for ( int i = 0; i < cpuNames.size(); i++ )
									{
										String name = cpuNames.get( i );
										if ( be )
											name += "-be";
										if ( hf )
											name += "-hf";
										cpuNames.set( i, name );
									}
								cpuName = cpuNames.get( 0 );
							}
						}
					}

					// Finally, search paths.
					final int cpuCount = cpuNames.size();
					String[] searchPaths = new String[cpuCount];
					if ( knownOs && knownCpu )
						for ( int i = 0; i < cpuCount; i++ )
						{
							final String name = cpuNames.get( i );
							searchPaths[i] = osName + "-" + name;
						}
					else
						searchPaths = new String[0];

					return new Object[] {osName, cpuName, osName + "-" + cpuName, searchPaths};
				}
			} );
			OS_ID = strings[0].toString();
			CPU_ID = strings[1].toString();
			ARCH_NAME = strings[2].toString();
			NATIVE_SEARCH_PATHS = ( String[] ) strings[3];
		}
	}

	public static class SortableFile implements Comparable<SortableFile>
	{
		public File f;
		public long t;

		public SortableFile( File file )
		{
			f = file;
			t = file.lastModified();
		}

		@Override
		public int compareTo( SortableFile o )
		{
			long u = o.t;
			return t < u ? -1 : t == u ? 0 : 1;
		}
	}
}
