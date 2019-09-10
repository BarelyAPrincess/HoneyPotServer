package com.marchnetworks.management.file.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.EventTypesEnum;
import com.marchnetworks.management.file.model.FileStorageType;

public class FileStorageEvent extends Event implements Notifiable
{
	protected String fileStorageId;
	protected String eventPathName;

	private FileStorageEvent( String fileStorageId, String eventPathName )
	{
		super( FileStorageEvent.class.getName() );
		this.fileStorageId = fileStorageId;
		this.eventPathName = eventPathName;
	}

	public static FileStorageEvent newFileStorageAddedEvent( String fileStorageId, FileStorageType fileType )
	{
		FileStorageEvent event = null;
		if ( fileType.equals( FileStorageType.FIRMWARE ) )
		{
			event = new FileStorageEvent( fileStorageId, EventTypesEnum.FIRMWARE_ADDED.getFullPathEventName() );
		}
		else
		{
			event = new FileStorageEvent( fileStorageId, EventTypesEnum.CERTIFICATE_ADDED.getFullPathEventName() );
		}
		return event;
	}

	public static FileStorageEvent newFileStorageRemovedEvent( String fileStorageId, FileStorageType fileType )
	{
		FileStorageEvent event = null;
		if ( fileType.equals( FileStorageType.FIRMWARE ) )
		{
			event = new FileStorageEvent( fileStorageId, EventTypesEnum.FIRMWARE_REMOVED.getFullPathEventName() );
		}
		else
		{
			event = new FileStorageEvent( fileStorageId, EventTypesEnum.CERTIFICATE_REMOVED.getFullPathEventName() );
		}
		return event;
	}

	public String getEventNotificationType()
	{
		return eventPathName;
	}

	public EventNotification getNotificationInfo()
	{
		return new Builder( getEventNotificationType() ).source( fileStorageId ).build();
	}

	public String getFileStorageId()
	{
		return fileStorageId;
	}
}

