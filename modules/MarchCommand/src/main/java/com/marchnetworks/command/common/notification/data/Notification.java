package com.marchnetworks.command.common.notification.data;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class Notification
{
	private Long id;
	private String name;
	private String appId;
	private String group;
	private String username;
	private NotificationFrequency frequency;
	private Integer dayOfWeek;
	private Integer sendTime;
	private String timeZone;
	private long lastSentTime;
	private List<String> recipients;
	private Boolean enhancedMailTemplate;

	@XmlElement( required = true, nillable = true )
	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String getAppId()
	{
		return appId;
	}

	public void setAppId( String appId )
	{
		this.appId = appId;
	}

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

	@XmlElement( required = true )
	public NotificationFrequency getFrequency()
	{
		return frequency;
	}

	public void setFrequency( NotificationFrequency frequency )
	{
		this.frequency = frequency;
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
	public Integer getSendTime()
	{
		return sendTime;
	}

	public void setSendTime( Integer sendTime )
	{
		this.sendTime = sendTime;
	}

	public String getTimeZone()
	{
		return timeZone;
	}

	public void setTimeZone( String timeZone )
	{
		this.timeZone = timeZone;
	}

	public List<String> getRecipients()
	{
		return recipients;
	}

	public void setRecipients( List<String> recipients )
	{
		this.recipients = recipients;
	}

	public long getLastSentTime()
	{
		return lastSentTime;
	}

	public void settLastSentTime( long lastSentTime )
	{
		this.lastSentTime = lastSentTime;
	}

	public Boolean getEnhancedMailTemplate()
	{
		return enhancedMailTemplate;
	}

	public void setEnhancedMailTemplate( Boolean enhancedMailTemplate )
	{
		this.enhancedMailTemplate = enhancedMailTemplate;
	}

	public long getNotificationStartTime( long endTime )
	{
		long TWO_WEEKS = 1209600000L;
		long timeWindow = frequency.getTimeWindow();

		int nTime = 1;
		if ( endTime == 0L )
		{
			return 0L;
		}

		if ( lastSentTime > 0L )
		{
			long timeDiff = endTime - lastSentTime;
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
		else if ( lastSentTime < 0L )
		{

			return endTime - 2592000000L;
		}

		return endTime - nTime * timeWindow;
	}

	public String toString()
	{
		return "Id :" + id + ", Name:" + name + ", AppId:" + appId + ", Group: " + group + ", Frequency" + frequency + ", DayOfWeek:" + dayOfWeek + ", SendTime:" + sendTime + ", TimeZone:" + timeZone;
	}
}
