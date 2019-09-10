package com.marchnetworks.command.api.serialization;

import com.google.gson.reflect.TypeToken;

public interface JsonSerializer
{
	String toJson( Object paramObject );

	String toJsonIndented( Object paramObject );

	<T> T fromJson( String paramString, Class<T> paramClass );

	<T> T collectionFromJson( String paramString, TypeToken<T> paramTypeToken );

	void registerCustomTypes( String paramString, Class<?>... paramVarArgs );

	void unregisterCustomTypes( String paramString, Class<?>... paramVarArgs );
}
