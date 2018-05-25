/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.session.adapters;

import com.chiorichan.permission.PermissionDispatcher;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.amelia.foundation.Kernel;
import io.amelia.http.session.SessionAdapterImpl;
import io.amelia.http.session.SessionData;
import io.amelia.http.session.SessionWrapper;
import io.amelia.http.session.Sessions;
import io.amelia.lang.SessionException;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Timing;
import io.amelia.support.data.Parcel;
import io.amelia.support.data.ParcelLoader;

public class FileAdapter implements SessionAdapterImpl
{
	private static File sessionsDirectory = null;

	public static File getSessionsDirectory()
	{
		if ( sessionsDirectory == null )
			sessionsDirectory = Kernel.getPath( Sessions.PATH_SESSIONS );

		// IO.setDirectoryAccessWithException( sessionsDirectory );
		return sessionsDirectory;
	}

	@Override
	public SessionData createSession( String sessionId, SessionWrapper wrapper ) throws SessionException.Error
	{
		return new FileSessionData( sessionId, wrapper );
	}

	@Override
	public List<SessionData> getSessions() throws SessionException.Error
	{
		List<SessionData> data = Lists.newArrayList();

		Timing.start( this );

		File[] files = getSessionsDirectory().listFiles( file -> file.isFile() && file.getName().endsWith( ".json" ) );

		if ( files == null )
			return data;

		for ( File f : files )
			try
			{
				data.add( new FileSessionData( f ) );
			}
			catch ( SessionException.Error e )
			{
				e.printStackTrace();
			}

			Sessions.L.info( "FileSession loaded " + data.size() + " sessions from the datastore in " + Timing.finish( this ) + "ms!" );

		return data;
	}

	class FileSessionData extends SessionData
	{
		File file;

		FileSessionData( File file ) throws SessionException.Error
		{
			super( FileAdapter.this, true );
			this.file = file;

			readSession();
		}

		FileSessionData( String sessionId, SessionWrapper wrapper ) throws SessionException.Error
		{
			super( FileAdapter.this, false );
			this.sessionId = sessionId;

			ipAddress = wrapper.getIpAddress();
			site = wrapper.getLocation() == null ? null : wrapper.getLocation().getId();

			save();
		}

		@Override
		protected void destroy() throws SessionException.Error
		{
			file.delete();
		}

		private void readSession() throws SessionException.Error
		{
			if ( file == null || !file.exists() )
				return;

			Parcel parcel = ParcelLoader.decodeJson( file );

			timeout = parcel.getLong( "timeout" ).filter( value -> value > timeout ).orElse( timeout );

			ipAddress = parcel.getString( "ipAddress" ).orElse( null );

			sessionName = parcel.getString( "sessionName" ).filter( Objs::isNotEmpty ).orElse( sessionName );

			sessionId = parcel.getString( "sessionId" ).orElse( sessionId );

			site = parcel.getString( "site" ).orElse( null );

			data = parcel.hasChild( "data" ) ? parcel.getChild( "data" ) : new Parcel();

			/*if ( !parcel.getString( "data", "" ).isEmpty() )
				data = new Gson().fromJson( parcel.getString( "data" ), new TypeToken<Map<String, String>>()
				{
					private static final long serialVersionUID = -1734352198651744570L;
				}.getType() );*/
		}

		@Override
		protected void reload() throws SessionException.Error
		{
			readSession();
		}

		@Override
		protected void save() throws SessionException.Error
		{
			// String dataJson = new Gson().toJson( data );

			if ( file == null || !file.exists() )
				file = new File( getSessionsDirectory(), sessionId + ".json" );

			Parcel parcel = new Parcel();

			parcel.setValue( "sessionName", sessionName );
			parcel.setValue( "sessionId", sessionId );
			parcel.setValue( "timeout", timeout );
			parcel.setValue( "ipAddress", ipAddress );
			parcel.setValue( "site", site );
			parcel.setValue( "data", data );

			try
			{
				IO.writeStringToFile( ParcelLoader.encodeJson( parcel ), file );
			}
			catch ( IOException e )
			{
				throw new SessionException.Error( "There was an exception thrown while trying to save the session.", e );
			}
		}
	}
}
