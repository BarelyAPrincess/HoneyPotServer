package com.marchnetworks.management.topology.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.management.topology.model.ResourceEntity;

import java.util.Calendar;

public abstract class AbstractResourceDAOImpl<T extends ResourceEntity> extends GenericHibernateDAO<T, Long> implements ResourceDAO<T>
{
	public void create( T entity )
	{
		entity.setTimeCreated( Calendar.getInstance() );
		super.create( entity );
	}
}

