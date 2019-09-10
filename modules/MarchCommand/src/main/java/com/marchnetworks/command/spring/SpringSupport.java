package com.marchnetworks.command.spring;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.util.tracker.ServiceTracker;

public class SpringSupport
{
	public static Object beanFromBundle( Bundle bundle, String name )
	{
		String bundleSymbolicName = bundle.getSymbolicName();
		String bundleVersion = bundle.getVersion().toString();
		BundleContext bundleContext = bundle.getBundleContext();
		ServiceTracker<BlueprintContainer, BlueprintContainer> blueprintTracker;

		String filterString = "(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(Bundle-SymbolicName=" + bundleSymbolicName + ")(Bundle-Version=" + bundleVersion + "))";
		try
		{
			blueprintTracker = new ServiceTracker( bundleContext, bundleContext.createFilter( filterString ), null );
		}
		catch ( InvalidSyntaxException e )
		{
			throw new RuntimeException( "Could not create BlueprintContainer service tracker for " + filterString, e );
		}
		try
		{
			blueprintTracker.open();
			BlueprintContainer bc = ( BlueprintContainer ) blueprintTracker.getService();
			if ( bc == null )
			{
				throw new RuntimeException( "Could not obtain BlueprintContainer for " + filterString + " osgi service" );
			}

			return bc.getComponentInstance( name );
		}
		finally
		{
			blueprintTracker.close();
		}
	}
}
