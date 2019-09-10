package com.marchnetworks.server.communications.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.apache.commons.lang.time.FastDateFormat;

import java.lang.reflect.Type;
import java.text.Format;
import java.util.Calendar;
import java.util.TimeZone;

public class CalendarTypeAdapter implements JsonSerializer<Calendar>
{
	private static Format DATE_FORMAT = FastDateFormat.getInstance( "yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone( "UTC" ) );

	public JsonElement serialize( Calendar src, Type typeOfSrc, JsonSerializationContext context )
	{
		String asISO = DATE_FORMAT.format( src.getTime() );
		return new JsonPrimitive( asISO );
	}
}

