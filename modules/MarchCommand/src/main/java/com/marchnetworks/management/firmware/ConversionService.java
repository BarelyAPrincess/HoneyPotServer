package com.marchnetworks.management.firmware;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.management.firmware.data.Firmware;
import com.marchnetworks.management.firmware.data.UpdateStateEnum;

public class ConversionService
{
	public static Firmware[] convertUpgradeStates( Firmware[] firmwares )
	{
		if ( firmwares == null )
		{
			return null;
		}

		for ( Firmware firmware : firmwares )
		{
			convertUpgradeState( firmware );
		}

		return firmwares;
	}

	public static Firmware convertUpgradeState( Firmware firmware )
	{
		if ( firmware == null )
		{
			return null;
		}

		String clientVersion = ( String ) CommonAppUtils.getDetailParameter( "x-client-version" );
		if ( ( clientVersion != null ) && ( CommonUtils.compareVersions( clientVersion, "2.5" ) < 0 ) && ( ( UpdateStateEnum.FIRMWARE_UPGRADE_WAITING_ACCEPT == firmware.getUpdateState() ) || ( UpdateStateEnum.FIRMWARE_UPGRADE_ACCEPTED == firmware.getUpdateState() ) ) )
		{
			firmware.setUpdateState( UpdateStateEnum.FIRMWARE_UPGRADE_PENDING );
		}

		return firmware;
	}
}

