package io.amelia.http.webroot;

import java.util.stream.Stream;

import io.amelia.http.mappings.DefaultDomainMapping;
import io.amelia.http.mappings.DomainMapping;
import io.amelia.support.Objs;

public class DefaultWebroot extends Webroot
{
	private DomainMapping defaultMapping;

	public DefaultWebroot()
	{
		super( "default" );

		defaultMapping = new DefaultDomainMapping( this );
	}

	public DomainMapping getDefaultMapping()
	{
		return defaultMapping;
	}

	public Stream<DomainMapping> getMappings()
	{
		return Stream.concat( Stream.of( defaultMapping ), super.getMappings() );
	}

	@Override
	public Stream<DomainMapping> getMappings( String fullDomain )
	{
		if ( Objs.isEmpty( fullDomain ) )
			return Stream.of( defaultMapping );
		return super.getMappings( fullDomain );
	}
}
