package com.marchnetworks.management.instrumentation.model;

import com.marchnetworks.command.common.device.data.AudioEncoderView;
import com.marchnetworks.command.common.device.data.DataEncoderView;
import com.marchnetworks.command.common.device.data.EncoderView;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.command.common.device.data.TextEncoderView;
import com.marchnetworks.command.common.device.data.VideoEncoderView;
import com.marchnetworks.command.common.transport.data.ConfigurationURL;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.management.instrumentation.data.DeviceNetworkInfoType;
import com.marchnetworks.server.communications.transport.datamodel.AudioDetails;
import com.marchnetworks.server.communications.transport.datamodel.ChannelDetails;
import com.marchnetworks.server.communications.transport.datamodel.DataDetails;
import com.marchnetworks.server.communications.transport.datamodel.DeviceDetails;
import com.marchnetworks.server.communications.transport.datamodel.TextDetails;
import com.marchnetworks.server.communications.transport.datamodel.VideoDetails;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.StandardMBean;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

@javax.persistence.Entity
@javax.persistence.Table( name = "DEVICE" )
@javax.persistence.Inheritance( strategy = javax.persistence.InheritanceType.SINGLE_TABLE )
@javax.persistence.DiscriminatorColumn( name = "DISCRIMINATOR", discriminatorType = javax.persistence.DiscriminatorType.STRING, length = 20 )
@javax.persistence.DiscriminatorValue( "Device" )
public class Device extends StandardMBean implements DeviceMBean, java.io.Serializable
{
	private static final long serialVersionUID = -7933814823854368339L;
	@javax.persistence.Transient
	private Map<String, Object> additionalDeviceRegistrationInfo;
	@javax.persistence.Id
	@javax.persistence.GeneratedValue
	@Column( name = "DEVICE_ID" )
	protected Long deviceId;
	@Column( name = "ADDRESS" )
	protected String address;
	@Column( name = "TYPE" )
	protected String type;
	@Column( name = "MODEL" )
	protected String model;
	@Column( name = "MODEL_NAME" )
	protected String modelName;
	@Column( name = "SERIAL" )
	protected String serial;
	@Column( name = "SOFTWARE_VERSION" )
	protected String softwareVersion;
	@Column( name = "FAMILY" )
	protected String family;
	@Column( name = "FAMILY_NAME" )
	protected String familyName;
	@Column( name = "MANUFACTURER" )
	protected String manufacturer;

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( "Device [additionalDeviceRegistrationInfo=" ).append( additionalDeviceRegistrationInfo ).append( ", deviceId=" ).append( deviceId ).append( ", address=" ).append( address ).append( ", type=" ).append( type ).append( ", model=" ).append( model ).append( ", modelName=" ).append( modelName ).append( ", serial=" ).append( serial ).append( ", softwareVersion=" ).append( softwareVersion ).append( ", family=" ).append( family ).append( ", familyName=" ).append( familyName ).append( ", manufacturer=" ).append( manufacturer ).append( ", manufacturerName=" ).append( manufacturerName ).append( ", hardwareVersion=" ).append( hardwareVersion ).append( ", timeCreated=" ).append( timeCreated ).append( ", lastCommunicationTime=" ).append( lastCommunicationTime ).append( ", timeRegStatusChanged=" ).append( timeRegStatusChanged ).append( ", registrationErrorMessage=" ).append( registrationErrorMessage ).append( ", parentDevice=" ).append( parentDevice ).append( ", registrationStatus=" ).append( registrationStatus ).append( ", m_ChannelsMax=" ).append( m_ChannelsMax ).append( ", m_ChannelsInUse=" ).append( m_ChannelsInUse ).append( ", m_sConfigurationUrl=" ).append( m_sConfigurationUrl ).append( ", deviceNetworkInfos=" ).append( deviceNetworkInfos ).append( ", channels=" ).append( channels ).append( ", version=" ).append( version ).append( ", patchList=" ).append( patchList ).append( "]" );

