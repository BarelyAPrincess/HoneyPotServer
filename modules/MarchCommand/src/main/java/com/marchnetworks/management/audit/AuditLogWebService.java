package com.marchnetworks.management.audit;

import com.marchnetworks.audit.common.AuditLogException;
import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditSearchQuery;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.data.DeviceAuditView;
import com.marchnetworks.audit.service.AuditLogService;
import com.marchnetworks.common.spring.ApplicationContextSupport;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;

@WebService( serviceName = "AuditService", name = "AuditService", portName = "AuditPort" )
@XmlSeeAlso( {AuditEventNameEnum.class} )
public class AuditLogWebService
{
	private AuditLogService service = ( AuditLogService ) ApplicationContextSupport.getBean( "auditLogServiceProxy" );

	@WebMethod( operationName = "getAuditLogs" )
	public List<AuditView> getAuditLogs( @WebParam( name = "auditSearchQuery" ) AuditSearchQuery auditViewCrit ) throws AuditLogException
	{
		return service.getAuditLogs( auditViewCrit );
	}

	@WebMethod( operationName = "getDeviceAuditLogs" )
	public List<DeviceAuditView> getDeviceAuditLogs( @WebParam( name = "startTime" ) long startTime, @WebParam( name = "endTime" ) long endTime ) throws AuditLogException
	{
		return service.getDeviceAuditLogs( startTime, endTime );
	}

	@WebMethod( operationName = "createAuditLog" )
	public void createAuditLog( @WebParam( name = "auditView" ) AuditView auditView )
	{
		service.logAudit( auditView );
	}
}
