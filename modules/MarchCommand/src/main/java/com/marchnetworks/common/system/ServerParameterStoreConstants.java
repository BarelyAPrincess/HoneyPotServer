package com.marchnetworks.common.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerParameterStoreConstants
{
	public static final String PARAM_NAME_BUILD_DATE = "ServerManifest.Build-Date";
	public static final String PARAM_NAME_BUILD_NUMBER = "ServerManifest.Build-Number";
	public static final String PARAM_NAME_PACKAGE_NUMBER = "ServerManifest.Package-Number";
	public static final String EXTERNAL_HTML = "External_HTML";
	public static final String MANIFEST_FILE_RELATIVE_PATH = "/META-INF/MANIFEST.MF";
	public static final String CASE_MANAGEMENT_FEATURE = "shared_cases_capable";
	public static final String NVR_MASS_CONFIGURATION_CAPABLE = "nvr_mass_configuration_capable";
	public static List<String> PUBLIC_PARAMETERS = new ArrayList();
	public static Map<String, String> PUBLIC_PARAMETERS_VALUES = new HashMap();

	static
	{
		PUBLIC_PARAMETERS = Arrays.asList( new String[] {"ServerManifest.Build-Date", "ServerManifest.Build-Number", "ServerManifest.Package-Number", "External_HTML", "shared_cases_capable", "nvr_mass_configuration_capable"} );
		PUBLIC_PARAMETERS_VALUES.put( "shared_cases_capable", "true" );
		PUBLIC_PARAMETERS_VALUES.put( "nvr_mass_configuration_capable", "true" );
	}
}
