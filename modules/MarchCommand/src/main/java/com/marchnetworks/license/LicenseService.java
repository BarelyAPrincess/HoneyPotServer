package com.marchnetworks.license;

import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.model.DeviceLicenseInfo;
import com.marchnetworks.license.model.License;
import com.marchnetworks.license.model.LicenseType;
import com.marchnetworks.license.model.ServerLicenseInfo;
import com.marchnetworks.management.instrumentation.events.ServerIdHashEvent;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;

import java.util.Collection;
import java.util.List;

public abstract interface LicenseService
{
	public abstract boolean isLicenseMatch( byte[] paramArrayOfByte );

	public abstract boolean validate( String paramString, byte[] paramArrayOfByte ) throws Exception;

	public abstract int loadParameter( String paramString );

	public abstract void saveParameter( int paramInt, String paramString );

	public abstract boolean checkAppLicense( String paramString );

	public abstract boolean isIdentifiedAndLicensedSession();

	public abstract String getServerId() throws Exception;

	public abstract void importLicense( String paramString ) throws LicenseException;

	public abstract void allocateForRegistration( Long paramLong ) throws LicenseException;

	public abstract Collection<ServerLicenseInfo> getAllLicenseInfo() throws LicenseException;

	public abstract DeviceLicenseInfo getDeviceLicense( Long paramLong );

	public abstract Collection<DeviceLicenseInfo> getAllDeviceLicense( LicenseType paramLicenseType ) throws LicenseException;

	public abstract void setDeviceLicense( Long paramLong, LicenseType paramLicenseType, int paramInt ) throws LicenseException;

	public abstract License getLicense( String paramString ) throws LicenseException;

	public abstract License[] getLicenses() throws LicenseException;

	public abstract List<Resource> filterAppResources( String paramString, Long paramLong, List<Resource> paramList ) throws LicenseException;

	public abstract void removeLicense( String paramString ) throws LicenseException;

	public abstract void setLicenseResources( String paramString, Long[] paramArrayOfLong ) throws LicenseException;

	public abstract void processDeviceUnregistered( String paramString );

	public abstract void allocateForTestDevice( DeviceMBean paramDeviceMBean ) throws LicenseException;

	public abstract void sendServerId( Long paramLong, ServerIdHashEvent paramServerIdHashEvent );

	public abstract String getHashedServerId() throws Exception;

	public abstract boolean checkAnalyticsLicense( Long paramLong, String paramString );
}

