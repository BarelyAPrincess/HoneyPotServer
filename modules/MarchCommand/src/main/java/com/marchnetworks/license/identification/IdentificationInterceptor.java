package com.marchnetworks.license.identification;

import com.marchnetworks.command.api.security.CommandAuthenticationDetails;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.license.LicenseService;

import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Method;

public class IdentificationInterceptor implements MethodBeforeAdvice
{
	private LicenseService licenseService;

	public void before( Method method, Object[] args, Object target ) throws Throwable
	{
		CommandAuthenticationDetails sessionDetails = CommonUtils.getAuthneticationDetails();

		if ( ( sessionDetails != null ) && ( !licenseService.isIdentifiedAndLicensedSession() ) )
		{
			throw new AccessDeniedException( "The requested operation " + method.getDeclaringClass().getName() + "." + method.getName() + " requires identification and a valid license" );
		}
	}

	public void setLicenseService( LicenseService licenseService )
	{
		this.licenseService = licenseService;
	}
}
