package com.marchnetworks.command.common.execution.trigger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class ExecutionTrigger
{
	private Long id;
	private String name;
	private String appId;
	private String group;
	private String username;
	private ExecutionFrequency frequency;
	private Integer dayOfWeek;
	private Integer executionTime;
	private String timeZone;
	private Long lastExecutionTime;

	@XmlElement( required = true, nillable = true )
	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	@XmlElement( required = true )
	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	@XmlTransient
	public String getAppId()
	{
		return appId;
	}

	public void setAppId( String appId )
	{
		this.appId = appId;
	}

	@XmlTransient
	public String getGroup()
	{
		return group;
	}

	public void setGroup( String group )
	{
		this.group = group;
	}

	@XmlTransient
	public String getUsername()
	{
		return username;
	}

	public void setUsername( String username )
	{
		this.username = username;
	}

	@XmlElement( required = true, nillable = true )
	public Integer getDayOfWeek()
	{
		return dayOfWeek;
	}

	public void setDayOfWeek( Integer dayOfWeek )
	{
		this.dayOfWeek = dayOfWeek;
	}

	@XmlElement( required = true )
	public Integer getExecutionTime()
	{
		return executionTime;
	}

	public void setExecutionTime( Integer executionTime )
	{
		this.executionTime = executionTime;
	}

	@XmlElement( required = true )
	public String getTimeZone()
	{
		return timeZone;
	}

	public void setTimeZone( String timeZone )
	{
		this.timeZone = timeZone;
	}

	@XmlElement( nillable = true, required = false )
	public Long getLastExecutionTime()
	{
		return lastExecutionTime;
	}

	public long getStartExecutionTime( long endTime )
	{
		long TWO_WEEKS = 1209600000L;
		long timeWindow = frequency.getTimeWindow();

		int nTime = 1;
		if ( endTime == 0L )
		{
			return 0L;
		}

		if ( lastExecutionTime.longValue() > 0L )
		{
			long timeDiff = endTime - lastExecutionTime.longValue();
			if ( timeDiff > 1209600000L )
			{
				timeDiff = 1209600000L;
			}

			nTime = ( int ) ( timeDiff / timeWindow );

			long remainder = timeDiff % timeWindow;
			if ( remainder > timeWindow / 2L )
			{
				nTime++;
			}
		}
		else if ( lastExecutionTime.longValue() < 0L )
		{

			return endTime - 2592000000L;
		}

		return endTime - nTime * timeWindow;
	}

	public void setLastExecutionTime( Long lastExecutionTime )
	{
		this.lastExecutionTime = lastExecutionTime;
	}

	@XmlElement( required = true )
	public ExecutionFrequency getFrequency()
	{
		return frequency;
	}

	public void setFrequency( ExecutionFrequency frequency )
	{
		this.frequency = frequency;
	}

	public String toString()
	{
		return "ExecutionTrigger [id=" + id + ", name=" + name + ", appId=" + appId + ", group=" + group + ", username=" + username + ", frequency=" + frequency + ", dayOfWeek=" + dayOfWeek + ", executionTime=" + executionTime + ", timeZone=" + timeZone + ", lastExecutionTime=" + lastExecutionTime + "]";
	}
}
