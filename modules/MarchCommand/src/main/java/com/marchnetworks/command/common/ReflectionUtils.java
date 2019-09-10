package com.marchnetworks.command.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReflectionUtils
{
	public static Object getSpecifiedFieldValue( String property, Object obj )
	{
		Object result = null;
		try
		{
			Class<?> objectClass = obj.getClass();
			Field objectField = getDeclaredField( property, objectClass );
			if ( objectField != null )
			{
				objectField.setAccessible( true );
				result = objectField.get( obj );
			}
		}
		catch ( Exception localException )
		{
		}
		return result;
	}

	public static Object getSpecifiedFieldValue( Field field, Object obj )
	{
		Object result = null;
		try
		{
			field.setAccessible( true );
			result = field.get( obj );
		}
		catch ( Exception localException )
		{
		}

		return result;
	}

	public static void setSpecifiedFieldValue( String property, Object obj, Object value )
	{
		try
		{
			Class<?> objectClass = obj.getClass();
			Field objectField = getDeclaredField( property, objectClass );
			if ( objectField != null )
			{
				objectField.setAccessible( true );
				objectField.set( obj, value );
			}
		}
		catch ( Exception localException )
		{
		}
	}

	public static void setSpecifiedFieldValue( Field field, Object obj, Object value )
	{
		try
		{
			field.setAccessible( true );
			field.set( obj, value );
		}
		catch ( Exception localException )
		{
		}
	}

	public static Field getDeclaredField( String fieldName, Class<?> type )
	{
		Field result = null;
		try
		{
			result = type.getDeclaredField( fieldName );
		}
		catch ( Exception localException )
		{
		}

		if ( result == null )
		{
			Class<?> superclass = type.getSuperclass();
			if ( ( superclass != null ) && ( !superclass.getName().equals( "java.lang.Object" ) ) )
			{
				return getDeclaredField( fieldName, type.getSuperclass() );
			}
		}
		return result;
	}

	public static List<Field> getAllDeclaredFields( Class<?> type )
	{
		List<Field> result = new ArrayList();
		try
		{
			CollectionUtils.addAll( result, type.getDeclaredFields() );
		}
		catch ( Exception localException )
		{
		}

		Class<?> superclass = type.getSuperclass();
		if ( ( superclass != null ) && ( !superclass.getName().equals( "java.lang.Object" ) ) )
		{
			result.addAll( getAllDeclaredFields( type.getSuperclass() ) );
		}

		return result;
	}

	public static Map<String, Field> getAllDeclaredFieldsMap( Class<?> type )
	{
		Map<String, Field> result = new HashMap();
		List<Field> fields = getAllDeclaredFields( type );

		for ( Field field : fields )
		{
			result.put( field.getName(), field );
		}

		return result;
	}

	public static Map<String, Class<?>> getAllFieldTypes( Class<?> type )
	{
		Map<String, Class<?>> types = new HashMap();
		List<Field> fields = getAllDeclaredFields( type );
		try
		{
			for ( Field field : fields )
			{
				types.put( field.getName(), field.getType() );
			}
		}
		catch ( Exception localException )
		{
		}
		return types;
	}

	public static Object getAnnotationValue( Class<?> type, Class<? extends Annotation> annotationType, String annotationProperty )
	{
		try
		{
			Annotation annotation = type.getAnnotation( annotationType );

			if ( annotation != null )
			{
				Method annotationMethod = annotationType.getDeclaredMethod( annotationProperty, new Class[0] );
				if ( annotationMethod != null )
				{
					return annotationMethod.invoke( annotation, ( Object[] ) null );
				}
			}
		}
		catch ( Exception localException )
		{
		}

		return null;
	}

	public static Object getAnnotationValue( Class<?> type, String fieldName, Class<? extends Annotation> annotationType, String annotationProperty )
	{
		try
		{
			Field field = getDeclaredField( fieldName, type );
			if ( field != null )
			{
				Annotation annotation = field.getAnnotation( annotationType );

				if ( annotation != null )
				{
					Method annotationMethod = annotationType.getDeclaredMethod( annotationProperty, new Class[0] );
					if ( annotationMethod != null )
					{
						return annotationMethod.invoke( annotation, ( Object[] ) null );
					}
				}
			}
		}
		catch ( Exception localException )
		{
		}

		return null;
	}

	public static Map<String, Object> getAllAnnotationValues( Class<?> type, Class<? extends Annotation> annotationType, String annotationProperty )
	{
		Map<String, Object> columnsMap = new LinkedHashMap();
		List<Field> fields = getAllDeclaredFields( type );
		try
		{
			for ( Field field : fields )
			{
				field.setAccessible( true );
				Annotation annotation = field.getAnnotation( annotationType );

				if ( annotation != null )
				{
					Method annotationMethod = annotationType.getDeclaredMethod( annotationProperty, new Class[0] );
					if ( annotationMethod != null )
					{
						columnsMap.put( field.getName(), ( String ) annotationMethod.invoke( annotation, ( Object[] ) null ) );
					}
				}
			}
		}
		catch ( Exception localException )
		{
		}
		return columnsMap;
	}

	public static Object callMethod( String className, String method, Object... params )
	{
		try
		{
			Class<?> clazz = Class.forName( className );
			Method[] methods = clazz.getDeclaredMethods();
			for ( Method m : methods )
			{
				if ( m.getName().equalsIgnoreCase( method ) )
				{
					m.setAccessible( true );
					return m.invoke( null, params );
				}
			}
		}
		catch ( ReflectiveOperationException e )
		{
			return null;
		}
		return null;
	}

	public static Object newInstance( String className, Class<?>[] paramTypes, Object... params )
	{
		try
		{
			Class<?> clazz = Class.forName( className );
			if ( params == null )
			{
				return clazz.newInstance();
			}
			Constructor<?> constructor = clazz.getDeclaredConstructor( paramTypes );
			constructor.setAccessible( true );
			return constructor.newInstance( params );
		}
		catch ( ReflectiveOperationException e )
		{
		}
		return null;
	}

	public static Object newGenericArray( String className, Object... objects )
	{
		try
		{
			Class<?> clazz = Class.forName( className );
			Object array = Array.newInstance( clazz, objects.length );
			int i = 0;
			for ( Object object : objects )
			{
				Array.set( array, i, object );
				i++;
			}
			return array;
		}
		catch ( ReflectiveOperationException e )
		{
		}
		return null;
	}
}
