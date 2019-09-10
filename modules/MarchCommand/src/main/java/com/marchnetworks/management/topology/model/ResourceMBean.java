package com.marchnetworks.management.topology.model;

@Deprecated
public abstract interface ResourceMBean
{
	public abstract String getId();

	public abstract String getName();

	public abstract Long getVersion();

	public abstract String getXml();

	public abstract String getXml( int paramInt );
}

