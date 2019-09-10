/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.webroot;

public class WebrootStorage // extends HomeStorageMethod
{
	public WebrootStorage()
	{
		// super( "webroot", "(.*)(?:\\\\|\\/)config.yaml", WebrootEntry::new );
	}

	public static class WebrootEntry
	{
		// private final Parcel data;
		// private final String path;

		/* public WebrootEntry( StorageDriver.Entry entry ) throws StorageException.Error
		{
			try
			{
				path = entry.getPath();
				data = entry.readYamlToParcel();

				if ( !data.hasValue( "siteId" ) )
					throw new StorageException.Error( "The webroot config \"" + path + "\" is missing the siteId key. This is required!" );

				String id = data.getString( "siteId" ).get().toLowerCase();
				String dir = Strs.regexCapture( path, "\\/([^\\/]*)\\/config.yaml" );

				if ( !Strs.isCamelCase( id ) )
					Storage.L.warning( String.format( "The webroot with id %s does not match our camelCase convention. It must start with a lowercase letter or number and each following word should start with an uppercase letter.", id ) );

				if ( !id.equals( dir ) )
				{
					Storage.L.warning( String.format( "We found a webroot data file at '%s' but the containing directory did not match the webroot id of '%s', we will now correct this by moving the config to the correct directory.", configFile.getAbsolutePath(), id ) );
					entry.moveTo( id );
				}
			}
			catch ( Exception e )
			{
				throw new StorageException.Error( e );
			}
		} */
	}
}
