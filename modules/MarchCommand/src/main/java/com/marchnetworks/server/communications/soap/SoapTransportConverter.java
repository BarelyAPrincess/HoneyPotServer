package com.marchnetworks.server.communications.soap;

import com.marchnetworks.command.common.transport.data.AddressZone;
import com.marchnetworks.command.common.transport.data.ConfigurationURL;
import com.marchnetworks.command.common.transport.data.Event;
import com.marchnetworks.command.common.transport.data.EventType;
import com.marchnetworks.command.common.transport.data.LocalZone;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.command.common.transport.data.TimeZoneInfo;
import com.marchnetworks.command.common.transport.data.Timestamp;
import com.marchnetworks.common.utils.XmlUtils;
import com.marchnetworks.device_ws.GenericBoolean;
import com.marchnetworks.device_ws.GenericDouble;
import com.marchnetworks.device_ws.GenericInt32;
import com.marchnetworks.device_ws.GenericInt64;
import com.marchnetworks.device_ws.GenericNull;
import com.marchnetworks.device_ws.GenericString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

public class SoapTransportConverter
{
	private static final HashSet<Class<?>> PRIMITIVE_TYPES = getPrimitiveTypes();
	private static final HashSet<String> COMMON_TYPES = getCommonTypes();

	private static final String SOAP_ARRAY_PREFIX = "ArrayOf";
	private static final String SOAP_PACKAGE = "com.marchnetworks.device_ws";
	private static final String MODEL_PACKAGE = "com.marchnetworks.server.communications.transport.datamodel";
	private static final String COMMON_MODEL_PACKAGE = "com.marchnetworks.command.common.transport.data";
	private static final Logger LOG = LoggerFactory.getLogger( SoapTransportConverter.class );

	private static HashSet<Class<?>> getPrimitiveTypes()
	{
		HashSet<Class<?>> ret = new HashSet<Class<?>>();
		ret.add( Boolean.class );
		ret.add( Character.class );
		ret.add( Byte.class );
		ret.add( Short.class );
		ret.add( Integer.class );
		ret.add( Long.class );
		ret.add( Float.class );
		ret.add( Double.class );
		ret.add( Void.class );
		ret.add( String.class );
		return ret;
	}

	private static boolean isPrimitiveType( Class<?> clazz )
	{
		return PRIMITIVE_TYPES.contains( clazz );
	}

	private static boolean isCommonType( String modelClassName )
	{
		return COMMON_TYPES.contains( modelClassName );
	}

	private static HashSet<String> getCommonTypes()
	{
		HashSet<String> ret = new HashSet();
		ret.add( AddressZone.class.getSimpleName() );
		ret.add( ConfigurationURL.class.getSimpleName() );
		ret.add( LocalZone.class.getSimpleName() );
		ret.add( Pair.class.getSimpleName() );
		ret.add( TimeZoneInfo.class.getSimpleName() );
		ret.add( Event.class.getSimpleName() );
		ret.add( EventType.class.getSimpleName() );
		ret.add( Timestamp.class.getSimpleName() );
		return ret;
	}

	public static Class<?> getModelClass( String modelClassName ) throws ClassNotFoundException
	{
		String path = null;
		if ( isCommonType( modelClassName ) )
		{
			path = "com.marchnetworks.command.common.transport.data." + modelClassName;
		}
		else
		{
			path = "com.marchnetworks.server.communications.transport.datamodel." + modelClassName;
		}
		return Class.forName( path );
	}

