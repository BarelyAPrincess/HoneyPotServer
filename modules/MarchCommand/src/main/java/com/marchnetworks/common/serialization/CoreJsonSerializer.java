package com.marchnetworks.common.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class CoreJsonSerializer
{
	private static Gson gson;
	private static Gson gsonIndented;

	static
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapterFactory( new CustomTypeAdapterFactory() );
		gson = builder.create();

		builder.setPrettyPrinting();
		gsonIndented = builder.create();
	}

	public static String toJson( Object source )
	{
		return gson.toJson( source );
	}

	public static String toJsonIndented( Object source )
	{
		return gsonIndented.toJson( source );
	}

	public static <T> T fromJson( String json, Class<T> classOfT )
	{
		return gson.fromJson( json, classOfT );
	}

	public static <T> T collectionFromJson( String jsonArray, TypeToken<T> typeToken )
	{
		return gson.fromJson( jsonArray, typeToken.getType() );
	}
}
