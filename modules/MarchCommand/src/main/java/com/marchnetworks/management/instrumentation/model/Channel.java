package com.marchnetworks.management.instrumentation.model;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.data.AudioEncoderView;
import com.marchnetworks.command.common.device.data.ChannelState;
import com.marchnetworks.command.common.device.data.DataEncoderView;
import com.marchnetworks.command.common.device.data.EncoderView;
import com.marchnetworks.command.common.device.data.TextEncoderView;
import com.marchnetworks.command.common.device.data.VideoEncoderView;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.utils.CommonUtils;

import java.io.Serializable;

import javax.management.StandardMBean;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

@Entity
@Table( name = "CHANNEL", uniqueConstraints = {@javax.persistence.UniqueConstraint( columnNames = {"CHANNEL_ID", "DEVICE"} )} )
@Inheritance( strategy = InheritanceType.SINGLE_TABLE )
public class Channel extends StandardMBean implements ChannelMBean, Serializable
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Column( name = "CHANNEL_ID" )
	private String channelId;
	@Column( name = "NAME" )
	private String name;
	@Column( name = "CHANNEL_STATE" )
	@Enumerated( EnumType.STRING )
	private ChannelState channelState;
	@Column( name = "PTZ_DOME_IDENTIFIER" )
	private String ptzDomeIdentifier;
	@Transient
	private EncoderView[] encoders;
	@Lob
	@Column( name = "ENCODERS_STRING" )
	private byte[] encodersString;
	@Transient
	private String[] assocIds;
	@Column( name = "ASSOC_IDS", length = 4000 )
	private String assocIdsString;
	@ManyToOne
	@JoinColumn( name = "DEVICE" )
	private Device device;
	@Version
	@Column( name = "VERSION" )
	private Long version;

	public Channel()
	{
		super( ChannelMBean.class, false );
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String getPtzDomeIdentifier()
	{
		return ptzDomeIdentifier;
	}

	public void setPtzDomeIdentifier( String ptzDomeIdentifier )
	{
		this.ptzDomeIdentifier = ptzDomeIdentifier;
	}

	public EncoderView[] getEncoders()
	{
		if ( encoders == null )
		{
			String json = getEncodersString();
			if ( json != null )
			{
				encoders = ( ( EncoderView[] ) CoreJsonSerializer.fromJson( json, EncoderView[].class ) );
			}
			else
			{
				encoders = new EncoderView[0];
			}
		}
		return encoders;
	}

	public void setEncoders( EncoderView[] encodersSet )
	{
		encoders = encodersSet;
		setEncodersString( CommonUtils.arrayToJson( encoders ) );
	}

	public void clearEncoders()
	{
		if ( encoders != null )
		{
			encoders = null;
		}
		encodersString = null;
	}

	protected String getEncodersString()
	{
		return CommonAppUtils.encodeToUTF8String( encodersString );
	}

	protected void setEncodersString( String encoders )
	{
		encodersString = CommonAppUtils.encodeStringToBytes( encoders );
	}

	public String[] getAssocIds()
	{
		if ( assocIds == null )
		{
			if ( assocIdsString != null )
			{
				assocIds = ( ( String[] ) CoreJsonSerializer.fromJson( assocIdsString, String[].class ) );
			}
			else
			{
				assocIds = new String[0];
			}
		}
		return assocIds;
	}

	public void setAssocIds( String[] assocIds )
	{
		this.assocIds = assocIds;
		assocIdsString = CommonUtils.arrayToJson( assocIds );
	}

	protected String getAssocIdsString()
	{
		return assocIdsString;
	}

	protected void setAssocIdsString( String assocIdsString )
	{
		this.assocIdsString = assocIdsString;
	}

	public Device getDevice()
	{
		return device;
	}

	public void setDevice( Device device )
	{
		this.device = device;
	}

	public Long getVersion()
	{
		return version;
	}

	public void setVersion( Long version )
	{
		this.version = version;
	}

	public void setChannelState( ChannelState channelState )
	{
		this.channelState = channelState;
	}

	public ChannelState getChannelState()
	{
		return channelState;
	}

	public String getChannelId()
	{
		return channelId;
	}

	public void setChannelId( String channelId )
	{
		this.channelId = channelId;
	}

	public String getChannelStateString()
	{
		return channelState.toString();
	}

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public String getIdAsString()
	{
		if ( id != null )
		{
			return String.valueOf( id );
		}
		return null;
	}

	public AudioEncoderView[] getAudioEncoders()
	{
		int size = 0;
		EncoderView[] encoders = getEncoders();
		for ( EncoderView encoder : encoders )
		{
			if ( ( encoder instanceof AudioEncoderView ) )
			{
				size++;
			}
		}
		if ( size == 0 )
		{
			return AudioEncoderView.NO_ENCODERS;
		}
		AudioEncoderView[] audioEncoders = new AudioEncoderView[size];
		int i = 0;
		for ( EncoderView encoder : getEncoders() )
		{
			if ( ( encoder instanceof AudioEncoderView ) )
			{
				audioEncoders[( i++ )] = ( ( AudioEncoderView ) encoder );
			}
		}
		return audioEncoders;
	}

	public VideoEncoderView[] getVideoEncoders()
	{
		int size = 0;
		EncoderView[] encoders = getEncoders();
		for ( EncoderView encoder : encoders )
		{
			if ( ( encoder instanceof VideoEncoderView ) )
			{
				size++;
			}
		}
		if ( size == 0 )
		{
			return VideoEncoderView.NO_ENCODERS;
		}
		VideoEncoderView[] videoEncoders = new VideoEncoderView[size];
		int i = 0;
		for ( EncoderView encoder : getEncoders() )
		{
			if ( ( encoder instanceof VideoEncoderView ) )
			{
				videoEncoders[( i++ )] = ( ( VideoEncoderView ) encoder );
			}
		}
		return videoEncoders;
	}

	public TextEncoderView[] getTextEncoders()
	{
		int size = 0;
		EncoderView[] encoders = getEncoders();
		for ( EncoderView encoder : encoders )
		{
			if ( ( encoder instanceof TextEncoderView ) )
			{
				size++;
			}
		}
		if ( size == 0 )
		{
			return TextEncoderView.NO_ENCODERS;
		}
		TextEncoderView[] textEncoders = new TextEncoderView[size];
		int i = 0;
		for ( EncoderView encoder : getEncoders() )
		{
			if ( ( encoder instanceof TextEncoderView ) )
			{
				textEncoders[( i++ )] = ( ( TextEncoderView ) encoder );
			}
		}
		return textEncoders;
	}

	public DataEncoderView[] getDataEncoders()
	{
		int size = 0;
		EncoderView[] encoders = getEncoders();
		for ( EncoderView encoder : encoders )
		{
			if ( ( encoder instanceof DataEncoderView ) )
			{
				size++;
			}
		}
		if ( size == 0 )
		{
			return DataEncoderView.NO_ENCODERS;
		}
		DataEncoderView[] dataEncoders = new DataEncoderView[size];
		int i = 0;
		for ( EncoderView encoder : getEncoders() )
		{
			if ( ( encoder instanceof DataEncoderView ) )
			{
				dataEncoders[( i++ )] = ( ( DataEncoderView ) encoder );
			}
		}
		return dataEncoders;
	}
}

