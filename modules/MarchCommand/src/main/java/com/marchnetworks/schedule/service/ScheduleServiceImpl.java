package com.marchnetworks.schedule.service;

import com.marchnetworks.app.core.OsgiManager;
import com.marchnetworks.app.events.AppEvent;
import com.marchnetworks.app.events.AppEventType;
import com.marchnetworks.app.service.AppManager;
import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.api.schedule.ScheduleConsumerService;
import com.marchnetworks.command.api.schedule.ScheduleCoreService;
import com.marchnetworks.command.api.schedule.ScheduleException;
import com.marchnetworks.command.api.schedule.ScheduleExceptionType;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.schedule.data.DaySchedule;
import com.marchnetworks.command.common.schedule.data.Schedule;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.Group;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.user.UserException;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.management.user.UserService;
import com.marchnetworks.schedule.dao.ScheduleDAO;
import com.marchnetworks.schedule.events.ScheduleEvent;
import com.marchnetworks.schedule.events.ScheduleEventType;
import com.marchnetworks.schedule.events.ScheduleJob;
import com.marchnetworks.schedule.model.ScheduleEntity;
import com.marchnetworks.schedule.model.ScheduleGroup;
import com.marchnetworks.server.event.EventListener;
import com.marchnetworks.server.event.EventRegistry;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ScheduleServiceImpl implements ScheduleCoreService, ScheduleService, InitializationListener, EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( ScheduleServiceImpl.class );

	private static final String SCHEDULE_GROUP = "SCHEDULE_GROUP";

	public static final String IS_START = "isStart";
	private ScheduleDAO scheduleDAO;
	private UserService userService;
	private EventRegistry eventRegistry;
	private Scheduler scheduler;
	private AppManager appManager;
	private OsgiManager osgiManager;
	private ResourceTopologyServiceIF resourceTopologyService;

	public void onAppInitialized()
	{
		List<ScheduleEntity> schedules = scheduleDAO.findAllDetached();
		for ( ScheduleEntity scheduleEntity : schedules )
		{
			Schedule schedule = scheduleEntity.toDataObject();
			ScheduleGroup group = ScheduleGroup.valueOf( schedule.getGroup() );
			if ( group.getNeedsNotifications() )
			{
				try
				{
					schedule( schedule );
				}
				catch ( ScheduleException e )
				{
					LOG.error( "Error initializing schedule " + schedule + ", details: " + e.getMessage() );
				}
			}
		}
	}

	public String getListenerName()
	{
		return ScheduleServiceImpl.class.getSimpleName();
	}

	public Schedule updateSchedule( Schedule schedule, String username ) throws ScheduleException
	{
		boolean newEntity = schedule.getId() == null;

		ScheduleEntity entity = newEntity ? new ScheduleEntity() : ( ScheduleEntity ) scheduleDAO.findById( schedule.getId() );

		if ( ( !newEntity ) && ( !checkUserEdit( username, entity.getSystemRoots(), entity.getLogicalRoots() ) ) )
		{
			String error = "User territory assignment doesn't fully contain Schedule territory.";
			LOG.error( error + " User:{}, Schedule:{}", username, entity.getName() );
			throw new ScheduleException( ScheduleExceptionType.SCHEDULE_INSUFFICIENT_EDIT_RIGHTS, error );
		}

		entity.readFromDataObject( schedule );

		if ( newEntity )
		{
			if ( scheduleDAO.existsByGroupAppIdAndName( schedule.getGroup(), schedule.getAppId(), schedule.getName() ) )
			{
				String error = String.format( "Schedule with name %s already exists", new Object[] {schedule.getName()} );
				LOG.error( error );
				throw new ScheduleException( ScheduleExceptionType.SCHEDULE_NAME_EXISTS, error );
			}

			scheduleDAO.create( entity );
			audit( entity, AuditEventNameEnum.SCHEDULE_CREATED );
		}
		else
		{
			audit( entity, AuditEventNameEnum.SCHEDULE_UPDATED );
		}

		if ( ( CommonAppUtils.isNullOrEmptyCollection( schedule.getSystemRoots() ) ) && ( CommonAppUtils.isNullOrEmptyCollection( schedule.getLogicalRoots() ) ) )
		{
			MemberView member;

			try
			{
				member = userService.getUser( username );
			}
			catch ( UserException e )
			{
				throw new ScheduleException( ScheduleExceptionType.SCHEDULE_USER_NOT_FOUND, "User " + username + " could not be found. No update will be performed on schedule: " + schedule.getName() );
			}

			schedule.setSystemRoots( member.getAssembledSystemRoots() );
			schedule.setLogicalRoots( member.getAssembledLogicalRoots() );
		}

		entity.setSystemRoots( schedule.getSystemRoots() );
		entity.setLogicalRoots( schedule.getLogicalRoots() );

		Schedule result = entity.toDataObject();
		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( "Persisted scheduler {}", result.toString() );
		}

		ScheduleGroup group = ScheduleGroup.valueOf( result.getGroup() );
		if ( group.getNeedsNotifications() )
		{
			schedule( result );
		}

		Set<Long> territoryIds = new HashSet( result.getSystemRoots() );
		territoryIds.addAll( result.getLogicalRoots() );
		Set<Long> copy = new HashSet( territoryIds );

		for ( Long territoryId : copy )
		{
			try
			{
				Resource territory = resourceTopologyService.getResource( territoryId );

				territoryIds.addAll( territory.getAllResourceAssociationIds() );

				while ( territory.getParentResource() != null )
				{
					territoryIds.add( territory.getParentResourceId() );

					territory = territory.getParentResource();
				}
			}
			catch ( TopologyException e )
			{
				throw new ScheduleException( ScheduleExceptionType.SCHEDULE_CREATE_ERROR, "Assigned resource error." );
			}
		}

		Event event = new ScheduleEvent( result, ScheduleEventType.UPDATED, territoryIds );
		eventRegistry.sendEventAfterTransactionCommits( event );

		return result;
	}

	public void deleteSchedule( Long id, boolean forceDelete, String username ) throws ScheduleException
	{
		ScheduleEntity schedule = ( ScheduleEntity ) scheduleDAO.findById( id );
		if ( schedule == null )
		{
			String error = String.format( "Schedule with id %s does not exist", new Object[] {id} );
			LOG.error( error );
			throw new ScheduleException( ScheduleExceptionType.NOT_FOUND, error );
		}

		if ( !checkUserEdit( username, schedule.getSystemRoots(), schedule.getLogicalRoots() ) )
		{
			String error = "User territory assignment doesn't fully contain Schedule territory.";
			LOG.error( error + " User:{}, Schedule:{}", username, schedule.getName() );
			throw new ScheduleException( ScheduleExceptionType.SCHEDULE_INSUFFICIENT_EDIT_RIGHTS, error );
		}

		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( "Deleted scheduler with id {}", id );
		}
		if ( !forceDelete )
		{
			List<ScheduleConsumerService> scheduleConsumerServices = osgiManager.getServices( ScheduleConsumerService.class );
			for ( ScheduleConsumerService scheduleConsumerService : scheduleConsumerServices )
			{
				if ( scheduleConsumerService.isScheduleInUse( id ) )
				{
					String error = String.format( "Schedule with name %s is in use", new Object[] {schedule.getName()} );
					LOG.error( error );
					throw new ScheduleException( ScheduleExceptionType.SCHEDULE_IN_USE, error );
				}
			}
		}
		scheduleDAO.delete( schedule );
		unschedule( id );

		Event event = new ScheduleEvent( id, ScheduleEventType.DELETED );
		eventRegistry.sendEventAfterTransactionCommits( event );
		audit( schedule, AuditEventNameEnum.SCHEDULE_DELETED );
	}

	public Schedule getById( Long id ) throws ScheduleException
	{
		ScheduleEntity result = ( ScheduleEntity ) scheduleDAO.findById( id );
		if ( result == null )
		{
			String error = String.format( "Schedule with id %s does not exist", new Object[] {id} );
			LOG.error( error );
			throw new ScheduleException( ScheduleExceptionType.NOT_FOUND, error );
		}
		return result.toDataObject();
	}

	public List<Schedule> getAllSchedules( String group, String appId, String username )
	{
		List<ScheduleEntity> entities = scheduleDAO.findAllByGroupAndAppId( group, appId );
		List<Schedule> schedules = new ArrayList( entities.size() );
		for ( ScheduleEntity entity : entities )
		{
			if ( checkUserAccess( username, entity.getSystemRoots(), entity.getLogicalRoots() ) )
			{
				schedules.add( entity.toDataObject() );
			}
		}
		return schedules;
	}

	public List<Schedule> getAllSchedules()
	{
		List<ScheduleEntity> entities = scheduleDAO.findAllDetached();
		List<Schedule> schedules = new ArrayList( entities.size() );
		for ( ScheduleEntity entity : entities )
		{
			schedules.add( entity.toDataObject() );
		}
		return schedules;
	}

	private void audit( ScheduleEntity schedule, AuditEventNameEnum eventName )
	{
		AuditView.Builder auditBuilder = new AuditView.Builder( eventName.getName() ).addDetailsPair( "name", schedule.getName() );
		if ( schedule.getAppId() != null )
		{
			auditBuilder.setAppId( schedule.getAppId() );
			auditBuilder.addDetailsPair( "app_name", appManager.getAppName( schedule.getAppId() ) );
		}
		eventRegistry.sendEventAfterTransactionCommits( new AuditEvent( auditBuilder.build() ) );
	}

	private boolean checkUserAccess( String currentUser, Set<Long> systemRoots, Set<Long> logicalRoots )
	{
		try
		{
			MemberView currentMember = userService.getUser( currentUser );

			return ( resourceTopologyService.isOnPath( currentMember.getAssembledSystemRoots(), systemRoots ) ) || ( resourceTopologyService.isOnPath( currentMember.getAssembledLogicalRoots(), logicalRoots ) );

		}
		catch ( UserException e )
		{
			LOG.error( "Error while checking user access of {} to schedule , Details: {} ", currentUser, e.getMessage() );
		}

		return false;
	}

	private boolean checkUserEdit( String currentUser, Set<Long> systemFolders, Set<Long> logicalFolders )
	{
		try
		{
			MemberView currentMember = userService.getUser( currentUser );

			Set<Long> scheduleTerritories = CollectionUtils.mergeSets( systemFolders, logicalFolders );
			if ( ( scheduleTerritories.containsAll( currentMember.getAssembledSystemRoots() ) ) && ( scheduleTerritories.containsAll( currentMember.getAssembledLogicalRoots() ) ) )
			{
				return true;
			}

			Long systemFolder;

			if ( !CommonAppUtils.isNullOrEmptyCollection( systemFolders ) )
			{
				if ( currentMember.getAssembledSystemRoots().isEmpty() )
				{
					return false;
				}
				for ( Iterator<Long> i$ = systemFolders.iterator(); i$.hasNext(); )
				{
					systemFolder = i$.next();
					for ( Long userSystemFolder : currentMember.getAssembledSystemRoots() )
						if ( !resourceTopologyService.isChild( userSystemFolder, systemFolder ) )
							return false;
				}
			}

			Long logicalFolder;

			if ( !CommonAppUtils.isNullOrEmptyCollection( logicalFolders ) )
			{
				if ( currentMember.getAssembledLogicalRoots().isEmpty() )
				{
					return false;
				}
				for ( Iterator<Long> i$ = logicalFolders.iterator(); i$.hasNext(); )
				{
					logicalFolder = i$.next();
					for ( Long userLogicalFolder : currentMember.getAssembledLogicalRoots() )
					{
						if ( !resourceTopologyService.isChild( userLogicalFolder, logicalFolder ) )
						{
							return false;
						}
					}
				}
			}

			return true;
		}
		catch ( UserException e )
		{
			LOG.error( "Error while checking user access of {} to schedule , Details: {} ", currentUser, e.getMessage() );
		}

		return false;
	}

	private void schedule( Schedule schedule ) throws ScheduleException
	{
		List<CronTrigger> triggers = new ArrayList();
		String jobName = String.valueOf( schedule.getId() );

		for ( DaySchedule daySchedule : schedule.getIntervals() )
		{
			CronTrigger trigger = createCronTrigger( jobName, daySchedule.getDayOfWeek(), daySchedule.getStartTime(), true );
			triggers.add( trigger );
			trigger = createCronTrigger( jobName, daySchedule.getDayOfWeek(), daySchedule.getEndTime(), false );
			triggers.add( trigger );
		}
		try
		{
			JobDetail job = new JobDetail();
			job.setName( jobName );
			job.setGroup( "SCHEDULE_GROUP" );
			job.setJobClass( ScheduleJob.class );

			scheduler.deleteJob( jobName, "SCHEDULE_GROUP" );
			scheduler.addJob( job, true );
			for ( CronTrigger trigger : triggers )
			{
				scheduler.scheduleJob( trigger );
			}
		}
		catch ( Exception e )
		{
			String errorMessage = "Failed to create Schedule, Details: " + e.getMessage();
			LOG.error( errorMessage );
			throw new ScheduleException( ScheduleExceptionType.SCHEDULE_CREATE_ERROR, errorMessage, e );
		}
	}

	private void unschedule( Long id )
	{
		String jobName = String.valueOf( id );
		try
		{
			scheduler.deleteJob( jobName, "SCHEDULE_GROUP" );
		}
		catch ( SchedulerException e )
		{
			LOG.error( "Error deleting Schedule id " + id + ", Details: " + e.getMessage() );
		}
	}

	public void processNotification( Long id, boolean isStart )
	{
		Event event = new ScheduleEvent( id, ScheduleEventType.NOTIFICATION, isStart );
		eventRegistry.send( event );
	}

	public void process( Event aEvent )
	{
		if ( ( aEvent instanceof AppEvent ) )
		{
			AppEvent appEvent = ( AppEvent ) aEvent;
			if ( appEvent.getAppEventType() == AppEventType.UNINSTALLED )
			{
				scheduleDAO.deleteByAppId( appEvent.getAppID() );
			}
		}
	}

	public boolean checkDeletions( List<Long> resources, boolean forceDeletion )
	{
		List<Long> folders = new ArrayList();

		for ( Long resourceId : resources )
		{
			try
			{
				Resource resource = resourceTopologyService.getResource( resourceId );
				if ( ( resource instanceof Group ) )
				{
					Group folder = ( Group ) resource;
					List<Resource> childFolders = folder.createFilteredResourceList( new Class[] {Group.class} );
					for ( Resource child : childFolders )
					{
						folders.add( child.getId() );
					}
				}
			}
			catch ( TopologyException e )
			{
				return false;
			}
		}

		if ( folders.isEmpty() )
		{
			return true;
		}

		List<ScheduleEntity> schedules = scheduleDAO.findAll();

		if ( !forceDeletion )
		{
			for ( ScheduleEntity schedule : schedules )
			{
				if ( ( !Collections.disjoint( schedule.getSystemRoots(), folders ) ) || ( !Collections.disjoint( schedule.getLogicalRoots(), folders ) ) )
				{
					return false;
				}
			}
		}
		else
		{
			for ( ScheduleEntity schedule : schedules )
			{
				Set<Long> systemRoots = schedule.getSystemRoots();
				Set<Long> logicalRoots = schedule.getLogicalRoots();

				if ( ( folders.containsAll( systemRoots ) ) && ( folders.containsAll( logicalRoots ) ) )
				{
					LOG.info( "Schedule {} will be left with no territory assignment. Aborting deletion.", schedule.getName() );
					return false;
				}
			}

			for ( ScheduleEntity schedule : schedules )
			{
				Set<Long> systemRoots = schedule.getSystemRoots();
				Set<Long> logicalRoots = schedule.getLogicalRoots();

				systemRoots.removeAll( folders );
				logicalRoots.removeAll( folders );

				schedule.setSystemRoots( systemRoots );
				schedule.setLogicalRoots( logicalRoots );
			}
		}

		return true;
	}

	private CronTrigger createCronTrigger( String jobName, Integer dayOfWeek, Integer time, boolean isStart )
	{
		CronTrigger trigger = new CronTrigger();
		trigger.setName( jobName + "-" + dayOfWeek + "-" + time + "-" + isStart );
		trigger.setGroup( "SCHEDULE_GROUP" );
		trigger.setJobName( jobName );
		trigger.setJobGroup( "SCHEDULE_GROUP" );
		JobDataMap map = new JobDataMap();
		map.put( "isStart", isStart );
		trigger.setJobDataMap( map );

		String cronExpression;

		if ( time == 1440 )
		{
			cronExpression = String.format( "59 59 23 ? * %s", dayOfWeek.intValue() + 1 );
		}
		else
		{
			Integer hours = time / 60;
			Integer minutes = time % 60;
			cronExpression = String.format( "0 %s %s ? * %s", minutes, hours, dayOfWeek.intValue() + 1 );
		}
		try
		{
			trigger.setCronExpression( cronExpression );
		}
		catch ( ParseException e )
		{
			LOG.error( "Invalid Cron expression, Details: " + e.getMessage() );
		}
		return trigger;
	}

	public void setScheduleDAO( ScheduleDAO scheduleDAO )
	{
		this.scheduleDAO = scheduleDAO;
	}

	public void setUserService( UserService userService )
	{
		this.userService = userService;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setScheduler( Scheduler scheduler )
	{
		this.scheduler = scheduler;
	}

	public void setAppManager( AppManager appManager )
	{
		this.appManager = appManager;
	}

	public void setOsgiManager( OsgiManager osgiManager )
	{
		this.osgiManager = osgiManager;
	}

	public void setResourceTopologyService( ResourceTopologyServiceIF resourceTopologyService )
	{
		this.resourceTopologyService = resourceTopologyService;
	}
}

