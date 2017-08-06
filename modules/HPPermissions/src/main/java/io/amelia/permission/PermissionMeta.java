package io.amelia.permission;

import io.amelia.permission.lang.PermissionValueException;
import io.amelia.support.Objs;
import io.amelia.util.OptionalBoolean;

public class PermissionMeta
{
	private final PermissionNamespace namespace;
	private boolean aDefault;
	private String description = "";
	private OptionalBoolean value = OptionalBoolean.empty();

	public PermissionMeta( PermissionNamespace namespace )
	{
		this.namespace = namespace;
	}

	public OptionalBoolean getDefault()
	{
		if ( value == null || this != PermissionDefault.DEFAULT.getNode().getPermissionMeta() )

			if ( value == null && this != PermissionDefault.DEFAULT.getNode().getModel() )
				return ( T ) PermissionDefault.DEFAULT.getNode().getModel().getValue();
			else if ( value == null )
				return ( T ) getType().getBlankValue();

		return ( T ) valueDefault;
	}

	public void setDefault( boolean aDefault )
	{
		this.aDefault = aDefault;
	}

	/**
	 * Gets a brief description of this permission, if set
	 *
	 * @return Brief description of this permission
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Sets the description of this permission.
	 * <p>
	 * This will not be saved to disk, and is a temporary operation until the server reloads permissions.
	 *
	 * @param description The new description to set
	 */
	public PermissionMeta setDescription( String description )
	{
		this.description = description == null ? "" : description;
		return this;
	}

	public Permission getPermission()
	{
		return namespace.getPermission();
	}

	public OptionalBoolean getValue()
	{
		if ( value == null || !value.isPresent() )
			return getValueDefault();

		return value;
	}

	public boolean hasDescription()
	{
		return !Objs.isEmpty( description );
	}

	public PermissionModelValue setValue( Object value )
	{
		if ( value == null )
			value = getValueDefault();

		try
		{
			Object obj = type.cast( value );
			if ( obj == null )
				throw new ClassCastException();
			this.value = new PermissionValue( this, obj );
		}
		catch ( ClassCastException e )
		{
			throw new PermissionValueException( "Can't cast %s to type %s", value.getClass().getName(), type );
		}

		return this;
	}

	public PermissionMeta setValueDefault( Object valueDefault )
	{
		if ( valueDefault == null )
			valueDefault = type.getBlankValue();

		try
		{
			Object obj = type.cast( valueDefault );
			if ( obj == null )
				throw new ClassCastException();
			this.valueDefault = obj;
		}
		catch ( ClassCastException e )
		{
			throw new PermissionValueException( "Can't cast %s to type %s", valueDefault.getClass().getName(), type );
		}

		return this;
	}
}
