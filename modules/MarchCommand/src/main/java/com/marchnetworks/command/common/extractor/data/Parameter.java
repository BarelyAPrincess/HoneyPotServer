package com.marchnetworks.command.common.extractor.data;

import java.util.Objects;

public class Parameter
{
	private String Path;
	private Object Value;

	public Parameter()
	{
	}

	public Parameter( String path, Object value )
	{
		Path = path;
		Value = value;
	}

	public String getPath()
	{
		return Path;
	}

	public void setPath( String path )
	{
		Path = path;
	}

	public Object getValue()
	{
		return Value;
	}

	public void setValue( Object value )
	{
		Value = value;
	}

	public int hashCode()
	{
		return Objects.hash( new Object[] {Path} );
	}

	public boolean equals( Object obj )
	{
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		Parameter other = ( Parameter ) obj;
		if ( Path == null )
		{
			if ( Path != null )
				return false;
		}
		else if ( !Path.equals( Path ) )
			return false;
		return true;
	}
}
