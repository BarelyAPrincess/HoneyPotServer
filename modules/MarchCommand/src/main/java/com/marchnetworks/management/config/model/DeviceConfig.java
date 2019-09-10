package com.marchnetworks.management.config.model;

import com.marchnetworks.management.config.DeviceImageState;
import com.marchnetworks.management.config.DeviceSnapshotState;
import com.marchnetworks.management.config.service.DeviceConfigDescriptor;
import com.marchnetworks.management.instrumentation.model.Device;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table( name = "DEVICECONFIG" )
public class DeviceConfig implements Serializable
{
	private static final long serialVersionUID = -5709651716191322023L;
	private static final Logger LOG = LoggerFactory.getLogger( DeviceConfig.class );

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn( name = "FK_DEVICEIMAGE_ID" )
	private DeviceImage image;

	@OneToOne( targetEntity = ConfigSnapshot.class )
	@JoinColumn( name = "FK_CONFIGSNAPSHOT_ID" )
	private ConfigSnapshot snapshot;

	@OneToOne( targetEntity = Device.class )
	@JoinColumn( name = "FK_DEVICE_ID" )
	private DeviceMBean device;

	@Enumerated( EnumType.STRING )
	@Column( name = "ASSIGN_STATE", nullable = false )
	private DeviceImageState assignState;

	@Enumerated( EnumType.STRING )
	@Column( name = "SNAPSHOT_STATE", nullable = false )
	private DeviceSnapshotState snapshotState;

	@Version
	@Column( name = "VER_OPTLOCK" )
	private Long version;

	@Column( name = "FAILURE_RETRY_COUNT" )
	private Long failureRetryCount;

	public DeviceConfig()
	{
	}

	public DeviceConfig( DeviceMBean dev )
	{
		device = dev;
		assignState = DeviceImageState.UNASSOCIATED;
		snapshotState = DeviceSnapshotState.UNKNOWN;
	}

	@XmlTransient
	public DeviceMBean getDevice()
	{
		return device;
	}

	public void setDevice( DeviceMBean val )
	{
		device = val;
	}

	public DeviceImage getImage()
	{
		return image;
	}

	public void setImage( DeviceImage val )
	{
		image = val;
	}

	public ConfigSnapshot getSnapshot()
	{
		return snapshot;
	}

	public void setSnapshot( ConfigSnapshot val )
	{
		snapshot = val;
	}

	public DeviceImageState getAssignState()
	{
		return assignState;
	}

	public void setAssignState( DeviceImageState val )
	{
		LOG.debug( "setAssignState:" + val );
		assignState = val;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public Long getId()
	{
		return id;
	}

	public void setSnapshotState( DeviceSnapshotState snapshotState )
	{
		LOG.debug( "setSnapshotState:" + snapshotState );
		this.snapshotState = snapshotState;
	}

	public DeviceSnapshotState getSnapshotState()
	{
		return snapshotState;
	}

	public Long getVersion()
	{
		return version;
	}

	public void setVersion( Long version )
	{
		this.version = version;
	}

	public Long getFailureRetryCount()
	{
		if ( failureRetryCount == null )
		{
			failureRetryCount = Long.valueOf( 0L );
		}
		return failureRetryCount;
	}

	public void setFailureRetryCount( Long count )
	{
		failureRetryCount = count;
	}

	public DeviceConfigDescriptor toDataObject()
	{
		DeviceConfigDescriptor deviceConfig = new DeviceConfigDescriptor();

		deviceConfig.setId( getId().toString() );
		deviceConfig.setAssignState( resolveDescriptorAssignState() );
		if ( getDevice() != null )
		{
			deviceConfig.setDeviceID( getDevice().getDeviceId() );
		}
		if ( getImage() != null )
		{
			deviceConfig.setImageID( getImage().getId().toString() );
		}

		if ( getSnapshot() == null )
		{
			deviceConfig.setFirmwareVersion( getDevice().getSoftwareVersion() );
			deviceConfig.setDeviceModel( getDevice().getModel() );
			deviceConfig.setDeviceFamily( getDevice().getFamily() );
			deviceConfig.setDeviceSerial( getDevice().getSerial() );
		}
		else
		{
			deviceConfig.setFirmwareVersion( getSnapshot().getFirmwareVersion() );
			deviceConfig.setDeviceModel( getSnapshot().getModel() );
			deviceConfig.setDeviceFamily( getSnapshot().getFamily() );
			deviceConfig.setDeviceSerial( getSnapshot().getSerial() );
		}

		return deviceConfig;
	}

	private DeviceImageState resolveDescriptorAssignState()
	{
		if ( ( getAssignState().equals( DeviceImageState.FAILED ) ) || ( getAssignState().equals( DeviceImageState.PENDING ) ) )
		{
			return getAssignState();
		}

		if ( ( getSnapshotState() != null ) && ( getSnapshotState().equals( DeviceSnapshotState.MISMATCH ) ) )
		{
			return DeviceImageState.MISMATCH;
		}
		return getAssignState();
	}
}
