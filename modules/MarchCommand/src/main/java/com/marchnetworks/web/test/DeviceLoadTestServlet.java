package com.marchnetworks.web.test;

import com.marchnetworks.alarm.service.AlarmTestService;
import com.marchnetworks.command.api.query.Criteria;
import com.marchnetworks.command.api.query.Restrictions;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.simulator.DeviceInfo;
import com.marchnetworks.command.common.simulator.DeviceSpecification;
import com.marchnetworks.command.common.topology.TopologyConstants;
import com.marchnetworks.command.common.topology.data.AlarmSourceResource;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.common.utils.ServerUtils;
import com.marchnetworks.common.utils.ServletUtils;
import com.marchnetworks.health.service.AlertTestService;
import com.marchnetworks.management.instrumentation.DeviceTestService;
import com.marchnetworks.management.topology.ResourceTopologyTestService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet( name = "DeviceLoadTest", urlPatterns = {"/DeviceLoadTest"} )
public class DeviceLoadTestServlet extends HttpServlet
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceLoadTestServlet.class );

	private static final long serialVersionUID = 8187597591733661043L;
	private static Map<String, List<Long>> benchmarkResults = new LinkedHashMap();

	private static Map<String, Integer> serverStats;
	private static DeviceTestService deviceTestServiceProxy = ( DeviceTestService ) ApplicationContextSupport.getBean( "deviceTestServiceProxy" );
	private static AlarmTestService alarmTestServiceProxy = ( AlarmTestService ) ApplicationContextSupport.getBean( "alarmTestServiceProxy" );
	private static AlarmTestService alarmTestService = ( AlarmTestService ) ApplicationContextSupport.getBean( "alarmTestService" );
	private static ResourceTopologyTestService topologyTestService = ( ResourceTopologyTestService ) ApplicationContextSupport.getBean( "topologyTestService" );
	private static ResourceTopologyTestService topologyTestServiceProxy = ( ResourceTopologyTestService ) ApplicationContextSupport.getBean( "topologyTestServiceProxy" );
	private static AlertTestService alertTestServiceProxy = ( AlertTestService ) ApplicationContextSupport.getBean( "alertTestServiceProxy" );
	private static AlertTestService alertTestService = ( AlertTestService ) ApplicationContextSupport.getBean( "alertTestService" );

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		createResponse( request, response, "Refresh Complete" );
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );

		String status = "";
		if ( request.getParameter( "createSimulatedDevices" ) != null )
		{
			int deviceNum = ServletUtils.getIntegerParameterValue( request.getParameter( "deviceNumber" ) ).intValue();
			int cameraNumber = ServletUtils.getIntegerParameterValue( request.getParameter( "cameraNumber" ) ).intValue();
			int alarmNumber = ServletUtils.getIntegerParameterValue( request.getParameter( "alarmNumber" ) ).intValue();
			Boolean createChildDevice = ServletUtils.getBooleanParameterValue( request.getParameter( "createChildDevice" ) );
			String simulatorAddress = request.getParameter( "simulatorAddress" );

			DeviceSpecification specification = new DeviceSpecification( deviceNum );
			specification.setNumCameras( cameraNumber );
			specification.setNumAlarms( alarmNumber );
			specification.setHasChildDevices( createChildDevice.booleanValue() );

			long start = System.currentTimeMillis();
			List<DeviceInfo> rootDevices = deviceTestServiceProxy.createSimulatedDevices( TopologyConstants.SYSTEM_ROOT_ID, specification, simulatorAddress );
			long end = System.currentTimeMillis();
			LOG.info( deviceNum + " simulated devices created in " + ( end - start ) + " ms." );

			deviceTestServiceProxy.sendDevicesToSimulator( rootDevices, cameraNumber, alarmNumber );

			status = deviceNum + " Simulated Devices Created";
		}
		if ( request.getParameter( "createLogicalTree" ) != null )
		{
			int channelLinkNum = ServletUtils.getIntegerParameterValue( request.getParameter( "channelLinkNumber" ) ).intValue();

			topologyTestServiceProxy.createLogicalTree( Integer.valueOf( channelLinkNum ) );

			status = "Logical Tree Created";
		}
		else if ( request.getParameter( "deleteAll" ) != null )
		{
			deviceTestServiceProxy.removeSimulatedDevices();
			status = "Simulated Devices Removed";
		}
		else if ( request.getParameter( "registerSimulator" ) != null )
		{
			String address = request.getParameter( "address" );
			if ( deviceTestServiceProxy.registerSimulator( address ) )
			{
				status = "Simulator Registered on " + address;
			}
			else
			{
				status = "Error: Simulator not running on " + address;
			}

		}
		else if ( request.getParameter( "unregisterSimulator" ) != null )
		{
			if ( deviceTestServiceProxy.unregisterSimulator() )
			{
				status = "Simulator Unregistered";
			}
			else
			{
				status = "Error: Simulator not running";
			}
		}
		else if ( request.getParameter( "injectAlarms" ) != null )
		{
			int numAlarms = ServletUtils.getIntegerParameterValue( request.getParameter( "numAlarms" ) ).intValue();
			int timePeriod = ServletUtils.getIntegerParameterValue( request.getParameter( "timePeriodAlarms" ) ).intValue();
			int percentDevicesAlarms = ServletUtils.getIntegerParameterValue( request.getParameter( "percentDevicesAlarms" ) ).intValue();

			deviceTestServiceProxy.injectSimulatedAlarms( numAlarms, timePeriod, percentDevicesAlarms );

			status = "Injected " + numAlarms + " alarms over " + timePeriod + " ms";
		}
		else
		{
			int throwable;
			if ( request.getParameter( "injectAlerts" ) != null )
			{
				int numAlerts = ServletUtils.getIntegerParameterValue( request.getParameter( "numAlerts" ) );
				int timePeriod = ServletUtils.getIntegerParameterValue( request.getParameter( "timePeriodAlerts" ) );
				throwable = ServletUtils.getIntegerParameterValue( request.getParameter( "percentDevicesAlerts" ) );

				deviceTestServiceProxy.injectSimulatedAlerts( numAlerts, timePeriod, throwable );

				status = "Injected " + numAlerts + " alerts over " + timePeriod + " ms";
			}
			else if ( request.getParameter( "createAlarmEntries" ) != null )
			{
				int alarmEntriesNumber = ServletUtils.getIntegerParameterValue( request.getParameter( "alarmEntriesNumber" ) ).intValue();

				alarmTestServiceProxy.generateAlarmEntries( alarmEntriesNumber );
				status = alarmEntriesNumber + " Random Alarm Entries have been generated";
			}
			else if ( request.getParameter( "deleteAlarmEntries" ) != null )
			{
				alarmTestServiceProxy.deleteAlarmEntries();
				status = "All Alarm Entries have been deleted";
			}
			else if ( request.getParameter( "createAlerts" ) != null )
			{
				int alertNumber = ServletUtils.getIntegerParameterValue( request.getParameter( "alertNumber" ) ).intValue();
				alertTestServiceProxy.generateAlerts( alertNumber );
				status = alertNumber + " Random Alerts have been created";
			}
			else if ( request.getParameter( "deleteAlerts" ) != null )
			{
				alertTestServiceProxy.deleteAlerts();
				status = "All alerts have been deleted";
			}
			else if ( request.getParameter( "uploadJson" ) != null )
			{
				Part filePart = request.getPart( "jsonFile" );

				InputStream filecontent = filePart.getInputStream();
				Throwable exp = null;

				try
				{
					String contents = CommonAppUtils.readInputStream( filecontent, "UTF-8" );
					BenchmarkResults benchmarks = ( BenchmarkResults ) CoreJsonSerializer.fromJson( contents, BenchmarkResults.class );

					benchmarkResults.clear();
					benchmarkResults.putAll( benchmarks.getResults() );
					serverStats = benchmarks.getServerStats();

					status = "Benchmark results loaded from file. Results in ms";
				}
				catch ( Throwable localThrowable1 )
				{
					exp = localThrowable1;
					throw localThrowable1;
				}
				finally
				{

					if ( filecontent != null )
						if ( exp != null )
							try
							{
								filecontent.close();
							}
							catch ( Throwable x2 )
							{
								exp.addSuppressed( x2 );
							}
						else
							filecontent.close();
				}
			}
			else if ( request.getParameter( "runBenchmark" ) != null )
			{
				Map<String, Long> benchmarkRun = new LinkedHashMap();
				String benchmarkOn = request.getParameter( "benchmarkOption" );
				switch ( Benchmark.valueOf( benchmarkOn ) )
				{
					case TOPOLOGY:
						benchmarkRun.putAll( topologyTestService.runBenchmark() );
						break;
					case ALARMS:
						benchmarkRun.putAll( alarmTestService.runBenchmark() );
						break;
					case ALERTS:
						benchmarkRun.putAll( alertTestService.runBenchmark() );
						break;
					default:
						benchmarkRun.putAll( topologyTestService.runBenchmark() );
						benchmarkRun.putAll( alarmTestService.runBenchmark() );
						benchmarkRun.putAll( alertTestService.runBenchmark() );
				}

				for ( Entry<String, Long> resultEntry : benchmarkRun.entrySet() )
				{
					List<Long> benchMarks = ( List ) benchmarkResults.get( resultEntry.getKey() );
					if ( benchMarks == null )
					{
						benchMarks = new ArrayList();
						benchmarkResults.put( resultEntry.getKey(), benchMarks );
					}
					benchMarks.add( resultEntry.getValue() );
				}

				if ( serverStats == null )
				{
					serverStats = new LinkedHashMap();

					Criteria criteria = new Criteria( DeviceResource.class );
					criteria.add( Restrictions.eq( "deviceView.parentDeviceId", null ) );
					serverStats.put( "(Root)Device Resource:", topologyTestService.getResourceCount( criteria ) );

					Map<Class<?>, Integer> resourceCounts = topologyTestService.getResourceCount( new Class[] {ChannelResource.class, AlarmSourceResource.class} );
					for ( Entry<Class<?>, Integer> entry : resourceCounts.entrySet() )
					{
						serverStats.put( ( ( Class ) entry.getKey() ).getSimpleName() + ":", entry.getValue() );
					}
					serverStats.put( "Alarm Entries:", Integer.valueOf( alarmTestServiceProxy.getAlarmEntriesCount() ) );
					serverStats.put( "Device Alert Entries:", Integer.valueOf( alertTestServiceProxy.getDeviceAlertCount() ) );
				}

				status = "Benchmark Complete. Results in ms";
			}
			else if ( request.getParameter( "clearBenchmark" ) != null )
			{
				benchmarkResults.clear();
				serverStats = null;
				status = "Benchmark results cleared";
			}
			else if ( request.getParameter( "saveBenchmark" ) != null )
			{
				BenchmarkResults benchMark = new BenchmarkResults( serverStats, benchmarkResults );
				String json = CoreJsonSerializer.toJson( benchMark );

				String fileName = ServletUtils.getLogsFolderPath() + ServerUtils.HOSTNAME_CACHED + "-" + DateUtils.getFileTimestampFromMillis( System.currentTimeMillis() ) + ".json";
				if ( CommonAppUtils.writeStringToFile( fileName, json ) )
				{
					status = "Benchmark results saved to CES logs folder";
				}
				else
				{
					status = "Failed to create file in CES logs folder";
				}
			}
			else
			{
				status = "Refresh complete";
			}
		}
		createResponse( request, response, status );
	}

	private void createResponse( HttpServletRequest request, HttpServletResponse response, String status ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );
		request.setAttribute( "status", status );
		request.setAttribute( "simulatorInfo", deviceTestServiceProxy.getSimulatorInfo() );
		request.setAttribute( "benchmarks", Benchmark.values() );
		request.setAttribute( "serverStats", serverStats );
		request.setAttribute( "benchmarkResults", benchmarkResults );
		request.setAttribute( "lastSelection", request.getParameter( "benchmarkOption" ) );

		request.setAttribute( "host", ServerUtils.HOSTNAME_CACHED );

		getServletContext().getRequestDispatcher( "/WEB-INF/pages/DeviceLoadTest.jsp" ).forward( request, response );
	}

	public static enum Benchmark
	{
		ALL,
		TOPOLOGY,
		ALARMS,
		ALERTS;

		private Benchmark()
		{
		}
	}

	public static class BenchmarkResults
	{
		private Map<String, Integer> serverStats;
		private Map<String, List<Long>> results;

		public BenchmarkResults()
		{
		}

		public BenchmarkResults( Map<String, Integer> serverStats, Map<String, List<Long>> results )
		{
			this.serverStats = serverStats;
			this.results = results;
		}

		public Map<String, Integer> getServerStats()
		{
			return serverStats;
		}

		void setServerStats( Map<String, Integer> serverStats )
		{
			this.serverStats = serverStats;
		}

		public Map<String, List<Long>> getResults()
		{
			return results;
		}

		void setResults( Map<String, List<Long>> results )
		{
			this.results = results;
		}
	}
}
