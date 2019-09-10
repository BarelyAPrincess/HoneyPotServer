package com.marchnetworks.command.api.license;

import com.marchnetworks.command.common.license.data.AppLicenseInfo;

import java.util.List;

public interface LicenseCoreService
{
	List<AppLicenseInfo> getAppLicenseInfo( String paramString );
}
