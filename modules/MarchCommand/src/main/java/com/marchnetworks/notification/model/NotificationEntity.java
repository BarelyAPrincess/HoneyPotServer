package com.marchnetworks.notification.model;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.command.common.execution.trigger.ExecutionFrequency;
import com.marchnetworks.command.common.execution.trigger.ExecutionTrigger;
import com.marchnetworks.command.common.notification.data.Notification;
import com.marchnetworks.command.common.notification.data.NotificationFrequency;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "NOTIFICATION" )
public class NotificationEntity
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Column( name = "NAME" )
	private String name;
	@Column( name = "APP_ID" )
	private String appId;
	@Column( name = "NOTIFICATION_GROUP" )
	private String group;
	@Column( name = "USERNAME" )
	private String username;
	@Column( name = "FREQUENCY" )
	private NotificationFrequency frequency;
	@Column( name = "DAY_OF_WEEK" )
	private Integer dayOfWeek;
	@Column( name = "SEND_TIME" )
	private Integer sendTime;
	@Column( name = "TIME_ZONE" )
	private String timeZone;
	@Column( name = "RECIPIENTS", length = 4000 )
	private String recipientsString;
	@Column( name = "LAST_SENT_TIME" )
	private long lastSentTime;
	@Column( name = "ENHANCED_MAIL_TEMPLATE" )
	private Boolean enhancedMailTemplate;

	public List<String> getRecipients()
	{
		if ( recipientsString == null )
			return null;

		return CoreJsonSerializer.collectionFromJson( recipientsString, new TypeToken<ArrayList<String>>()
		{
		} );
	}

	public void setRecipients( List<String> recipients )
	{
		if ( ( recipients == null ) || ( recipients.isEmpty() ) )
		{
			recipientsString = null;
		}
		else
		{
			recipientsString = CoreJsonSerializer.toJson( recipients );
		}
	}

	public ExecutionTrigger toExecutionTrigger()
	{
		ExecutionTrigger trigger = new ExecutionTrigger();

		trigger.setAppId( appId );
		trigger.setDayOfWeek( dayOfWeek );
		trigger.setExecutionTime( sendTime );

		switch ( frequency )
		{
			case DAILY:
				trigger.setFrequency( ExecutionFrequency.DAILY );
				break;
			case HOURLY:
				trigger.setFrequency( ExecutionFrequency.HOURLY );
				break;
			default:
				trigger.setFrequency( ExecutionFrequency.WEEKLY );
		}

		trigger.setGroup( group );
		trigger.setId( id );
		trigger.setLastExecutionTime( Long.valueOf( lastSentTime ) );
		trigger.setName( name );
		trigger.setTimeZone( timeZone );
		trigger.setUsername( username );

		return trigger;
	}

	public Notification toDataObject()
	{
		Notification target = new Notification();
		target.setId( id );
		target.setName( name );
		target.setRecipients( getRecipients() );
		target.setAppId( appId );
		target.setGroup( group );
		target.setUsername( username );
		target.setFrequency( frequency );
		target.setDayOfWeek( dayOfWeek );
		target.setSendTime( sendTime );
		target.setTimeZone( timeZone );
		target.settLastSentTime( lastSentTime );
		target.setEnhancedMailTemplate( enhancedMailTemplate );
		return target;
	}

	public void readFromDataObject( Notification origin )
	{
		name = origin.getName();
		setRecipients( origin.getRecipients() );
		appId = origin.getAppId();
		group = origin.getGroup();
		frequency = origin.getFrequency();
		dayOfWeek = origin.getDayOfWeek();
		sendTime = origin.getSendTime();
		timeZone = origin.getTimeZone();
		enhancedMailTemplate = origin.getEnhancedMailTemplate();
		lastSentTime = 0L;
	}

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

	public String getUsername()
	{
		return username;
	}

	public void setUsername( String username )
	{
		this.username = username;
	}

	public NotificationFrequency getFrequency()
	{
		return frequency;
	}

	public void setFrequency( NotificationFrequency frequency )
	{
		this.frequency = frequency;
	}

	public Integer getDayOfWeek()
	{
		return dayOfWeek;
	}

	public void setDayOfWeek( Integer dayOfWeek )
	{
		this.dayOfWeek = dayOfWeek;
	}

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

	public long getLastSentTime()
	{
		return lastSentTime;
	}

	public void settLastSentTime( long lastSentTime )
	{
		this.lastSentTime = lastSentTime;
	}

	public Boolean getDefaultMailTemplate()
	{
		return enhancedMailTemplate;
	}

	public void setDefaultMailTemplate( Boolean enhancedMailTemplate )
	{
		this.enhancedMailTemplate = enhancedMailTemplate;
	}
}

