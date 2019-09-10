package com.marchnetworks.license;

import com.marchnetworks.command.api.alert.AlertDefinitionEnum;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.WarningTracker;
import com.marchnetworks.health.input.AlertInput;
import com.marchnetworks.health.input.ServerAlertInput;
import com.marchnetworks.health.service.HealthServiceIF;
import com.marchnetworks.license.dao.ServerLicenseDAO;
import com.marchnetworks.license.events.LicenseFraudStateEvent;
import com.marchnetworks.license.events.LicenseImportFinishedEvent;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.exception.LicenseExceptionType;
import com.marchnetworks.license.model.DeviceLicenseInfo;
import com.marchnetworks.license.model.Expiry;
import com.marchnetworks.license.model.LicenseType;
import com.marchnetworks.license.model.RecordingLicenseImport;
import com.marchnetworks.license.model.ServerLicenseEntity;
import com.marchnetworks.license.model.ServerLicenseImport;
import com.marchnetworks.license.model.ServerLicenseType;
import com.marchnetworks.license.service.LicenseServiceImpl;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.server.event.EventRegistry;
import com.marchnetworks.server.event.StateCacheService;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerLicenseBO
{
	private static final Logger LOG = LoggerFactory.getLogger( ServerLicenseBO.class );

	private static final String PARAM_FRAUDSTATE = "instrumentation_data";

	protected DeviceLicenseBO m_DeviceLicenseBO;

	protected ServerLicenseDAO serverLicenseDAO;

	protected DeviceService m_DeviceService;

	protected DeviceRegistry m_DeviceRegistry;
	protected EventRegistry m_EventRegistry;
	protected HealthServiceIF m_HealthService;
	protected LicenseService m_licenseService;
	protected ResourceTopologyServiceIF resourceTopologyService;
	protected Map<LicenseType, ServerLicenseType> m_Data = new ConcurrentHashMap();

	protected Map<LicenseType, WarningTracker> m_WarningTrackers = new ConcurrentHashMap();

	protected int m_iFraudState = -1;
	protected int m_iLastFraudState = -2;

	public void start()
	{
		ArrayList<ServerLicenseImport> licenses = new ArrayList();

		initDataStore();

		m_iFraudState = m_licenseService.loadParameter( "instrumentation_data" );

		if ( m_iFraudState > 0 )
		{
			m_iFraudState -= 1;
			if ( m_iFraudState < 0 )
			{
				m_iFraudState = 0;
			}
		}

		LOG.debug( "Loading license import history..." );
		List<ServerLicenseEntity> list = serverLicenseDAO.findAll();
		for ( ServerLicenseEntity sle : list )
		{
			String xml = sle.getLicense();
			try
			{
				ServerLicenseImport sli = ServerLicenseImport.parseServerLicenseImport( xml );

				if ( m_licenseService.validate( sli.getContentString(), sli.getSignature() ) )
				{
					licenses.add( sli );
				}
				else
				{
					LOG.error( "Skipping invalid license found in import history" );
				}
			}
			catch ( Exception e )
			{
				LOG.error( "Skipping corrupt license found in import history. Error: {}", e );
				LOG.error( "Contents:\n {}", xml );
			}
		}

		boolean licenseMatched = list.isEmpty();
		for ( int i = 0; i < licenses.size(); i++ )
		{
			ServerLicenseImport sli = ( ServerLicenseImport ) licenses.get( i );
			if ( m_licenseService.isLicenseMatch( sli.getServerId() ) )
			{
				licenseMatched = true;
				break;
			}
		}

		if ( licenseMatched )
		{
			m_iFraudState = -1;
		}
		else if ( m_iFraudState == -1 )
		{
			m_iFraudState = 360;
		}

		if ( m_iFraudState != 0 )
		{
			for ( ServerLicenseImport l : licenses )
			{
				if ( !m_licenseService.isLicenseMatch( l.getServerId() ) )
				{
					LOG.debug( "Skipping license that doesn't match this server. LicenseDate=" + l.getDate() );

				}
				else
				{
					for ( RecordingLicenseImport slit : l.getRecordingLicenses() )
					{
						ServerLicenseType slt = ( ServerLicenseType ) m_Data.get( slit.getType() );
						try
						{
							slt.load( slit, -1, null );
						}
						catch ( LicenseException e )
						{
							LOG.error( "Error loading license from storage: " + e.getMessage() + ". Skipping" );
						}
					}
				}
			}
		}

		m_licenseService.saveParameter( m_iFraudState, "instrumentation_data" );
		updateLicenseStateEvent( true );

		m_DeviceLicenseBO.start();
	}

	protected void initDataStore()
	{
		m_Data.clear();
		m_Data = initDataMap();
	}

	protected Map<LicenseType, ServerLicenseType> initDataMap()
	{
		LicenseType[] vals = LicenseType.values();
		Map<LicenseType, ServerLicenseType> result = new ConcurrentHashMap( vals.length );
		for ( LicenseType t : vals )
		{
			result.put( t, new ServerLicenseType( t ) );
		}
		return result;
	}

	public Collection<ServerLicenseType> getServerLicenses()
	{
		return Collections.unmodifiableCollection( m_Data.values() );
	}

	public ServerLicenseType getServerLicense( LicenseType t )
	{
		return ( ServerLicenseType ) m_Data.get( t );
	}

	public int getTotal( LicenseType t )
	{
		return ( ( ServerLicenseType ) m_Data.get( t ) ).getTotal();
	}

	public int getAllocated( LicenseType t )
	{
		return m_DeviceLicenseBO.getAllocated( t );
	}

	public int getInUse( LicenseType t )
	{
		int Result = 0;

		Collection<DeviceLicenseInfo> dls = getAllDeviceLicense( t );

		if ( t == LicenseType.RECORDER )
			return dls.size();
		if ( t == LicenseType.CHANNEL )
		{
			for ( DeviceLicenseInfo dl : dls )
			{
				Result += getInUse( dl.getDeviceId() );
			}
			return Result;
		}
		throw new NotImplementedException();
	}

	protected int getInUse( Long deviceId )
	{
		int Result = 0;
		DeviceMBean d = m_DeviceRegistry.getDevice( deviceId.toString() );
		if ( ( d != null ) && ( d.getChannelsInUse() != null ) )
		{
			Result = d.getChannelsInUse().intValue();
		}

		return Result;
	}

	public Expiry getExpiry( LicenseType t )
	{
		return ( ( ServerLicenseType ) m_Data.get( t ) ).getExpiry();
	}

	public Collection<DeviceLicenseInfo> getAllDeviceLicense( LicenseType t )
	{
		return m_DeviceLicenseBO.getAllDeviceLicense( t );
	}

	public DeviceLicenseInfo getDeviceLicense( Long deviceId )
	{
		return m_DeviceLicenseBO.getDeviceLicense( deviceId );
	}

	public void importLicense( ServerLicenseImport sli ) throws LicenseException
	{
		if ( sli.getRecordingLicenses().isEmpty() )
		{
			return;
		}

		Date now = new Date();

		if ( m_iFraudState == -1 )
		{
			checkLicenseImport( sli, now, m_Data );
		}
		else
		{
			for ( LicenseType t : m_Data.keySet() )
			{
				ServerLicenseType slt = ( ServerLicenseType ) m_Data.get( t );

				int currentInUse = getInUse( t );
				if ( currentInUse > 0 )
				{
					RecordingLicenseImport slitFound = null;
					for ( RecordingLicenseImport slit : sli.getRecordingLicenses() )
					{
						if ( slit.getType() == t )
						{
							slitFound = slit;
							break;
						}
					}

					LicenseExceptionType exceptionType = t == LicenseType.RECORDER ? LicenseExceptionType.LICENSE_COUNT_RECORDER : LicenseExceptionType.LICENSE_COUNT_CHANNEL;

					if ( slitFound == null )
					{
						throw new LicenseException( "Type=" + t + " Not enough licenses. NewCount(" + 0 + ") < CurrentInUse(" + currentInUse + ")", exceptionType );
					}

					if ( slitFound.getCount() < currentInUse )
					{
						throw new LicenseException( "Type=" + t + " Not enough licenses. NewCount(" + slitFound.getCount() + ") < CurrentInUse(" + currentInUse + ")", exceptionType );
					}

					slt.checkExpiry( slitFound, now );
				}
			}

			Map<LicenseType, ServerLicenseType> data = initDataMap();
			checkLicenseImport( sli, now, data );

			m_Data = data;
		}

		ServerLicenseEntity serverLicenseEntity = new ServerLicenseEntity();
		serverLicenseEntity.setLicense( sli.getSourceXml() );
		serverLicenseDAO.create( serverLicenseEntity );

		for ( RecordingLicenseImport slit : sli.getRecordingLicenses() )
		{
			int oldAllocated = getAllocated( slit.getType() );

			ServerLicenseType slt = ( ServerLicenseType ) m_Data.get( slit.getType() );
			Expiry origExpiry = slt.getExpiry();
			slt.load( slit, getInUse( slit.getType() ), now );

			if ( slt.getExpiry() == Expiry.TRIAL )
			{
				LOG.info( "ServerLicenseType imported: Type=" + slt.getType() + " Expiry=" + slt.getExpiry() + " Start=" + LicenseUtils.date2expiryString( slt.getStart() ) + " End=" + LicenseUtils.date2expiryString( slt.getEnd() ) + " New Total=" + slt.getTotal() );

			}
			else
			{

				LOG.info( "ServerLicenseType imported: Type=" + slt.getType() + " Expiry=" + slt.getExpiry() + " New Total=" + slt.getTotal() );
			}

			resetWarnings( slit.getType() );

			m_iFraudState = -1;
			m_licenseService.saveParameter( m_iFraudState, "instrumentation_data" );
			updateLicenseStateEvent();

			if ( ( origExpiry != Expiry.PERMANENT ) || ( slt.getExpiry() != Expiry.PERMANENT ) )
			{

				boolean bTotalLessAlloc = slt.getTotal() < oldAllocated;
				if ( bTotalLessAlloc )
				{
					LOG.info( "New Total for license type " + slt.getType() + "(" + slt.getTotal() + ") is less than previous allocation(" + oldAllocated + ")." );
					LOG.info( " --> Setting all " + slt.getTotal() + " devices to have count == InUse (Minimum needed)" );

					setAllDeviceLicenseToMinimum( slt.getType() );
				}
			}
		}

		m_DeviceLicenseBO.updateDeviceLicenses( false );

		LicenseImportFinishedEvent event = new LicenseImportFinishedEvent();
		m_EventRegistry.sendEventAfterTransactionCommits( event );
	}

	protected void checkLicenseImport( ServerLicenseImport sli, Date now, Map<LicenseType, ServerLicenseType> data ) throws LicenseException
	{
		for ( RecordingLicenseImport slit : sli.getRecordingLicenses() )
		{
			ServerLicenseType slt = ( ServerLicenseType ) data.get( slit.getType() );
			slt.checkLoad( slit, getInUse( slit.getType() ), now );
		}
	}

	public void checkForLicenseExpiredTask()
	{
		Collection<ServerLicenseType> licenses = getServerLicenses();
		Date dNow = new Date();

		for ( ServerLicenseType slt : licenses )
		{
			if ( slt.getExpiry() == Expiry.TRIAL )
			{
				if ( dNow.after( slt.getEnd() ) )
				{
					doExpiredAction( slt.getType() );
				}
				else
				{
					WarningTracker wt = getWarningTracker( slt.getType(), slt.getEnd() );

					if ( wt.check( dNow ) )
					{
						sendOutWarnings( slt.getType(), wt.getDaysLeft() );
					}
				}
			}
		}
	}

	public void resetWarnings( LicenseType t )
	{
		m_WarningTrackers.remove( t );

		AlertInput alert = new ServerAlertInput( AlertDefinitionEnum.LICENSE_EXPIRE, t.toString(), "", "", false );

		m_HealthService.processHealthAlert( alert );
	}

	protected WarningTracker getWarningTracker( LicenseType t, Date end )
	{
		int[] warningDays = {7, 3};
		WarningTracker wt = ( WarningTracker ) m_WarningTrackers.get( t );
		if ( wt == null )
		{
			try
			{
				wt = new WarningTracker( warningDays, end );
			}
			catch ( Exception e )
			{
				LOG.error( "Critical error: Warning Days array is invalid" );
				return null;
			}
			m_WarningTrackers.put( t, wt );
		}
		return wt;
	}

	private void sendOutWarnings( LicenseType t, int iDaysLeft )
	{
		LOG.info( "Sending out warning to client: Server license " + t + " is going to expire in " + iDaysLeft + " days!" );

		AlertInput alert = new ServerAlertInput( AlertDefinitionEnum.LICENSE_EXPIRE, t.toString(), "", Integer.toString( iDaysLeft ), true );

		m_HealthService.processHealthAlert( alert );
	}

	public void checkLicenseStateExpire( boolean firstPoll )
	{
		if ( ( firstPoll ) && ( m_iFraudState == 0 ) )
		{
			doGraceExpire();
		}
		else if ( m_iFraudState > 0 )
		{
			int i = m_iFraudState - 1;
			if ( i <= 0 )
			{
				m_iFraudState = 0;
				doGraceExpire();
			}
			else
			{
				m_iFraudState = i;
				LOG.info( "Grace period = " + m_iFraudState );
			}
		}

		updateLicenseStateEvent();
	}

	protected void doGraceExpire()
	{
		LOG.info( "Grace period expired, removing licenses" );
		m_iFraudState = 0;

		initDataStore();
		for ( LicenseType t : m_Data.keySet() )
		{
			doExpiredAction( t );
		}
	}

	protected void updateLicenseStateEvent()
	{
		updateLicenseStateEvent( false );
	}

	protected void updateLicenseStateEvent( boolean force )
	{
		if ( ( !force ) && ( m_iFraudState == m_iLastFraudState ) )
		{
			return;
		}

		LicenseFraudStateEvent ev = new LicenseFraudStateEvent( m_iFraudState );

		StateCacheService scs = ( StateCacheService ) ApplicationContextSupport.getBean( "stateCacheService" );
		scs.putIntoCache( ev );
		m_EventRegistry.sendEventAfterTransactionCommits( ev );

		m_iLastFraudState = m_iFraudState;

		m_licenseService.saveParameter( m_iFraudState, "instrumentation_data" );
	}

	protected void doExpiredAction( LicenseType t )
	{
		ServerLicenseType slt = ( ServerLicenseType ) m_Data.get( t );

		if ( slt.expired() )
		{
			return;
		}

		LOG.info( "License Type " + t + " is expired. Performing expiry actions.." );
		slt.setExpired( true );

		try
		{
			m_DeviceLicenseBO.updateDeviceLicenses( true );
		}
		catch ( LicenseException e1 )
		{
			LOG.error( "Critical error updatingDeviceLicenses: ", e1 );
		}

		LOG.info( "License Type " + t + " expiry actions complete." );
	}

	public void setDeviceLicense( Long deviceId, LicenseType t, int count ) throws LicenseException
	{
		int total = getTotal( t );
		int inUsed = getAllocated( t );
		int free = total - inUsed;
		int deviceLicenseCount = 0;

		if ( m_iFraudState > -1 )
		{
			throw new LicenseException( "License counts frozen", LicenseExceptionType.LICENSE_COUNT_FROZEN );
		}

		DeviceLicenseInfo dlt = m_DeviceLicenseBO.getDeviceLicense( deviceId );
		if ( dlt != null )
		{
			deviceLicenseCount = dlt.getCount();
		}

		LicenseExceptionType exceptionType = t == LicenseType.RECORDER ? LicenseExceptionType.LICENSE_COUNT_RECORDER : LicenseExceptionType.LICENSE_COUNT_CHANNEL;

		if ( count > free + deviceLicenseCount )
		{
			throw new LicenseException( "NewCount(" + count + ") > Free(" + free + ") + CurrentlyAssigned(" + deviceLicenseCount + "), cannot create DeviceLicense for deviceId=" + deviceId, exceptionType );
		}

		ServerLicenseType slt = getServerLicense( t );
		if ( slt.isEmpty() )
		{
			if ( count != 0 )
			{
				throw new LicenseException( "No available license for type " + t, exceptionType );
			}

		}
		else if ( slt.getExpiry() == Expiry.TRIAL )
		{
			Date dNow = new Date();

			if ( dNow.before( slt.getStart() ) )
			{
				throw new LicenseException( "trial license will start from :" + slt.getStart(), LicenseExceptionType.LICENSE_NOT_YET_VALID );
			}

			if ( dNow.after( slt.getEnd() ) )
			{
				throw new LicenseException( "License type " + t.toString() + " is expired", LicenseExceptionType.LICENSE_EXPIRED );
			}
		}

		m_DeviceLicenseBO.setDeviceLicense( deviceId, t, count );
	}

	public void setTestDeviceLicense( Long deviceId, LicenseType type, int count ) throws LicenseException
	{
		m_DeviceLicenseBO.setTestDeviceLicense( deviceId, type, count );
	}

	protected void setAllDeviceLicenseToMinimum( LicenseType t )
	{
		Collection<DeviceLicenseInfo> devices = getAllDeviceLicense( t );
		for ( DeviceLicenseInfo dld : devices )
		{
			try
			{
				DeviceMBean d = m_DeviceRegistry.getDevice( dld.getDeviceId().toString() );
				int min = LicenseServiceImpl.getMinCountRequired( d, t );

				m_DeviceLicenseBO.setDeviceLicense( dld.getDeviceId(), t, min );
			}
			catch ( LicenseException e )
			{
				LOG.error( "Error reassigning device license: ", e );
			}
		}
	}

	public void setDeviceLicenseBO( DeviceLicenseBO deviceLicenseBO )
	{
		m_DeviceLicenseBO = deviceLicenseBO;
	}

	public void setServerLicenseDAO( ServerLicenseDAO serverLicenseDAO )
	{
		this.serverLicenseDAO = serverLicenseDAO;
	}

	public void setDeviceService( DeviceService deviceService )
	{
		m_DeviceService = deviceService;
	}

	public void setDeviceRegistry( DeviceRegistry dr )
	{
		m_DeviceRegistry = dr;
	}

	public void setEventRegistry( EventRegistry er )
	{
		m_EventRegistry = er;
	}

	public void setHealthService( HealthServiceIF healthService )
	{
		m_HealthService = healthService;
	}

	public void setLicenseService( LicenseService licenseService )
	{
		m_licenseService = licenseService;
	}

	public ResourceTopologyServiceIF getResourceTopologyService()
	{
		return resourceTopologyService;
	}

	public void setResourceTopologyService( ResourceTopologyServiceIF resourceTopologyService )
	{
		this.resourceTopologyService = resourceTopologyService;
	}
}

