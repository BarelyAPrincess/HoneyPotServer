package com.marchnetworks.management.config.util;

import com.marchnetworks.command.common.CommonAppUtils;

import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MergeUtil
{
	private static Logger LOG = LoggerFactory.getLogger( MergeUtil.class );

	public static XMLConfiguration createXMLConfiguration( byte[] data )
	{
		XMLConfiguration xmlCfg = new XMLConfiguration();
		StringReader xmlReader = new StringReader( CommonAppUtils.encodeToUTF8String( data ) );
		try
		{
			xmlCfg.setDelimiterParsingDisabled( true );
			xmlCfg.setEncoding( "UTF-8" );
			xmlCfg.load( xmlReader );
		}
		catch ( ConfigurationException e )
		{
			LOG.error( "Fail to load XML data", e );
			return null;
		}
		return xmlCfg;
	}

	public static byte[] convertXMLtoBinary( XMLConfiguration xml )
	{
		StringWriter vmsWriter = new StringWriter();
		try
		{
			xml.setDelimiterParsingDisabled( true );
			xml.save( vmsWriter );
		}
		catch ( ConfigurationException e )
		{
			LOG.error( "Fail to load XML data", e );
			return null;
		}
		return vmsWriter.toString().getBytes();
	}
}
