package com.marchnetworks.license.service;

import com.marchnetworks.license.model.AppType;
import com.marchnetworks.license.model.ApplicationIdentityToken;

public class AppIds
{
	public static final ApplicationIdentityToken SEARCHLIGHT_4_0 = new ApplicationIdentityToken();
	public static final ApplicationIdentityToken SEARCHLIGHT_4_1 = new ApplicationIdentityToken();

	static
	{
		SEARCHLIGHT_4_0.setId( "9260cdbc-d999-48bd-92dd-259aea30a0e3" );

		SEARCHLIGHT_4_1.setId( "9b944d3e-810f-4487-8d00-1bf583112ba1" );
		SEARCHLIGHT_4_1.setName( "Searchlight" );
		SEARCHLIGHT_4_1.setAppType( AppType.LICENSE_EXEMPT_APP );
		SEARCHLIGHT_4_1.setDeveloper( "March Networks" );
		SEARCHLIGHT_4_1.setDeveloperUrl( "http://www.marchnetworks.com" );
		SEARCHLIGHT_4_1.setDeveloperEmail( "techsupport@marchnetworks.com" );
		SEARCHLIGHT_4_1.setDescription( "March Networks Searchlight is a powerful business intelligence engine, designed to seamlessly integrate video surveillance with the analysis of large amounts of unstructured data from different vertical markets. It helps to identify, develop, or otherwise create strategic information to help grow your business." );
		SEARCHLIGHT_4_1.setAccessCode( "E5RV9E4JC6bVwqDTkfUM84802pSc7NDVStbCpq7E3gH4me3jaJRNVVXUUAfQIz55iLuiTUEWkyBaVaHnl5wxXva/mKVYE/JfDknZnq/rzD9H1cIok3GmFAbF5pRsE/vV0h2QDF62slSIFOqLMElFqSjp/ZleD4qlpZDRGsXxJk72HVidTtlLVITdsNGjvoS2s5KVRNu/ExhkS47qkCfky23lPbfuLsUiaWG00/JxhdEIsS4EPLnEFb/xqXi8wbxa+4S2QerizMncidiVObWuiZ4evquQKnuWtWleXBmJ/LEym9rH2h4wgFSN6hLD6BjNdBCbNua3Eo4TzDivTeFJz299wYlLNq1oe/8iVPcr4+5SCnwJ" );
	}
}
