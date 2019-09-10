package com.marchnetworks.command.common.topology.data;

import com.marchnetworks.command.common.device.data.SwitchView;

import javax.xml.bind.annotation.XmlTransient;

public class SwitchResource extends Resource
{
	private transient Long switchId;
	private SwitchView switchView;

	public void update( Resource resource )
	{
		if ( ( resource instanceof SwitchResource ) )
		{
			super.update( resource );
			SwitchResource switchResource = ( SwitchResource ) resource;
			switchId = switchResource.getSwitchId();
			switchView = switchResource.getSwitchView();
		}
	}

	public LinkType getLinkType()
	{
		return LinkType.SWITCH;
	}

	@XmlTransient
	public Long getSwitchId()
	{
		return switchId;
	}

	public void setSwitchId( Long switchId )
	{
		this.switchId = switchId;
	}

	public SwitchView getSwitchView()
	{
		return switchView;
	}

	public void setSwitchView( SwitchView switchView )
	{
		this.switchView = switchView;
	}
}
