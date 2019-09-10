package com.marchnetworks.command.api.notification;

import com.marchnetworks.command.common.notification.data.Notification;

import java.util.List;

public class EmailContentSpecification
{
	private String product;
	private String reportName;
	private Notification notification;
	private List<List<String>> tableData;
	private long startTime;
	private long endTime;

	public String getProduct()
	{
		return product;
	}

	public void setProduct( String product )
	{
		this.product = product;
	}

	public String getReportName()
	{
		return reportName;
	}

	public void setReportName( String reportName )
	{
		this.reportName = reportName;
	}

	public Notification getNotification()
	{
		return notification;
	}

	public void setNotification( Notification notification )
	{
		this.notification = notification;
	}

	public List<List<String>> getTableData()
	{
		return tableData;
	}

	public void setTableData( List<List<String>> tableData )
	{
		this.tableData = tableData;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public void setStartTime( long startTime )
	{
		this.startTime = startTime;
	}

	public long getEndTime()
	{
		return endTime;
	}

	public void setEndTime( long endTime )
	{
		this.endTime = endTime;
	}
}