	public static Object convertToModel( Object obj )
	{
		if ( obj == null )
		{
			return null;
		}
		Class<?> soapClass = obj.getClass();

		if ( isPrimitiveType( soapClass ) )
		{
			return obj;
		}
		String soapClassName = soapClass.getSimpleName();

		if ( ( obj instanceof com.marchnetworks.device_ws.GenericValue ) )
		{
			return convertToModelGenericValue( ( com.marchnetworks.device_ws.GenericValue ) obj );
		}
		if ( ( obj instanceof XMLGregorianCalendar ) )
		{
			return Long.valueOf( convertXmlGregorianCalendarToModel( ( XMLGregorianCalendar ) obj ) );
		}

		if ( ( obj instanceof List ) )
		{
			List<?> soapList = ( List ) obj;
			List<Object> resultList = new ArrayList( soapList.size() );
			for ( int i = 0; i < soapList.size(); i++ )
			{
				Object soapElement = convertToModel( soapList.get( i ) );
				resultList.add( soapElement );
			}
			return resultList;
		}

		if ( soapClassName.startsWith( "ArrayOf" ) )
		{
			String modelClassName = soapClassName.substring( "ArrayOf".length() );

			try
			{
				Method[] methods = soapClass.getMethods();
				Method arrayGetMethod = methods[0];

				ParameterizedType listType = ( ParameterizedType ) arrayGetMethod.getGenericReturnType();
				Class<?> listClass = ( Class ) listType.getActualTypeArguments()[0];

				Class<?> modelArrayClass = null;

				if ( isPrimitiveType( listClass ) )
				{
					modelArrayClass = listClass;
				}
				else
				{
					modelArrayClass = getModelClass( modelClassName );
				}

				List soapList = ( List ) arrayGetMethod.invoke( obj, ( Object[] ) null );

				Object resultList = Array.newInstance( modelArrayClass, soapList.size() );

				for ( int i = 0; i < soapList.size(); i++ )
				{
					Object arrayItem = convertToModel( soapList.get( i ) );
					Array.set( resultList, i, arrayItem );
				}
				return resultList;
			}
			catch ( Exception e )
			{
				LOG.info( "Error converting from SOAP array", e );
				return null;
			}
		}

		try
		{
			Class<?> modelClass = getModelClass( soapClassName );

			if ( modelClass.isEnum() )
			{
				Method valMethod = soapClass.getMethod( "value", new Class[0] );
				String value = ( String ) valMethod.invoke( obj, ( Object[] ) null );

				Object[] consts = modelClass.getEnumConstants();
				Class<?> sub = consts[0].getClass();
				Method subMethod = sub.getDeclaredMethod( "fromValue", new Class[] {String.class} );
				return subMethod.invoke( consts[0], new Object[] {value} );
			}

			Object modelObject = modelClass.newInstance();

			Field[] fields = modelClass.getDeclaredFields();
			for ( Field modelField : fields )
			{
				try
				{
					Field soapField = soapClass.getDeclaredField( modelField.getName() );
					soapField.setAccessible( true );
					Object soapValue = soapField.get( obj );

					Object modelValue = convertToModel( soapValue );
					modelField.setAccessible( true );
					modelField.set( modelObject, modelValue );
				}
				catch ( NoSuchFieldException e )
				{
					LOG.debug( "Field not found in SOAP class " + modelField.getName() );
				}
				catch ( SecurityException e )
				{
					LOG.warn( "Unexpected error converting to SOAP", e );
					return null;
				}
			}
			return modelObject;
		}
		catch ( ClassNotFoundException e )
		{
			return null;
		}
		catch ( Exception e )
		{
			LOG.info( "Error converting from SOAP class", e );
		}
		return null;
	}

	public static Object convertToSOAP( Object obj )
	{
		if ( obj == null )
		{
			return null;
		}
		Class<?> modelClass = obj.getClass();

		if ( isPrimitiveType( modelClass ) )
		{
			return obj;
		}
		String modelClassName = modelClass.getSimpleName();

		Class<?> arrayType = modelClass.getComponentType();

		if ( ( obj instanceof com.marchnetworks.command.common.transport.data.GenericValue ) )
		{
			return convertToSoapGenericValue( ( com.marchnetworks.command.common.transport.data.GenericValue ) obj );
		}

		if ( ( obj instanceof DateTimeWrapper ) )
		{
			return convertToSoapXmlGregorianCalendar( ( DateTimeWrapper ) obj );
		}

		if ( arrayType != null )
		{
			String soapArrayClassName = "ArrayOf" + arrayType.getSimpleName();
			try
			{
				Class<?> soapArrayClass = Class.forName( "com.marchnetworks.device_ws." + soapArrayClassName );

				Object soapObject = soapArrayClass.newInstance();

				Method[] methods = soapArrayClass.getMethods();
				Method arrayGetMethod = methods[0];

				List soapList = ( List ) arrayGetMethod.invoke( soapObject, ( Object[] ) null );
				Object[] modelArray = ( Object[] ) obj;

				for ( int i = 0; i < modelArray.length; i++ )
				{
					Object arrayItem = convertToSOAP( modelArray[i] );
					soapList.add( arrayItem );
				}

				return soapObject;
			}
			catch ( Exception e )
			{
				LOG.info( "Error converting to SOAP array", e );
				return null;
			}
		}

		try
		{
			Class<?> soapClass = Class.forName( "com.marchnetworks.device_ws." + modelClassName );

			if ( soapClass.isEnum() )
			{

				Method valMethod = modelClass.getMethod( "value", new Class[0] );
				String value = ( String ) valMethod.invoke( obj, ( Object[] ) null );

				Object[] consts = soapClass.getEnumConstants();
				Class<?> enumClass = consts[0].getClass();
				Method enumCreateMethod = enumClass.getDeclaredMethod( "fromValue", new Class[] {String.class} );
				return enumCreateMethod.invoke( consts[0], new Object[] {value} );
			}

			Object soapObject = soapClass.newInstance();

			Field[] fields = soapClass.getDeclaredFields();
			for ( Field soapField : fields )
			{
				try
				{
					Field modelField = modelClass.getDeclaredField( soapField.getName() );
					modelField.setAccessible( true );
					Object modelValue = modelField.get( obj );

					if ( isDateTimeType( soapField.getType() ) )
					{
						modelValue = DateTimeWrapper.getInstance( modelValue );
					}

					Object soapValue = convertToSOAP( modelValue );
					soapField.setAccessible( true );
					soapField.set( soapObject, soapValue );
				}
				catch ( NoSuchFieldException e )
				{
					LOG.info( "Field not found in model class " + soapField.getName() );
				}
				catch ( SecurityException e )
				{
					LOG.warn( "Unexpected error converting to SOAP", e );
					return null;
				}
			}
			return soapObject;
		}
		catch ( ClassNotFoundException e )
		{
			return null;
		}
		catch ( Exception e )
		{
			LOG.info( "Error converting to SOAP class", e );
		}
		return null;
	}

