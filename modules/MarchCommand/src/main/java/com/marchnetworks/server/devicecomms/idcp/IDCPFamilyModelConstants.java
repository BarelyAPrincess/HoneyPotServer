package com.marchnetworks.server.devicecomms.idcp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class IDCPFamilyModelConstants
{
	private static final Map<Integer, String> IDCP_FAMILY_MAP = new HashMap();
	private static final Map<Integer, Map<Integer, String>> IDCP_MODEL_MAP = new HashMap();

	static
	{
		initFamilyMap();
		initModelMap();
	}

	private static void initFamilyMap()
	{
		IDCP_FAMILY_MAP.put( Integer.valueOf( 0 ), "Nettuno" );
		IDCP_FAMILY_MAP.put( Integer.valueOf( 1 ), "Linearis" );
		IDCP_FAMILY_MAP.put( Integer.valueOf( 2 ), "Proxima" );
		IDCP_FAMILY_MAP.put( Integer.valueOf( 3 ), "SiteManager DS" );
		IDCP_FAMILY_MAP.put( Integer.valueOf( 4 ), "Command Recording Server" );
		IDCP_FAMILY_MAP.put( Integer.valueOf( 5 ), "I/O Board" );
		IDCP_FAMILY_MAP.put( Integer.valueOf( 6 ), "NVR" );
		IDCP_FAMILY_MAP.put( Integer.valueOf( 256 ), "R4" );
		IDCP_FAMILY_MAP.put( Integer.valueOf( 257 ), "R5" );
	}

	private static void initModelMap()
	{
		for ( Iterator i$ = IDCP_FAMILY_MAP.keySet().iterator(); i$.hasNext(); )
		{
			int familyId = ( ( Integer ) i$.next() ).intValue();
			Map<Integer, String> modelMap = new HashMap();
			switch ( familyId )
			{
				case 0:
					modelMap.put( Integer.valueOf( 1 ), "VS Edge 1" );
					modelMap.put( Integer.valueOf( 2 ), "VS Edge 1" );
					modelMap.put( Integer.valueOf( 3 ), "Nettuno Mini" );
					modelMap.put( Integer.valueOf( 4 ), "VS PTZ XDome" );
					modelMap.put( Integer.valueOf( 5 ), "VS CamPX" );
					modelMap.put( Integer.valueOf( 6 ), "Nettuno HTR" );
					modelMap.put( Integer.valueOf( 7 ), "Nettuno Senses" );
					modelMap.put( Integer.valueOf( 8 ), "VS MegaPX 2M" );
					modelMap.put( Integer.valueOf( 9 ), "VS PTZ MiniDome" );
					modelMap.put( Integer.valueOf( 10 ), "Nettuno CamPX Senses" );
					modelMap.put( Integer.valueOf( 11 ), "VS Edge 4" );
					modelMap.put( Integer.valueOf( 12 ), "VS PTZ MegaDome" );
					modelMap.put( Integer.valueOf( 13 ), "VS CamPX H" );
					modelMap.put( Integer.valueOf( 14 ), "VS CamPX D" );
					modelMap.put( Integer.valueOf( 15 ), "VS PTZ Microdome" );
					modelMap.put( Integer.valueOf( 16 ), "VS MegaPX 720p" );
					modelMap.put( Integer.valueOf( 17 ), "VS MegaPX 1080p" );
					modelMap.put( Integer.valueOf( 18 ), "VS MegaPX 1080ps" );
					modelMap.put( Integer.valueOf( 19 ), "VS CamHT" );
					modelMap.put( Integer.valueOf( 20 ), "VS MegaPX 1080p mD" );
					modelMap.put( Integer.valueOf( 21 ), "VS MegaPX 720p mD" );
					modelMap.put( Integer.valueOf( 22 ), "Edge Analytic Gateway" );
					modelMap.put( Integer.valueOf( 23 ), "VS Dome SD" );
					modelMap.put( Integer.valueOf( 24 ), "VS Edge 1 Micro" );
					modelMap.put( Integer.valueOf( 25 ), "VS CampPX MicroDome" );
					modelMap.put( Integer.valueOf( 26 ), "VS PTZ Shield" );
					modelMap.put( Integer.valueOf( 27 ), "DM365EVM" );
					modelMap.put( Integer.valueOf( 28 ), "VS PTZ MiniDome" );
					modelMap.put( Integer.valueOf( 32 ), "VS MegaPX 1080pl" );
					modelMap.put( Integer.valueOf( 33 ), "VS Mdome PTZ HD" );
					modelMap.put( Integer.valueOf( 34 ), "VS MegaPX NanoDome" );
					modelMap.put( Integer.valueOf( 36 ), "VS MegaPX 3MP" );
					modelMap.put( Integer.valueOf( 37 ), "VS MegaPX 5MP" );
					break;
				case 1:
					modelMap.put( Integer.valueOf( 0 ), "VS NVR Mini / Linearis Mini" );
					modelMap.put( Integer.valueOf( 1 ), "Linearis" );
					break;
				case 2:
					modelMap.put( Integer.valueOf( 0 ), "Proxima Mini" );
					break;
				case 3:
					modelMap.put( Integer.valueOf( 0 ), "VS Edge DecodeStation" );
					modelMap.put( Integer.valueOf( 1 ), "VS DecodeStation VX" );
					break;
				case 4:
					modelMap.put( Integer.valueOf( 0 ), "VS NVR 2.0/VMS 2.0/Spectiva" );
					modelMap.put( Integer.valueOf( 1 ), "VS VMS 2.1" );
					modelMap.put( Integer.valueOf( 2 ), "Command Recording Server 1.2.1" );
					break;
				case 5:
					modelMap.put( Integer.valueOf( 1 ), "VS I/O extension board" );
					break;
				case 6:
					modelMap.put( Integer.valueOf( 1 ), "7532" );
					modelMap.put( Integer.valueOf( 2 ), "7424" );
					break;
				case 256:
					modelMap.put( Integer.valueOf( 1 ), "VS R4 DVR" );
					modelMap.put( Integer.valueOf( 2 ), "5308" );
					modelMap.put( Integer.valueOf( 3 ), "5412" );
					break;
				case 257:
					modelMap.put( Integer.valueOf( 1 ), "VS R5 DVR" );
					modelMap.put( Integer.valueOf( 2 ), "8732R" );
					modelMap.put( Integer.valueOf( 3 ), "8532R" );
					modelMap.put( Integer.valueOf( 4 ), "8532S" );
					modelMap.put( Integer.valueOf( 5 ), "8516S" );
					modelMap.put( Integer.valueOf( 6 ), "8516R" );
					modelMap.put( Integer.valueOf( 7 ), "8708S" );
					modelMap.put( Integer.valueOf( 8 ), "8704S" );
					modelMap.put( Integer.valueOf( 9 ), "8508S" );
					modelMap.put( Integer.valueOf( 10 ), "GT08A" );
					modelMap.put( Integer.valueOf( 11 ), "GT08" );
					modelMap.put( Integer.valueOf( 12 ), "GT12" );
					modelMap.put( Integer.valueOf( 13 ), "GT16" );
					modelMap.put( Integer.valueOf( 14 ), "GT20" );
					modelMap.put( Integer.valueOf( 15 ), "MT04" );
					modelMap.put( Integer.valueOf( 16 ), "MT08" );
					modelMap.put( Integer.valueOf( 17 ), "RT20" );
					modelMap.put( Integer.valueOf( 18 ), "8716P" );
					modelMap.put( Integer.valueOf( 18 ), "VNVR" );
					modelMap.put( Integer.valueOf( 20 ), "SVR24" );
					modelMap.put( Integer.valueOf( 21 ), "9132" );
					modelMap.put( Integer.valueOf( 22 ), "9248" );
					modelMap.put( Integer.valueOf( 23 ), "9264" );
			}

			IDCP_MODEL_MAP.put( Integer.valueOf( familyId ), modelMap );
		}
	}

	public static String getFamilyName( int familyValue )
	{
		return ( String ) IDCP_FAMILY_MAP.get( Integer.valueOf( familyValue ) );
	}

	public static String getModelName( int familyValue, int modelValue )
	{
		String modelName = null;
		Map<Integer, String> familyModels = ( Map ) IDCP_MODEL_MAP.get( Integer.valueOf( familyValue ) );
		if ( familyModels != null )
		{
			modelName = ( String ) familyModels.get( Integer.valueOf( modelValue ) );
		}
		return modelName;
	}
}

