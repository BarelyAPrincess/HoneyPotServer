package com.marchnetworks.common.migration;

public enum DatabaseVersion
{
	VERSION_1_6( 5L ),
	VERSION_1_7( 6L ),
	VERSION_1_8( 7L ),
	VERSION_1_9( 8L ),
	VERSION_1_10( 9L ),
	VERSION_1_10_1( 10L ),
	VERSION_1_11_0( 11L ),
	VERSION_2_0( 12L ),
	VERSION_2_1( 13L ),
	VERSION_2_1_3( 14L ),
	VERSION_2_3( 15L ),
	VERSION_2_5( 16L ),
	VERSION_2_6( 17L );

	private long value;

	DatabaseVersion( long value )
	{
		this.value = value;
	}

	public long getValue()
	{
		return value;
	}

	public static DatabaseVersion fromValue( long value )
	{
		for ( DatabaseVersion def : values() )
		{
			if ( def.value == value )
				return def;
		}
		return null;
	}
}
