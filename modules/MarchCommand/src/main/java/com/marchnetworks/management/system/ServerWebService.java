package com.marchnetworks.management.system;

import com.marchnetworks.command.common.data.GenericString;
import com.marchnetworks.command.common.data.GenericValue;
import com.marchnetworks.command.common.timezones.TimezonesDictionary;
import com.marchnetworks.command.common.timezones.data.Timezone;
import com.marchnetworks.command.common.transport.data.TimeZoneInfo;
import com.marchnetworks.common.device.ServerServiceException;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.system.ServerParameterStoreServiceIF;
import com.marchnetworks.common.utils.DateUtils;

import java.util.List;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.WebServiceContext;

@WebService( serviceName = "ServerService", name = "ServerService", portName = "ServerPort" )
@XmlSeeAlso( {GenericValue.class, GenericString.class} )
public class ServerWebService
{
	private ServerParameterStoreServiceIF parameterStore = ( ServerParameterStoreServiceIF ) ApplicationContextSupport.getBean( "serverParameterStoreProxy" );
	@Resource
	WebServiceContext wsContext;

	@WebMethod( operationName = "getParameterValue" )
	public GenericValue getParameterValue( @WebParam( name = "parameterName" ) String parameterName ) throws ServerServiceException
	{
		String paramValue = parameterStore.getParameterValueService( parameterName );
		return new GenericString( paramValue );
	}

	@WebMethod( operationName = "getTimeZoneInfo" )
	public TimeZoneInfo getTimeZoneInfo()
	{
		TimeZone timeZone = DateUtils.getServerTimeZone();
		TimeZoneInfo tzInfo = new TimeZoneInfo();

		tzInfo.setAutoAdjust( timeZone.useDaylightTime() );
		tzInfo.setDaylightBias( timeZone.getDSTSavings() / -60000 );
		tzInfo.setDaylightDate( DateUtils.formatDSTDate( timeZone, true ) );
		tzInfo.setDaylightName( timeZone.getDisplayName( true, 1 ) );
		tzInfo.setStandardDate( DateUtils.formatDSTDate( timeZone, false ) );
		tzInfo.setStandardName( TimezonesDictionary.fromOlsonToWindow( timeZone.getID() ) );
		tzInfo.setZoneBias( timeZone.getRawOffset() / -60000 );

		return tzInfo;
	}

	@WebMethod( operationName = "getTimezones" )
	public List<Timezone> getTimezones()
	{
		return TimezonesDictionary.getTimezones();
	}
}
