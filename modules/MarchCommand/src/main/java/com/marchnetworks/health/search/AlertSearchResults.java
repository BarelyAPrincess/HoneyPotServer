package com.marchnetworks.health.search;

import com.marchnetworks.health.data.AlertData;

public class AlertSearchResults
{
	public static int MAX_RESULTS_TO_LOG = 10;

	private AlertSearchQuery query;
	private AlertData[] results = null;
	private String errorMessage = null;

	public AlertSearchResults()
	{
	}

	public AlertSearchResults( AlertSearchQuery query )
	{
		this.query = query;
	}

	public AlertSearchResults( AlertSearchQuery query, AlertData[] results )
	{
		this.query = query;
		this.results = results;
	}

	public AlertSearchResults( AlertSearchQuery query, String errorMessage )
	{
		this.query = query;
		this.errorMessage = errorMessage;
	}

	public AlertSearchResults( AlertSearchQuery query, AlertData[] results, String errorMessage )
	{
		this.query = query;
		this.results = results;
		this.errorMessage = errorMessage;
	}

	public void setEmptyResult()
	{
		results = new AlertData[0];
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage( String errorMessage )
	{
		this.errorMessage = errorMessage;
	}

	public AlertSearchQuery getQuery()
	{
		return query;
	}

	public void setQuery( AlertSearchQuery query )
	{
		this.query = query;
	}

	public AlertData[] getResults()
	{
		return results;
	}

	public void setResults( AlertData[] results )
	{
		this.results = results;
	}
}
