package com.marchnetworks.command.common.dao;

import java.util.List;

public interface GenericDAO<Type, IdType>
{
	void create( Type obj );

	void merge( Type obj );

	void delete( Type obj );

	boolean deleteById( IdType id );

	int deleteByIdsDetached( List<IdType> idTypeList );

	boolean deleteDetached( IdType id );

	void flush();

	void clear();

	void flushAndClear();

	Type findById( IdType id );

	Type findByIdDetached( IdType id );

	List<Type> findByIds( List<IdType> idTypeList );

	List<Type> findAll();

	List<Type> findAllDetached();

	List<Type> findAllDehydrated( List<String> paramList );

	void evict( List<Type> typeList );

	int getRowCount();

	IdType getLastId();

	Object findMinValue( String paramString );

	Object findMaxValue( String paramString );

	void deleteAll();

	Type findFirst();
}
