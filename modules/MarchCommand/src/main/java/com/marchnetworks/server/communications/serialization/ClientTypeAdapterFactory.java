package com.marchnetworks.server.communications.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.marchnetworks.command.common.extractor.data.Job;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.health.data.AlertData;

import java.io.IOException;
import java.util.Map;

public class ClientTypeAdapterFactory implements TypeAdapterFactory
{
	public static Class<?>[] clientRestSubclasses = {Resource.class, AlertData.class, Job.class};

	public <T> TypeAdapter<T> create( Gson gson, TypeToken<T> type )
	{
		boolean foundSublass = false;
		for ( Class<?> clazz : clientRestSubclasses )
		{
			if ( clazz.isAssignableFrom( type.getRawType() ) )
			{
				foundSublass = true;
				break;
			}
		}

		final boolean isEnum = type.getRawType().isEnum();
		final boolean isSubclass = foundSublass;

		if ( ( !isSubclass ) && ( !isEnum ) )
		{
			return null;
		}

		final TypeAdapter<T> delegate = gson.getDelegateAdapter( this, type );
		final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter( JsonElement.class );

		return new TypeAdapter<T>()
		{
			public void write( JsonWriter out, T value ) throws IOException
			{
				if ( value == null )
				{
					out.nullValue();
					return;
				}

				if ( isSubclass )
				{
					JsonObject custom = delegate.toJsonTree( value ).getAsJsonObject();

					JsonObject result = new JsonObject();
					result.add( "$type", new JsonPrimitive( value.getClass().getSimpleName() ) );
					for ( Map.Entry<String, JsonElement> entry : custom.entrySet() )
					{
						result.add( ( String ) entry.getKey(), ( JsonElement ) entry.getValue() );
					}
					elementAdapter.write( out, result );

				}
				else if ( isEnum )
				{
					Enum<?> enumValue = ( Enum ) value;
					out.value( enumValue.ordinal() );
				}
			}

			public T read( JsonReader in ) throws IOException
			{
				JsonElement tree = ( JsonElement ) elementAdapter.read( in );
				return ( T ) delegate.fromJsonTree( tree );
			}
		};
	}
}

