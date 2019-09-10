package com.marchnetworks.management.configmerge.parse;

import com.google.common.collect.Multimap;

public class NoMergeParser extends BaseParser
{
	public NoMergeParser()
	{
		supportedModels.put( "0", "1" );
		supportedModels.put( "0", "2" );
		supportedModels.put( "0", "4" );
		supportedModels.put( "0", "5" );
		supportedModels.put( "0", "9" );
		supportedModels.put( "0", "12" );
		supportedModels.put( "1", "1" );
		supportedModels.put( "1", "2" );
		supportedModels.put( "1", "3" );
		supportedModels.put( "1", "4" );
		supportedModels.put( "1", "5" );
		supportedModels.put( "1", "6" );
		supportedModels.put( "1", "7" );
		supportedModels.put( "1", "8" );
		supportedModels.put( "1", "9" );
		supportedModels.put( "2", "1" );
		supportedModels.put( "2", "2" );
		supportedModels.put( "2", "3" );
		supportedModels.put( "2", "4" );
	}

	public byte[] mergeConfig( byte[] master, byte[] merged )
	{
		return master;
	}
}
