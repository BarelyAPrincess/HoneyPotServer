package com.marchnetworks.license.model;

import com.marchnetworks.license.exception.LicenseException;

import org.w3c.dom.Node;

public class RecordingLicenseImport extends LicenseImport
{
	private LicenseType type;

	public RecordingLicenseImport( LicenseType type, Node nLicense ) throws LicenseException
	{
		super( nLicense );

		this.type = type;
	}

	public LicenseType getType()
	{
		return type;
	}

	public void setType( LicenseType type )
	{
		this.type = type;
	}

	public String getLicenseTypeName()
	{
		return type.name();
	}
}
