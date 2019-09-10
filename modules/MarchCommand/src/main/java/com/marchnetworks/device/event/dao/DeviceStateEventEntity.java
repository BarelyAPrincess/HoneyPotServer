package com.marchnetworks.device.event.dao;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.management.instrumentation.events.GenericDeviceStateEvent;

import java.util.ArrayList;
import java.util.List;

import io.amelia.data.parcel.Parcel;
import io.amelia.data.parcel.ParcelSerializer;
import io.amelia.data.parcel.Parcelable;
import io.amelia.lang.ParcelableException;

// @Entity
// @Table( name = "DEVICE_EVENTS" )
@Parcelable( DeviceStateEventEntity.Serializer.class )
public class DeviceStateEventEntity
{
	// @Id
	// @Column( name = "DEVICE_ID" )
	private Long deviceId;
	// @Column( name = "EVENTS", length = 4000 )
	private String eventsString;

	public Long getDeviceId()
	{
		return deviceId;
	}

	public List<GenericDeviceStateEvent> getEvents()
	{
		if ( eventsString != null )
		{
			CoreJsonSerializer.collectionFromJson( eventsString, new TypeToken<ArrayList<GenericDeviceStateEvent>>()
			{
			} );
		}

		return new ArrayList<>();
	}

	public void setDeviceId( Long deviceId )
	{
		this.deviceId = deviceId;
	}

	public void setEvents( List<GenericDeviceStateEvent> eventData )
	{
		if ( ( eventData == null ) || ( eventData.isEmpty() ) )
		{
			eventsString = null;
		}
		eventsString = CoreJsonSerializer.toJson( eventData );
	}

	public static class Serializer implements ParcelSerializer<DeviceStateEventEntity>
	{
		@Override
		public DeviceStateEventEntity readFromParcel( Parcel src ) throws ParcelableException.Error
		{
			DeviceStateEventEntity deviceStateEventEntity = new DeviceStateEventEntity();
			deviceStateEventEntity.deviceId = src.getLongOrThrow( "id" );
			deviceStateEventEntity.eventsString = src.getStringOrThrow( "eventsString" );
			return deviceStateEventEntity;
		}

		@Override
		public void writeToParcel( DeviceStateEventEntity obj, Parcel dest ) throws ParcelableException.Error
		{
			dest.setValue( "id", obj.deviceId );
			dest.setValue( "eventsString", obj.eventsString );
		}
	}
}
