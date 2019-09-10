package com.marchnetworks.server.communications.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Calendar;

public class ClientJsonSerializer
{
	private static Gson gson;

	static
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapterFactory( new ClientTypeAdapterFactory() );
		builder.registerTypeAdapter( Calendar.getInstance().getClass(), new CalendarTypeAdapter() );

		gson = builder.create();
	}

	public static String toJson( Object source )
	{
		return gson.toJson( source );
	}

	public static <T> T fromJson( String json, Class<T> classOfT )
	{
		T result = gson.fromJson( json, classOfT );
		return result;
	}

	public static <T> T collectionFromJson( String jsonArray, TypeToken<T> typeToken )
	{
		T result = gson.fromJson( jsonArray, typeToken.getType() );
		return result;
	}
}

