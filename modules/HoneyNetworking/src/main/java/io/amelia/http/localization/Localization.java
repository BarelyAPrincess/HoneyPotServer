/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.localization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import io.amelia.data.parcel.Parcel;
import io.amelia.data.parcel.ParcelLoader;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ParcelableException;
import io.amelia.support.DateAndTime;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Pair;
import io.amelia.support.Strs;

public class Localization
{
	private Path basePath;
	private Map<String, Pair<Long, Parcel>> cache = new ConcurrentHashMap<>();
	private Locale locale;
	private Path localePath;

	public Localization( @Nonnull Path basePath ) throws IOException
	{
		this( basePath, Locale.US );
	}

	public Localization( @Nonnull Path basePath, @Nonnull Locale locale ) throws IOException
	{
		this.locale = locale;
		this.basePath = basePath;
		this.localePath = Paths.get( locale.toLanguageTag() ).resolve( basePath );
		IO.forceCreateDirectory( this.localePath );
	}

	private Parcel getLang( String key ) throws LocalizationException, IOException, ParcelableException.Error
	{
		if ( key.contains( "/" ) || key.contains( "\\" ) )
			throw new LocalizationException( "Locale key can't contain forward/back slashes." );

		Path langPath = Paths.get( key.replace( ".", "/" ) + ".yaml" ).resolve( basePath );
		if ( !Files.exists( langPath ) )
			throw new LocalizationException( "The language file " + IO.relPath( langPath ) + " does not exist." );
		Parcel parcel = ParcelLoader.decodeYaml( langPath );

		String langName = Strs.splitLiteral( key, "." ).reduce( ( first, second ) -> second ).orElse( null );

		if ( parcel.getKeys().size() == 0 && parcel.hasChild( langName ) )
			parcel = parcel.getChild( langName );

		return parcel;
	}

	public Locale getLocale()
	{
		return locale;
	}

	public void setLocale( @Nonnull Locale locale ) throws IOException
	{
		this.locale = locale;
		this.localePath = Paths.get( locale.toLanguageTag() ).resolve( this.basePath );
		IO.forceCreateDirectory( this.localePath );
		cache.clear();
	}

	public String localePlural( String key, int cnt ) throws LocalizationException, IOException, ParcelableException.Error
	{
		String str = localeTrans( key );

		if ( str.contains( "|" ) )
		{
			String[] choices = str.split( "|" );

			for ( String choice : choices )
				if ( choice.startsWith( "[" ) && choice.contains( "]" ) )
				{
					String range = choice.substring( 1, choice.indexOf( "]" ) );
					try
					{
						if ( Integer.parseInt( range ) == cnt )
							return choice.substring( choice.indexOf( "]" ) ).trim();
					}
					catch ( NumberFormatException e )
					{
						String[] numbers = range.contains( "," ) ? range.split( "," ) : new String[] {range};
						for ( String num : numbers )
							try
							{
								if ( num.contains( "-" ) )
								{
									String[] lr = num.split( "-" );
									for ( int n = Integer.parseInt( lr[0] ); n <= Integer.parseInt( lr[1] ); n++ )
										if ( n == cnt )
											return choice.substring( choice.indexOf( "]" ) ).trim();
								}
								else if ( Integer.parseInt( num ) == cnt )
									return choice.substring( choice.indexOf( "]" ) ).trim();
							}
							catch ( NumberFormatException ee )
							{
								// Ignore
							}
					}
				}
				else
					break;

			if ( choices.length == 2 )
			{
				if ( cnt <= 1 )
					return choices[0];
				return choices[1];
			}
			else if ( choices.length == 3 )
			{
				if ( cnt == 0 )
					return choices[0];
				if ( cnt == 1 )
					return choices[1];
				return choices[2];
			}
			else
				return choices[0];
		}

		return str;
	}

	public String localeTrans( String key, Map<String, String> params ) throws LocalizationException, IOException, ParcelableException.Error
	{
		String str = localeTrans( key );

		for ( Map.Entry<String, String> param : params.entrySet() )
		{
			int inx = str.toLowerCase().indexOf( ":" + param.getKey().toLowerCase() );
			if ( inx == -1 )
				throw new LocalizationException( "Locale param is not found within language string. {key: " + param.getKey() + ", string: " + str + "}" );
			String tester = str.substring( inx, param.getKey().length() );
			String val = param.getValue();

			if ( Strs.isUppercase( tester ) )
				val = val.toUpperCase();
			if ( Strs.isCapitalizedWords( tester ) )
				val = Strs.capitalizeWords( val );

			str = str.substring( 0, inx ) + val + str.substring( inx + param.getKey().length() );
		}

		return str;
	}

	public String localeTrans( String key ) throws LocalizationException, IOException, ParcelableException.Error
	{
		Objs.notEmpty( key );
		key = Strs.trimAll( key, '.' );
		if ( !key.contains( "." ) )
			throw new LocalizationException( "Language key must contain a prefix file, e.g., general.yaml -> general.welcomeText. [" + key + "]" );
		if ( !key.matches( "^[a-zA-Z0-9._-]*$" ) )
			throw new LocalizationException( "Language key contains illegal characters. [" + key + "]" );

		String prefix = key.substring( 0, key.lastIndexOf( "." ) );

		if ( Objs.isEmpty( prefix ) )
			throw new LocalizationException( "Language prefix is empty." );

		key = key.substring( key.lastIndexOf( "." ) + 1 );

		if ( Objs.isEmpty( key ) )
			throw new LocalizationException( "Language key is empty." );

		Parcel lang;
		if ( cache.containsKey( prefix ) && cache.get( prefix ).getKey() > DateAndTime.epoch() - ( Kernel.isDevelopment() ? DateAndTime.SECOND_15 : DateAndTime.HOUR ) )
			lang = cache.get( prefix ).getValue();
		else
		{
			lang = getLang( prefix );
			cache.put( prefix, new Pair<>( DateAndTime.epoch(), lang ) );
		}

		return lang.getString( key ).orElse( null );
	}
}
