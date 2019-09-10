package com.marchnetworks.health.alerts;

import com.marchnetworks.health.input.AlertInput;
import com.marchnetworks.health.input.DeviceAlertInput;
import com.marchnetworks.health.input.ServerAlertInput;

public class AlertEntityFactory
{
	public static AlertEntity createAlertEntity( AlertInput input )
	{
		AlertEntity result = null;

		if ( ( input instanceof DeviceAlertInput ) )
		{
			DeviceAlertInput deviceInput = ( DeviceAlertInput ) input;
			result = new DeviceAlertEntity( deviceInput.getDeviceId(), deviceInput.getAlertId(), deviceInput.getAlertCode(), deviceInput.getCategory(), deviceInput.getSourceId(), deviceInput.getValue(), deviceInput.getAlertTime(), deviceInput.getLastTime(), deviceInput.isDeviceState(), deviceInput.getInfo(), deviceInput.getThresholdDuration(), deviceInput.getThresholdFrequency() );

		}
		else if ( ( input instanceof ServerAlertInput ) )
		{
			ServerAlertInput serverInput = ( ServerAlertInput ) input;
			result = new ServerAlertEntity( serverInput.getServerId(), serverInput.getAlertCode(), serverInput.getCategory(), serverInput.getSourceId(), serverInput.getValue(), serverInput.getAlertTime(), serverInput.getLastTime(), serverInput.getInfo() );
		}

		return result;
	}

	public static boolean updateAlertEntity( AlertEntity alertEntity, AlertInput alertInput )
	{
		int alertCount = 0;
		if ( ( alertInput instanceof DeviceAlertInput ) )
		{
			DeviceAlertInput deviceAlertInput = ( DeviceAlertInput ) alertInput;
			DeviceAlertEntity deviceAlertEntity = ( DeviceAlertEntity ) alertEntity;
			deviceAlertEntity.setThresholdDuration( Integer.valueOf( deviceAlertInput.getThresholdDuration() ) );
			deviceAlertEntity.setThresholdFrequency( Integer.valueOf( deviceAlertInput.getThresholdFrequency() ) );
			alertCount = deviceAlertInput.getCount();
		}
		alertEntity.setDeviceState( alertInput.isDeviceState() );
		alertEntity.setCount( Long.valueOf( alertCount > 0 ? alertCount : alertEntity.getCount().longValue() + 1L ) );
		alertEntity.setLastInstanceTime( Long.valueOf( alertInput.getLastTime() ) );

		if ( alertInput.getResolvedTime() > 0L )
		{
			alertEntity.setAlertClear( alertInput.getResolvedTime() );
		}

		boolean result = false;
		if ( !alertEntity.getSourceDesc().equals( alertInput.getValue() ) )
		{
			result = true;
			alertEntity.setSourceDesc( alertInput.getValue() );
		}
		if ( ( alertInput.getInfo() != null ) && ( !alertInput.getInfo().equals( alertEntity.getInfo() ) ) )
		{
			result = true;
			alertEntity.setInfo( alertInput.getInfo() );
		}
		return result;
	}
}
