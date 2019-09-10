package com.marchnetworks.app.service;

import com.marchnetworks.app.data.AppXmlDescriptor;
import com.marchnetworks.license.model.ApplicationIdentityToken;

public class AppInformation
{
	private AppXmlDescriptor descriptor;
	private ApplicationIdentityToken identity;

	public AppInformation( AppXmlDescriptor descriptor, ApplicationIdentityToken identity )
	{
		this.descriptor = descriptor;
		this.identity = identity;
	}

	public AppXmlDescriptor getDescriptor()
	{
		return descriptor;
	}

	public void setDescriptor( AppXmlDescriptor descriptor )
	{
		this.descriptor = descriptor;
	}

	public ApplicationIdentityToken getIdentity()
	{
		return identity;
	}

	public void setIdentity( ApplicationIdentityToken identity )
	{
		this.identity = identity;
	}
}
