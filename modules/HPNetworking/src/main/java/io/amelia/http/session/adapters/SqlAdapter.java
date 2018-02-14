/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.session.adapters;

import com.chiorichan.permission.PermissionDispatcher;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import io.amelia.http.session.SessionAdapterImpl;
import io.amelia.http.session.SessionData;
import io.amelia.http.session.SessionWrapper;
import io.amelia.http.session.Sessions;
import io.amelia.lang.DatabaseException;
import io.amelia.lang.SessionException;
import io.amelia.storage.Database;
import io.amelia.storage.StorageModule;
import io.amelia.storage.elegant.ElegantQuerySelect;
import io.amelia.support.DateAndTime;
import io.amelia.support.Timing;

public class SqlAdapter implements SessionAdapterImpl
{
	@Override
	public SessionData createSession( String sessionId, SessionWrapper wrapper ) throws SessionException.Error
	{
		return new SqlSessionData( sessionId, wrapper );
	}

	@Override
	List<SessionData> getSessions() throws SessionException.Error
	{
		List<SessionData> data = Lists.newArrayList();
		Database sql = StorageModule.i().getDatabase();

		if ( sql == null )
			throw new SessionException.Error( "Sessions can't be stored in a SQL Database without a properly configured server database." );

		Timing.start( this );

		try
		{
			// Attempt to delete all expired sessions before we try and load them.
			int expired = sql.table( "sessions" ).delete().where( "timeout" ).moreThan( 0 ).where( "timeout" ).lessThan( DateAndTime.epoch() ).executeWithException().count();
			PermissionDispatcher.getLogger().info( String.format( "SqlSession removed %s expired sessions from the datastore!", expired ) );

			ElegantQuerySelect select = sql.table( "sessions" ).select().execute();

			if ( select.count() > 0 )
			{
				do
					try
					{
						data.add( new SqlSessionData( select ) );
					}
					catch ( SessionException e )
					{
						e.printStackTrace();
					}
				while ( select.next() );
			}
		}
		catch ( DatabaseException e )
		{
			Sessions.getLogger().warning( "There was a problem reloading saved sessions.", e );
		}

		PermissionDispatcher.getLogger().info( "SqlSession loaded " + data.size() + " sessions from the datastore in " + Timing.finish( this ) + "ms!" );

		return data;
	}

	class SqlSessionData extends SessionData
	{
		SqlSessionData( ElegantQuerySelect querySelect ) throws SessionException.Error
		{
			super( SqlAdapter.this, true );
			readSession( querySelect );
		}

		SqlSessionData( String sessionId, SessionWrapper wrapper ) throws SessionException.Error
		{
			super( SqlAdapter.this, false );
			this.sessionId = sessionId;

			ipAddress = wrapper.getIpAddress();
			site = wrapper.getLocation().getId();

			save();
		}

		@Override
		void destroy() throws SessionException.Error
		{
			try
			{
				if ( StorageModule.i().getDatabase().table( "sessions" ).delete().where( "sessionId" ).matches( sessionId ).executeWithException().count() < 1 )
					Sessions.getLogger().severe( "Failed to remove the session '" + sessionId + "' from the database, no results." );
			}
			catch ( DatabaseException e )
			{
				throw new SessionException.Error( "There was an exception thrown while trying to destroy the session.", e );
			}
		}

		private void readSession( ElegantQuerySelect rs )
		{
			timeout = rs.getInt( "timeout" );
			ipAddress = rs.getString( "ipAddress" );

			if ( rs.getString( "sessionName" ) != null && !rs.getString( "sessionName" ).isEmpty() )
				sessionName = rs.getString( "sessionName" );
			sessionId = rs.getString( "sessionId" );

			site = rs.getString( "sessionSite" );

			if ( !rs.getString( "data" ).isEmpty() )
				data = new Gson().fromJson( rs.getString( "data" ), new TypeToken<Map<String, String>>()
				{
					private static final long serialVersionUID = -1734352198651744570L;
				}.getType() );
		}

		@Override
		void reload() throws SessionException.Error
		{
			try
			{
				ElegantQuerySelect select = StorageModule.i().getDatabase().table( "sessions" ).select().where( "sessionId" ).matches( sessionId ).executeWithException();
				// rs = Loader.getDatabase().query( "SELECT * FROM `sessions` WHERE `sessionId` = '" + sessionId + "'" );
				if ( select.count() < 1 )
					return;
				readSession( select );
			}
			catch ( DatabaseException e )
			{
				throw new SessionException( e );
			}
		}

		@Override
		void save() throws SessionException.Error
		{
			try
			{
				String dataJson = new Gson().toJson( data );
				Database db = StorageModule.i().getDatabase();

				if ( db == null )
					throw new SessionException( "Sessions can't be stored in a SQL Database without a properly configured server database." );

				ElegantQuerySelect select = db.table( "sessions" ).select().where( "sessionId" ).matches( sessionId ).executeWithException();
				// query( "SELECT * FROM `sessions` WHERE `sessionId` = '" + sessionId + "';" );

				if ( select.count() < 1 )
					db.table( "sessions" ).insert().value( "sessionId", sessionId ).value( "timeout", timeout ).value( "ipAddress", ipAddress ).value( "sessionName", sessionName ).value( "sessionSite", site ).value( "data", dataJson ).executeWithException();
					// sql.queryUpdate( "INSERT INTO `sessions` (`sessionId`, `timeout`, `ipAddress`, `sessionName`, `sessionSite`, `data`) VALUES ('" + sessionId + "', '" + timeout + "', '" + ipAddress + "', '" + sessionName + "', '" + site + "', '"
					// + dataJson + "');" );
				else
					db.table( "sessions" ).update().value( "timeout", timeout ).value( "ipAddress", ipAddress ).value( "sessionName", sessionName ).value( "sessionSite", site ).value( "data", dataJson ).where( "sessionId" ).matches( sessionId ).executeWithException();
				// sql.queryUpdate( "UPDATE `sessions` SET `data` = '" + dataJson + "', `timeout` = '" + timeout + "', `sessionName` = '" + sessionName + "', `ipAddress` = '" + ipAddress + "', `sessionSite` = '" + site + "' WHERE `sessionId` = '"
				// + sessionId + "';" );
			}
			catch ( DatabaseException e )
			{
				throw new SessionException.Error( "There was an exception thrown while trying to save the session.", e );
			}
		}
	}
}
