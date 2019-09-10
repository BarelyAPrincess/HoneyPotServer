package com.marchnetworks.server.communications.soap;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;

public class PoolableServicePortFactory extends BaseKeyedPoolableObjectFactory
{
	public Object makeObject( Object key ) throws Exception
	{
		return new PortHolder();
	}
}

