package com.marchnetworks.management.config.model;

import com.marchnetworks.command.common.CommonAppUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table( name = "CONFIGSNAPSHOT" )
public class ConfigSnapshot implements Serializable
{
	private static final long serialVersionUID = -4335642396612191509L;
	private static Logger LOG = LoggerFactory.getLogger( ConfigSnapshot.class );

	@Id
	@GeneratedValue
	private Long id;

	@Column( name = "NAME" )
	private String name;

	@Column( name = "DESCRIPTION", length = 4000, nullable = true )
	private String description;

	@Lob
	@Column( name = "CONFIGDATA", length = 2000000 )
	private byte[] configData;

	@Column( name = "FIRMWARE_VERSION" )
	private String firmwareVersion;

	@Column( name = "FAMILY" )
	private String family;

	@Column( name = "MODEL" )
	private String model;

	@Column( name = "HASH" )
	private String hash;

	@Column( name = "CFG_TIMESTAMP" )
	private String timestamp;

	@Column( name = "SERIAL" )
	private String serial;

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription( String description )
	{
		this.description = description;
	}

	public void setFirmwareVersion( String version )
	{
		firmwareVersion = version;
	}

	public void setModel( String model )
	{
		this.model = model;
	}

	public String getFirmwareVersion()
	{
		return firmwareVersion;
	}

	public String getModel()
	{
		return model;
	}

	public String getConfigDataAsString()
	{
		return CommonAppUtils.encodeToUTF8String( configData );
	}

	public byte[] getConfigData()
	{
		return configData;
	}

	public void setConfigDataAsString( String val )
	{
		configData = CommonAppUtils.encodeStringToBytes( val );
	}

	public void setConfigData( byte[] val )
	{
		configData = val;
	}

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public String getHash()
	{
		return hash;
	}

	public void setHash( String hash )
	{
		this.hash = hash;
	}

	public String getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp( String timestamp )
	{
		this.timestamp = timestamp;
	}

	public String getFamily()
	{
		return family;
	}

	public void setFamily( String family )
	{
		this.family = family;
	}

	public void readConfigData( String filePath ) throws IOException
	{
		String config = readFileAsString( filePath );
		setConfigDataAsString( config );
	}

	private String readFileAsString( String filePath ) throws IOException
	{
		LOG.info( "Reading config file {}", filePath );
		byte[] buffer = new byte[( int ) new File( filePath ).length()];
		FileInputStream f = new FileInputStream( filePath );
		f.read( buffer );
		LOG.info( "Read {} bytes", Integer.valueOf( buffer.length ) );
		f.close();
		return new String( buffer );
	}

	public ConfigSnapshot copy()
	{
		ConfigSnapshot copyConfig = new ConfigSnapshot();
		copyConfig.setConfigData( getConfigData() );
		copyConfig.setDescription( getDescription() );
		copyConfig.setFirmwareVersion( getFirmwareVersion() );
		copyConfig.setModel( getModel() );
		copyConfig.setFamily( getFamily() );
		copyConfig.setName( getName() );
		copyConfig.setHash( getHash() );
		copyConfig.setTimestamp( getTimestamp() );
		copyConfig.setSerial( getSerial() );

		return copyConfig;
	}

	public String getSerial()
	{
		return serial;
	}

	public void setSerial( String serial )
	{
		this.serial = serial;
	}
}
