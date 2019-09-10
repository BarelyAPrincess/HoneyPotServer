package com.marchnetworks.common.id;

import com.marchnetworks.command.api.id.IdCoreService;

public class IdServiceImpl implements IdCoreService
{
	private IdDAO idDAO;

	public Long getNextId( Long lastId, String tableName )
	{
		IdEntity idEntity = idDAO.findByTableName( tableName );
		if ( idEntity == null )
		{
			idEntity = new IdEntity();
			idEntity.setLastId( lastId );
			idEntity.setTableName( tableName );
			idDAO.create( idEntity );
		}
		idEntity.setLastId( Long.valueOf( idEntity.getLastId().longValue() + 1L ) );
		return idEntity.getLastId();
	}

	public Long getLastId( String tableName )
	{
		IdEntity idEntity = idDAO.findByTableName( tableName );
		if ( idEntity == null )
		{
			return Long.valueOf( 0L );
		}
		return idEntity.getLastId();
	}

	public void deleteRow( String tableName )
	{
		IdEntity idEntity = idDAO.findByTableName( tableName );
		if ( idEntity != null )
		{
			idDAO.delete( idEntity );
		}
	}

	public void setIdDAO( IdDAO idDAO )
	{
		this.idDAO = idDAO;
	}
}
