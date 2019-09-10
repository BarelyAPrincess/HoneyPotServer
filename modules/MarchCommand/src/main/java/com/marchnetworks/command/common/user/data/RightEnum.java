package com.marchnetworks.command.common.user.data;

public enum RightEnum
{
	LIVE_VIDEO( "Live Video" ),
	ARCHIVE_VIDEO( "Archive Video" ),
	EXPORT_MP4( "Export to MP4 (H.264/AAC)" ),
	PTZ_CONTROL( "PTZ Control" ),
	MANAGE_SYSTEM_TREE( "Edit System Tree" ),
	MONITOR_SYSTEM_TREE( "System Tree" ),
	MANAGE_LOGICAL_TREE( "Edit Logical Tree" ),
	MONITOR_LOGICAL_TREE( "Logical Tree" ),

	HEALTH_MONITORING( "Health Monitoring" ),

	MANAGE_APPS( "Additional Components Management" ),
	MANAGE_USERS( "User Management" ),
	MANAGE_ALARMS( "Alarm Monitoring" ),
	MANAGE_DEVICES( "Device Management" ),
	MASS_CONFIGURATION( "Mass Management" ),
	PERSONAL_TREE( "Personal Tree" ),
	ACCESS_LOGS( "Server Logs" ),
	EXPORT_LOCAL( "Remote USB Export" ),
	MANAGE_CASE_MANAGEMENT( "Save Cases" ),
	PRIVACY_UNMASK( "Privacy Unmask" ),

	EXPORT_NATIVE( "Export to CME" );

	private String property;

	private RightEnum( String property )
	{
		this.property = property;
	}

	public String getProperty()
	{
		return property;
	}

	public static RightEnum getRightFromString( String rightName )
	{
		RightEnum ret = null;

		rightName = rightName.replace( "ROLE_", "" );
		try
		{
			ret = valueOf( rightName );
		}
		catch ( IllegalArgumentException localIllegalArgumentException )
		{
		}

		return ret;
	}
}
