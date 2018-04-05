/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permission;

import io.amelia.support.Objs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holds the different types of permissions that can exist.
 */
public abstract class PermissionType
{
	public static final PermissionType BOOL = new PermissionType( "Boolean", "bool" )
	{
		@Override
		Boolean cast( Object obj ) throws ClassCastException
		{
			try
			{
				return Objs.castToBooleanWithException( obj );
			}
			catch ( ClassCastException e )
			{
				return true;
			}
		}

		@Override
		public Object getBlankValue()
		{
			return false;
		}

		@Override
		public Class<? extends Object> getValueClass()
		{
			return Boolean.class;
		}

		@Override
		public boolean hasEnumerate()
		{
			return false;
		}

		@Override
		public boolean hasMax()
		{
			return false;
		}

		@Override
		public boolean hasMin()
		{
			return false;
		}

		@Override
		public int maxValue()
		{
			return 0;
		}

		@Override
		public int minValue()
		{
			return 0;
		}
	};
	public static final PermissionType DEFAULT = new PermissionType( "Default", "Permission", "Perm", "none", "" )
	{
		@Override
		Boolean cast( Object obj ) throws ClassCastException
		{
			try
			{
				return Objs.castToBooleanWithException( obj );
			}
			catch ( ClassCastException e )
			{
				return true;
			}
		}

		@Override
		public Object getBlankValue()
		{
			return false;
		}

		@Override
		public Class<? extends Object> getValueClass()
		{
			return Boolean.class;
		}

		@Override
		public boolean hasEnumerate()
		{
			return false;
		}

		@Override
		public boolean hasMax()
		{
			return false;
		}

		@Override
		public boolean hasMin()
		{
			return false;
		}

		@Override
		public int maxValue()
		{
			return 0;
		}

		@Override
		public int minValue()
		{
			return 0;
		}
	};
	public static final PermissionType DOUBLE = new PermissionType( "Double", "dbl" )
	{
		@Override
		Double cast( Object obj ) throws ClassCastException
		{
			return Objs.castToDoubleWithException( obj );
		}

		@Override
		public Object getBlankValue()
		{
			return 0d;
		}

		@Override
		public Class<? extends Object> getValueClass()
		{
			return Double.class;
		}

		@Override
		public boolean hasEnumerate()
		{
			return false;
		}

		@Override
		public boolean hasMax()
		{
			return true;
		}

		@Override
		public boolean hasMin()
		{
			return true;
		}

		@Override
		public int maxValue()
		{
			return Double.MAX_EXPONENT;
		}

		@Override
		public int minValue()
		{
			return Double.MIN_EXPONENT;
		}
	};
	public static final PermissionType ENUM = new PermissionType( "Enum", "enumerate" )
	{
		@Override
		String cast( Object obj ) throws ClassCastException
		{
			return Objs.castToStringWithException( obj );
		}

		@Override
		public Object getBlankValue()
		{
			return "";
		}

		@Override
		public Class<? extends Object> getValueClass()
		{
			return String.class;
		}

		@Override
		public boolean hasEnumerate()
		{
			return true;
		}

		@Override
		public boolean hasMax()
		{
			return true;
		}

		@Override
		public boolean hasMin()
		{
			return false;
		}

		@Override
		public int maxValue()
		{
			return 255;
		}

		@Override
		public int minValue()
		{
			return 0;
		}
	};
	public static final PermissionType INT = new PermissionType( "Integer", "int" )
	{
		@Override
		Integer cast( Object obj )
		{
			return Objs.castToIntWithException( obj );
		}

		@Override
		public Object getBlankValue()
		{
			return 0;
		}

		@Override
		public Class<? extends Object> getValueClass()
		{
			return Integer.class;
		}

		@Override
		public boolean hasEnumerate()
		{
			return false;
		}

		@Override
		public boolean hasMax()
		{
			return true;
		}

		@Override
		public boolean hasMin()
		{
			return true;
		}

		@Override
		public int maxValue()
		{
			return Integer.MAX_VALUE;
		}

		@Override
		public int minValue()
		{
			return Integer.MIN_VALUE;
		}
	};
	public static final PermissionType VAR = new PermissionType( "Variable", "var", "string", "str" )
	{
		@Override
		String cast( Object obj ) throws ClassCastException
		{
			return Objs.castToStringWithException( obj );
		}

		@Override
		public Object getBlankValue()
		{
			return "";
		}

		@Override
		public Class<? extends Object> getValueClass()
		{
			return String.class;
		}

		@Override
		public boolean hasEnumerate()
		{
			return false;
		}

		@Override
		public boolean hasMax()
		{
			return true;
		}

		@Override
		public boolean hasMin()
		{
			return false;
		}

		@Override
		public int maxValue()
		{
			return 255;
		}

		@Override
		public int minValue()
		{
			return 0;
		}
	};
	private static volatile List<PermissionType> types = new ArrayList<>();

	public static PermissionType valueOf( final String name )
	{
		if ( name == null )
			return PermissionType.DEFAULT;

		for ( PermissionType t : types )
			if ( Arrays.asList( t.names() ).contains( name.toLowerCase() ) )
				return t;

		return PermissionType.DEFAULT;
	}

	private final String[] names;

	private PermissionType( String... names )
	{
		this.names = names;
		types.add( this );
	}

	/**
	 * Expects the returned value to have been checked and casted to the proper class
	 *
	 * @param obj The object to be casted.
	 * @return A successfully casted object, null will be considered unsuccessful and will negate the value.
	 * @throws ClassCastException if casting failed
	 */
	abstract Object cast( Object obj ) throws ClassCastException;

	/**
	 * Provides a blank value, used for when no default value was provided.
	 *
	 * @return A blank Object, e.g., String, Integer, etc.
	 */
	public abstract Object getBlankValue();

	/**
	 * Provides the class associated with this type
	 *
	 * @return The class for this type
	 */
	public abstract Class<? extends Object> getValueClass();

	/**
	 * Informs if this is an enumerate type, i.e., has a set of possible values, all others will be negated.
	 *
	 * @return True if this is so.
	 */
	public abstract boolean hasEnumerate();

	/**
	 * Informs if a String has a max length or if a number has a max value.
	 *
	 * @return True if it does.
	 */
	public abstract boolean hasMax();

	/**
	 * Informs if a String has a min length or if a number has a min value.
	 *
	 * @return True if this is so.
	 */
	public abstract boolean hasMin();

	/**
	 * If type has a max value, this will provide the max value.
	 *
	 * @return The max value
	 */
	public abstract int maxValue();

	/**
	 * If type has a min value, this will provide the min value.
	 *
	 * @return The min value
	 */
	public abstract int minValue();

	public final String name()
	{
		return names[0];
	}

	public final String[] names()
	{
		return names;
	}

	@Override
	public final String toString()
	{
		return names[0];
	}
}
