package com.marchnetworks.management.topology.dao;

import com.marchnetworks.management.topology.model.ChannelResourceEntity;

public abstract interface ChannelResourceDAO extends ResourceDAO<ChannelResourceEntity>
{
	public abstract ChannelResourceEntity getChannel( String paramString1, String paramString2 );

	public abstract Long getChannelId( String paramString1, String paramString2 );
}

