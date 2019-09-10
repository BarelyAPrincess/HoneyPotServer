package com.marchnetworks.notification.service;

import com.marchnetworks.app.data.AppStatus;
import com.marchnetworks.app.events.AppEvent;
import com.marchnetworks.app.events.AppEventType;
import com.marchnetworks.app.events.AppStateEvent;
import com.marchnetworks.app.service.AppManager;
import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.command.api.execution.trigger.ExecutionTriggerServiceException;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.api.notification.NotificationCoreService;
import com.marchnetworks.command.api.notification.NotificationException;
import com.marchnetworks.command.api.notification.NotificationExceptionType;
import com.marchnetworks.command.api.provider.ContentProvider;
import com.marchnetworks.command.common.notification.data.Notification;
import com.marchnetworks.command.common.notification.data.NotificationContent;
import com.marchnetworks.command.common.user.UserException;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.execution.trigger.ExecutionTriggerService;
import com.marchnetworks.management.user.UserService;
import com.marchnetworks.management.user.events.UserRemovedEvent;
import com.marchnetworks.notification.dao.NotificationDAO;
import com.marchnetworks.notification.data.ContentProviderKey;
import com.marchnetworks.notification.data.NotificationMessage;
import com.marchnetworks.notification.events.NotificationEvent;
import com.marchnetworks.notification.events.NotificationJob;
import com.marchnetworks.notification.model.NotificationEntity;
import com.marchnetworks.server.event.EventListener;
import com.marchnetworks.server.event.EventRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotificationServiceImpl implements NotificationCoreService, NotificationService, EventListener, InitializationListener
{
	private static final Logger LOG = LoggerFactory.getLogger( NotificationServiceImpl.class );

	private static final String NOTIFICATION_GROUP = "NOTIFICATION_GROUP";
	private Map<ContentProviderKey, ContentProvider<List<NotificationContent>, Notification>> contentProviders = new HashMap();

	private NotificationDAO notificationDAO;
	private UserService userService;
	private SendNotificationService sendNotificationService;
	private AppManager appManager;
	private ExecutionTriggerService executionTriggerService;

	public void onAppInitialized()
	{
		List<NotificationEntity> notifications = notificationDAO.findAllDetached();

		for ( NotificationEntity notification : notifications )
		{
			try
			{
				executionTriggerService.schedule( notification.toExecutionTrigger(), true, "NOTIFICATION_GROUP", NotificationJob.class );
			}
			catch ( Exception e )
			{
				LOG.error( "Failed to schedule notification at startup" );
			}
		}
	}

	public String getListenerName()
	{
		return NotificationServiceImpl.class.getSimpleName();
	}

	public Notification updateNotification( Notification notification, String username ) throws NotificationException
	{
		boolean newEntity = notification.getId() == null;

		NotificationEntity entity = newEntity ? new NotificationEntity() : ( NotificationEntity ) notificationDAO.findById( notification.getId() );
		entity.readFromDataObject( notification );

		if ( newEntity )
		{
			if ( notificationDAO.existsByGroupAppIdAndName( notification.getGroup(), notification.getAppId(), notification.getName() ) )
			{
				String error = "Notification with name " + notification.getName() + " already exists";
				LOG.error( error );
				throw new NotificationException( NotificationExceptionType.NOTIFICATION_NAME_EXISTS, error );
			}
			entity.setUsername( username );
			notificationDAO.create( entity );
			audit( entity, AuditEventNameEnum.NOTIFICATION_CREATED );
		}
		else
		{
			audit( entity, AuditEventNameEnum.NOTIFICATION_UPDATED );
		}
		try
		{
			executionTriggerService.schedule( entity.toExecutionTrigger(), newEntity, "NOTIFICATION_GROUP", NotificationJob.class );
		}
		catch ( ExecutionTriggerServiceException e )
		{
			throw new NotificationException( NotificationExceptionType.SCHEDULE_ERROR, e );
		}

		return entity.toDataObject();
	}

	public void deleteNotifications( Long[] ids ) throws NotificationException
	{
		boolean allFound = true;
		for ( Long id : ids )
		{
			NotificationEntity entity = ( NotificationEntity ) notificationDAO.findById( id );
			if ( entity == null )
			{
				LOG.error( "Notification with id {} does not exist", id );
				allFound = false;
			}
			else
			{
				notificationDAO.delete( entity );
				unscheduleNotification( id );
				Event event = new NotificationEvent( id );
				getEventRegistry().sendEventAfterTransactionCommits( event );
				audit( entity, AuditEventNameEnum.NOTIFICATION_DELETED );
			}
		}
		if ( !allFound )
		{
			throw new NotificationException( NotificationExceptionType.NOT_FOUND, "One or more Notifications not found when deleting" );
		}
	}

	public List<Notification> getAllNotifications( String group, String appId, String username )
	{
		List<NotificationEntity> entities = notificationDAO.findAllByGroupAndAppId( group, appId );
		List<Notification> notifications = new ArrayList( entities.size() );
		for ( NotificationEntity entity : entities )
		{
			if ( checkUserAccess( username, entity.getUsername() ) )
			{
				notifications.add( entity.toDataObject() );
			}
		}
		return notifications;
	}

	public List<Notification> getAllNotifications()
	{
		List<NotificationEntity> entities = notificationDAO.findAllDetached();
		List<Notification> notification = new ArrayList( entities.size() );
		for ( NotificationEntity entity : entities )
		{
			notification.add( entity.toDataObject() );
		}
		return notification;
	}

	public void setLastSentTime( Long id )
	{
		NotificationEntity entity = ( NotificationEntity ) notificationDAO.findById( id );
		if ( entity != null )
		{
			entity.settLastSentTime( System.currentTimeMillis() );
		}
	}

	public Long getNotificationLastSentTime( Long id )
	{
		NotificationEntity entity = ( NotificationEntity ) notificationDAO.findById( id );
		Long result = null;
		if ( entity != null )
		{
			result = Long.valueOf( entity.getLastSentTime() );
		}
		return result;
	}

	public void processNotification( Long id )
	{
		NotificationEntity entity = notificationDAO.findByIdDetached( id );
		ContentProvider<List<NotificationContent>, Notification> provider = contentProviders.get( new ContentProviderKey( entity.getGroup(), entity.getAppId() ) );

		if ( provider == null )
			LOG.error( "No content provider is set for notification name: " + entity.getName() + ", group: " + entity.getGroup() );
		else
		{
			List<NotificationContent> contents = provider.getContent( entity.toDataObject() );
			List<NotificationMessage> messages = new ArrayList<NotificationMessage>();

			for ( NotificationContent content : contents )
			{

				NotificationMessage message = new NotificationMessage();
				List<String> emails = new ArrayList<String>();
				for ( String recipient : content.getRecipients() )
				{
					String email = userService.getMember( recipient ).getDetailsView().getEmail();
					if ( email != null )
					{
						emails.add( email );
					}
				}
				if ( !emails.isEmpty() )
				{

					message.setTo( ( String[] ) emails.toArray( new String[emails.size()] ) );
					message.setSubject( content.getSubject() );
					message.setText( content.getMessage() );

					if ( !content.getInlines().isEmpty() )
					{
						message.addAllInlines( content.getInlines() );
					}

					messages.add( message );
				}
			}
			sendNotificationService.sendMessages( id, messages );
		}
	}

	public void setContentProvider( String group, String appId, ContentProvider<List<NotificationContent>, Notification> provider )
	{
		ContentProviderKey key = new ContentProviderKey( group, appId );
		contentProviders.put( key, provider );
	}

	public void updateRecipientsAndUsername( String oldName, String newName )
	{
		List<NotificationEntity> entities = notificationDAO.findAllByUsername( oldName );
		for ( NotificationEntity entity : entities )
		{
			List<String> recipients = entity.getRecipients();
			if ( recipients.contains( oldName ) )
			{
				recipients.remove( oldName );
				recipients.add( newName );
				entity.setRecipients( recipients );
			}

			if ( oldName.equals( entity.getUsername() ) )
			{
				entity.setUsername( newName );
			}
		}
	}

	public void process( Event aEvent )
	{
		UserRemovedEvent userRemoved;
		if ( ( aEvent instanceof AppStateEvent ) )
		{
			AppStateEvent appEvent = ( AppStateEvent ) aEvent;
			if ( appEvent.getStatus() != AppStatus.RUNNING )
			{
				removeApp( appEvent.getAppID() );
			}
		}
		else if ( ( aEvent instanceof AppEvent ) )
		{
			AppEvent appEvent = ( AppEvent ) aEvent;
			if ( appEvent.getAppEventType() == AppEventType.UNINSTALLED )
			{
				removeApp( appEvent.getAppID() );
				notificationDAO.deleteByAppId( appEvent.getAppID() );
			}
		}
		else if ( ( aEvent instanceof UserRemovedEvent ) )
		{
			userRemoved = ( UserRemovedEvent ) aEvent;
			List<NotificationEntity> entities = notificationDAO.findAll();
			for ( NotificationEntity entity : entities )
			{
				List<String> recipients = entity.getRecipients();
				if ( recipients.contains( userRemoved.getUserName() ) )
				{
					recipients.remove( userRemoved.getUserName() );
				}
				entity.setRecipients( recipients );
			}
		}
	}

	private void audit( NotificationEntity entity, AuditEventNameEnum eventName )
	{
		AuditView.Builder auditBuilder = new AuditView.Builder( eventName.getName() ).addDetailsPair( "name", entity.getName() );
		if ( entity.getAppId() != null )
		{
			auditBuilder.setAppId( entity.getAppId() );
			auditBuilder.addDetailsPair( "app_name", appManager.getAppName( entity.getAppId() ) );
		}
		getEventRegistry().sendEventAfterTransactionCommits( new AuditEvent( auditBuilder.build() ) );
	}

	private void removeApp( String appId )
	{
		Set<ContentProviderKey> set = contentProviders.keySet();
		for ( Iterator<ContentProviderKey> iterator = set.iterator(); iterator.hasNext(); )
		{
			ContentProviderKey key = ( ContentProviderKey ) iterator.next();
			if ( appId.equals( key.getAppId() ) )
			{
				iterator.remove();
			}
		}
	}

	private void unscheduleNotification( Long id ) throws NotificationException
	{
		String jobName = String.valueOf( id );
		try
		{
			executionTriggerService.unscheduleJob( jobName, "NOTIFICATION_GROUP" );
		}
		catch ( ExecutionTriggerServiceException e )
		{
			String errorMessage = "Failed to unschedule Notification, Exception " + e.getMessage();
			LOG.error( errorMessage );
			throw new NotificationException( NotificationExceptionType.SCHEDULE_ERROR, errorMessage, e );
		}
	}

	private boolean checkUserAccess( String currentUser, String user )
	{
		if ( ( currentUser == null ) || ( currentUser.equals( user ) ) )
		{
			return true;
		}
		try
		{
			return userService.hasUserAccess( user );
		}
		catch ( UserException e )
		{
			LOG.error( "Error while checking user access of " + currentUser + " for " + user + ", Exception: " + e.getMessage() );
		}
		return false;
	}

	public void setNotificationDAO( NotificationDAO notificationDAO )
	{
		this.notificationDAO = notificationDAO;
	}

	public void setUserService( UserService userService )
	{
		this.userService = userService;
	}

	public void setSendNotificationService( SendNotificationService sendNotificationService )
	{
		this.sendNotificationService = sendNotificationService;
	}

	private EventRegistry getEventRegistry()
	{
		return ( EventRegistry ) ApplicationContextSupport.getBean( "eventRegistry" );
	}

	public void setAppManager( AppManager appManager )
	{
		this.appManager = appManager;
	}

	public void setExecutionTriggerService( ExecutionTriggerService executionTriggerService )
	{
		this.executionTriggerService = executionTriggerService;
	}
}

