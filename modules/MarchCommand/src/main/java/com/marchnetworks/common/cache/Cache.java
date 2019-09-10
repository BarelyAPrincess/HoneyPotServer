package com.marchnetworks.common.cache;

public interface Cache<S, T>
{
	T getObject( S paramS );

	String getTag( S paramS );

	String createTag( S paramS );

	void updateObject( S paramS, T paramT );

	void returnObject( S paramS, T paramT );

	void removeObject( S paramS );

	void evict();
}
