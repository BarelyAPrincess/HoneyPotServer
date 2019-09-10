package com.marchnetworks.health.service;

import com.marchnetworks.command.api.alert.AlertCategoryEnum;
import com.marchnetworks.command.api.alert.AlertDefinitionEnum;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.types.AlertSeverityEnum;
import com.marchnetworks.common.types.AlertUserStateEnum;
import com.marchnetworks.health.alerts.DeviceAlertEntity;
import com.marchnetworks.health.dao.DeviceAlertDAO;
import com.marchnetworks.health.dao.TestDeviceAlertDAO;
import com.marchnetworks.health.data.AlertData;
import com.marchnetworks.health.data.DeviceAlertData;
import com.marchnetworks.health.input.DeviceAlertInput;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.management.topology.ResourceTopologyTestService;
import com.marchnetworks.server.event.health.HealthFault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AlertTestServiceImpl implements AlertTestService
{
	private static final Logger LOG = LoggerFactory.getLogger( AlertTestServiceImpl.class );
	private static final Random RANDOM = new Random();

	private static final String ALERT_SOURCE_DESCRIPTION = "Failed";
	private static final HealthServiceIF healthServiceProxy = ( HealthServiceIF ) ApplicationContextSupport.getBean( "healthServiceProxy_internal" );

	private TestDeviceAlertDAO testDeviceAlertDAO;
	private DeviceAlertDAO deviceAlertDAO;
	private ResourceTopologyServiceIF topologyService;
	private ResourceTopologyTestService topologyTestService;

	public void generateAlerts( int amount )
	{
		List<DeviceAlertEntity> alerts = new ArrayList();

		int batchSize = 100;

		List<DeviceResource> devices = topologyService.getAllDeviceResources();

		if ( devices.isEmpty() )
		{
			return;
		}

		long start = System.currentTimeMillis();

		for ( int i = 0; i < amount; i++ )
		{
			DeviceResource device = ( DeviceResource ) devices.get( RANDOM.nextInt( devices.size() ) );
			AlertCategoryEnum category = getRandomAlertCategory();
			AlertDefinitionEnum alertType = getRandomAlertDefinition();

			DeviceAlertEntity entity = new DeviceAlertEntity( device.getDeviceId(), getDeviceAlertGuid( device ), alertType.getPath(), category, getRandomDeviceSource( device ), "Failed", System.currentTimeMillis(), System.currentTimeMillis(), true, null, 1, 1 );

			entity.setSeverity( getRandomAlertSeverity() );
			entity.setUserState( AlertUserStateEnum.CLOSED );
			entity.setAlertResolvedTime( System.currentTimeMillis() );
			entity.setLastUserStateChangedTime( System.currentTimeMillis() );
			alerts.add( entity );
		}

		testDeviceAlertDAO.batchInsert( alerts, batchSize );

		long end = System.currentTimeMillis();

		LOG.info( "Alert creation of " + amount + " alerts with batch size of " + batchSize + " took " + ( end - start ) + " ms." );
	}

	public void deleteAlerts()
	{
		testDeviceAlertDAO.deleteAll();
	}

	public Long getLastDeviceAlertId()
	{
		return deviceAlertDAO.getLastId();
	}

	public void findAlertByDeviceAndDeviceAlertId( String deviceId, String deviceAlertId )
	{
		deviceAlertDAO.findAlert( deviceId, deviceAlertId );
	}

	public int getDeviceAlertCount()
	{
		return deviceAlertDAO.getRowCount().intValue();
	}

	public Map<String, Long> runBenchmark()
	{
		Map<String, Long> results = new LinkedHashMap();
		Long id = getLastDeviceAlertIdBenchmark();
		long start = 0L;
		if ( id != null )
		{
			start = System.currentTimeMillis();
			AlertData alert = findDeviceAlertById( id );
			results.put( "Find last alert by id ", Long.valueOf( System.currentTimeMillis() - start ) );

			if ( ( alert instanceof DeviceAlertData ) )
			{
				DeviceAlertData deviceAlert = ( DeviceAlertData ) alert;

				start = System.currentTimeMillis();
				findDeviceAlertByDeviceAndDeviceAlertId( deviceAlert.getDeviceId(), deviceAlert.getDeviceAlertId() );
				results.put( "Find last alert by device id and device alert id", Long.valueOf( System.currentTimeMillis() - start ) );
			}
		}

		DeviceResource lastDeviceResource = topologyTestService.getLastDeviceResource();
		String channelId = topologyTestService.getFirstChannelIdFromDevice( lastDeviceResource.getId() );
		start = System.currentTimeMillis();
		processChannelRemoved( lastDeviceResource.getDeviceId(), channelId );
		results.put( "Alert processing of channel removed", Long.valueOf( System.currentTimeMillis() - start ) );

		DeviceAlertInput alertInput = new DeviceAlertInput( lastDeviceResource.getDeviceId(), getDeviceAlertGuid( lastDeviceResource ), 1, getRandomAlertDefinition(), getRandomDeviceSource( lastDeviceResource ), System.currentTimeMillis(), System.currentTimeMillis(), 0L, null, "Failed", true, 1, 1 );

		start = System.currentTimeMillis();
		processNewDeviceAlert( alertInput );
		results.put( "Processing of new device alert", Long.valueOf( System.currentTimeMillis() - start ) );

		return results;
	}

	private AlertData findDeviceAlertById( Long id )
	{
		try
		{
			return healthServiceProxy.getAlertById( id.longValue() );
		}
		catch ( HealthFault localHealthFault )
		{
		}
		return null;
	}

	private Long getLastDeviceAlertIdBenchmark()
	{
		AlertTestService alertTestServiceProxy = ( AlertTestService ) ApplicationContextSupport.getBean( "alertTestServiceProxy" );
		return alertTestServiceProxy.getLastDeviceAlertId();
	}

	private void findDeviceAlertByDeviceAndDeviceAlertId( String deviceId, String deviceAlertId )
	{
		AlertTestService alertTestServiceProxy = ( AlertTestService ) ApplicationContextSupport.getBean( "alertTestServiceProxy" );
		alertTestServiceProxy.findAlertByDeviceAndDeviceAlertId( deviceId, deviceAlertId );
	}

	private void processChannelRemoved( String deviceId, String channelId )
	{
		healthServiceProxy.processDeviceChannelRemoved( deviceId, channelId );
	}

	private void processNewDeviceAlert( DeviceAlertInput alertInput )
	{
		healthServiceProxy.processHealthAlert( alertInput );
	}

	private AlertCategoryEnum getRandomAlertCategory()
	{
		return AlertCategoryEnum.values()[RANDOM.nextInt( AlertCategoryEnum.values().length )];
	}

	private String getDeviceAlertGuid( DeviceResource deviceResource )
	{
		String channelId = getRandomDeviceSource( deviceResource );
		return deviceResource.getDeviceId() + "-" + channelId + "-" + System.currentTimeMillis() / 1000L;
	}

	private String getRandomDeviceSource( DeviceResource deviceResource )
	{
		try
		{
			List<String> deviceChannels = topologyService.getChannelIdsFromDevice( deviceResource.getId() );
			return ( String ) deviceChannels.get( RANDOM.nextInt( deviceChannels.size() ) );
		}
		catch ( TopologyException e )
		{
			LOG.warn( "Error when looking up topology. Details:{}", e.getMessage() );
		}
		return null;
	}

	private AlertSeverityEnum getRandomAlertSeverity()
	{
		return AlertSeverityEnum.values()[RANDOM.nextInt( AlertSeverityEnum.values().length )];
	}

	private AlertDefinitionEnum getRandomAlertDefinition()
	{
		return AlertDefinitionEnum.values()[RANDOM.nextInt( AlertDefinitionEnum.values().length )];
	}

	public void setTestDeviceAlertDAO( TestDeviceAlertDAO testDeviceAlertDAO )
	{
		this.testDeviceAlertDAO = testDeviceAlertDAO;
	}

	public void setDeviceAlertDAO( DeviceAlertDAO deviceAlertDAO )
	{
		this.deviceAlertDAO = deviceAlertDAO;
	}

	public void setTopologyTestService( ResourceTopologyTestService topologyTestService )
	{
		this.topologyTestService = topologyTestService;
	}

	public void setTopologyService( ResourceTopologyServiceIF topologyService )
	{
		this.topologyService = topologyService;
	}
}
