package com.marchnetworks.command.common.timezones;

import com.marchnetworks.command.common.timezones.data.Generation;
import com.marchnetworks.command.common.timezones.data.MapZone;
import com.marchnetworks.command.common.timezones.data.SupplementalData;
import com.marchnetworks.command.common.timezones.data.Timezone;
import com.marchnetworks.command.common.timezones.data.Timezones;
import com.marchnetworks.command.common.timezones.data.Version;
import com.marchnetworks.command.common.timezones.data.WindowsZones;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.time.FastDateFormat;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class TimezonesDictionary
{
	private static final String TERRITORY = "\" territory=";
	private static final String OTHER = "other=";
	private static final Object DEFAULT_TERRITORY = "001";
	private static final String UNMAPPABLE = "Unmappable";
	private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance( "yyyy-MM-dd HH:mm:ss" );

	private static final String CUSTOM = "CUSTOM";

	private static final String WINDOWS_ZONE_FILE_NAME = "windowsZones.xml";

	private static final String TIMEZONES_FILE_NAME = "timezones.xml";

	private static Map<String, String> olsons;
	private static Map<String, String> windowsToEnglish;
	private static Map<String, String> englishToWindows;
	private static Map<String, List<MapZone>> windows;
	private static List<Timezone> timezones;
	private static JAXBContext CONTEXT;
	private static Unmarshaller UNMARSHALLER;
	private static Marshaller MARSHALLER;
	private static Bundle BUNDLE;

	static
	{
		boolean customFile = true;

		try
		{
			init();

			SupplementalData data = checkCustomFile();

			if ( data == null )
			{
				data = ( SupplementalData ) UNMARSHALLER.unmarshal( BUNDLE.getEntry( "/windowsZones.xml" ) );

				customFile = false;
			}

			for ( MapZone mapZone : data.getWindowsZones().getMapZones() )
			{
				if ( mapZone.getTerritory().equals( DEFAULT_TERRITORY ) )
				{
					olsons.put( mapZone.getOlson(), mapZone.getWindow() );
				}
				else if ( olsons.get( mapZone.getOlson() ) == null )
				{

					olsons.put( mapZone.getOlson(), mapZone.getWindow() );
				}

				filliInWindowDictionary( mapZone );
			}

			if ( customFile )
			{
				fillInEnglishDictionaryAndTimezones();
			}
			else
			{
				fillInEnglishDictionariesAndMapZonesList( BUNDLE.getEntry( "/windowsZones.xml" ).openConnection().getInputStream() );
			}

		}
		catch ( Exception e )
		{
			throw new ExceptionInInitializerError( e );
		}
	}

	private static void fillInEnglishDictionaryAndTimezones() throws JAXBException
	{
		File customTimezoneFile = new File( "timezones.xml" );

		Timezones timezones = ( Timezones ) JAXBContext.newInstance( Timezones.class ).createUnmarshaller().unmarshal( customTimezoneFile );
		for ( Timezone timezone : timezones.getTimezones() )
		{
			windowsToEnglish.put( timezone.getWindow(), timezone.getEnglish() );
			englishToWindows.put( timezone.getEnglish(), timezone.getWindow() );
		}
	}

	private static void filliInWindowDictionary( MapZone mapZone )
	{
		List<MapZone> mapZones = ( List ) windows.get( mapZone.getWindow() );
		if ( mapZones == null )
		{
			mapZones = new ArrayList();
		}
		mapZones.add( mapZone );
		windows.put( mapZone.getWindow(), mapZones );
	}

	private static SupplementalData checkCustomFile() throws JAXBException
	{
		File customFile = new File( "windowsZones.xml" );
		if ( customFile.exists() )
		{
			return ( SupplementalData ) UNMARSHALLER.unmarshal( customFile );
		}
		return null;
	}

	private static void init() throws JAXBException
	{
		BUNDLE = FrameworkUtil.getBundle( TimezonesDictionary.class );

		CONTEXT = JAXBContext.newInstance( new Class[] {SupplementalData.class} );
		UNMARSHALLER = CONTEXT.createUnmarshaller();
		MARSHALLER = CONTEXT.createMarshaller();

		windows = new HashMap();
		olsons = new HashMap();
		windowsToEnglish = new HashMap();
		englishToWindows = new TreeMap();
		timezones = new ArrayList();
	}

	public static List<Timezone> getTimezones()
	{
		return timezones;
	}

	private static void fillInEnglishDictionariesAndMapZonesList( InputStream inputStream )
	{
		BufferedReader reader = null;

		try
		{
			reader = new BufferedReader( new InputStreamReader( inputStream ) );
			String line;
			String delta;
			String english;
			String window;
			while ( ( line = reader.readLine() ) != null )
			{
				if ( ( line.contains( "(UTC" ) ) && ( line.contains( "<!--" ) ) )
				{
					delta = line.substring( line.indexOf( "(" ) + 1, line.indexOf( ")" ) );
					english = line.substring( line.indexOf( ")" ) + 1, line.indexOf( "-->" ) - 1 ).trim();
					line = reader.readLine();
					if ( ( !line.contains( "Unmappable" ) ) && ( englishToWindows.get( english ) == null ) )
					{

						window = line.substring( line.indexOf( "other=" ) + "other=".length() + 1, line.indexOf( "\" territory=" ) ).trim();
						windowsToEnglish.put( window, english );
						englishToWindows.put( english, window );
						for ( MapZone olson : windows.get( window ) )
							if ( olson.getTerritory().equals( DEFAULT_TERRITORY ) )
								timezones.add( new Timezone( olson.getOlson(), window, delta, english ) );
					}
				}
			}
			return;
		}
		catch ( IOException e )
		{
			throw new ExceptionInInitializerError( e );
		}
		finally
		{
			try
			{
				if ( reader != null )
				{
					reader.close();
				}
			}
			catch ( IOException e )
			{
				throw new ExceptionInInitializerError( e );
			}
		}
	}

	public static String fromOlsonToWindow( String olson )
	{
		return ( String ) olsons.get( olson );
	}

	public static String fromWindowToOlson( String window )
	{
		if ( windows.get( window ) != null )
		{
			for ( MapZone mapZone : windows.get( window ) )
			{
				if ( mapZone.getTerritory().equals( DEFAULT_TERRITORY ) )
				{
					return mapZone.getOlson();
				}
			}
		}
		return null;
	}

	public static String fromWindowToEnglish( String window )
	{
		return ( String ) windowsToEnglish.get( window );
	}

	public static String fromEnglishToWindow( String english )
	{
		return ( String ) englishToWindows.get( english );
	}

	public static TimeZone fromWindowToTimeZone( String window )
	{
		return TimeZone.getTimeZone( fromWindowToOlson( window ) );
	}

	public static List<String> getAllWindowsTimezones()
	{
		List<String> timezones = new ArrayList( windows.keySet() );
		Collections.sort( timezones );
		return timezones;
	}

	public static List<String> getAllEnglishTimezones()
	{
		List<String> timezones = new ArrayList( englishToWindows.keySet() );
		Collections.sort( timezones );
		return timezones;
	}

	public static List<String> getAllOlson()
	{
		List<String> timezones = new ArrayList( olsons.keySet() );
		Collections.sort( timezones );
		return timezones;
	}

	public static List<MapZone> addNewData( InputStream inputStream ) throws JAXBException, IOException
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		byte[] buffer = new byte['Ѐ'];
		int len;
		while ( ( len = inputStream.read( buffer ) ) > -1 )
		{
			byteArrayOutputStream.write( buffer, 0, len );
		}
		byteArrayOutputStream.flush();

		SupplementalData data = ( SupplementalData ) UNMARSHALLER.unmarshal( new ByteArrayInputStream( byteArrayOutputStream.toByteArray() ) );

		List<MapZone> added = new ArrayList();

		boolean addNewData = false;

		for ( MapZone mapZone : data.getWindowsZones().getMapZones() )
		{
			if ( isToBeAdd( mapZone ) )
			{
				addNewData = true;

				added.add( mapZone );

				olsons.put( mapZone.getOlson(), mapZone.getWindow() );

				filliInWindowDictionary( mapZone );
			}
		}

		if ( addNewData )
		{
			fillInEnglishDictionariesAndMapZonesList( new ByteArrayInputStream( byteArrayOutputStream.toByteArray() ) );
			save();
		}

		return added;
	}

	private static void save() throws JAXBException, IOException
	{
		SupplementalData data = new SupplementalData();
		data.setGeneration( new Generation( DATE_FORMAT.format( Calendar.getInstance().getTime() ) ) );
		data.setVersion( new Version( "CUSTOM" ) );

		List<MapZone> mapZones = new ArrayList<MapZone>();

		for ( String key : windows.keySet() )
			mapZones.addAll( windows.get( key ) );

		data.setWindowsZones( new WindowsZones( mapZones ) );

		OutputStream os = new FileOutputStream( "windowsZones.xml" );
		MARSHALLER.marshal( data, os );
		os.flush();

		OutputStream tzOut = new FileOutputStream( "timezones.xml" );
		JAXBContext context = JAXBContext.newInstance( Timezones.class );
		context.createMarshaller().marshal( new Timezones( timezones ), tzOut );
		tzOut.flush();
	}

	private static boolean isToBeAdd( MapZone mapZone )
	{
		if ( ( olsons.get( mapZone.getOlson() ) == null ) || ( !( ( String ) olsons.get( mapZone.getOlson() ) ).equals( mapZone.getWindow() ) ) )
			return true;
		return false;
	}
}