		return builder.toString();
	}

	@Column( name = "MANUFACTURER_NAME" )
	protected String manufacturerName;

	@Column( name = "HARDWARE_VERSION" )
	protected String hardwareVersion;

	@Column( name = "TIME_CREATED" )
	@Type( type = "com.marchnetworks.common.hibernate.UTCCalendarType" )
	@Temporal( TemporalType.DATE )
	private Calendar timeCreated;

	@Column( name = "LAST_COMMUNICATION_TIME", nullable = true )
	@Type( type = "com.marchnetworks.common.hibernate.UTCCalendarType" )
	@Temporal( TemporalType.DATE )
	private Calendar lastCommunicationTime;

	@Column( name = "TIME_REG_STAUTS_CHANGED" )
	@Type( type = "com.marchnetworks.common.hibernate.UTCCalendarType" )
	@Temporal( TemporalType.DATE )
	private Calendar timeRegStatusChanged;

	@Column( name = "REG_ERROR_MSG", length = 2048 )
	protected String registrationErrorMessage;

	@javax.persistence.ManyToOne
	@javax.persistence.JoinColumn( name = "PARENT_DEVICE" )
	protected CompositeDevice parentDevice;

	@Column( name = "REGISTRATION_STATUS" )
	@javax.persistence.Enumerated( javax.persistence.EnumType.STRING )
	protected RegistrationStatus registrationStatus;

	@Column( name = "CHANNELS_MAX" )
	private Integer m_ChannelsMax;

	@Column( name = "CHANNELS_INUSE" )
	private Integer m_ChannelsInUse;

	@Column( name = "CONFIGURATION_URL", length = 4000, nullable = true )
	private String m_sConfigurationUrl;

	@Column( name = "PATCH_LIST", length = 4000, nullable = true )
	protected String patchList;

	@OneToMany( mappedBy = "device", cascade = {javax.persistence.CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.EAGER )
	private Set<DeviceNetworkInfo> deviceNetworkInfos = new java.util.TreeSet();
	@OneToMany( cascade = {javax.persistence.CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.EAGER )
	@javax.persistence.JoinColumn( name = "DEVICE", nullable = true )
	private Map<String, Channel> channels = new HashMap();

	@javax.persistence.Version
	@Column( name = "VERSION" )
	protected Long version;

	public Device()
	{
		super( DeviceMBean.class, false );
	}

	protected Device( Class<?> mbeanInterface, boolean isMXBean )
	{
		super( mbeanInterface, isMXBean );
	}

	public String getParentDeviceId()
	{
		if ( parentDevice != null )
		{
			return parentDevice.getDeviceId();
		}
		return null;
	}

	public CompositeDevice getParentDevice()
	{
		return parentDevice;
	}

	public void setParentDevice( CompositeDevice parentDevice )
	{
		this.parentDevice = parentDevice;
	}

	public void setDeviceSystemInfoFromTransport( DeviceDetails deviceDetails )
	{
		if ( isValidTransportObject( deviceDetails ) )
		{
			setModel( String.valueOf( deviceDetails.getModelId() ) );
			setModelName( deviceDetails.getModelName() );
			setDeviceNetworkInfoFromTransport( deviceDetails );
			setSoftwareVersion( deviceDetails.getSwVersion() );
			setSerial( deviceDetails.getSerial() );
			setFamily( String.valueOf( deviceDetails.getFamilyId() ) );
			setFamilyName( deviceDetails.getFamilyName() );
			setManufacturer( String.valueOf( deviceDetails.getManufacturerId() ) );
			setManufacturerName( deviceDetails.getManufacturerName() );
			setHardwareVersion( deviceDetails.getHwVersion() );
			setConfigurationUrl( deviceDetails.getConfigurationURL() );
			setPatchList( deviceDetails.getPatchList() );
		}
	}

	public void setDeviceInfoFromTransport( DeviceDetails deviceDetails )
	{
		setDeviceSystemInfoFromTransport( deviceDetails );
		if ( isValidTransportObject( deviceDetails ) )
		{
			for ( ChannelDetails channelDetail : deviceDetails.getDeviceChannels() )
			{
				updateChannelFromTransport( channelDetail );
			}
		}
	}

	public void setPartialDeviceInfo( DeviceDetails d )
	{
		if ( !isValidTransportObject( d ) )
		{
			return;
		}

		setManufacturer( String.valueOf( d.getManufacturerId() ) );
		setFamily( String.valueOf( d.getFamilyId() ) );
		setModel( String.valueOf( d.getModelId() ) );
		setChannelsInUse( d.getChannelsInUse() );
		setChannelsMax( d.getMaxChannels() );
	}

	protected boolean isValidTransportObject( DeviceDetails deviceDetail )
	{
		if ( deviceDetail == null )
		{
			return false;
		}

		if ( ( deviceDetail.getIPAddresses() == null ) || ( deviceDetail.getIPAddresses().length == 0 ) )
		{
			return false;
		}

		return true;
	}

	protected void setDeviceNetworkInfoFromTransport( DeviceDetails deviceDetail )
	{
		getDeviceNetworkInfos().clear();
		if ( ( deviceDetail.getMACAddresses() != null ) && ( deviceDetail.getMACAddresses().length > 0 ) )
		{
			DeviceNetworkInfo deviceNetInfo = new DeviceNetworkInfo();
			deviceNetInfo.setNetworkInfoType( DeviceNetworkInfoType.MAC_ADDRESS );
			deviceNetInfo.setValue( CoreJsonSerializer.toJson( deviceDetail.getMACAddresses() ) );
			deviceNetInfo.setDevice( this );
			getDeviceNetworkInfos().add( deviceNetInfo );
		}
		if ( ( deviceDetail.getIPAddresses() != null ) && ( deviceDetail.getIPAddresses().length > 0 ) )
		{
			DeviceNetworkInfo deviceNetInfo = new DeviceNetworkInfo();
			deviceNetInfo.setNetworkInfoType( DeviceNetworkInfoType.NETWORK_ADDRESS );
			deviceNetInfo.setValue( CoreJsonSerializer.toJson( deviceDetail.getIPAddresses() ) );
			deviceNetInfo.setDevice( this );
			getDeviceNetworkInfos().add( deviceNetInfo );
		}

		if ( ( deviceDetail.getNetworkNames() != null ) && ( deviceDetail.getNetworkNames().length > 0 ) )
		{
			DeviceNetworkInfo deviceNetInfo = new DeviceNetworkInfo();
			deviceNetInfo.setNetworkInfoType( DeviceNetworkInfoType.NETWORK_NAME );
			deviceNetInfo.setValue( CoreJsonSerializer.toJson( deviceDetail.getNetworkNames() ) );
			deviceNetInfo.setDevice( this );
			getDeviceNetworkInfos().add( deviceNetInfo );
		}
	}

	public Channel decorateNewChannelFromTransport( Channel newChannel, ChannelDetails channelDetail )
	{
		setChannelInfoFromTransport( newChannel, channelDetail );
		return newChannel;
	}

	public boolean isChannelIdFromDevice( String channelId )
	{
		boolean isFromDevice = false;
		if ( channelId != null )
		{
			for ( Channel deviceChannel : getChannels().values() )
			{
				if ( channelId.equals( deviceChannel.getChannelId() ) )
				{
					isFromDevice = true;
					break;
				}
			}
		}

		return isFromDevice;
	}

	public void updateChannelFromTransport( ChannelDetails channelDetail )
	{
		if ( ChannelDetails.isChannelStateUnknown( channelDetail ) )
		{
			return;
		}

		if ( isChannelIdFromDevice( channelDetail.getId() ) )
		{
			Channel currentChannel = getChannelFromDevice( channelDetail.getId() );
			setChannelInfoFromTransport( currentChannel, channelDetail );
		}
	}

	private void setChannelInfoFromTransport( Channel channel, ChannelDetails channelDetail )
	{
		if ( channelDetail != null )
		{
			channel.setChannelId( channelDetail.getId() );
			channel.setName( channelDetail.getName() );
			channel.setChannelState( com.marchnetworks.command.common.device.data.ChannelState.valueOf( channelDetail.getChannelState().name() ) );
			channel.setPtzDomeIdentifier( channelDetail.getPtzDomeId() );
			channel.setAssocIds( channelDetail.getAssocIds() );

			int size = 0;
			if ( channelDetail.getVideo() != null )
			{
				size += channelDetail.getVideo().length;
			}
			if ( channelDetail.getAudio() != null )
			{
				size += channelDetail.getAudio().length;
			}
			if ( channelDetail.getText() != null )
			{
				size += channelDetail.getText().length;
			}
			if ( channelDetail.getData() != null )
			{
				size += channelDetail.getData().length;
			}
			EncoderView[] encoders = new EncoderView[size];
			int i = 0;
			if ( channelDetail.getVideo() != null )
			{
				for ( VideoDetails videoDetail : channelDetail.getVideo() )
				{
					VideoEncoderView videoEncoder = new VideoEncoderView();
					videoEncoder.setEncoderId( videoDetail.getEncoderId() );
					videoEncoder.setCodec( videoDetail.getCodec() );
					videoEncoder.setCodecPrvData( videoDetail.getCodecPrvData() );
					videoEncoder.setEstBPS( videoDetail.getEstBPS() );
					videoEncoder.setFps( videoDetail.getFps() );
					videoEncoder.setHeight( videoDetail.getHeight() );
					videoEncoder.setProfile( videoDetail.getProfile() );
					videoEncoder.setResolutionHint( videoDetail.getResolutionHint() );
					videoEncoder.setWidth( videoDetail.getWidth() );
					encoders[( i++ )] = videoEncoder;
				}
			}
			if ( channelDetail.getAudio() != null )
			{
				for ( AudioDetails audioDetail : channelDetail.getAudio() )
				{
					AudioEncoderView audioEncoder = new AudioEncoderView();
					audioEncoder.setEncoderId( audioDetail.getEncoderId() );
					audioEncoder.setCodec( audioDetail.getCodec() );
					audioEncoder.setCodecPrvData( audioDetail.getCodecPrvData() );
					audioEncoder.setEstBPS( audioDetail.getEstBPS() );
					audioEncoder.setSampBits( audioDetail.getSampBits() );
					audioEncoder.setSampRate( audioDetail.getSampRate() );
					encoders[( i++ )] = audioEncoder;
				}
			}
			if ( channelDetail.getText() != null )
			{
				for ( TextDetails textDetail : channelDetail.getText() )
				{
					TextEncoderView textEncoder = new TextEncoderView();
					textEncoder.setEncoderId( textDetail.getEncoderId() );
					textEncoder.setProtocolName( textDetail.getProtocolName() );
					encoders[( i++ )] = textEncoder;
				}
			}
			if ( channelDetail.getData() != null )
			{
				for ( DataDetails dataDetail : channelDetail.getData() )
				{
					DataEncoderView dataEncoder = new DataEncoderView();
					dataEncoder.setEncoderId( dataDetail.getEncoderId() );
					dataEncoder.setCodec( dataDetail.getCodec() );
					encoders[( i++ )] = dataEncoder;
				}
			}
			channel.setEncoders( encoders );
		}
	}

	public Channel getChannelFromDevice( String channelId )
	{
		Channel deviceChannel = null;
		if ( channelId != null )
		{
			for ( Channel channel : getChannels().values() )
			{
				if ( channelId.equals( channel.getChannelId() ) )
				{
					deviceChannel = channel;
					break;
				}
			}
		}
		return deviceChannel;
	}

	public boolean isR5()
	{
		String manuID = getManufacturer();
		String famID = getFamily();
		return com.marchnetworks.command.api.rest.DeviceManagementConstants.isR5Device( manuID, famID );
	}

	public boolean isRootDevice()
	{
		return getParentDevice() == null;
	}

	public String getAddress()
	{
		DeviceNetworkInfo netAddressInfo = getDeviceNetworkInfoSetByType( DeviceNetworkInfoType.NETWORK_ADDRESS );
		if ( netAddressInfo == null )
		{
			return address;
		}
		String[] netAddresses = ( String[] ) CoreJsonSerializer.fromJson( netAddressInfo.getValue(), String[].class );
		return netAddresses[0];
	}

	public String[] getIpAddresses()
	{
		DeviceNetworkInfo ipAddressInfo = getDeviceNetworkInfoSetByType( DeviceNetworkInfoType.NETWORK_ADDRESS );
		if ( ipAddressInfo == null )
		{
			return null;
		}
		return ( String[] ) CoreJsonSerializer.fromJson( ipAddressInfo.getValue(), String[].class );
	}

	public String getDeviceId()
	{
		if ( deviceId != null )
		{
			return String.valueOf( deviceId );
		}
		return null;
	}

	public String getModel()
	{
		return model;
	}

	public String getSoftwareVersion()
	{
		return softwareVersion;
	}

	public void setDeviceId( String deviceId )
	{
		this.deviceId = Long.valueOf( deviceId );
	}

	public void setModel( String model )
	{
		this.model = model;
	}

	public void setSoftwareVersion( String softwareVersion )
	{
		this.softwareVersion = softwareVersion;
	}

	public String getRegistrationStatusInString()
	{
		if ( registrationStatus != null )
		{
			return registrationStatus.toString();
		}
		return null;
	}

	public RegistrationStatus getRegistrationStatus()
	{
		return registrationStatus;
	}

	public void setRegistrationStatus( RegistrationStatus registrationStatus )
	{
		this.registrationStatus = registrationStatus;
	}

	public String getType()
	{
		return type;
	}

	public void setType( String type )
	{
		this.type = type;
	}

	public Long getVersion()
	{
		return version;
	}

	public void setVersion( Long version )
	{
		this.version = version;
	}

	public String getSerial()
	{
		return serial;
	}

	public void setSerial( String serial )
	{
		this.serial = serial;
	}

	public long getTimeCreatedInMillis()
	{
		return timeCreated.getTimeInMillis();
	}

	public String getTimeRegStatusChangedInString()
	{
		return timeRegStatusChanged.getTime().toString();
	}

	public Calendar getTimeCreated()
	{
		return timeCreated;
	}

	public void setTimeCreated( Calendar timeCreated )
	{
		this.timeCreated = timeCreated;
	}

	public Calendar getLastCommunicationTime()
	{
		return lastCommunicationTime;
	}

	public void setLastCommunicationTime( Calendar lastCommunicationTime )
	{
		this.lastCommunicationTime = lastCommunicationTime;
	}

	public Long getLastCommunicationTimeInMillis()
	{
		if ( lastCommunicationTime != null )
		{
			return Long.valueOf( lastCommunicationTime.getTimeInMillis() );
		}
		return null;
	}

	public com.marchnetworks.command.common.device.data.ConnectState getConnectState()
	{
		DeviceRegistry dr = getDeviceRegistry();
		return dr.getConnectState( deviceId.toString() );
	}

	public String getModelName()
	{
		return modelName;
	}

	public void setModelName( String modelName )
	{
		this.modelName = modelName;
	}

	public Map<String, Channel> getChannels()
	{
		return channels;
	}

	public void setStreams( Map<String, Channel> channels )
	{
		this.channels = channels;
	}

	public Map<String, ChannelMBean> getChannelMBeans()
	{
		Map<String, ChannelMBean> ret = new HashMap();
		ret.putAll( getChannels() );
		return ret;
	}

	public Calendar getTimeRegStatusChanged()
	{
		return timeRegStatusChanged;
	}

	public void setTimeRegStatusChanged( Calendar timeRegStatusChanged )
	{
		this.timeRegStatusChanged = timeRegStatusChanged;
	}

	public String getRegistrationErrorMessage()
	{
		return registrationErrorMessage;
	}

	public void setRegistrationErrorMessage( String registrationErrorMessage )
	{
		this.registrationErrorMessage = registrationErrorMessage;
		if ( this.registrationErrorMessage.length() > 2048 )
		{
			this.registrationErrorMessage = this.registrationErrorMessage.substring( 0, 2048 );
		}
	}

	public Map<String, Object> getAdditionalDeviceRegistrationInfo()
	{
		return additionalDeviceRegistrationInfo;
	}

	public void setAdditionalDeviceRegistrationInfo( Map<String, Object> additionalDeviceRegistrationInfo )
	{
		this.additionalDeviceRegistrationInfo = additionalDeviceRegistrationInfo;
	}

	@Deprecated
	public String getSubModel()
	{
		return null;
	}

	@Deprecated
	public String getSubModelName()
	{
		return null;
	}

	public String getFamily()
	{
		return family;
	}

	public void setFamily( String family )
	{
		this.family = family;
	}

	public String getFamilyName()
	{
		return familyName;
	}

	public void setFamilyName( String familyName )
	{
		this.familyName = familyName;
	}

	public String getManufacturer()
	{
		return manufacturer;
	}

	public void setManufacturer( String manufacturer )
	{
		this.manufacturer = manufacturer;
	}

	public String getManufacturerName()
	{
		return manufacturerName;
	}

	public void setManufacturerName( String manufacturerName )
	{
		this.manufacturerName = manufacturerName;
	}

	public String getPatchList()
	{
		return patchList;
	}

	public void setPatchList( String patchList )
	{
		this.patchList = patchList;
	}

	public String getHardwareVersion()
	{
		return hardwareVersion;
	}

	public void setHardwareVersion( String hardwareVersion )
	{
		this.hardwareVersion = hardwareVersion;
	}

	public Integer getChannelsMax()
	{
		return m_ChannelsMax;
	}

	public void setChannelsMax( Integer channelsMax )
	{
		m_ChannelsMax = channelsMax;
	}

	public Integer getChannelsInUse()
	{
		return m_ChannelsInUse;
	}

	public void setChannelsInUse( Integer channelsInUse )
	{
		m_ChannelsInUse = channelsInUse;
	}

	public ConfigurationURL getConfigurationUrl()
	{
		if ( com.marchnetworks.command.common.CommonAppUtils.isNullOrEmptyString( m_sConfigurationUrl ) )
		{
			return null;
		}
		return ( ConfigurationURL ) CoreJsonSerializer.fromJson( m_sConfigurationUrl, ConfigurationURL.class );
	}

	public void setConfigurationUrl( ConfigurationURL cu )
	{
		if ( cu == null )
		{
			m_sConfigurationUrl = null;
		}
		else
		{
			m_sConfigurationUrl = CoreJsonSerializer.toJson( cu );
		}
	}

	public void setDeviceId( Long deviceId )
	{
		this.deviceId = deviceId;
	}

	public String convertToDeviceIdFromChannelId( String channelId )
	{
		if ( channelId != null )
		{
			for ( ChannelMBean channel : getChannels().values() )
			{
				if ( channel.getChannelId().equals( channelId ) )
				{
					return getDeviceId();
				}
			}
		}
		return null;
	}

	public void setAddress( String address )
	{
		this.address = address;
	}

	public String getMacAddress()
	{
		DeviceNetworkInfo macInfo = getDeviceNetworkInfoSetByType( DeviceNetworkInfoType.MAC_ADDRESS );
		if ( macInfo == null )
		{
			return null;
		}

		String[] macInfoArray = ( String[] ) CoreJsonSerializer.fromJson( macInfo.getValue(), String[].class );
		return macInfoArray[0];
	}

	public String[] getMacAddresses()
	{
		DeviceNetworkInfo macAddressInfo = getDeviceNetworkInfoSetByType( DeviceNetworkInfoType.MAC_ADDRESS );
		if ( macAddressInfo == null )
		{
			return null;
		}
		return ( String[] ) CoreJsonSerializer.fromJson( macAddressInfo.getValue(), String[].class );
	}

	public String getName()
	{
		DeviceNetworkInfo networkNames = getDeviceNetworkInfoSetByType( DeviceNetworkInfoType.NETWORK_NAME );
		if ( networkNames == null )
		{
			return null;
		}
		String[] networkNamesArray = ( String[] ) CoreJsonSerializer.fromJson( networkNames.getValue(), String[].class );
		return networkNamesArray[0];
	}

	public String[] getNames()
	{
		DeviceNetworkInfo hostnameInfo = getDeviceNetworkInfoSetByType( DeviceNetworkInfoType.NETWORK_NAME );
		if ( hostnameInfo == null )
		{
			return null;
		}
		return ( String[] ) CoreJsonSerializer.fromJson( hostnameInfo.getValue(), String[].class );
	}

	public Set<DeviceNetworkInfo> getDeviceNetworkInfos()
	{
		return deviceNetworkInfos;
	}

	public void setDeviceNetworkInfos( Set<DeviceNetworkInfo> deviceNetworkInfoSet )
	{
		deviceNetworkInfos = deviceNetworkInfoSet;
	}

	public DeviceNetworkInfo getDeviceNetworkInfoSetByType( DeviceNetworkInfoType netInfoType )
	{
		DeviceNetworkInfo deviceNetworkInfo = null;

		for ( DeviceNetworkInfo dni : getDeviceNetworkInfos() )
		{
			if ( dni.getNetworkInfoType().equals( netInfoType ) )
			{
				deviceNetworkInfo = dni;
				break;
			}
		}
		return deviceNetworkInfo;
	}

	protected DeviceRegistry getDeviceRegistry()
	{
		return ( DeviceRegistry ) com.marchnetworks.common.spring.ApplicationContextSupport.getBean( "deviceRegistry" );
	}
}

