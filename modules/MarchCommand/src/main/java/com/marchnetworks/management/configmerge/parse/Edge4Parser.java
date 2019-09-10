package com.marchnetworks.management.configmerge.parse;

import com.google.common.collect.Multimap;
import com.marchnetworks.management.config.DeviceSnapshotState;
import com.marchnetworks.management.config.util.MergeUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.XMLConfiguration;

public class Edge4Parser extends BaseParser
{
	private Map.Entry<String, String> selectedModel;

	public Edge4Parser()
	{
		supportedModels.put( "0", "11" );
		supportedModels.put( "0", "13" );
		supportedModels.put( "0", "14" );
		supportedModels.put( "0", "15" );
	}

	public boolean canParseModel( String familyId, String modelId )
	{
		Collection<Map.Entry<String, String>> entrySet = supportedModels.entries();
		for ( Map.Entry<String, String> entry : entrySet )
		{
			if ( ( ( ( String ) entry.getKey() ).equals( familyId ) ) && ( ( ( String ) entry.getValue() ).equals( modelId ) ) )
			{
				selectedModel = entry;
				return true;
			}
		}
		return false;
	}

	public DeviceSnapshotState compareConfig( String master, String snapshot )
	{
		return DeviceSnapshotState.MISMATCH;
	}

	public byte[] mergeConfig( byte[] master, byte[] merged )
	{
		XMLConfiguration masterCfg = MergeUtil.createXMLConfiguration( master );
		masterCfg.setDelimiterParsingDisabled( true );
		XMLConfiguration mergedCfg = MergeUtil.createXMLConfiguration( merged );
		mergedCfg.setDelimiterParsingDisabled( true );

		mergeTopNode( masterCfg, mergedCfg, "Users" );

		if ( ( ( String ) selectedModel.getValue() ).equals( "11" ) )
		{
			mergeTopNode( masterCfg, mergedCfg, "Recorder" );
		}
		mergeTopNode( masterCfg, mergedCfg, "Video" );

		mergeDateTime( masterCfg, mergedCfg );

		if ( ( ( ( String ) selectedModel.getValue() ).equals( "11" ) ) || ( ( ( String ) selectedModel.getValue() ).equals( "13" ) ) || ( ( ( String ) selectedModel.getValue() ).equals( "14" ) ) )
		{

			mergeTopNode( masterCfg, mergedCfg, "Auxes" );
		}
		mergeTopNode( masterCfg, mergedCfg, "SwMPEG4Encoders" );

		mergeTopNode( masterCfg, mergedCfg, "rtp" );

		mergeTopNode( masterCfg, mergedCfg, "Protocols" );

		mergeTopNode( masterCfg, mergedCfg, "HwH264Encoders" );

		if ( ( ( ( String ) selectedModel.getValue() ).equals( "11" ) ) || ( ( ( String ) selectedModel.getValue() ).equals( "13" ) ) || ( ( ( String ) selectedModel.getValue() ).equals( "14" ) ) )
		{

			mergeTopNode( masterCfg, mergedCfg, "Audio" );
		}
		mergeTopNode( masterCfg, mergedCfg, "Alarms" );

		return MergeUtil.convertXMLtoBinary( mergedCfg );
	}
}
