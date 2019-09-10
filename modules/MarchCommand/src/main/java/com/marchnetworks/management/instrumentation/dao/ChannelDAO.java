package com.marchnetworks.management.instrumentation.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.management.instrumentation.model.Channel;

import java.util.List;

public abstract interface ChannelDAO extends GenericDAO<Channel, Long>
{
	public abstract List<Channel> findByChannelId( String paramString );
}

