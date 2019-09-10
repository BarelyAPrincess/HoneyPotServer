/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.session.adapters;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.amelia.data.ContainerBase;
import io.amelia.data.parcel.Parcel;
import io.amelia.data.parcel.ParcelLoader;
import io.amelia.foundation.Kernel;
import io.amelia.http.session.SessionAdapterImpl;
import io.amelia.http.session.SessionData;
import io.amelia.http.session.SessionRegistry;
import io.amelia.http.session.SessionWrapper;
import io.amelia.lang.ParcelableException;
import io.amelia.lang.SessionException;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Streams;
import io.amelia.support.Timing;

public class FileAdapter implements SessionAdapterImpl
{
	private static Path sessionsDirectory = null;

	public static Path getSessionsDirectory()
	{
		if ( sessionsDirectory == null )
			sessionsDirectory = Kernel.getPath( SessionRegistry.PATH_SESSIONS );

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

		try
		{
			Streams.forEachWithException( Files.list( getSessionsDirectory() ).filter( file -> Files.isRegularFile( file ) && file.getFileName().endsWith( ".json" ) ), file -> data.add( new FileSessionData( file ) ) );
		}
		catch ( IOException e )
		{
			// Ignorable?
			e.printStackTrace();
		}

		SessionRegistry.L.info( "FileSession loaded " + data.size() + " sessions from the backend in " + Timing.finish( this ) + "ms!" );

		return data;
	}

	class FileSessionData extends SessionData
	{
		Path file;

		FileSessionData( Path file ) throws SessionException.Error
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
			webroot = wrapper.getWebroot() == null ? null : wrapper.getWebroot().getWebrootId();

			save();
		}

		@Override
		protected void destroy() throws SessionException.Error
		{
			try
			{
				Files.delete( file );
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}
		}

		private void readSession() throws SessionException.Error
		{
			if ( file == null || Files.notExists( file ) )
				return;

			try
			{
				Parcel parcel = ParcelLoader.decodeJson( file );

				timeout = parcel.getLong( "timeout" ).filter( value -> value > timeout ).orElse( timeout );

				ipAddress = parcel.getString( "ipAddress" ).orElse( null );

				sessionName = parcel.getString( "sessionName" ).filter( Objs::isNotEmpty ).orElse( sessionName );

				sessionId = parcel.getString( "sessionId" ).orElse( sessionId );

				webroot = parcel.getString( "webroot" ).orElse( null );

				data = parcel.getChildOrCreate( "data" );

				/*if ( !parcel.getString( "data", "" ).isEmpty() )
					data = new Gson().fromJson( parcel.getString( "data" ), new TypeToken<Map<String, String>>()
					{
						private static final long serialVersionUID = -1734352198651744570L;
					}.getType() );*/
			}
			catch ( IOException | ParcelableException.Error e )
			{
				throw new SessionException.Error( "There was an exception thrown while trying to read the session.", e );
			}
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

			if ( file == null || Files.notExists( file ) )
				file = getSessionsDirectory().resolve( sessionId + ".json" );

			try
			{
				Parcel parcel = Parcel.empty();

				parcel.setValue( "sessionName", sessionName );
				parcel.setValue( "sessionId", sessionId );
				parcel.setValue( "timeout", timeout );
				parcel.setValue( "ipAddress", ipAddress );
				parcel.setValue( "webroot", webroot );
				parcel.addChild( "data", data, ContainerBase.ConflictStrategy.OVERWRITE );

				IO.writeStringToPath( ParcelLoader.encodeJson( parcel ), file );
			}
			catch ( IOException | ParcelableException.Error e )
			{
				throw new SessionException.Error( "There was an exception thrown while trying to save the session.", e );
			}
		}
	}
}
