package com.marchnetworks.license.service;

import com.marchnetworks.command.common.license.data.AppLicenseInfo;
import com.marchnetworks.command.common.topology.data.GenericResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.model.License;
import com.marchnetworks.license.model.ServerLicenseImport;

import java.util.List;

public interface AppLicenseService
{
	void start();

	void importLicense( ServerLicenseImport paramServerLicenseImport ) throws LicenseException;

	boolean checkAppLicense( String paramString );

	boolean isIdentifiedAndLicensedSession();

	byte[] getIdentitySignature( String paramString );

	void checkExpiredLicenses();

	void checkGraceExpire();

	License getLicense( String paramString ) throws LicenseException;

	List<Resource> filterAppResources( String paramString, Long paramLong, List<Resource> paramList ) throws LicenseException;

	License[] getLicenses() throws LicenseException;

	void removeLicense( String paramString ) throws LicenseException;

	void setLicenseResources( String paramString, Long[] paramArrayOfLong ) throws LicenseException;

	void processDeviceUnregistered( String paramString );

	void processChannelRemoved( String paramString1, String paramString2 );

	void processGenericResourceRemoved( GenericResource paramGenericResource );

	List<AppLicenseInfo> getAppLicenseInfo( String paramString );

	boolean checkAnalyticsLicense( Long paramLong );
}
