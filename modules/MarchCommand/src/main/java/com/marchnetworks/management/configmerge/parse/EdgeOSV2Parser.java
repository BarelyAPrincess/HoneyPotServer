package com.marchnetworks.management.configmerge.parse;

import com.google.common.collect.Multimap;
import com.marchnetworks.management.config.DeviceSnapshotState;
import com.marchnetworks.management.config.util.MergeUtil;

import java.util.ArrayList;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;

public class EdgeOSV2Parser extends BaseParser
{
	public EdgeOSV2Parser()
	{
		supportedModels.put( "6", "3" );
		supportedModels.put( "6", "4" );
		supportedModels.put( "6", "6" );
		supportedModels.put( "6", "7" );
		supportedModels.put( "6", "8" );
		supportedModels.put( "6", "9" );
		supportedModels.put( "0", "46" );
		supportedModels.put( "0", "47" );
		supportedModels.put( "0", "51" );
		supportedModels.put( "0", "58" );
	}

	public DeviceSnapshotState compareConfig( String masterCfg, String snapshotCfg )
	{
		return DeviceSnapshotState.MISMATCH;
	}

	public byte[] mergeConfig( byte[] master, byte[] merged )
	{
		XMLConfiguration masterCfg = MergeUtil.createXMLConfiguration( master );
		masterCfg.setDelimiterParsingDisabled( true );
		XMLConfiguration mergedCfg = MergeUtil.createXMLConfiguration( merged );
		mergedCfg.setDelimiterParsingDisabled( true );

		String s1 = getProcessModuleKey( mergedCfg, "HostManager" );
		ArrayList<ConfigurationNode> nl = new ArrayList();

		int num = mergedCfg.getMaxIndex( s1 + ".RESOURCE" );
		for ( int i = 0; i <= num; i++ )
		{

			StringBuilder p = new StringBuilder( s1 + ".RESOURCE(" + i + ")" );

			String type = mergedCfg.getString( p.toString() + "[@type]" );
			if ( type.equalsIgnoreCase( "NetworkInterface" ) )
			{

				SubnodeConfiguration netNode = mergedCfg.configurationAt( p.toString() );

				nl.add( netNode.getRootNode() );
			}
		}

		s1 = getProcessModuleKey( masterCfg, "HostManager" );
		String networkNode = null;
		while ( ( networkNode = getModuleResourceKey( masterCfg, s1, "NetworkInterface" ) ) != null )
		{
			masterCfg.clearTree( networkNode );
		}

		masterCfg.addNodes( s1, nl );

		return MergeUtil.convertXMLtoBinary( masterCfg );
	}
}
