package com.marchnetworks.alarm.service;

import com.marchnetworks.alarm.dao.AlarmEntryDAO;
import com.marchnetworks.alarm.dao.AlarmSourceDAO;
import com.marchnetworks.alarm.dao.TestAlarmEntryDAO;
import com.marchnetworks.alarm.data.AlarmEntryView;
import com.marchnetworks.alarm.model.AlarmEntryEntity;
import com.marchnetworks.alarm.model.AlarmSourceEntity;
import com.marchnetworks.alarm.model.AlarmSourceMBean;
import com.marchnetworks.command.common.alarm.data.AlarmSourceView;
import com.marchnetworks.command.common.alarm.data.AlarmState;
import com.marchnetworks.command.common.topology.ResourceAssociationType;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.AlarmSourceResource;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.events.DeviceAlarmEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlarmEventType;
import com.marchnetworks.management.instrumentation.model.CompositeDevice;
import com.marchnetworks.management.instrumentation.model.CompositeDeviceMBean;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class AlarmTestServiceImpl implements AlarmTestService
{
	private static final Logger LOG = LoggerFactory.getLogger( AlarmTestServiceImpl.class );
	private static final Random RANDOM = new Random();

	private static final AlarmService alarmServiceProxy = ( AlarmService ) ApplicationContextSupport.getBean( "alarmServiceProxy_internal" );
	private AlarmEntryDAO alarmEntryDAO;
	private AlarmService alarmService;
	private AlarmSourceDAO alarmSourceDAO;
	private DeviceService deviceService;
	private TestAlarmEntryDAO testAlarmEntryDAO;
	private ResourceTopologyServiceIF topologyService;

	private void alarmEventBenchmark( DeviceAlarmEvent alarmEvent )
	{
		alarmServiceProxy.processAlarmEvent( alarmEvent );
	}

	public void createAlarmSource( String deviceId, String deviceAlarmSourceId, String alarmTypeString, String alarmSourceName, boolean associateCameras )
	{
		AlarmSourceView alarmSource = new AlarmSourceView();
		alarmSource.setDeviceId( deviceId );
		alarmSource.setDeviceAlarmSourceId( deviceAlarmSourceId );
		alarmSource.setAlarmType( alarmTypeString );
		alarmSource.setName( alarmSourceName );
		alarmSource.setState( AlarmState.OFF );
		alarmSource.setAssociatedChannels( new String[0] );

		if ( associateCameras )
		{
			List<String> channels = deviceService.findChannelIdsFromDeviceAndChildren( deviceId );
			alarmSource.setAssociatedChannels( ( String[] ) channels.toArray( new String[0] ) );
		}
		alarmService.createAlarmSource( alarmSource, false );
	}

	public void createManyAlarmSources( int alarmSourceNum, boolean associateCameras )
	{
		List<CompositeDeviceMBean> devices = deviceService.getAllCompositeDevices();

		int deviceNum = devices.size();
		for ( int i = 0; i < alarmSourceNum; i++ )
		{
			AlarmSourceEntity alarmSourceEntity = new AlarmSourceEntity();
			alarmSourceEntity.setDeviceAlarmSourceID( "testSource" + i );
			String deviceId = ( ( CompositeDeviceMBean ) devices.get( i % deviceNum ) ).getDeviceId();
			alarmSourceEntity.setDeviceId( Long.valueOf( Long.parseLong( deviceId ) ) );
			alarmSourceEntity.setAlarmType( "user.motion" );
			alarmSourceEntity.setName( "Test Alarm Source " + i );
			alarmSourceEntity.setState( AlarmState.OFF );

			if ( associateCameras )
			{
				List<String> channels = deviceService.findChannelIdsFromDeviceAndChildren( deviceId );
				alarmSourceEntity.setAssociatedChannels( ( String[] ) channels.toArray( new String[0] ) );
			}
			alarmSourceDAO.create( alarmSourceEntity );

			AlarmSourceResource alarmSourceResource = new AlarmSourceResource();
			alarmSourceResource.setAlarmSourceId( alarmSourceEntity.getIdAsString() );

			Long parentResourceId = topologyService.getResourceIdByDeviceId( alarmSourceEntity.getDeviceIdAsString() );
			try
			{
				topologyService.createResource( alarmSourceResource, parentResourceId, ResourceAssociationType.ALARM_SOURCE.name() );
			}
			catch ( TopologyException ex )
			{
				LOG.error( "Error creating AlarmSourceResource for deviceId:" + alarmSourceEntity.getDeviceIdAsString(), ex );
			}
		}
	}

	private AlarmEntryEntity createRandomAlarmEntry( List<AlarmSourceEntity> sources )
	{
		AlarmEntryEntity entity = new AlarmEntryEntity();
		int chosenSource = RANDOM.nextInt( sources.size() );
		AlarmSourceEntity source = ( AlarmSourceEntity ) sources.get( chosenSource );
		long now = System.currentTimeMillis();
		entity.setAlarmSource( source );
		entity.setFirstInstanceTime( now * 1000L );
		entity.setCount( RANDOM.nextInt( 100 ) + 1 );
		entity.setDeviceAlarmEntryID( source.getDeviceId().toString() + "-" + source.getName() + "-" + now / 1000L );
		entity.setClosedTime( System.currentTimeMillis() );
		entity.setClosedByUser( "DeviceLoadTest" );
		return entity;
	}

	public Map<Long, List<AlarmSourceEntity>> createSimulatedAlarmSources( List<CompositeDevice> rootDevices, int numAlarms )
	{
		String[] types = {"user.motion", "physical", "network", "analytic"};
		Random r = new Random();

		Map<Long, List<AlarmSourceEntity>> alarmSources = new HashMap( rootDevices.size() );
		for ( CompositeDevice rootDevice : rootDevices )
		{
			Long deviceId = Long.valueOf( Long.parseLong( rootDevice.getDeviceId() ) );
			List<AlarmSourceEntity> deviceAlarms = new ArrayList( numAlarms );
			for ( int j = 1; j <= numAlarms; j++ )
			{
				AlarmSourceEntity alarmSourceEntity = new AlarmSourceEntity();
				alarmSourceEntity.setDeviceAlarmSourceID( "alarm-" + j );
				alarmSourceEntity.setDeviceId( deviceId );
				alarmSourceEntity.setAlarmType( types[r.nextInt( 4 )] );
				alarmSourceEntity.setName( "Alarm-" + j );
				alarmSourceEntity.setState( AlarmState.OFF );

				alarmSourceDAO.create( alarmSourceEntity );

				deviceAlarms.add( alarmSourceEntity );
			}
			alarmSources.put( deviceId, deviceAlarms );
		}
		return alarmSources;
	}

	public void deleteAlarmEntries()
	{
		alarmEntryDAO.deleteAll();
	}

	public void deleteTestAlarmSourcesAndEntries()
	{
		List<AlarmEntryEntity> entries = alarmEntryDAO.findAll();

		for ( AlarmEntryEntity alarmEntryEntity : entries )
		{
			if ( alarmEntryEntity.getDeviceAlarmEntryID().startsWith( "testEntry" ) )
			{
				alarmEntryDAO.delete( alarmEntryEntity );
			}
		}

		List<AlarmSourceEntity> sources = alarmSourceDAO.findAll();

		for ( AlarmSourceEntity alarmSourceEntity : sources )
		{
			if ( alarmSourceEntity.getDeviceAlarmSourceID().startsWith( "testSource" ) )
			{
				Long resourceId = topologyService.getAlarmSourceResourceId( alarmSourceEntity.getId() );

				if ( resourceId != null )
				{
					try
					{
						topologyService.removeResource( resourceId );
					}
					catch ( TopologyException ex )
					{
						LOG.error( "Error removing AlarmSourceResource id:" + resourceId, ex );
					}
				}

				alarmSourceDAO.delete( alarmSourceEntity );
			}
		}
	}

	public AlarmEntryView findUnclosedAlarmEntry( String alarmSourceId )
	{
		AlarmSourceEntity alarmSource = ( AlarmSourceEntity ) alarmSourceDAO.findById( Long.valueOf( Long.parseLong( alarmSourceId ) ) );
		AlarmEntryEntity alarmEntry = alarmEntryDAO.findUnclosedByAlarmSource( alarmSource );
		if ( alarmEntry != null )
		{
			return alarmEntry.toDataObject();
		}
		return null;
	}

	public void generateAlarmEntries( int amount )
	{
		long start = System.currentTimeMillis();
		List<String> fields = Arrays.asList( new String[] {"id", "name", "deviceId"} );
		List<AlarmSourceEntity> sources = alarmSourceDAO.findAllDehydrated( fields );

		if ( sources.isEmpty() )
		{
			return;
		}

		int batchSize = 100;
		List<AlarmEntryEntity> entries = new ArrayList();

		for ( int i = 0; i < amount; i++ )
		{
			AlarmEntryEntity entity = createRandomAlarmEntry( sources );
			entries.add( entity );
		}

		testAlarmEntryDAO.batchInsert( entries, batchSize );

		long end = System.currentTimeMillis();

		LOG.info( "Alarm entry creation of " + amount + " alarms took " + ( end - start ) + " ms." );
	}

	public List<AlarmEntryView> getAlarmEntries()
	{
		List<AlarmEntryView> results = new ArrayList();
		List<AlarmEntryEntity> entries = alarmEntryDAO.findAllDetached();

		if ( entries != null )
		{
			for ( AlarmEntryEntity alarmEntryEntity : entries )
			{
				results.add( alarmEntryEntity.toDataObject() );
			}
		}
		return results;
	}

	public int getAlarmEntriesCount()
	{
		return alarmEntryDAO.getRowCount().intValue();
	}

	public AlarmEntryView getAlarmEntry( String alarmEntryId )
	{
		AlarmEntryEntity alarmEntry = ( AlarmEntryEntity ) alarmEntryDAO.findById( Long.valueOf( Long.parseLong( alarmEntryId ) ) );
		if ( alarmEntry != null )
		{
			return alarmEntry.toDataObject();
		}
		return null;
	}

	private AlarmSourceMBean getAlarmSourceBenchmark( Long deviceId, String deviceAlarmSourceId )
	{
		AlarmTestService alarmTestServiceProxy = ( AlarmTestService ) ApplicationContextSupport.getBean( "alarmTestServiceProxy" );
		return alarmTestServiceProxy.getAlarmSourceByDeviceAndDeviceAlarmSourceId( deviceId, deviceAlarmSourceId );
	}

	private AlarmSourceMBean getAlarmSourceBenchmark( Long alarmSourceId )
	{
		return alarmServiceProxy.getAlarmSource( alarmSourceId.toString() );
	}

	public AlarmSourceMBean getAlarmSourceByDeviceAndDeviceAlarmSourceId( Long deviceId, String deviceAlarmSourceId )
	{
		return alarmSourceDAO.findByDeviceIdAndDeviceAlarmSourceId( deviceId, deviceAlarmSourceId );
	}

	public AlarmSourceView getAlarmSourceData( String alarmSourceId )
	{
		if ( alarmSourceId == null )
		{
			return null;
		}
		AlarmSourceEntity alarmSource = ( AlarmSourceEntity ) alarmSourceDAO.findById( Long.valueOf( Long.parseLong( alarmSourceId ) ) );
		if ( alarmSource != null )
		{
			return alarmSource.toDataObject();
		}
		return null;
	}

	public List<AlarmSourceView> getAlarmSources()
	{
		List<AlarmSourceEntity> sources = alarmSourceDAO.findAllDetached();

		if ( sources != null )
		{
			List<AlarmSourceView> results = new ArrayList();
			for ( AlarmSourceEntity alarmSourceEntity : sources )
			{
				results.add( alarmSourceEntity.toDataObject() );
			}
			return results;
		}
		return new ArrayList();
	}

	public List<AlarmSourceView> getAlarmSourcesIncludeDeleted()
	{
		List<AlarmSourceView> results = new ArrayList();
		List<AlarmSourceEntity> sources = alarmSourceDAO.findAllIncludeDeleted();

		if ( sources != null )
		{
			for ( AlarmSourceEntity alarmSourceEntity : sources )
			{
				results.add( alarmSourceEntity.toDataObject() );
			}
		}
		return results;
	}

	public Long getLastAlarmSourceId()
	{
		return alarmSourceDAO.getLastId();
	}

	private Long getLastAlarmSourceIdBenchmark()
	{
		AlarmTestService alarmTestServiceProxy = ( AlarmTestService ) ApplicationContextSupport.getBean( "alarmTestServiceProxy" );
		return alarmTestServiceProxy.getLastAlarmSourceId();
	}

	public List<AlarmEntryView> getOpenAlarmEntries()
	{
		List<AlarmEntryView> results = new ArrayList();
		List<AlarmEntryEntity> entries = alarmEntryDAO.findAllOpen();

		if ( entries != null )
		{
			for ( AlarmEntryEntity alarmEntryEntity : entries )
			{
				results.add( alarmEntryEntity.toDataObject() );
			}
		}
		return results;
	}

	public Map<String, Long> runBenchmark()
	{
		Map<String, Long> results = new TreeMap();

		Long id = getLastAlarmSourceIdBenchmark();
		long start = System.currentTimeMillis();
		AlarmSourceMBean alarmSource = getAlarmSourceBenchmark( id );
		results.put( "Get Alarm Source by Id", Long.valueOf( System.currentTimeMillis() - start ) );

		start = System.currentTimeMillis();
		topologyService.getAlarmSourceResourceId( id );
		results.put( "Get Alarm resource by alarm source id", Long.valueOf( System.currentTimeMillis() - start ) );

		if ( alarmSource.getDeviceId() != null )
		{
			start = System.currentTimeMillis();
			getAlarmSourceBenchmark( alarmSource.getDeviceId(), alarmSource.getDeviceAlarmSourceID() );
			results.put( "Get Alarm Source by device id and device source id", Long.valueOf( System.currentTimeMillis() - start ) );

			AlarmEntryEntity alarmEntry = createRandomAlarmEntry( Collections.singletonList( ( AlarmSourceEntity ) alarmSource ) );

			Pair pairCount = new Pair( "count", String.valueOf( alarmEntry.getCount() ) );
			Pair pairFirstInstance = new Pair( "first", String.valueOf( alarmEntry.getFirstInstanceTime() * 1000L ) );
			Pair pairLastInstance = new Pair( "last", String.valueOf( System.currentTimeMillis() * 1000L ) );
			DeviceAlarmEvent alarmEvent = new DeviceAlarmEvent( DeviceAlarmEventType.ALARM_ENTRY, alarmSource.getDeviceId().toString(), alarmEntry.getFirstInstanceTime(), alarmSource.getDeviceAlarmSourceID(), alarmEntry.getDeviceAlarmEntryID(), new Pair[] {pairCount, pairFirstInstance, pairLastInstance} );

			DeviceAlarmEvent alarmStateEvent = new DeviceAlarmEvent( DeviceAlarmEventType.ALARM_STATE, alarmSource.getDeviceId().toString(), System.currentTimeMillis() * 1000L, alarmSource.getDeviceAlarmSourceID(), AlarmState.ON.toString(), null );

			start = System.currentTimeMillis();
			alarmEventBenchmark( alarmStateEvent );
			alarmEventBenchmark( alarmEvent );
			results.put( "Processing of alarm.entry+alarm.state events", Long.valueOf( System.currentTimeMillis() - start ) );
		}

		return results;
	}

	public void setAlarmEntryDAO( AlarmEntryDAO alarmEntryDAO )
	{
		this.alarmEntryDAO = alarmEntryDAO;
	}

	public void setAlarmService( AlarmService alarmService )
	{
		this.alarmService = alarmService;
	}

	public void setAlarmSourceDAO( AlarmSourceDAO alarmSourceDAO )
	{
		this.alarmSourceDAO = alarmSourceDAO;
	}

	public void setDeviceService( DeviceService deviceService )
	{
		this.deviceService = deviceService;
	}

	public void setTestAlarmEntryDAO( TestAlarmEntryDAO testAlarmEntryDAO )
	{
		this.testAlarmEntryDAO = testAlarmEntryDAO;
	}

	public void setTopologyService( ResourceTopologyServiceIF topologyService )
	{
		this.topologyService = topologyService;
	}
}
