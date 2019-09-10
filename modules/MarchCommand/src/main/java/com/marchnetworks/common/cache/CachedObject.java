package com.marchnetworks.common.cache;

import com.marchnetworks.common.utils.DateUtils;

public class CachedObject<T>
{
	private String tag;
	private T cachedObject;
	private long updatedTime;

	public CachedObject( String tag, T cachedObject )
	{
		this.tag = tag;
		this.cachedObject = cachedObject;
		updatedTime = DateUtils.getCurrentUTCTimeInMillis();
	}

	public String getTag()
	{
		return tag;
	}

	public void setTag( String tag )
	{
		this.tag = tag;
	}

	public T getCachedObject()
	{
		return ( T ) cachedObject;
	}

	public void setCachedObject( T cachedObject )
	{
		this.cachedObject = cachedObject;
		updatedTime = DateUtils.getCurrentUTCTimeInMillis();
	}

	public void clearCachedObject()
	{
		cachedObject = null;
	}

	public boolean hasCachedObject()
	{
		return cachedObject != null;
	}

	public long getUpdatedTime()
	{
		return updatedTime;
	}

	public void setUpdatedTime( long updatedTime )
	{
		this.updatedTime = updatedTime;
	}
}
