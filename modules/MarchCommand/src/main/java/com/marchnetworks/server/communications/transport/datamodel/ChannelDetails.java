package com.marchnetworks.server.communications.transport.datamodel;

public class ChannelDetails
{
	protected String id;

	protected DeviceDetails ipDevice;

	protected String name;

	protected ChannelState channelState;

	protected String ptzDomeId;

	protected String[] assocIds;

	protected VideoDetails[] video;

	protected AudioDetails[] audio;

	protected TextDetails[] text;

	protected DataDetails[] data;

	public static boolean isChannelStateUnknown( ChannelDetails channelDetail )
	{
		if ( channelDetail != null )
			return ChannelState.UNKNOWN == channelDetail.channelState;

		return false;
	}

	public static boolean isChannelEnabled( ChannelDetails channelDetail )
	{
		if ( channelDetail != null )
			return ( ChannelState.ONLINE == channelDetail.channelState ) || ( ChannelState.OFFLINE == channelDetail.channelState );

		return false;
	}

	public String getId()
	{
		return id;
	}

	public void setId( String value )
	{
		id = value;
	}

	public DeviceDetails getIpDevice()
	{
		return ipDevice;
	}

	public void setIpDevice( DeviceDetails value )
	{
		ipDevice = value;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String value )
	{
		name = value;
	}

	public ChannelState getChannelState()
	{
		return channelState;
	}

	public void setChannelState( ChannelState value )
	{
		channelState = value;
	}

	public String getPtzDomeId()
	{
		return ptzDomeId;
	}

	public void setPtzDomeId( String value )
	{
		ptzDomeId = value;
	}

	public String[] getAssocIds()
	{
		return assocIds;
	}

	public void setAssocIds( String[] value )
	{
		assocIds = value;
	}

	public VideoDetails[] getVideo()
	{
		return video;
	}

	public void setVideo( VideoDetails[] value )
	{
		video = value;
	}

	public AudioDetails[] getAudio()
	{
		return audio;
	}

	public void setAudio( AudioDetails[] value )
	{
		audio = value;
	}

	public TextDetails[] getText()
	{
		return text;
	}

	public void setText( TextDetails[] value )
	{
		text = value;
	}

	public DataDetails[] getData()
	{
		return data;
	}

	public void setData( DataDetails[] data )
	{
		this.data = data;
	}

	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = 31 * result + ( id == null ? 0 : id.hashCode() );
		return result;
	}

	public boolean equals( Object obj )
	{
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		ChannelDetails other = ( ChannelDetails ) obj;
		if ( id == null )
		{
			if ( id != null )
				return false;
		}
		else if ( !id.equals( id ) )
			return false;
		return true;
	}
}

