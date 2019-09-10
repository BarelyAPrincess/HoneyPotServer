package com.marchnetworks.management.configmerge;

import com.marchnetworks.management.config.DeviceSnapshotState;
import com.marchnetworks.management.configmerge.parse.BaseParser;
import com.marchnetworks.management.configmerge.parse.CRSParser;
import com.marchnetworks.management.configmerge.parse.Edge4Parser;
import com.marchnetworks.management.configmerge.parse.EdgeOSV1Parser;
import com.marchnetworks.management.configmerge.parse.EdgeOSV2Parser;
import com.marchnetworks.management.configmerge.parse.NoMergeParser;
import com.marchnetworks.management.configmerge.service.ConfigurationMergeService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigurationMergeServiceImpl implements ConfigurationMergeService
{
	static final String NOT_SUPPORTED = "Not supported yet.";
	private List<BaseParser> configParsers = new ArrayList<>();

	public ConfigurationMergeServiceImpl()
	{
		Collections.addAll( configParsers, new BaseParser[] {new CRSParser(), new Edge4Parser(), new EdgeOSV1Parser(), new EdgeOSV2Parser(), new NoMergeParser()} );
	}

	public DeviceSnapshotState compareConfig( String family, String model, byte[] masterCfg, byte[] snapshotCfg )
	{
		return DeviceSnapshotState.MISMATCH;
	}

	public byte[] mergeConfig( String family, String model, byte[] masterCfg, byte[] mergedCfg )
	{
		BaseParser parser = null;
		for ( BaseParser configParser : configParsers )
		{
			if ( configParser.canParseModel( family, model ) )
			{
				parser = configParser;
				break;
			}
		}

		if ( parser == null )
		{
			parser = new EdgeOSV1Parser();
		}
		return parser.mergeConfig( masterCfg, mergedCfg );
	}
}
