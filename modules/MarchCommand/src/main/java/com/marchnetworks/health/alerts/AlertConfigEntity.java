package com.marchnetworks.health.alerts;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.health.data.AlertConfigData;
import com.marchnetworks.health.data.AlertThresholdData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table( name = "ALERT_CONFIG" )
public class AlertConfigEntity implements Serializable
{
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Version
	@GeneratedValue
	@Column( name = "VERSION" )
	private Long version;
	@Lob
	@Column( name = "DATA", length = 20971520 )
	private byte[] configData;

	public AlertConfigEntity()
	{
	}

	public AlertConfigEntity( byte[] configData )
	{
		this.configData = configData;
	}

	public AlertConfigEntity( List<AlertThresholdData> thresholds )
	{
		setConfigDataThresholds( thresholds );
	}

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public void setConfigDataThresholds( Collection<AlertThresholdData> thresholds )
	{
		setConfigDataString( CoreJsonSerializer.toJson( thresholds ) );
	}

	public List<AlertThresholdData> getConfigDataThresholds()
	{
		return CoreJsonSerializer.collectionFromJson( getConfigDataString(), new TypeToken<ArrayList<AlertThresholdData>>()
		{
		} );
	}

	public String getConfigDataString()
	{
		return CommonAppUtils.encodeToUTF8String( configData );
	}

	public void setConfigDataString( String configData )
	{
		this.configData = CommonAppUtils.encodeStringToBytes( configData );
	}

	public void setConfigData( byte[] configData )
	{
		this.configData = configData;
	}

	public byte[] getConfigData()
	{
		return configData;
	}

	public void setVersion( Long version )
	{
		this.version = version;
	}

	public Long getVersion()
	{
		return version;
	}

	public void setAlertConfig( AlertConfigData alertConfig )
	{
		setConfigDataThresholds( alertConfig.getThresholds() );
	}

	public AlertConfigData getAlertConfig()
	{
		return new AlertConfigData( version, CoreJsonSerializer.collectionFromJson( getConfigDataString(), new TypeToken<ArrayList<AlertThresholdData>>()
		{
		} ) );
	}
}
