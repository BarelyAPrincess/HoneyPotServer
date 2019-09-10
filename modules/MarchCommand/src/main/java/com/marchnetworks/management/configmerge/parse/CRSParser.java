package com.marchnetworks.management.configmerge.parse;

import com.google.common.collect.Multimap;
import com.marchnetworks.management.config.DeviceSnapshotState;
import com.marchnetworks.management.config.util.MergeUtil;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CRSParser extends BaseParser
{
	private static final String REGISTRY_NODE = "REGISTRY";
	private static final String CONFIGURATION_NODE = "CONFIGURATION";
	private static final String CONFIGURATION_ESM_NODE = "CONFIGURATION.ESM";
	private static final Logger LOG = LoggerFactory.getLogger( CRSParser.class );

	public CRSParser()
	{
		supportedModels.put( "4", "1" );
		supportedModels.put( "4", "2" );
		supportedModels.put( "6", "1" );
	}

	public DeviceSnapshotState compareConfig( String masterCfg, String snapshotCfg )
	{
		return DeviceSnapshotState.MISMATCH;
	}

	public byte[] mergeConfig( byte[] master, byte[] merged )
	{
		LOG.debug( "Configuration from Snapshot: {}, Current configuration: {} ", master, merged );

		XMLConfiguration masterCfg = MergeUtil.createXMLConfiguration( master );
		masterCfg.setDelimiterParsingDisabled( true );
		XMLConfiguration mergedCfg = MergeUtil.createXMLConfiguration( merged );
		mergedCfg.setDelimiterParsingDisabled( true );

		SubnodeConfiguration esmNode = null;
		try
		{
			esmNode = mergedCfg.configurationAt( "CONFIGURATION.ESM" );
		}
		catch ( IllegalArgumentException iae )
		{
			LOG.debug( "No <ESM> node found in snapshot's configuration." );
		}

		String[] nodesToMerge = {"CONFIGURATION", "REGISTRY"};
		for ( String node : nodesToMerge )
		{
			SubnodeConfiguration configNode = masterCfg.configurationAt( node );

			List<ConfigurationNode> nodeList = new ArrayList();
			nodeList.add( configNode.getRootNode() );
			mergedCfg.clearTree( node );
			mergedCfg.addNodes( "", nodeList );

			if ( ( esmNode != null ) && ( node.equals( "CONFIGURATION" ) ) )
			{
				nodeList.clear();
				mergedCfg.clearTree( "CONFIGURATION.ESM" );
				nodeList.add( esmNode.getRootNode() );
				mergedCfg.addNodes( node, nodeList );
			}
		}

		return MergeUtil.convertXMLtoBinary( mergedCfg );
	}
}
