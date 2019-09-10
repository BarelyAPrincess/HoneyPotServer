package com.marchnetworks.license.model;

public enum LicenseFeature
{
	THIRD_PARTY_ANALYTICS( "Third party analytics" );

	private String name;

	private LicenseFeature( String name )
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}
