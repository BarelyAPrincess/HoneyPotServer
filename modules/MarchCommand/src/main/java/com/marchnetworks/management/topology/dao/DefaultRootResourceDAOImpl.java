package com.marchnetworks.management.topology.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.management.topology.model.DefaultRootResourceEntity;

public class DefaultRootResourceDAOImpl extends GenericHibernateDAO<DefaultRootResourceEntity, String> implements DefaultRootResourceDAO
{
	private String systemRootResourceId;
	private String logicalRootResourceId;

	public DefaultRootResourceEntity getLogicalRootResource()
	{
		return ( DefaultRootResourceEntity ) findById( logicalRootResourceId );
	}

	public DefaultRootResourceEntity getSystemRootResource()
	{
		return ( DefaultRootResourceEntity ) findById( systemRootResourceId );
	}

	public void setSystemRootResourceId( String systemRootResourceId )
	{
		this.systemRootResourceId = systemRootResourceId;
	}

	public void setLogicalRootResourceId( String logicalRootResourceId )
	{
		this.logicalRootResourceId = logicalRootResourceId;
	}
}

