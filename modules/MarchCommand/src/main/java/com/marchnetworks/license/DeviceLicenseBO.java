package com.marchnetworks.license;

import com.marchnetworks.command.api.rest.DeviceManagementConstants;
import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.common.service.CertificationService;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.license.dao.DeviceLicenseDAO;
import com.marchnetworks.license.events.LicenseAllocatedChangedEvent;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.exception.LicenseExceptionType;
import com.marchnetworks.license.model.DeviceLicenseEntity;
import com.marchnetworks.license.model.DeviceLicenseInfo;
import com.marchnetworks.license.model.Expiry;
import com.marchnetworks.license.model.LicenseType;
import com.marchnetworks.license.model.ServerLicenseType;
import com.marchnetworks.license.service.LicenseServiceImpl;
import com.marchnetworks.license.task.SendDeviceLicenseTask;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.server.event.EventRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceLicenseBO
{
	private static Logger LOG = LoggerFactory.getLogger( DeviceLicenseBO.class );

	private TaskScheduler m_TaskScheduler;

	private DeviceLicenseGenEngine m_DeviceLicenseGenEngine;

	private DeviceLicenseDAO m_DeviceLicenseDAO;

	private EventRegistry m_EventRegistry;
	private ServerLicenseBO m_ServerLicenseBO;
	private DeviceService m_DeviceService;
	private DeviceRegistry m_DeviceRegistry;
	private ResourceTopologyServiceIF topologyService;
	private Map<Long, DeviceLicense> m_Data = new ConcurrentHashMap();

	public void start()
	{
		CertificationService cs = ( CertificationService ) ApplicationContextSupport.getBean( "certificationService" );
		m_DeviceLicenseGenEngine = new DeviceLicenseGenEngine( cs.getCommandCredential() );

		List<DeviceLicenseEntity> list = m_DeviceLicenseDAO.findAll();
		Collection<DeviceMBean> rootDevices = m_DeviceRegistry.getAllRootDevices();

		for ( Iterator<DeviceMBean> iterator = rootDevices.iterator(); iterator.hasNext(); )
		{
			DeviceMBean device = ( DeviceMBean ) iterator.next();
			if ( DeviceManagementConstants.isExtractorDevice( device.getManufacturer(), device.getFamily() ) )
			{
				iterator.remove();
			}
		}
		for ( DeviceLicenseEntity dle : list )
		{

			if ( !containsDevice( rootDevices, dle.getDeviceId() ) )
			{
				LOG.warn( "Found a DeviceLicenseEntity with no corresponding deviceId(" + dle.getDeviceId() + "). Removing from DB" );
				m_DeviceLicenseDAO.delete( dle );

			}
			else if ( m_Data.containsKey( dle.getDeviceId() ) )
			{
				LOG.warn( "Duplicate DeviceLicenseEntity for the same deviceId=" + dle.getDeviceId() + " found. Removing from DB" );
				m_DeviceLicenseDAO.delete( dle );
			}
			else
			{
				DeviceLicense dl = new DeviceLicense( dle );
				m_Data.put( dl.getDeviceId(), dl );
			}
		}

		for ( DeviceMBean d : rootDevices )
		{
			Long id = Long.valueOf( d.getDeviceId() );
			DeviceLicense dl = ( DeviceLicense ) m_Data.get( id );
			if ( dl == null )
			{
				LicenseType t = LicenseServiceImpl.getLicenseTypeRequired( d );
				if ( t.isExternal() )
				{
					LOG.warn( "No DeviceLicense found for DeviceId=" + id + ". Adding DeviceLicense with Type=" + t + ", allocation to be set when device comes online" );
					dl = new DeviceLicense( id, t );
				}
				else
				{
					int alloc = LicenseServiceImpl.getMinCountRequired( d, t );
					LOG.warn( "No DeviceLicense found for DeviceId=" + id + ". Adding DeviceLicense with Type=" + t + ", allocation(" + alloc + ")" );
					dl = new DeviceLicense( id, t, alloc );
				}
				m_Data.put( id, dl );
			}
		}

		if ( m_ServerLicenseBO.m_iFraudState != 0 )
		{
			for ( LicenseType t : LicenseType.values() )
			{
				ServerLicenseType slt = m_ServerLicenseBO.getServerLicense( t );
				int allocated = getAllocated( t );

				if ( allocated > slt.getTotal() )
				{
					LOG.warn( "License Type " + t + " device allocations(" + allocated + ") are greater than total licenses imported(" + slt.getTotal() + ")" );

					int minReqForType = 0;
					for ( DeviceLicense dl : m_Data.values() )
					{
						DeviceMBean d = m_DeviceRegistry.getDevice( dl.getDeviceId().toString() );
						minReqForType += LicenseServiceImpl.getMinCountRequired( d, t );
					}

					if ( slt.getTotal() >= minReqForType )
					{
						LOG.warn( "Assigning minimum required licenses for each device" );
						for ( DeviceLicense dl : m_Data.values() )
						{
							DeviceMBean d = m_DeviceRegistry.getDevice( dl.getDeviceId().toString() );
							dl.setCount( LicenseServiceImpl.getMinCountRequired( d, t ) );
						}
					}
					else
					{
						LOG.error( "Critical Error: LicenseType " + t + " Total licenses = " + slt.getTotal() + ", yet registered devices need " + minReqForType + " licenses." );
					}
				}
			}
		}
	}

	protected boolean containsDevice( Collection<DeviceMBean> devices, Long deviceId )
	{
		for ( DeviceMBean d : devices )
		{
			Long l = Long.valueOf( d.getDeviceId() );
			if ( l.equals( deviceId ) )
			{
				return true;
			}
		}
		return false;
	}

	public void doCheckDeviceOnline( Long deviceId, boolean force )
	{
		DeviceLicense dl = m_Data.get( deviceId );
		if ( dl == null )
		{
			return;
		}

		DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( deviceId.toString() );
		DeviceView dmb = device.getDeviceView();

		if ( ( !force ) && ( dmb.getConnectState() == ConnectState.OFFLINE ) )
		{
			return;
		}

		if ( dl.NeedsInfoCheck() )
		{
			int alloc = 0;
			LOG.info( "Setting DeviceLicense allocation for deviceId(" + deviceId + ")" );

			if ( dl.getType().isExternal() )
			{
				try
				{
					alloc = m_DeviceService.grabAllocatedLicenses( deviceId );
					if ( alloc < 0 )
					{
						LOG.warn( "deviceId(" + deviceId + "), grabbed negative allocation, setting to zero" );
						alloc = 0;
					}
					else
					{
						LOG.info( "deviceId(" + deviceId + "), grabbed allocation of " + alloc );
					}
				}
				catch ( DeviceException e )
				{
					LOG.error( "Couldn't explicitly grab the allocated channel licenses from the device, setting to zero" );
				}
			}
			else
			{
				alloc = LicenseServiceImpl.getMinCountRequired( dmb, dl.getType() );
				LOG.info( "deviceId(" + deviceId + "), internal license type, setting to required count of " + alloc );
			}

			int TotalAlloc = 0;
			for ( DeviceLicense dld : m_Data.values() )
			{
				if ( ( dld.getType() == dl.getType() ) && ( dl.getDeviceId() != dld.getDeviceId() ) )
				{
					TotalAlloc += dl.getCount();
				}
			}
			int free = m_ServerLicenseBO.getTotal( dl.getType() ) - TotalAlloc;

			if ( ( m_ServerLicenseBO.m_iFraudState < 1 ) && ( alloc > free ) )
			{
				LOG.warn( "CheckDeviceOnline: Id=" + dmb.getDeviceId() + " Address=" + dmb.getRegistrationAddress() + " LicenseType=" + dl.getType() );
				LOG.warn( "Current allocation(" + alloc + ") for deviceId(" + dl.getDeviceId() + ") is greater than the number of free licenses(" + free + "). Setting allocation to " + free );
				alloc = free;
			}

			dl.setCount( alloc );

			m_EventRegistry.send( new LicenseAllocatedChangedEvent( dl.getType(), getAllocated( dl.getType() ) ) );
		}

		if ( ( dl.m_bNeedXMLSend ) || ( force ) )
			dl.sendDeviceLicenseSoon();
	}

	public int getAllocated( LicenseType t )
	{
		int Result = 0;
		for ( DeviceLicense dl : m_Data.values() )
		{
			if ( dl.getType() == t )
			{
				Result += dl.getCount();
			}
		}
		return Result;
	}

	public DeviceLicenseInfo getDeviceLicense( Long deviceId )
	{
		DeviceLicense dl = ( DeviceLicense ) m_Data.get( deviceId );
		if ( dl != null )
		{
			return dl.toInfo();
		}
		return null;
	}

	public Collection<DeviceLicenseInfo> getAllDeviceLicense( LicenseType t )
	{
		List<DeviceLicenseInfo> Result = new ArrayList();
		for ( DeviceLicense dl : m_Data.values() )
		{
			if ( t == dl.getType() )
			{
				Result.add( dl.toInfo() );
			}
		}
		return Result;
	}

	public boolean setDeviceLicense( Long deviceId, LicenseType t, int count ) throws LicenseException
	{
		DeviceLicense dl = ( DeviceLicense ) m_Data.get( deviceId );
		boolean send = false;
		int prevAlloc = 0;

		if ( dl == null )
		{
			dl = new DeviceLicense( deviceId, t, count );
			m_Data.put( deviceId, dl );
			send = true;
		}
		else
		{
			prevAlloc = dl.getCount();
			if ( dl.getType() != t )
			{
				throw new LicenseException( "Each device can only have one type of license", LicenseExceptionType.LICENSE_DEVICE_ERROR );
			}
			send = dl.setCount( count );
		}

		if ( send )
		{
			try
			{
				dl.sendDeviceLicenseNow();
				m_EventRegistry.send( new LicenseAllocatedChangedEvent( t, getAllocated( t ) ) );
			}
			catch ( LicenseException e )
			{
				dl.setCount( prevAlloc );
				throw e;
			}
		}

		return send;
	}

	public void setTestDeviceLicense( Long deviceId, LicenseType t, int count ) throws LicenseException
	{
		DeviceLicense dl = ( DeviceLicense ) m_Data.get( deviceId );

		if ( dl == null )
		{
			dl = new DeviceLicense( deviceId, t, count );
			m_Data.put( deviceId, dl );
		}
		else
		{
			if ( dl.getType() != t )
			{
				throw new LicenseException( "Each device can only have one type of license", LicenseExceptionType.LICENSE_DEVICE_ERROR );
			}
			dl.setCount( count );
		}
		m_EventRegistry.send( new LicenseAllocatedChangedEvent( t, getAllocated( t ) ) );
	}

	public void updateDeviceLicenses( boolean now ) throws LicenseException
	{
		LOG.info( "Refreshing any changed device license parameters" );

		for ( DeviceLicense dl : m_Data.values() )
		{
			if ( dl.checkAndUpdateXML() )
			{
				if ( now )
				{
					dl.sendDeviceLicenseNow();
				}
				else
				{
					dl.sendDeviceLicenseSoon();
				}
			}
		}
	}

	public void removeDeviceLicense( Long deviceId )
	{
		DeviceLicense dl = ( DeviceLicense ) m_Data.get( deviceId );
		if ( dl == null )
		{
			return;
		}

		LOG.info( "Removing DeviceLicense for deviceId=" + deviceId );

		DeviceLicenseEntity dle = dl.findDLE();
		if ( dle != null )
		{
			m_DeviceLicenseDAO.delete( dle );
		}

		LicenseType t = dl.getType();
		m_Data.remove( deviceId );
		m_EventRegistry.send( new LicenseAllocatedChangedEvent( t, getAllocated( t ) ) );
	}

	public boolean resendDeviceLicense( Long deviceId )
	{
		DeviceLicense dl = ( DeviceLicense ) m_Data.get( deviceId );
		if ( dl == null )
		{
			LOG.warn( "DeviceLicense " + deviceId + " not found." );
			return false;
		}
		try
		{
			dl.sendDeviceLicenseNow();
			return true;
		}
		catch ( LicenseException e )
		{
			LOG.warn( "Couldn't send device license to deviceId=" + deviceId );
		}
		return false;
	}

	protected class DeviceLicense
	{
		protected Long m_DBid;

		protected Long m_DeviceId;

		protected LicenseType m_Type;

		protected Integer m_iCount;

		protected boolean m_bRevoked;

		protected Expiry m_Expiry;

		protected Date m_dStart;

		protected Date m_dEnd;

		protected String m_sXMLcache;

		protected boolean m_bNeedXMLSend;

		public DeviceLicense( Long deviceId, LicenseType t, int count )
		{
			create( deviceId, t, Integer.valueOf( count ) );
		}

		public DeviceLicense( Long deviceId, LicenseType t )
		{
			create( deviceId, t, null );
		}

		public DeviceLicense( DeviceLicenseEntity dle )
		{
			m_DBid = dle.getId();
			m_DeviceId = dle.getDeviceId();
			m_Type = dle.getType();
			m_iCount = dle.getAssigned();
			updateXML();
		}

		private void create( Long deviceId, LicenseType t, Integer count )
		{
			m_DeviceId = deviceId;
			m_Type = t;
			m_iCount = count;
			m_DBid = null;
			updateDB();
			updateXML();
			m_bNeedXMLSend = true;
			DeviceLicenseBO.LOG.debug( "Device License created: DeviceId={} Type={} Count={}", new Object[] {m_DeviceId, m_Type, m_iCount} );
		}

		public Long getDeviceId()
		{
			return m_DeviceId;
		}

		public LicenseType getType()
		{
			return m_Type;
		}

		public int getCount()
		{
			if ( m_iCount == null )
			{
				return 0;
			}
			return m_iCount.intValue();
		}

		public boolean setCount( int count )
		{
			if ( ( m_iCount != null ) && ( m_iCount.intValue() == count ) )
			{
				return false;
			}

			m_iCount = Integer.valueOf( count );
			updateDB();
			updateXML();
			m_bNeedXMLSend = true;
			return true;
		}

		public boolean getRevoked()
		{
			return m_bRevoked;
		}

		public boolean checkAndUpdateXML() throws LicenseException
		{
			ServerLicenseType slt = m_ServerLicenseBO.getServerLicense( m_Type );

			if ( ( m_Expiry != slt.getExpiry() ) || ( m_dStart != slt.getStart() ) || ( m_dEnd != slt.getEnd() ) || ( m_bRevoked != slt.expired() ) )
			{

				updateXML();
				m_bNeedXMLSend = true;
				return true;
			}
			return false;
		}

		protected void updateXML()
		{
			ServerLicenseType slt = m_ServerLicenseBO.getServerLicense( m_Type );
			String sDeviceId = m_DeviceId.toString();
			m_bRevoked = slt.expired();
			m_Expiry = slt.getExpiry();
			m_dStart = slt.getStart();
			m_dEnd = slt.getEnd();
			m_sXMLcache = null;

			if ( !m_Type.isExternal() )
			{
				return;
			}

			if ( m_bRevoked )
			{
				m_sXMLcache = "";
				DeviceLicenseBO.LOG.info( "Device license revoked for deviceId=" + sDeviceId );
			}
			else
			{
				if ( m_Expiry == null )
				{
					m_Expiry = Expiry.PERMANENT;
					m_iCount = Integer.valueOf( 0 );
				}

				int alloc = 0;
				if ( m_iCount != null )
				{
					alloc = m_iCount.intValue();
				}

				m_sXMLcache = m_DeviceLicenseGenEngine.generateSignedLicense( m_Expiry.toString(), sDeviceId, Integer.toString( alloc ), m_Type.toString(), LicenseUtils.date2expiryString( m_dStart ), LicenseUtils.date2expiryString( m_dEnd ) );

				DeviceLicenseBO.LOG.debug( "Updated DeviceLicense for deviceId={}", sDeviceId );
			}
		}

		public void sendDeviceLicenseSoon()
		{
			SendDeviceLicenseTask sdlt = new SendDeviceLicenseTask( m_DeviceId );
			m_TaskScheduler.executeNow( sdlt );
		}

		public void sendDeviceLicenseNow() throws LicenseException
		{
			if ( !m_Type.isExternal() )
			{
				return;
			}

			if ( m_sXMLcache == null )
			{
				updateXML();
			}
			if ( m_sXMLcache == null )
			{
				throw new LicenseException( "Couldn't generate DeviceLicenseXML for deviceId=" + m_DeviceId, LicenseExceptionType.LICENSE_DEVICE_ERROR );
			}
			try
			{
				m_DeviceService.sendDeviceLicense( m_DeviceId, m_sXMLcache );
				m_bNeedXMLSend = false;
			}
			catch ( DeviceException e1 )
			{
				throw new LicenseException( "Couldn't send license to deviceId=" + m_DeviceId, LicenseExceptionType.LICENSE_DEVICE_ERROR, e1 );
			}
		}

		public DeviceLicenseInfo toInfo()
		{
			DeviceLicenseInfo dl = new DeviceLicenseInfo();
			dl.setDeviceId( m_DeviceId );
			dl.setType( m_Type );
			dl.setExpiry( m_Expiry );
			dl.setCount( getCount() );
			dl.setStart( m_dStart );
			dl.setEnd( m_dEnd );
			dl.setRevoked( m_bRevoked );
			return dl;
		}

		public boolean NeedsInfoCheck()
		{
			return m_iCount == null;
		}

		protected void updateDB()
		{
			DeviceLicenseEntity dle = findDLE();
			if ( dle == null )
			{
				dle = new DeviceLicenseEntity();
				dle.setDeviceId( m_DeviceId );
				dle.setType( m_Type );
				dle.setAssigned( m_iCount );

				DeviceLicenseBO.LOG.debug( "Creating DeviceLicenseEntity: DeviceId={} Type={} Assigned={}", new Object[] {m_DeviceId, m_Type, m_iCount} );
				m_DeviceLicenseDAO.create( dle );
				m_DBid = dle.getId();
			}
			else
			{
				DeviceLicenseBO.LOG.debug( "Updating DeviceLicenseEntity: DeviceId={} Type={} Assigned={}", new Object[] {m_DeviceId, m_Type, m_iCount} );
				dle.setAssigned( m_iCount );
			}
		}

		protected DeviceLicenseEntity findDLE()
		{
			if ( m_DBid == null )
			{
				return null;
			}

			DeviceLicenseEntity Result = ( DeviceLicenseEntity ) m_DeviceLicenseDAO.findById( m_DBid );
			if ( Result != null )
			{
				return Result;
			}

			List<DeviceLicenseEntity> dles = m_DeviceLicenseDAO.findAll();
			for ( DeviceLicenseEntity dle : dles )
			{
				if ( ( dle.getDeviceId() == m_DeviceId ) && ( dle.getType() == m_Type ) )
				{
					return dle;
				}
			}
			return null;
		}
	}

	public ResourceTopologyServiceIF getTopologyService()
	{
		if ( topologyService == null )
		{
			topologyService = ( ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyServiceProxy_internal" ) );
		}
		return topologyService;
	}

	public void setDeviceLicenseDAO( DeviceLicenseDAO deviceLicenseDAO )
	{
		m_DeviceLicenseDAO = deviceLicenseDAO;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		m_EventRegistry = eventRegistry;
	}

	public void setTaskScheduler( TaskScheduler ts )
	{
		m_TaskScheduler = ts;
	}

	public void setServerLicenseBO( ServerLicenseBO slb )
	{
		m_ServerLicenseBO = slb;
	}

	public void setDeviceService( DeviceService ds )
	{
		m_DeviceService = ds;
	}

	public void setDeviceRegistry( DeviceRegistry dr )
	{
		m_DeviceRegistry = dr;
	}
}
