package com.marchnetworks.command.api.id;

import com.marchnetworks.command.common.ReflectionUtils;
import com.marchnetworks.command.common.dao.GenericHibernateDAO;

import javax.persistence.Table;

public class IdGenerateDAOImpl<T extends IdSetter, ID> extends GenericHibernateDAO<T, ID>
{
	private IdCoreService idCoreService;

	public void create( T entity )
	{
		Long lastId = getLastId();
		String tableName = ( String ) ReflectionUtils.getAnnotationValue( entity.getClass(), Table.class, "name" );
		Long id = idCoreService.getNextId( lastId, tableName );
		entity.setId( id );
		super.create( entity );
	}

	public void setIdCoreService( IdCoreService idCoreService )
	{
		this.idCoreService = idCoreService;
	}
}
