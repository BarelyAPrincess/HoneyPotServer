package com.marchnetworks.server.communications.serialization;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.reflect.TypeToken;
import com.marchnetworks.app.data.AppStatus;
import com.marchnetworks.app.events.AppEvent;
import com.marchnetworks.app.events.AppEventType;
import com.marchnetworks.app.events.AppStateEvent;
import com.marchnetworks.command.api.serialization.JsonSerializer;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.serialization.CustomTypeAdapterFactory;
import com.marchnetworks.server.event.EventListener;

import java.util.Collection;

public class JsonSerializerServiceImpl implements JsonSerializer, EventListener
{
	private Multimap<String, Class<?>> appsTypesMap = ArrayListMultimap.create();

	public String toJson( Object source )
	{
		return CoreJsonSerializer.toJson( source );
	}

	public String toJsonIndented( Object source )
	{
		return CoreJsonSerializer.toJsonIndented( source );
	}

	public <T> T fromJson( String json, Class<T> classOfT )
	{
		return ( T ) CoreJsonSerializer.fromJson( json, classOfT );
	}

	public <T> T collectionFromJson( String jsonArray, TypeToken<T> typeToken )
	{
		return ( T ) CoreJsonSerializer.collectionFromJson( jsonArray, typeToken );
	}

	public void registerCustomTypes( String appId, Class<?>... classNames )
	{
		for ( Class<?> clazz : classNames )
		{
			CustomTypeAdapterFactory.registerType( clazz );
			appsTypesMap.put( appId, clazz );
		}
	}

	public void unregisterCustomTypes( String appId, Class<?>... classNames )
	{
		for ( Class<?> clazz : classNames )
		{
			CustomTypeAdapterFactory.unregisterType( clazz );
			appsTypesMap.remove( appId, clazz );
		}
	}

	public void process( Event aEvent )
	{
		if ( ( aEvent instanceof AppStateEvent ) )
		{
			AppStateEvent appStateEvent = ( AppStateEvent ) aEvent;
			if ( !appStateEvent.getStatus().equals( AppStatus.RUNNING ) )
			{
				removeAppTypes( appStateEvent.getAppID() );
			}
		}
		else if ( ( aEvent instanceof AppEvent ) )
		{
			AppEvent appEvent = ( AppEvent ) aEvent;
			if ( appEvent.getAppEventType() == AppEventType.UNINSTALLED )
			{
				removeAppTypes( appEvent.getAppID() );
			}
		}
	}

	public String getListenerName()
	{
		return JsonSerializerServiceImpl.class.getSimpleName();
	}

	private void removeAppTypes( String appId )
	{
		Collection<Class<?>> clazzes = appsTypesMap.removeAll( appId );
		for ( Class<?> clazz : clazzes )
		{
			CustomTypeAdapterFactory.unregisterType( clazz );
		}
	}
}

