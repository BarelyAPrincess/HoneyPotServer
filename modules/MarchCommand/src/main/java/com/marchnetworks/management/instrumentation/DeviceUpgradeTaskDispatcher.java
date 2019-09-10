package com.marchnetworks.management.instrumentation;

import com.marchnetworks.command.api.rest.DeviceManagementConstants;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.data.ChannelState;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.configuration.ConfigSettings;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.common.types.DeviceExceptionTypes;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceConfigurationEvent;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.ChannelConnectionStateEvent;
import com.marchnetworks.management.instrumentation.events.DeviceConfigurationEventType;
import com.marchnetworks.management.instrumentation.events.GenericDeviceConfigurationEvent;
import com.marchnetworks.management.instrumentation.events.GenericDeviceUpgradeEvent;
import com.marchnetworks.management.instrumentation.model.ChannelMBean;
import com.marchnetworks.management.instrumentation.model.CompositeDevice;
import com.marchnetworks.management.instrumentation.model.Device;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.server.communications.transport.datamodel.ChannelDetails;
import com.marchnetworks.server.communications.transport.datamodel.DeviceDetails;
import com.marchnetworks.server.event.BandwidthSettingsChangedEvent;
import com.marchnetworks.server.event.EventListener;
import com.marchnetworks.server.event.EventRegistry;
import com.marchnetworks.shared.config.CommonConfiguration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceUpgradeTaskDispatcher implements EventListener, DeviceEventMessageInterceptor
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceUpgradeTaskDispatcher.class );
	private CommonConfiguration commonConfig;
	private DeviceAdaptorFactory deviceAdaptorFactory;
	private EventRegistry eventRegistry;
	private TaskScheduler taskScheduler;

	public DeviceUpgradeTaskDispatcher()
	{
		localTaskId = new AtomicLong( 1L );
		activeTasksCounter = new AtomicInteger( 0 );

		requestQueue = new LinkedBlockingQueue();
	}

	public void process( Event event )
	{
		if ( ( event instanceof BandwidthSettingsChangedEvent ) )
		{
			BandwidthSettingsChangedEvent bscEvent = ( BandwidthSettingsChangedEvent ) event;
			int delta = bscEvent.getSettings().getMaxSimultaneousUpdates() - maxSimultaneousRequests;
			maxSimultaneousRequests = bscEvent.getSettings().getMaxSimultaneousUpdates();

			for ( int i = 0; i < delta; i++ )
			{
				processQueue();
			}
			LOG.debug( "Updated maxSimultaneousRequests to {}", Integer.valueOf( maxSimultaneousRequests ) );
		}
	}

	public boolean doInterceptDeviceEvent( AbstractDeviceEvent event )
	{
		boolean result = true;
		if ( ( event instanceof AbstractDeviceConfigurationEvent ) )
		{
			final AbstractDeviceConfigurationEvent deviceConfigurationEvent = ( AbstractDeviceConfigurationEvent ) event;
			LOG.info( "Handling {} event for device {} and task {}.", new Object[] {deviceConfigurationEvent.getDeviceEventType(), deviceConfigurationEvent.getDeviceId(), deviceConfigurationEvent.getTaskId()} );

			if ( ( deviceConfigurationEvent.getDeviceEventType().equals( DeviceConfigurationEventType.CONFIG_APPLIED ) ) || ( deviceConfigurationEvent.getDeviceEventType().equals( DeviceConfigurationEventType.CONFIG_APPLIED_LASTCONFIG ) ) )
			{

				Long delaySeconds = Long.valueOf( 15L );
				if ( resourceTopologyService.getDeviceResourceByDeviceId( deviceConfigurationEvent.getDeviceId() ).getDeviceView().isR5() )
				{
					delaySeconds = Long.valueOf( 2L );
				}
				taskScheduler.schedule( new Runnable()
				{

					public void run()
					{
						eventRegistry.send( new GenericDeviceConfigurationEvent( deviceConfigurationEvent.getDeviceId(), DeviceConfigurationEventType.CONFIG_APPLIED_INTERNAL, deviceConfigurationEvent.getTaskId(), null ) );
					}
				}, delaySeconds.longValue(), TimeUnit.SECONDS );
			}

			updateRequest( deviceConfigurationEvent.getDeviceId(), deviceConfigurationEvent.getTaskId() );
		}
		else if ( ( event instanceof ChannelConnectionStateEvent ) )
		{
			ChannelConnectionStateEvent stateEvent = ( ChannelConnectionStateEvent ) event;
			if ( stateEvent.getConnectionState().equals( ChannelState.ONLINE ) )
			{
				LOG.debug( "Handling {} event for device {}.", new Object[] {event, event.getDeviceId()} );
				Request request = getRequest( event.getDeviceId() );
				if ( request == null )
				{
					LOG.debug( "No request found for deviceId {}", event.getDeviceId() );
					return true;
				}

				if ( request.getChildDevice() == null )
				{
					LOG.debug( "ChannelConnectionState is for a channel that hasn't been added yet. Device id {}, Channel id:{}", stateEvent.getDeviceId(), stateEvent.getChannelId() );
					return true;
				}
				try
				{
					RemoteDeviceOperations adaptor = deviceAdaptorFactory.getDeviceAdaptor( resourceTopologyService.getDeviceResourceByDeviceId( request.getDevice().getDeviceId() ) );
					ChannelDetails channelDetails = adaptor.retrieveChannelDetails( stateEvent.getChannelId() );

					if ( channelDetails.getIpDevice() == null )
					{
						return true;
					}

					if ( ( channelDetails.getIpDevice() != null ) && ( channelDetails.getIpDevice().getSwVersion().equals( request.getVersion() ) ) )
					{
						eventRegistry.send( new GenericDeviceUpgradeEvent( request.getDeviceId(), DeviceConfigurationEventType.UPGRADE_ACCEPTED, request.getRemoteTaskId(), null ) );
					}
					else
					{
						eventRegistry.send( new GenericDeviceUpgradeEvent( request.getDeviceId(), DeviceConfigurationEventType.UPGRADE_FAILED, request.getRemoteTaskId(), "Device not in the target version after reconnection." ) );
					}
				}
				catch ( DeviceException ex )
				{
					eventRegistry.send( new GenericDeviceUpgradeEvent( request.getDeviceId(), DeviceConfigurationEventType.UPGRADE_FAILED, request.getRemoteTaskId(), "Failed to verify device version." ) );
				}
			}
		}
		return result;
	}

	public String getListenerName()
	{
		return DeviceUpgradeTaskDispatcher.class.getSimpleName();
	}

	private ResourceTopologyServiceIF resourceTopologyService;

	private AtomicLong localTaskId;

	private AtomicInteger activeTasksCounter;

	private int maxSimultaneousRequests;

	private Queue<Request> requestQueue;

	public String upgrade( CompositeDevice device, Device childDevice, String version, String fileName, InputStream fileContent, String key ) throws DeviceException
	{
		Request request = new Request( RequestType.UPGRADE, device, childDevice, version, fileName, fileContent, key );
		addRequest( request );
		return request.getLocalTaskId();
	}

	public String upgrade( CompositeDevice device, Map<String, String> channelIDs, String version, String fileName, InputStream fileContent, String key ) throws DeviceException
	{
		Request request = new Request( RequestType.UPGRADE, device, channelIDs, version, fileName, fileContent, key );
		addRequest( request );
		return request.getLocalTaskId();
	}

	public String configure( CompositeDevice device, Device childDevice, byte[] configuration, String configSnapshotID )
	{
		if ( isConfigurationRequestExisting( device, childDevice, configSnapshotID ) )
		{
			LOG.warn( "Configuration request for device {} with configurationSnapShotID {} is already in the Queue, Ingore it.", device.getDeviceId(), configSnapshotID );
			return null;
		}
		Request request = new Request( RequestType.CONFIG_APPLY, device, childDevice, configuration, configSnapshotID );
		addRequest( request );
		LOG.info( "Add Configuration Request for device: " + device.getDeviceId() + " configurationSnapShotID: " + configSnapshotID + " in the Queue and TaskID is: " + request.getLocalTaskId() );

		return request.getLocalTaskId();
	}

	private boolean isConfigurationRequestExisting( CompositeDevice device, Device childDevice, String configSnapshotID )
	{
		if ( requestQueue.size() > 0 )
		{
			Iterator<Request> iterator = requestQueue.iterator();
			while ( iterator.hasNext() )
			{
				Request request = ( Request ) iterator.next();
				if ( ( request.getType().equals( RequestType.CONFIG_APPLY ) ) && ( ( ( childDevice == null ) && ( request.getDeviceId().equals( device.getDeviceId() ) ) && ( request.getConfigSnapshotID().equals( configSnapshotID ) ) ) || ( ( childDevice != null ) && ( request.getDeviceId().equals( childDevice.getDeviceId() ) ) && ( request.getConfigSnapshotID().equals( configSnapshotID ) ) ) ) )
				{

					return true;
				}
			}
		}

		return false;
	}

	private synchronized void processQueue()
	{
		LOG.debug( "Current activeTasks counter = {} , maxSimultaneousRequests {}", Integer.valueOf( activeTasksCounter.get() ), Integer.valueOf( maxSimultaneousRequests ) );
		if ( activeTasksCounter.get() < maxSimultaneousRequests )
		{
			int counter = 0;
			while ( counter < requestQueue.size() )
			{
				Request request = ( Request ) requestQueue.poll();
				if ( ( request != null ) && ( !request.hasStarted() ) )
				{
					LOG.debug( "Processing request queue for device {}.", request.getDeviceId() );
					activeTasksCounter.incrementAndGet();
					taskScheduler.executeNow( new MassManagementTask( request ) );
					break;
				}

				requestQueue.add( request );
				counter++;
			}
		}
		else
		{
			LOG.debug( "Maximum simultaneous mass management tasks threshold crossed. Queuing task..." );
		}
	}

	private void addRequest( Request request )
	{
		requestQueue.add( request );
		LOG.info( "Enqueued request of type {} for device {}. Current queue size = {}", new Object[] {request.getType().toString(), request.getDeviceId(), Integer.valueOf( requestQueue.size() )} );
		processQueue();
	}

	private void updateRequest( String deviceId, String taskId )
	{
		if ( ( CommonAppUtils.isNullOrEmptyString( deviceId ) ) || ( CommonAppUtils.isNullOrEmptyString( taskId ) ) )
		{
			return;
		}

		Request request = getRequestByTaskId( taskId );
		if ( request == null )
		{
			LOG.warn( "No current task waiting for response for device {} with taskId {}.", deviceId, taskId );
			return;
		}

		if ( ( request.getChannelIds() != null ) && ( request.getChannelIds().size() != 0 ) )
		{
			if ( request.getChannelIds().containsKey( deviceId ) )
			{
				LOG.info( "**** removing child device {} from request {}.", deviceId, taskId );
				request.getChannelIds().remove( deviceId );
				ScheduledFuture<?> timer = request.getTimeoutTask();
				taskScheduler.cancelSchedule( timer );
				timer = null;
				if ( request.getChannelIds().size() > 0 )
				{
					timer = taskScheduler.schedule( new RequestTimeoutTask( request ), getTimeoutForRequest( request ), TimeUnit.SECONDS );
					request.setTimeoutTask( timer );
				}
				else
				{
					endRequest( request );
				}
			}
		}
		else
		{
			endRequest( request );
		}
	}

	private void endRequest( Request request )
	{
		if ( ( request != null ) && ( request.hasStarted() ) )
		{
			requestQueue.remove( request );
			LOG.info( "Removed TaskId {} from deviceId {}", request.getRemoteTaskId(), request.getDeviceId() );

			taskScheduler.cancelSchedule( request.getTimeoutTask() );
		}
		processQueue();
	}

	private Request getRequestByTaskId( String remoteTaskId )
	{
		if ( requestQueue.size() > 0 )
		{
			Iterator<Request> iterator = requestQueue.iterator();
			while ( iterator.hasNext() )
			{
				Request request = ( Request ) iterator.next();
				if ( remoteTaskId.equals( request.getRemoteTaskId() ) )
				{
					return request;
				}
			}
		}
		return null;
	}

	private Request getRequest( String deviceId )
	{
		if ( requestQueue.size() > 0 )
		{
			Iterator<Request> iterator = requestQueue.iterator();
			while ( iterator.hasNext() )
			{
				Request request = ( Request ) iterator.next();
				if ( request.getDeviceId().equals( deviceId ) )
				{
					return request;
				}
			}
		}
		return null;
	}

	private boolean processRequest( Request request )
	{
		boolean result = false;
		LOG.debug( "Processing request for device {} and task {}.", new String[] {request.getDeviceId(), request.getLocalTaskId()} );
		try
		{
			if ( request.getType().equals( RequestType.UPGRADE ) )
			{
				processUpgradeRequest( request );
			}
			else
			{
				processConfigRequest( request );
			}
			result = true;
		}
		catch ( DeviceException ex )
		{
			LOG.warn( "Exception processing request for device {} and task {}: {}.", new String[] {request.getDeviceId(), request.getLocalTaskId(), ex.getMessage()} );
			if ( request.getType().equals( RequestType.UPGRADE ) )
			{
				sendUpgradeFailedEvent( request, ex.getMessage() );
			}
			else
			{
				String reason;

				if ( ex.isCommunicationError() )
				{
					reason = "Communication_Error";
				}
				else
				{
					reason = ex.getMessage();
				}
				eventRegistry.send( new GenericDeviceConfigurationEvent( request.getDeviceId(), DeviceConfigurationEventType.CONFIG_FAILED, request.getLocalTaskId(), reason ) );
			}
		}
		return result;
	}

	public String getChannelId( Device channelDevice )
	{
		String channelId = null;
		if ( channelDevice.getChannelMBeans() != null )
		{
			for ( ChannelMBean channel : channelDevice.getChannelMBeans().values() )
			{
				if ( !channel.getChannelState().equals( ChannelState.DISABLED ) )
				{
					channelId = channel.getChannelId();
					break;
				}
			}
		}

		return channelId;
	}

	private void processUpgradeRequest( Request request ) throws DeviceException
	{
		String taskId = null;
		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) deviceAdaptorFactory.getDeviceAdaptor( resourceTopologyService.getDeviceResourceByDeviceId( request.getDevice().getDeviceId() ) );

		if ( request.getChannelIds() != null )
		{
			if ( request.getChannelIds().isEmpty() )
			{
				throw new DeviceException( "No enabled channel could be found under the child device.", DeviceExceptionTypes.DEVICE_NOT_FOUND );
			}
			List<String> channelIds = new ArrayList( request.getChannelIds().values() );
			taskId = adaptor.upgrade( channelIds, request.getFileName(), request.getFileContent(), request.getUpgradeKey() );
		}
		else if ( request.getChildDevice() != null )
		{
			String channelId = getChannelId( request.getChildDevice() );
			if ( channelId == null )
			{
				throw new DeviceException( "No enabled channel could be found under the child device.", DeviceExceptionTypes.DEVICE_NOT_FOUND );
			}

			taskId = adaptor.upgrade( channelId, request.getFileName(), request.getFileContent(), request.getUpgradeKey() );
		}
		else
		{
			taskId = adaptor.upgrade( request.getFileName(), request.getFileContent(), request.getUpgradeKey() );

			eventRegistry.send( new GenericDeviceUpgradeEvent( request.getDeviceId(), DeviceConfigurationEventType.UPGRADE_WAITING_ACCEPT, taskId, null ) );
		}
		request.setRemoteTaskId( taskId );
		LOG.info( "Received taskId {} from DeviceId {}", request.getRemoteTaskId(), request.getDeviceId() );
	}

	private void processConfigRequest( Request request ) throws DeviceException
	{
		String taskId = null;
		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) deviceAdaptorFactory.getDeviceAdaptor( resourceTopologyService.getDeviceResourceByDeviceId( request.getDevice().getDeviceId() ) );
		if ( request.getChildDevice() != null )
		{
			String channelId = null;
			if ( request.getChildDevice().getChannelMBeans() != null )
			{
				for ( ChannelMBean channel : request.getChildDevice().getChannelMBeans().values() )
				{
					if ( !channel.getChannelState().equals( ChannelState.DISABLED ) )
					{
						channelId = channel.getChannelId();
						break;
					}
				}
			}
			if ( channelId == null )
			{
				throw new DeviceException( "No enabled channel could be found under the child device.", DeviceExceptionTypes.DEVICE_NOT_FOUND );
			}
			LOG.info( "Start to send Configuration to Channel device {}, trigger config_pending event ", request.getDeviceId() );
			eventRegistry.send( new GenericDeviceConfigurationEvent( request.getDeviceId(), DeviceConfigurationEventType.CONFIG_PENDING, null, null ) );
			taskId = adaptor.configure( channelId, request.getConfiguration() );
		}
		else
		{
			LOG.info( "Start to send Configuration to root device {}, trigger config_pending event ", request.getDeviceId() );
			eventRegistry.send( new GenericDeviceConfigurationEvent( request.getDeviceId(), DeviceConfigurationEventType.CONFIG_PENDING, null, null ) );
			taskId = adaptor.configure( request.getConfiguration() );
		}
		request.setRemoteTaskId( taskId );
		LOG.info( "Received Config taskId {} from DeviceId {}", request.getRemoteTaskId(), request.getDeviceId() );
	}

	private String getNextLocalTaskId()
	{
		return String.valueOf( localTaskId.getAndIncrement() );
	}

	private int getTimeoutForRequest( Request request )
	{
		if ( request.getType().equals( RequestType.CONFIG_APPLY ) )
		{
			if ( DeviceManagementConstants.isMobileDevice( request.getDevice().getFamily(), request.getDevice().getModel() ) )
			{
				return commonConfig.getIntProperty( ConfigProperty.CONFIG_APPLY_MOBILE_TIMEOUT, 172800 );
			}
			return commonConfig.getIntProperty( ConfigProperty.CONFIG_APPLY_TIMEOUT, 900 );
		}

		if ( DeviceManagementConstants.isMobileDevice( request.getDevice().getFamily(), request.getDevice().getModel() ) )
		{
			return commonConfig.getIntProperty( ConfigProperty.FIRMWARE_UPGRADE_MOBILE_TIMEOUT, 172800 );
		}
		return commonConfig.getIntProperty( ConfigProperty.FIRMWARE_UPGRADE_TIMEOUT, 900 );
	}

	private void sendUpgradeFailedEvent( Request request, String reason )
	{
		if ( request.getChannelIds() == null )
		{
			eventRegistry.send( new GenericDeviceUpgradeEvent( request.getDeviceId(), DeviceConfigurationEventType.UPGRADE_FAILED, request.getRemoteTaskId(), reason ) );
		}
		else
		{
			for ( String channelDeviceId : request.getChannelIds().keySet() )
			{
				eventRegistry.send( new GenericDeviceUpgradeEvent( channelDeviceId, DeviceConfigurationEventType.UPGRADE_FAILED, request.getRemoteTaskId(), reason ) );
			}
		}
	}

	private class MassManagementTask implements Runnable
	{
		private Request request;

		public MassManagementTask( Request request )
		{
			this.request = request;
		}

		public void run()
		{
			boolean result = false;
			try
			{
				request.setStartState();

				requestQueue.add( request );
				result = DeviceUpgradeTaskDispatcher.this.processRequest( request );
				if ( result )
				{
					ScheduledFuture<?> timer = taskScheduler.schedule( new RequestTimeoutTask( request ), DeviceUpgradeTaskDispatcher.this.getTimeoutForRequest( request ), TimeUnit.SECONDS );
					request.setTimeoutTask( timer );
				}
			}
			finally
			{
				activeTasksCounter.decrementAndGet();
				if ( !result )
				{
					DeviceUpgradeTaskDispatcher.this.endRequest( request );
				}
			}
		}
	}

	private class RequestTimeoutTask implements Runnable
	{
		private Request request;

		public RequestTimeoutTask( Request request )
		{
			this.request = request;
		}

		public void run()
		{
			DeviceUpgradeTaskDispatcher.LOG.info( "TaskId {} for deviceId {} timed out.", request.getRemoteTaskId(), request.getDeviceId() );
			DeviceUpgradeTaskDispatcher.this.endRequest( request );
			if ( request.getType().equals( RequestType.CONFIG_APPLY ) )
			{
				eventRegistry.send( new GenericDeviceConfigurationEvent( request.getDeviceId(), DeviceConfigurationEventType.CONFIG_FAILED, request.getRemoteTaskId(), "Task_Time_out" ) );
			}
			else
			{
				DeviceUpgradeTaskDispatcher.this.sendUpgradeFailedEvent( request, "Timed out." );
			}
		}
	}

	private class Request
	{
		private CompositeDevice device;
		private Device childDevice;
		private Map<String, String> channelIds = null;
		private String version;
		private String fileName;
		private InputStream fileContent;
		private RequestType type;
		private RequestState state;
		private String localTaskId;
		private String remoteTaskId;
		private ScheduledFuture<?> timeoutTask;
		private String upgradeKey;
		private byte[] configuration;
		private String configSnapshotID;

		public Request( RequestType type, CompositeDevice device, Device childDevice, String version, String fileName, InputStream fileContent, String key )
		{
			this.type = type;
			this.device = device;
			this.childDevice = childDevice;
			this.version = version;
			this.fileName = fileName;
			this.fileContent = fileContent;
			localTaskId = DeviceUpgradeTaskDispatcher.this.getNextLocalTaskId();
			upgradeKey = key;
			state = RequestState.QUEUED;
		}

		public Request( RequestType type, CompositeDevice device, Map<String, String> channelIds, String version, String fileName, InputStream fileContent, String key )
		{
			this.type = type;
			this.device = device;
			this.channelIds = channelIds;
			this.version = version;
			this.fileName = fileName;
			this.fileContent = fileContent;
			localTaskId = DeviceUpgradeTaskDispatcher.this.getNextLocalTaskId();
			upgradeKey = key;
			state = RequestState.QUEUED;
		}

		public Request( RequestType type, CompositeDevice device, Device childDevice, byte[] configContents, String configSnapshotID )
		{
			this.type = type;
			this.device = device;
			this.childDevice = childDevice;
			configuration = configContents;
			state = RequestState.QUEUED;
			localTaskId = DeviceUpgradeTaskDispatcher.this.getNextLocalTaskId();
			this.configSnapshotID = configSnapshotID;
		}

		public int hashCode()
		{
			int prime = 31;
			int result = 1;
			result = 31 * result + getOuterType().hashCode();
			result = 31 * result + ( localTaskId == null ? 0 : localTaskId.hashCode() );
			return result;
		}

		public boolean equals( Object obj )
		{
			if ( this == obj )
			{
				return true;
			}
			if ( obj == null )
			{
				return false;
			}
			if ( getClass() != obj.getClass() )
			{
				return false;
			}
			Request other = ( Request ) obj;
			if ( !getOuterType().equals( other.getOuterType() ) )
			{
				return false;
			}
			if ( localTaskId == null )
			{
				if ( localTaskId != null )
				{
					return false;
				}
			}
			else if ( !localTaskId.equals( localTaskId ) )
			{
				return false;
			}
			return true;
		}

		public String getLocalTaskId()
		{
			return localTaskId;
		}

		public String getDeviceId()
		{
			if ( childDevice != null )
			{
				return childDevice.getDeviceId();
			}
			return device.getDeviceId();
		}

		public CompositeDevice getDevice()
		{
			return device;
		}

		public Device getChildDevice()
		{
			return childDevice;
		}

		public Map<String, String> getChannelIds()
		{
			return channelIds;
		}

		public String getFileName()
		{
			return fileName;
		}

		public InputStream getFileContent()
		{
			return fileContent;
		}

		public String getRemoteTaskId()
		{
			return remoteTaskId;
		}

		public void setRemoteTaskId( String remoteTaskId )
		{
			this.remoteTaskId = remoteTaskId;
		}

		public String getVersion()
		{
			return version;
		}

		public String getUpgradeKey()
		{
			return upgradeKey;
		}

		public byte[] getConfiguration()
		{
			return configuration;
		}

		public ScheduledFuture<?> getTimeoutTask()
		{
			return timeoutTask;
		}

		public void setTimeoutTask( ScheduledFuture<?> timeoutTask )
		{
			this.timeoutTask = timeoutTask;
		}

		public RequestType getType()
		{
			return type;
		}

		public boolean hasStarted()
		{
			return state.equals( RequestState.STARTED );
		}

		public void setStartState()
		{
			state = RequestState.STARTED;
		}

		private DeviceUpgradeTaskDispatcher getOuterType()
		{
			return DeviceUpgradeTaskDispatcher.this;
		}

		public String getConfigSnapshotID()
		{
			return configSnapshotID;
		}
	}

	private static enum RequestType
	{
		UPGRADE,
		CONFIG_APPLY;

		private RequestType()
		{
		}
	}

	private static enum RequestState
	{
		QUEUED,
		STARTED;

		private RequestState()
		{
		}
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}

	public void setDeviceAdaptorFactory( DeviceAdaptorFactory deviceAdaptorFactory )
	{
		this.deviceAdaptorFactory = deviceAdaptorFactory;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setCommonConfig( CommonConfiguration commonConfig )
	{
		this.commonConfig = commonConfig;
	}

	public void setResourceTopologyService( ResourceTopologyServiceIF resourceTopologyService )
	{
		this.resourceTopologyService = resourceTopologyService;
	}
}

