package io.amelia.scripting;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import groovy.lang.MissingPropertyException;

/**
 * Our own extended binding so we can better track if and when a binding variable is changed
 */
public class ScriptBinding
{
	private final Set<String> history = Sets.newHashSet();
	private Map<String, Object> variables;

	public ScriptBinding()
	{

	}

	public ScriptBinding( Map<String, Object> map )
	{
		variables = map;
	}

	public void clearHistory()
	{
		history.clear();
	}

	public Set<String> getUpdateHistory()
	{
		return Collections.unmodifiableSet( history );
	}

	/**
	 * @param name the name of the variable to lookup
	 *
	 * @return the variable value
	 */
	public Object getVariable( String name )
	{
		if ( variables == null )
			throw new MissingPropertyException( name, this.getClass() );

		Object result = variables.get( name );

		if ( result == null && !variables.containsKey( name ) )
			throw new MissingPropertyException( name, this.getClass() );

		return result;
	}

	public Map<String, Object> getVariables()
	{
		if ( variables == null )
			variables = Maps.newLinkedHashMap();
		return variables;
	}

	/**
	 * Simple check for whether the binding contains a particular variable or not.
	 *
	 * @param name the name of the variable to check for
	 */
	public boolean hasVariable( String name )
	{
		return variables != null && variables.containsKey( name );
	}

	/**
	 * Sets the value of the given variable
	 *
	 * @param name  the name of the variable to set
	 * @param value the new value for the given variable
	 */
	public void setVariable( String name, Object value )
	{
		if ( variables == null )
			variables = Maps.newLinkedHashMap();
		variables.put( name, value );
		history.add( name );
	}
}
