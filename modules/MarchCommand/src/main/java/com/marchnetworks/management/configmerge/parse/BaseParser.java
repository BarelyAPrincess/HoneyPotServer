package com.marchnetworks.management.configmerge.parse;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;

public abstract class BaseParser
{
	protected Multimap<String, String> supportedModels = ArrayListMultimap.create();

	public abstract byte[] mergeConfig( byte[] paramArrayOfByte1, byte[] paramArrayOfByte2 );

	public boolean canParseModel( String familyId, String modelId )
	{
		Collection<Map.Entry<String, String>> entrySet = supportedModels.entries();
		for ( Map.Entry<String, String> entry : entrySet )
		{
			if ( ( ( ( String ) entry.getKey() ).equals( familyId ) ) && ( ( ( String ) entry.getValue() ).equals( modelId ) ) )
			{
				return true;
			}
		}
		return false;
	}

	public ConfigurationNode getChildConfigNode( ConfigurationNode parent, String key )
	{
		ConfigurationNode node = null;
		if ( parent == null )
		{
			return node;
		}
		if ( parent.getName().equalsIgnoreCase( key ) )
		{
			return parent;
		}

		List<?> children = parent.getChildren();
		for ( Iterator<?> nodeItr = children.iterator(); nodeItr.hasNext(); )
		{
			node = getChildConfigNode( ( ConfigurationNode ) nodeItr.next(), key );
		}

		return node;
	}

	public void mergeDateTime( XMLConfiguration masterCfg, XMLConfiguration mergedCfg )
	{
		String method = masterCfg.getString( "DateTime.Method" );
		if ( Integer.valueOf( method ).intValue() == 2 )
		{

			SubnodeConfiguration TSubNode = mergedCfg.configurationAt( "DateTime.Current" );
			SubnodeConfiguration TZSubNode = mergedCfg.configurationAt( "DateTime.TimeZone" );
			SubnodeConfiguration DSTSubNode = null;
			if ( mergedCfg.getMaxIndex( "DateTime.DST" ) != -1 )
			{
				DSTSubNode = mergedCfg.configurationAt( "DateTime.DST" );
			}

			SubnodeConfiguration dateTimeNode = masterCfg.configurationAt( "DateTime" );
			ArrayList<ConfigurationNode> nodeList = new ArrayList();
			nodeList.add( dateTimeNode.getRootNode() );
			mergedCfg.clearTree( "DateTime" );
			mergedCfg.addNodes( "", nodeList );

			nodeList.clear();
			nodeList.add( TZSubNode.getRootNode() );
			mergedCfg.clearTree( "DateTime.TimeZone" );
			mergedCfg.addNodes( "DateTime", nodeList );

			nodeList.clear();
			nodeList.add( TSubNode.getRootNode() );
			mergedCfg.clearTree( "DateTime.Current" );
			mergedCfg.addNodes( "DateTime", nodeList );

			mergedCfg.clearTree( "DateTime.DST" );

			if ( DSTSubNode != null )
			{
				nodeList.clear();
				nodeList.add( DSTSubNode.getRootNode() );
				mergedCfg.addNodes( "DateTime", nodeList );
			}
		}
	}

	public void mergeTopNode( XMLConfiguration masterCfg, XMLConfiguration mergedCfg, String nodeName )
	{
		SubnodeConfiguration subNode = masterCfg.configurationAt( nodeName );
		ArrayList<ConfigurationNode> nodeList = new ArrayList();
		nodeList.add( subNode.getRootNode() );
		mergedCfg.clearTree( nodeName );
		mergedCfg.addNodes( "", nodeList );

		if ( nodeName.equalsIgnoreCase( "Auxes" ) )
		{
			mergedCfg.addProperty( "Auxes.ShortEmptytag(-1)", "" );
		}
		else if ( nodeName.equalsIgnoreCase( "Alarms" ) )
		{
			mergedCfg.addProperty( "Alarms.ShortEmptytag(-1)", "" );
		}
		else if ( nodeName.equalsIgnoreCase( "Addresses" ) )
		{
			mergedCfg.addProperty( "Addresses.ShortEmptytag(-1)", "" );
		}
	}

	public String getProcessModuleKey( XMLConfiguration config, String name )
	{
		String key = null;

		int nMaxIndex = config.getMaxIndex( "HOST.PROCESS.MODULE" );
		for ( int i = 0; i <= nMaxIndex; i++ )
		{
			String nextKey = "HOST.PROCESS.MODULE(" + i + ")";
			String lib = config.getString( nextKey + "[@lib]" );
			if ( lib.contains( name ) )
			{
				key = nextKey;
				break;
			}
		}

		return key;
	}

	public String getModuleResourceKey( XMLConfiguration config, String ModuleKey, String resName )
	{
		String key = null;
		int nMaxIndex = config.getMaxIndex( ModuleKey + ".RESOURCE" );
		for ( int i = 0; i <= nMaxIndex; i++ )
		{
			String nextKey = ModuleKey + ".RESOURCE(" + i + ")";
			String lib = config.getString( nextKey + "[@type]" );
			if ( lib.contains( resName ) )
			{
				key = nextKey;
				break;
			}
		}

		return key;
	}
}
