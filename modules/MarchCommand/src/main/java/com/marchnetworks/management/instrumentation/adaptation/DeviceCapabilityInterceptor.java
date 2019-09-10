package com.marchnetworks.management.instrumentation.adaptation;

import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.common.types.DeviceExceptionTypes;
import com.marchnetworks.management.instrumentation.DeviceAdaptor;
import com.marchnetworks.management.instrumentation.model.DeviceCapability;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.aop.MethodBeforeAdvice;

public class DeviceCapabilityInterceptor implements MethodBeforeAdvice
{
	private DeviceCapabilityServiceImpl deviceCapabilityService;

	public void before( Method method, Object[] args, Object target ) throws Throwable
	{
		if ( ( target instanceof DeviceAdaptor ) )
		{
			Annotation[] metadata = method.getAnnotations();
			for ( Annotation annotation : metadata )
			{
				if ( DeviceCapability.class.isInstance( annotation ) )
				{
					String capability = ( ( DeviceCapability ) annotation ).name();

					boolean result = getDeviceCapabilityService().isCapabilityEnabled( findDeviceId( target ).longValue(), capability );
					if ( !result )
					{
						throw new DeviceException( "Device doesn't currently support " + capability, DeviceExceptionTypes.FEATURE_NOT_SUPPORTED );
					}
				}
			}
		}
	}

	private Long findDeviceId( Object targetObject )
	{
		Long deviceId = null;
		DeviceResource device = ( ( DeviceAdaptor ) targetObject ).getDeviceResource();
		if ( device == null )
		{
			throw new IllegalStateException( "Intercepted mal-formed DeviceAdaptor. Device has not been set." );
		}
		deviceId = Long.valueOf( Long.parseLong( device.getDeviceId() ) );

		return deviceId;
	}

	private DeviceCapabilityServiceImpl getDeviceCapabilityService()
	{
		if ( deviceCapabilityService == null )
		{
			deviceCapabilityService = ( ( DeviceCapabilityServiceImpl ) ApplicationContextSupport.getBean( "deviceCapabilityService" ) );
		}
		return deviceCapabilityService;
	}
}