	public static com.marchnetworks.command.common.transport.data.GenericValue convertToModelGenericValue( com.marchnetworks.device_ws.GenericValue soapValue )
	{
		com.marchnetworks.command.common.transport.data.GenericValue modelValue = new com.marchnetworks.command.common.transport.data.GenericValue();

		if ( ( soapValue instanceof GenericString ) )
		{
			modelValue.setValue( ( ( GenericString ) soapValue ).getValue() );
		}
		else if ( ( soapValue instanceof GenericBoolean ) )
		{
			modelValue.setValue( ( ( GenericBoolean ) soapValue ).isValue() );
		}
		else if ( ( soapValue instanceof GenericInt32 ) )
		{
			modelValue.setValue( ( ( GenericInt32 ) soapValue ).getValue() );
		}
		else if ( ( soapValue instanceof GenericInt64 ) )
		{
			modelValue.setValue( ( ( GenericInt64 ) soapValue ).getValue() );
		}
		else if ( ( soapValue instanceof GenericDouble ) )
		{
			modelValue.setValue( ( ( GenericDouble ) soapValue ).getValue() );
		}

		return modelValue;
	}

	public static com.marchnetworks.device_ws.GenericValue convertToSoapGenericValue( com.marchnetworks.command.common.transport.data.GenericValue modelValue )
	{
		short type = modelValue.getType();

		if ( type == 3 )
		{
			GenericBoolean genericBoolean = new GenericBoolean();
			genericBoolean.setValue( modelValue.getBooleanValue() );
			return genericBoolean;
		}
		if ( type == 1 )
		{
			GenericDouble genericDouble = new GenericDouble();
			genericDouble.setValue( modelValue.getDoubleValue() );
			return genericDouble;
		}
		if ( type == 0 )
		{
			GenericInt32 genericInt = new GenericInt32();
			genericInt.setValue( modelValue.getIntValue() );
			return genericInt;
		}
		if ( type == 2 )
		{
			GenericInt64 genericLong = new GenericInt64();
			genericLong.setValue( modelValue.getLongValue() );
			return genericLong;
		}
		if ( type == 4 )
		{
			GenericString genericString = new GenericString();
			genericString.setValue( modelValue.getStringValue() );
			return genericString;
		}

		return new GenericNull();
	}

	private static long convertXmlGregorianCalendarToModel( XMLGregorianCalendar timestampCalendar )
	{
		return timestampCalendar.toGregorianCalendar().getTimeInMillis();
	}

	private static XMLGregorianCalendar convertToSoapXmlGregorianCalendar( DateTimeWrapper wrapper )
	{
		if ( ( wrapper.getTimestamp() == null ) || ( !( wrapper.getTimestamp() instanceof Long ) ) )
		{
			return null;
		}

		return XmlUtils.getXmlGregorianCalendarFromTime( ( ( Long ) wrapper.getTimestamp() ).longValue() );
	}

	private static boolean isDateTimeType( Class<?> clazz )
	{
		return XMLGregorianCalendar.class.isAssignableFrom( clazz );
	}

	private static class DateTimeWrapper
	{
		private Object timestamp;

		static DateTimeWrapper getInstance( Object timestamp )
		{
			DateTimeWrapper wrapper = new DateTimeWrapper();
			wrapper.setTimeInMicros( timestamp );
			return wrapper;
		}

		void setTimeInMicros( Object timestamp )
		{
			this.timestamp = timestamp;
		}

		Object getTimestamp()
		{
			return timestamp;
		}
	}
}

