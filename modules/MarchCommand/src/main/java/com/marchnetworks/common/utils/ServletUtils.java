package com.marchnetworks.common.utils;

import com.marchnetworks.command.common.CommonAppUtils;

import org.apache.commons.lang.time.FastDateFormat;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

public class ServletUtils
{
	public static final String LOGS_FOLDER = "logs";

	public static String getStringParameterValue( String parameter )
	{
		if ( CommonAppUtils.isNullOrEmptyString( parameter ) )
		{
			return null;
		}
		return parameter;
	}

	public static Long getLongParameterValue( String parameter )
	{
		if ( CommonAppUtils.isNullOrEmptyString( parameter ) )
		{
			return null;
		}
		return Long.valueOf( Long.parseLong( parameter ) );
	}

	public static Boolean getBooleanParameterValue( String parameter )
	{
		if ( ( CommonAppUtils.isNullOrEmptyString( parameter ) ) || ( parameter.equals( "false" ) ) )
		{
			return Boolean.valueOf( false );
		}
		return Boolean.valueOf( true );
	}

	public static BigDecimal getBigDecimalParameterValue( String parameter )
	{
		if ( CommonAppUtils.isNullOrEmptyString( parameter ) )
		{
			return null;
		}
		return new BigDecimal( parameter );
	}

	public static String getDateStringFromMillis( long millis )
	{
		return FastDateFormat.getInstance( "MM-dd-yyyy HH:mm:ss" ).format( millis );
	}

	public static long dateStringtoMillis( String dateString )
	{
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat( "MM-dd-yyyy HH:mm:ss" );
			Date date = sdf.parse( dateString );
			return date.getTime();
		}
		catch ( ParseException localParseException )
		{
		}
		return 0L;
	}

	public static Integer getIntegerParameterValue( String parameter )
	{
		if ( CommonAppUtils.isNullOrEmptyString( parameter ) )
		{
			return null;
		}
		return new Integer( parameter );
	}

	public static Long getParameterId( Enumeration<String> params, String paramName )
	{
		while ( params.hasMoreElements() )
		{
			String param = ( String ) params.nextElement();
			if ( param.startsWith( paramName + "-" ) )
			{
				String paramString = param.substring( param.indexOf( "-" ) + 1 );
				return Long.valueOf( Long.parseLong( paramString ) );
			}
		}
		return null;
	}

	public static List<Long> getIdList( String idListString )
	{
		List<Long> idList = null;
		if ( idListString != null )
		{
			idList = new ArrayList();
			String[] ids = idListString.split( "," );
			for ( String id : ids )
			{
				idList.add( Long.valueOf( Long.parseLong( id ) ) );
			}
		}
		return idList;
	}

	public static List<String> getStringList( String stringListString )
	{
		List<String> result = null;
		if ( stringListString != null )
		{
			String[] ids = stringListString.split( "," );
			return Arrays.asList( ids );
		}
		return result;
	}

	public static String getLogsFolderPath()
	{
		File tempFile = new File( "" );
		String pathName = tempFile.getAbsolutePath();
		return pathName.substring( 0, pathName.length() - 6 ) + "logs" + File.separatorChar;
	}
}

