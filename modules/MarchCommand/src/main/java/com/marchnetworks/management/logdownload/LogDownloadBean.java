package com.marchnetworks.management.logdownload;

import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.data.AuditView.Builder;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.command.api.metrics.MetricsCoreService;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.common.crypto.CryptoUtils;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.license.LicenseService;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.server.event.EventRegistry;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;

public class LogDownloadBean
{
	private static final Logger LOG = LoggerFactory.getLogger( LogDownloadBean.class );
	public static final String LOG_FILE_NAME = "logs";
	public static final String LOG_FULL_FILE_NAME = "logs.zip";
	public static final String REGISTERED_DEVICES_LOG_FILE_NAME = "devices.log";
	public static final String SERVER_ID_LOG_FILE_NAME = "serverId.log";
	public static final String XML_CONFIG = "march.server.config.xml";

	@Secured( {"ROLE_ACCESS_LOGS"} )
	public ServletOutputStream getDownloadFile( HttpServletResponse resp ) throws IOException
	{
		setResponseHeaders( resp );

		File tempFile = new File( "" );
		String pathName = tempFile.getAbsolutePath();
		ServletOutputStream op = resp.getOutputStream();
		pathName = pathName.substring( 0, pathName.length() - 6 ) + "logs";

		File xmlCopy = new File( pathName + "/" + "march.server.config.xml" );
		File xml = new File( xmlCopy.getParentFile().getParentFile().getPath() + "/config/" + "march.server.config.xml" );

		FileUtils.copyFile( xml, xmlCopy );

		ZipOutputStream out = new ZipOutputStream( new BufferedOutputStream( op ) );
		int BUFFER = 524288;
		try
		{
			createManagedDevicesLogFile( pathName );
			createServerIdLogFile( pathName );
			dumpCurrentMetrics();

			File f = new File( pathName );

			String[] files = f.list();
			byte[] lineSeparator = System.getProperty( "line.separator" ).getBytes();
			for ( int i = 0; i < files.length; i++ )
			{
				String fileName = files[i];
				if ( ( fileName.contains( ".log" ) ) || ( fileName.contains( ".tdump" ) ) || ( fileName.contains( ".json" ) ) || ( fileName.contains( ".xml" ) ) )
				{
					LOG.info( "************************************Adding: " + pathName + File.separatorChar + files[i] );
					File file = new File( pathName + File.separatorChar + fileName );
					ZipEntry entry = new ZipEntry( fileName );
					entry.setTime( file.lastModified() );
					out.putNextEntry( entry );

					FileReader fr = new FileReader( file );
					BufferedReader in = new BufferedReader( fr, 524288 );
					try
					{
						String content = null;
						while ( ( content = in.readLine() ) != null )
						{
							out.write( content.getBytes() );
							out.write( lineSeparator );
						}
					}
					finally
					{
					}
				}
			}

			out.flush();
		}
		catch ( Exception e )
		{
			LOG.warn( "Error when zipping log files for client download. {}", e );
		}
		finally
		{
			out.close();
		}

		EventRegistry eventRegistry = ( EventRegistry ) ApplicationContextSupport.getBean( "eventRegistry" );
		AuditView av = new Builder( AuditEventNameEnum.DOWNLOAD_SERVER_LOGS.getName() ).build();
		eventRegistry.send( new AuditEvent( av ) );
		return op;
	}

	private void setResponseHeaders( HttpServletResponse resp )
	{
		resp.setContentType( "application/x-zip-compressed" );

		StringBuilder sb = new StringBuilder( "attachment; filename=" );
		sb.append( "logs.zip" );
		sb.append( ";" );
		resp.addHeader( "Content-Disposition", sb.toString() );

		Calendar lastModification = DateUtils.getCurrentUTCTime();
		resp.addHeader( "Last-Modified", DateUtils.getDateInRFC1123( lastModification.getTime() ) );
		resp.addHeader( "Expires", "-1" );
	}

	private void createManagedDevicesLogFile( String filePath )
	{
		File f = new File( filePath + File.separatorChar + "devices.log" );
		try
		{
			if ( f.exists() )
			{
				f.delete();
			}

			f.createNewFile();
			FileOutputStream fos = new FileOutputStream( f );
			ResourceTopologyServiceIF topologyService = ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyService_internal" );
			List<DeviceResource> managedDevices = topologyService.getAllDeviceResources();
			StringBuffer sb = new StringBuffer();
			for ( DeviceResource rootDevice : managedDevices )
			{
				sb.append( "Device Id:" );
				sb.append( rootDevice.getDeviceId() );
				sb.append( ";Resource Id:" );
				sb.append( rootDevice.getIdAsString() );
				sb.append( ";Device Name:" );
				sb.append( rootDevice.getName() );
				sb.append( ";Address:" );
				sb.append( rootDevice.getDeviceView().getRegistrationAddress() );
				sb.append( ";Family:" );
				sb.append( rootDevice.getDeviceView().getFamilyName() );
				sb.append( ";Software Version:" );
				sb.append( rootDevice.getDeviceView().getSoftwareVersion() );
				sb.append( ";Last Comm:" );
				long lastTime = 0L;
				if ( rootDevice.getDeviceView().getLastCommunicationTime() > 0L )
				{
					lastTime = rootDevice.getDeviceView().getLastCommunicationTime();
				}
				sb.append( DateUtils.getDateStringFromMillis( lastTime ) );
				sb.append( ";Time Delta:" );
				sb.append( rootDevice.getDeviceView().getTimeDelta() );
				sb.append( System.getProperty( "line.separator" ) );
			}
			fos.write( sb.toString().getBytes() );
			fos.flush();
			fos.close();
		}
		catch ( IOException e )
		{
			LOG.warn( "Failed to create new file in logs directory. Error details: {}", e.getMessage() );
		}
	}

	private void createServerIdLogFile( String filePath )
	{
		File f = new File( filePath + File.separatorChar + "serverId.log" );
		try
		{
			if ( f.exists() )
			{
				f.delete();
			}

			f.createNewFile();
			FileOutputStream fos = new FileOutputStream( f );
			LicenseService licenseService = ( LicenseService ) ApplicationContextSupport.getBean( "licenseService_internal" );
			String base64ServerId = licenseService.getServerId();

			byte[] hashedServerId = CryptoUtils.sha1( base64ServerId.getBytes() );
			StringBuffer sb = new StringBuffer( "CES Server Id:" );
			sb.append( CommonAppUtils.byteToBase64( hashedServerId ) );
			fos.write( sb.toString().getBytes() );
			fos.flush();
			fos.close();
		}
		catch ( IOException e )
		{
			LOG.warn( "I/O error when trying to create the serverId log. Error details {}", e.getMessage() );
		}
		catch ( Exception e )
		{
			LOG.warn( "Unexpected error when trying to create the serverId log. Error details {}", e.getMessage() );
		}
	}

	private void dumpCurrentMetrics()
	{
		MetricsCoreService metricsService = ( MetricsCoreService ) ApplicationContextSupport.getBean( "metricsService" );
		metricsService.snapshotMetrics();
	}
}
