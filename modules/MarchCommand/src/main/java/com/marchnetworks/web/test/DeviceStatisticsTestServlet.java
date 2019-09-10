package com.marchnetworks.web.test;

import com.marchnetworks.command.common.device.DeviceEventsEnum;
import com.marchnetworks.command.common.transport.data.GenericValue;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.common.event.StateCacheable;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.management.instrumentation.adaptation.DeviceEventHandlerScheduler;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.DeviceStatisticsListEvent;
import com.marchnetworks.management.instrumentation.events.DeviceStatisticsStateEvent;
import com.marchnetworks.management.instrumentation.model.ChannelMBean;
import com.marchnetworks.management.instrumentation.model.CompositeDeviceMBean;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet( name = "DeviceStatsTest", urlPatterns = {"/DeviceStatsTest"} )
public class DeviceStatisticsTestServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		doPost( request, response );
	}

	protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		DeviceRegistry deviceRegistry = ( DeviceRegistry ) ApplicationContextSupport.getBean( "deviceRegistryProxy" );
		DeviceEventHandlerScheduler deviceEventHandlerScheduler = ( DeviceEventHandlerScheduler ) ApplicationContextSupport.getBean( "deviceEventHandlerScheduler" );

		List<String> systemEventsList = new ArrayList<String>();
		Collections.addAll( systemEventsList, DeviceEventsEnum.SYSTEM_STREAMING.getPath(), DeviceEventsEnum.SYSTEM_RECORDING.getPath(), DeviceEventsEnum.SYSTEM_CONFIGURED.getPath(), DeviceEventsEnum.SYSTEM_BANDWIDTH_RECORDING.getPath(), DeviceEventsEnum.SYSTEM_BANDWIDTH_INCOMING_IP.getPath(), DeviceEventsEnum.SYSTEM_BANDWIDTH_OUTGOING.getPath(), DeviceEventsEnum.SYSTEM_CPULOAD.getPath(), DeviceEventsEnum.SYSTEM_MEMORYUSED.getPath(), DeviceEventsEnum.SYSTEM_CPULOAD_TOTAL.getPath(), DeviceEventsEnum.SYSTEM_MEMORYUSED_TOTAL.getPath(), DeviceEventsEnum.SYSTEM_MEMORY_TOTAL.getPath() );

		List<String> channelEventsList = new ArrayList<String>();
		Collections.addAll( channelEventsList, DeviceEventsEnum.CHANNEL_RECORDING.getPath(), DeviceEventsEnum.CHANNEL_STREAMING.getPath(), DeviceEventsEnum.CHANNEL_CONFIGURED.getPath(), DeviceEventsEnum.CHANNEL_BANDWIDH_INCOMING.getPath(), DeviceEventsEnum.CHANNEL_BANDWIDH_RECORDING.getPath() );

		List<String> stateEventsList = new ArrayList<String>();
		Collections.addAll( stateEventsList, DeviceEventsEnum.SYSTEM_STREAMING.getPath(), DeviceEventsEnum.SYSTEM_RECORDING.getPath(), DeviceEventsEnum.SYSTEM_CONFIGURED.getPath(), DeviceEventsEnum.CHANNEL_RECORDING.getPath(), DeviceEventsEnum.CHANNEL_STREAMING.getPath(), DeviceEventsEnum.CHANNEL_CONFIGURED.getPath() );

		List<String> stateValues = new ArrayList<String>();
		Collections.addAll( stateValues, "ok", "failed" );

		Collection<DeviceMBean> managedDevices = deviceRegistry.getAllRootDevices();
		for ( DeviceMBean deviceMBean : managedDevices )
		{
			List<StateCacheable> stats = new ArrayList<StateCacheable>();
			CompositeDeviceMBean rootDevice = ( CompositeDeviceMBean ) deviceRegistry.getDeviceEagerDetached( deviceMBean.getDeviceId() );
			List<ChannelMBean> allRootDeviceChannels = new ArrayList<ChannelMBean>();

			if ( ( rootDevice.getChannelMBeans() != null ) && ( rootDevice.getChannelMBeans().size() > 0 ) )
			{
				allRootDeviceChannels.addAll( rootDevice.getChannelMBeans().values() );
			}

			if ( ( rootDevice.getChildDeviceMBeans() != null ) && ( rootDevice.getChildDeviceMBeans().size() > 0 ) )
			{
				for ( DeviceMBean childDevice : rootDevice.getChildDeviceMBeans().values() )
				{
					allRootDeviceChannels.addAll( childDevice.getChannelMBeans().values() );
				}
			}

			ChannelMBean channel;

			if ( allRootDeviceChannels.size() > 0 )
			{
				for ( Iterator<ChannelMBean> i$ = allRootDeviceChannels.iterator(); i$.hasNext(); )
				{
					channel = i$.next();

					for ( String channelStatEvent : channelEventsList )
					{
						long randomValue = Math.round( Math.random() * 10000.0D );
						GenericValue value = new GenericValue();
						if ( stateEventsList.indexOf( channelStatEvent ) >= 0 )
						{
							Collections.shuffle( stateValues );
							value.setValue( ( String ) stateValues.get( 0 ) );
						}
						else
						{
							value.setValue( randomValue );
						}
						Pair[] details = {new Pair( "min", String.valueOf( randomValue ) ), new Pair( "max", String.valueOf( randomValue * 2L ) )};

						DeviceStatisticsStateEvent event = new DeviceStatisticsStateEvent( rootDevice.getDeviceId(), channel.getChannelId(), channelStatEvent, value, details, System.currentTimeMillis(), false );

						stats.add( event );
					}
				}
			}

			for ( String systemStatEvent : systemEventsList )
			{
				long randomValue = Math.round( Math.random() * 10000.0D );
				GenericValue value = new GenericValue();
				if ( stateEventsList.indexOf( systemStatEvent ) >= 0 )
				{
					Collections.shuffle( stateValues );
					value.setValue( ( String ) stateValues.get( 0 ) );
				}
				else
				{
					value.setValue( randomValue );
				}
				Pair[] details = {new Pair( "min", String.valueOf( randomValue ) ), new Pair( "max", String.valueOf( randomValue * 2L ) )};

				DeviceStatisticsStateEvent event = new DeviceStatisticsStateEvent( rootDevice.getDeviceId(), rootDevice.getDeviceId(), systemStatEvent, value, details, System.currentTimeMillis(), false );

				stats.add( event );
			}
			AbstractDeviceEvent statisticsList = new DeviceStatisticsListEvent( rootDevice.getDeviceId(), stats );
			deviceEventHandlerScheduler.scheduleDeviceEventHandling( rootDevice.getDeviceId(), Collections.singletonList( statisticsList ) );
		}
		PrintWriter out = response.getWriter();
		out.write( "Generated stat events for " + managedDevices.size() + " root devices and all its channels." );
	}
}
