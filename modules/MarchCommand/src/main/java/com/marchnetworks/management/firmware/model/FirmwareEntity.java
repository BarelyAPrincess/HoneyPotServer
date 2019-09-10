package com.marchnetworks.management.firmware.model;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.management.firmware.data.Firmware;
import com.marchnetworks.management.firmware.data.UpdateStateEnum;
import com.marchnetworks.management.firmware.data.UpdateTypeEnum;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "FIRMWARE" )
public class FirmwareEntity implements Serializable
{
	private static final long serialVersionUID = -3256245362760358124L;
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Column( name = "DEVICE_ID", nullable = false )
	private Long deviceId;
	@Column( name = "SCHEDULER_ID" )
	private Long schedulerId;
	@Column( name = "TARGET_FIRMWARE_ID" )
	private Long targetFirmwareId;
	@Enumerated( EnumType.STRING )
	@Column( name = "UPDATE_TYPE", nullable = false )
	private UpdateTypeEnum updateType;
	@Enumerated( EnumType.STRING )
	@Column( name = "UPDATE_STATE" )
	private UpdateStateEnum updateState;
	@Column( name = "OPT_PARAMETERS" )
	private String optParameters;
	@Column( name = "FAILURE_RETRY_COUNT" )
	private long failureRetryCount;

	public FirmwareEntity()
	{
		updateState = UpdateStateEnum.FIRMWARE_UPGRADE_IDLE;
	}

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public Long getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( Long deviceId )
	{
		this.deviceId = deviceId;
	}

	public Long getSchedulerId()
	{
		return schedulerId;
	}

	public void setSchedulerId( Long schedulerId )
	{
		this.schedulerId = schedulerId;
	}

	public Long getTargetFirmwareId()
	{
		return targetFirmwareId;
	}

	public void setTargetFirmwareId( Long targetFirmwareId )
	{
		this.targetFirmwareId = targetFirmwareId;
	}

	public UpdateTypeEnum getUpdateType()
	{
		return updateType;
	}

	public void setUpdateType( UpdateTypeEnum updateType )
	{
		this.updateType = updateType;
	}

	public UpdateStateEnum getUpdateState()
	{
		return updateState;
	}

	public void setUpdateState( UpdateStateEnum updateState )
	{
		this.updateState = updateState;
	}

	public String getOptParameters()
	{
		return optParameters;
	}

	public void getOptParameters( String optParameters )
	{
		this.optParameters = optParameters;
	}

	public long getFailureRetryCount()
	{
		return failureRetryCount;
	}

	public void setFailureRetryCount( long count )
	{
		failureRetryCount = count;
	}

	public Firmware toDataObject()
	{
		Firmware firmware = new Firmware();
		firmware.setDeviceId( deviceId.toString() );
		if ( targetFirmwareId != null )
		{
			firmware.setFirmwareId( targetFirmwareId.toString() );
		}
		firmware.setUpdateType( updateType );
		if ( ( schedulerId != null ) && ( schedulerId.longValue() != 0L ) )
		{
			firmware.setSchedulerId( schedulerId );
		}
		firmware.setUpdateState( updateState );
		firmware.setOptParams( optParameters );
		return firmware;
	}

	public void readFromDataObject( Firmware firmware )
	{
		deviceId = Long.valueOf( firmware.getDeviceId() );
		if ( !CommonAppUtils.isNullOrEmptyString( firmware.getFirmwareId() ) )
		{
			targetFirmwareId = Long.valueOf( firmware.getFirmwareId() );
		}
		else
		{
			targetFirmwareId = null;
		}
		updateType = firmware.getUpdateType();
		schedulerId = firmware.getSchedulerId();
		optParameters = firmware.getOptParams();
		failureRetryCount = 0L;
	}
}

