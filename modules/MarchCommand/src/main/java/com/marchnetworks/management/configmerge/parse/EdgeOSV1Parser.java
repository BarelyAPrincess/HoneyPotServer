package com.marchnetworks.management.configmerge.parse;

import com.google.common.collect.Multimap;
import com.marchnetworks.management.config.DeviceSnapshotState;
import com.marchnetworks.management.config.util.MergeUtil;

import java.util.ArrayList;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;

public class EdgeOSV1Parser extends BaseParser
{
	public EdgeOSV1Parser()
	{
		supportedModels.put( "0", "16" );
		supportedModels.put( "0", "17" );
		supportedModels.put( "0", "20" );
		supportedModels.put( "0", "21" );
		supportedModels.put( "0", "23" );
		supportedModels.put( "0", "34" );
		supportedModels.put( "0", "35" );
		supportedModels.put( "0", "33" );
		supportedModels.put( "0", "36" );
		supportedModels.put( "0", "37" );
		supportedModels.put( "0", "38" );
		supportedModels.put( "0", "25" );
		supportedModels.put( "0", "24" );
		supportedModels.put( "0", "40" );
		supportedModels.put( "0", "43" );
		supportedModels.put( "0", "44" );
		supportedModels.put( "0", "32769" );
		supportedModels.put( "0", "32770" );
		supportedModels.put( "0", "32772" );
		supportedModels.put( "0", "32773" );
		supportedModels.put( "0", "32774" );
	}

	/**
	 * @deprecated
	 */
	public DeviceSnapshotState CompareConfig( String masterCfg, String snapshotCfg )
	{
		return DeviceSnapshotState.MISMATCH;
	}

	public byte[] mergeConfig( byte[] master, byte[] merged )
	{
		XMLConfiguration masterCfg = MergeUtil.createXMLConfiguration( master );
		masterCfg.setDelimiterParsingDisabled( true );
		XMLConfiguration mergedCfg = MergeUtil.createXMLConfiguration( merged );
		mergedCfg.setDelimiterParsingDisabled( true );

		ArrayList<ConfigurationNode> nodeList = new ArrayList();
		SubnodeConfiguration subNode = null;
		SubnodeConfiguration dataNode = null;
		SubnodeConfiguration alarmHndlerNode = null;

		if ( masterCfg.getRootNode() != null )
		{
			subNode = masterCfg.configurationAt( "USERS" );
			nodeList.clear();
			nodeList.add( subNode.getRootNode() );
			mergedCfg.clearTree( "USERS" );
			mergedCfg.addNodes( "", nodeList );

			String AHkey = getProcessModuleKey( mergedCfg, "AlarmHandler" );
			if ( AHkey != null )
			{
				alarmHndlerNode = mergedCfg.configurationAt( AHkey );
			}

			if ( alarmHndlerNode != null )
			{
				String AHDCapkey = getProcessModuleKey( mergedCfg, "AHDCapture" );
				if ( AHDCapkey != null )
				{
					String VMotionKey = getModuleResourceKey( mergedCfg, AHDCapkey, "VideoMotion" );

					if ( VMotionKey != null )
					{
						dataNode = mergedCfg.configurationAt( VMotionKey + ".DATA" );
					}
				}
			}

			subNode = masterCfg.configurationAt( "HOST.PROCESS" );
			nodeList.clear();
			nodeList.add( subNode.getRootNode() );
			mergedCfg.clearTree( "HOST.PROCESS" );
			mergedCfg.addNodes( "HOST", nodeList );

			subNode = masterCfg.configurationAt( "HOST.AUTH" );
			nodeList.clear();
			nodeList.add( subNode.getRootNode() );
			mergedCfg.clearTree( "HOST.AUTH" );
			mergedCfg.addNodes( "HOST", nodeList );

			if ( dataNode != null )
			{
				String AHDCapkey = getProcessModuleKey( mergedCfg, "AHDCapture" );
				if ( AHDCapkey != null )
				{
					String VMotionKey = getModuleResourceKey( mergedCfg, AHDCapkey, "VideoMotion" );
					if ( VMotionKey != null )
					{
						nodeList.clear();
						nodeList.add( dataNode.getRootNode() );
						mergedCfg.clearTree( VMotionKey + ".DATA" );
						mergedCfg.addNodes( VMotionKey, nodeList );
					}
				}
			}
		}

		String cloudClientKey = getProcessModuleKey( mergedCfg, "CloudClient" );
		if ( cloudClientKey != null )
		{
			SubnodeConfiguration registrationNode = mergedCfg.configurationAt( cloudClientKey + ".DATA.REGISTRATION" );
			if ( registrationNode.getRootNode().getChildrenCount() == 0 )
			{
				registrationNode.addProperty( "ACCOUNT.ShortEmptytag(-1)", "" );
				registrationNode.addProperty( "PASSWORD.ShortEmptytag(-1)", "" );
				registrationNode.addProperty( "USERNAME.ShortEmptytag(-1)", "" );
			}
		}

		return MergeUtil.convertXMLtoBinary( mergedCfg );
	}
}
