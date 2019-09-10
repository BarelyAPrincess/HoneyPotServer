package com.marchnetworks.common.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.marchnetworks.command.api.metrics.BucketCounter;
import com.marchnetworks.command.api.metrics.BucketMinMaxAvg;
import com.marchnetworks.command.api.metrics.BucketValue;
import com.marchnetworks.command.api.metrics.ConcurrentAction;
import com.marchnetworks.command.api.metrics.Counter;
import com.marchnetworks.command.api.metrics.CurrentMaxAvg;
import com.marchnetworks.command.api.metrics.MaxValue;
import com.marchnetworks.command.api.metrics.Metric;
import com.marchnetworks.command.api.metrics.MinMaxAvg;
import com.marchnetworks.command.api.metrics.RetryAction;
import com.marchnetworks.command.common.data.GenericBoolean;
import com.marchnetworks.command.common.data.GenericDecimalNumber;
import com.marchnetworks.command.common.data.GenericInt32Number;
import com.marchnetworks.command.common.data.GenericInt64Number;
import com.marchnetworks.command.common.data.GenericLongArray;
import com.marchnetworks.command.common.data.GenericString;
import com.marchnetworks.command.common.data.GenericStringArray;
import com.marchnetworks.command.common.data.GenericValue;
import com.marchnetworks.command.common.device.data.AudioEncoderView;
import com.marchnetworks.command.common.device.data.DataEncoderView;
import com.marchnetworks.command.common.device.data.EncoderView;
import com.marchnetworks.command.common.device.data.TextEncoderView;
import com.marchnetworks.command.common.device.data.VideoEncoderView;
import com.marchnetworks.command.common.extractor.data.Job;
import com.marchnetworks.command.common.extractor.data.RecorderJob;
import com.marchnetworks.command.common.extractor.data.datacollection.DataCollectionJob;
import com.marchnetworks.command.common.extractor.data.image.ImageExtractionJob;
import com.marchnetworks.command.common.extractor.data.image.ScheduledImageExtractionJob;
import com.marchnetworks.command.common.extractor.data.media.MediaExtractionJob;
import com.marchnetworks.command.common.extractor.data.transaction.TransactionExtractionJob;
import com.marchnetworks.command.common.schedule.data.Schedule;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CustomTypeAdapterFactory implements TypeAdapterFactory
{
	public static Set<Class<?>> classes = new HashSet<Class<?>>();

	static
	{
		registerType( EncoderView.class );
		registerType( VideoEncoderView.class );
		registerType( AudioEncoderView.class );
		registerType( TextEncoderView.class );
		registerType( DataEncoderView.class );
		registerType( Job.class );
		registerType( RecorderJob.class );
		registerType( MediaExtractionJob.class );
		registerType( TransactionExtractionJob.class );
		registerType( ScheduledImageExtractionJob.class );
		registerType( ImageExtractionJob.class );
		registerType( DataCollectionJob.class );
		registerType( GenericValue.class );
		registerType( GenericBoolean.class );
		registerType( GenericDecimalNumber.class );
		registerType( GenericInt32Number.class );
		registerType( GenericInt64Number.class );
		registerType( GenericLongArray.class );
		registerType( GenericString.class );
		registerType( GenericStringArray.class );
		registerType( Schedule.class );
		registerType( Metric.class );
		registerType( RetryAction.class );
		registerType( ConcurrentAction.class );
		registerType( Counter.class );
		registerType( BucketCounter.class );
		registerType( MaxValue.class );
		registerType( MinMaxAvg.class );
		registerType( CurrentMaxAvg.class );
		registerType( BucketMinMaxAvg.class );
		registerType( BucketValue.class );
	}

	public static void registerType( Class<?> clazz )
	{
		classes.add( clazz );
	}

	public static void unregisterType( Class<?> clazz )
	{
		classes.remove( clazz );
	}

	public <T> TypeAdapter<T> create( Gson gson, TypeToken<T> type )
	{
		boolean foundSublass = false;
		for ( Class<?> clazz : classes )
		{
			if ( clazz.getName().equals( type.getRawType().getName() ) )
			{
				foundSublass = true;
				break;
			}
		}

		if ( !foundSublass )
		{
			return null;
		}

		return new CustomTypeAdapter<T>( gson.getAdapter( JsonElement.class ), gson.getDelegateAdapter( this, type ), gson );
	}

	class CustomTypeAdapter<T> extends TypeAdapter<T>
	{
		private final TypeAdapter<T> delegate;
		private final Gson gson;
		private TypeAdapter<JsonElement> elementAdapter;

		CustomTypeAdapter( TypeAdapter<JsonElement> elementAdapter, TypeAdapter<T> delegate, Gson gson )
		{
			this.elementAdapter = elementAdapter;
			this.delegate = delegate;
			this.gson = gson;
		}

		public T read( JsonReader in ) throws IOException
		{
			JsonElement tree = ( JsonElement ) elementAdapter.read( in );
			JsonObject jsonObject = tree.getAsJsonObject();
			JsonPrimitive prim = ( JsonPrimitive ) jsonObject.get( "$type" );
			String className = prim.getAsString();

			Class<?> clazz = null;
			for ( Class<?> customType : CustomTypeAdapterFactory.classes )
			{
				if ( ( className.equals( customType.getName() ) ) || ( className.equals( customType.getSimpleName() ) ) )
				{
					clazz = customType;
					break;
				}
			}

			if ( clazz == null )
				throw new JsonParseException( "custom type " + className + " is not registered as a custom type." );

			TypeToken<T> token = ( TypeToken<T> ) TypeToken.get( clazz );
			TypeAdapter<T> adapter = gson.getDelegateAdapter( CustomTypeAdapterFactory.this, token );
			T out = adapter.fromJsonTree( tree );

			return out;
		}

		public void write( JsonWriter out, T value ) throws IOException
		{
			if ( value == null )
			{
				out.nullValue();
				return;
			}

			JsonObject custom = delegate.toJsonTree( value ).getAsJsonObject();

			JsonObject result = new JsonObject();
			result.add( "$type", new JsonPrimitive( value.getClass().getSimpleName() ) );
			for ( Map.Entry<String, JsonElement> entry : custom.entrySet() )
			{
				result.add( ( String ) entry.getKey(), ( JsonElement ) entry.getValue() );
			}

			elementAdapter.write( out, result );
		}
	}
}
