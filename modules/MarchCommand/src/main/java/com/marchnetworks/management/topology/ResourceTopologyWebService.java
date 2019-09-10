package com.marchnetworks.management.topology;

import com.marchnetworks.command.api.app.AppIds;
import com.marchnetworks.command.api.security.CommandAuthenticationDetails;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.alarm.data.AlarmSourceView;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.device.data.MassRegistrationInfo;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.command.common.topology.ResourceAssociationType;
import com.marchnetworks.command.common.topology.ResourceRootType;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.TopologyExceptionTypeEnum;
import com.marchnetworks.command.common.topology.data.AlarmSourceLinkResource;
import com.marchnetworks.command.common.topology.data.AlarmSourceResource;
import com.marchnetworks.command.common.topology.data.AudioOutputLinkResource;
import com.marchnetworks.command.common.topology.data.AudioOutputResource;
import com.marchnetworks.command.common.topology.data.ChannelLinkResource;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DataResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.GenericLinkResource;
import com.marchnetworks.command.common.topology.data.GenericResource;
import com.marchnetworks.command.common.topology.data.Group;
import com.marchnetworks.command.common.topology.data.MapResource;
import com.marchnetworks.command.common.topology.data.ResourceAssociation;
import com.marchnetworks.command.common.topology.data.ResourceMarkForReplacement;
import com.marchnetworks.command.common.topology.data.ResourcePathNode;
import com.marchnetworks.command.common.topology.data.SwitchLinkResource;
import com.marchnetworks.command.common.topology.data.SwitchResource;
import com.marchnetworks.command.common.topology.data.ViewResource;
import com.marchnetworks.command.common.user.data.RightEnum;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.management.topology.data.ResourceType;
import com.marchnetworks.management.user.UserService;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.springframework.security.access.AccessDeniedException;

@XmlSeeAlso( {DataResource.class, ChannelResource.class, DeviceResource.class, Group.class, ResourceAssociationType.class, AlarmSourceResource.class, AlarmSourceView.class, AlarmSourceLinkResource.class, ChannelLinkResource.class, ViewResource.class, SwitchResource.class, SwitchLinkResource.class, AudioOutputResource.class, AudioOutputLinkResource.class, GenericResource.class, GenericLinkResource.class, ResourceType.class, ResourceRootType.class} )
@WebService( serviceName = "ResourceTopologyService", name = "ResourceTopologyService", portName = "ResourceTopologyPort" )
public class ResourceTopologyWebService
{
	private ResourceTopologyServiceIF topologyService = ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyServiceProxy" );
	private UserService userService = ( UserService ) ApplicationContextSupport.getBean( "userServiceProxy_internal" );
	private String accessDenied = "not_authorized";

	@javax.annotation.Resource
	WebServiceContext wsContext;

	public List<com.marchnetworks.command.common.topology.data.Resource> getRootResources() throws TopologyException
	{
		try
		{
			return topologyService.getRootResources( ResourceRootType.ALL );
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	public com.marchnetworks.command.common.topology.data.Resource createResource( @WebParam( name = "resource" ) com.marchnetworks.command.common.topology.data.Resource resource, @WebParam( name = "parentResourceId" ) Long parentResourceId, @WebParam( name = "associationType" ) String associationType ) throws TopologyException
	{
		try
		{
			return topologyService.createResource( resource, parentResourceId, associationType );
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
		catch ( IllegalArgumentException ise )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, ise.getMessage(), ise );
		}
	}

	public void createResources( @WebParam( name = "resources" ) com.marchnetworks.command.common.topology.data.Resource[] resources ) throws TopologyException
	{
		try
		{
			topologyService.createResources( ( com.marchnetworks.command.common.topology.data.Resource[] ) resources );
		}
		catch ( AccessDeniedException ade )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
		catch ( IllegalArgumentException ise )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, ise.getMessage(), ise );
		}
	}

	public List<com.marchnetworks.command.common.topology.data.Resource> getResources( @WebParam( name = "resourceIds" ) Long[] resourceIds, @WebParam( name = "recursionLevel" ) int recursionLevel ) throws TopologyException
	{
		try
		{
			return topologyService.getResources( resourceIds, recursionLevel );
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	public com.marchnetworks.command.common.topology.data.Resource updateResource( @WebParam( name = "resource" ) com.marchnetworks.command.common.topology.data.Resource resource ) throws TopologyException
	{
		try
		{
			return topologyService.updateResource( resource );
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	public boolean removeResource( @WebParam( name = "resourceId" ) Long resourceId, @WebParam( name = "forceDeletion" ) Boolean forceDeletion ) throws TopologyException
	{
		try
		{
			if ( forceDeletion == null )
			{
				forceDeletion = Boolean.valueOf( false );
			}
			topologyService.removeResources( new Long[] {resourceId}, forceDeletion.booleanValue() );
			return true;
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	public void removeResources( @WebParam( name = "resourceIds" ) Long[] resourceIds, @WebParam( name = "forceDeletion" ) Boolean forceDeletion ) throws TopologyException
	{
		try
		{
			if ( forceDeletion == null )
			{
				forceDeletion = Boolean.valueOf( false );
			}
			topologyService.removeResources( resourceIds, forceDeletion.booleanValue() );
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
		catch ( IllegalArgumentException ise )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, ise.getMessage(), ise );
		}
	}

	public void updateAssociation( @WebParam( name = "association" ) ResourceAssociation association, @WebParam( name = "newAssociation" ) ResourceAssociation newAssociation ) throws TopologyException
	{
		try
		{
			topologyService.updateAssociation( association, newAssociation );
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	public void updateAssociations( @WebParam( name = "associations" ) ResourceAssociation[] associations, @WebParam( name = "newAssociations" ) ResourceAssociation[] newAssociations ) throws TopologyException
	{
		try
		{
			topologyService.updateAssociations( associations, newAssociations );
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
		catch ( IllegalArgumentException ise )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, ise.getMessage(), ise );
		}
	}

	public DeviceResource registerDeviceResource( @WebParam( name = "groupId" ) Long groupId, @WebParam( name = "deviceAddress" ) String deviceAddress, @WebParam( name = "deviceAdmin" ) String deviceAdmin, @WebParam( name = "deviceAdminPassword" ) String deviceAdminPassword, @WebParam( name = "deviceSessionId" ) String deviceSessionId, @WebParam( name = "stationId" ) String stationId ) throws TopologyException
	{
		try
		{
			CommandAuthenticationDetails sessionDetails = CommonUtils.getAuthneticationDetails();
			String detectedRemoteAddr = null;

			if ( AppIds.isR5AppId( sessionDetails.getAppId() ) )
			{
				HttpServletRequest request = ( HttpServletRequest ) wsContext.getMessageContext().get( "javax.xml.ws.servlet.request" );
				detectedRemoteAddr = request.getRemoteAddr();
			}

			return topologyService.registerDeviceResource( groupId, deviceAddress, detectedRemoteAddr, deviceAdmin, deviceAdminPassword, deviceSessionId, stationId );
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	public void reregisterDeviceResource( @WebParam( name = "deviceResourceId" ) Long deviceResourceId, @WebParam( name = "deviceAddress" ) String deviceAddress, @WebParam( name = "deviceAdmin" ) String deviceAdmin, @WebParam( name = "deviceAdminPassword" ) String deviceAdminPassword, @WebParam( name = "deviceSessionId" ) String deviceSessionId, @WebParam( name = "deviceId" ) String deviceId ) throws TopologyException
	{
		try
		{
			CommandAuthenticationDetails sessionDetails = CommonUtils.getAuthneticationDetails();
			String detectedRemoteAddr = null;
			if ( AppIds.isR5AppId( sessionDetails.getAppId() ) )
			{
				HttpServletRequest request = ( HttpServletRequest ) wsContext.getMessageContext().get( "javax.xml.ws.servlet.request" );
				detectedRemoteAddr = request.getRemoteAddr();
			}
			topologyService.reregisterDeviceResource( deviceResourceId, deviceAddress, detectedRemoteAddr, deviceAdmin, deviceAdminPassword, deviceSessionId, deviceId );
		}
		catch ( AccessDeniedException e )
		{
			DeviceResource deviceResource;
			RegistrationStatus regStatus;
			MessageContext context;
			HttpServletResponse response;
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
		finally
		{
			if ( ( deviceResourceId == null ) && ( !CommonAppUtils.isNullOrEmptyString( deviceId ) ) )
			{
				DeviceResource deviceResource = topologyService.getDeviceResourceByDeviceId( deviceId );
				if ( deviceResource != null )
				{
					RegistrationStatus regStatus = deviceResource.getDeviceView().getRegistrationStatus();
					MessageContext context = wsContext.getMessageContext();
					HttpServletResponse response = ( HttpServletResponse ) context.get( "javax.xml.ws.servlet.response" );
					response.addHeader( "x-registration-status", regStatus.toString() );
				}
			}
		}
	}

	public void unregisterDeviceResource( @WebParam( name = "deviceResourceId" ) Long deviceResourceId, @WebParam( name = "deviceId" ) String deviceId ) throws TopologyException
	{
		if ( !userService.hasRight( RightEnum.MANAGE_DEVICES ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
		if ( deviceResourceId == null )
		{
			topologyService.unregisterDeviceById( deviceId );
		}
		else
		{
			topologyService.removeResource( deviceResourceId );
		}
	}

	public void retryReplacement( @WebParam( name = "deviceResourceId" ) Long deviceResourceId ) throws TopologyException
	{
		try
		{
			topologyService.retryReplacement( deviceResourceId );
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	public void markForReplacements( @WebParam( name = "resourceMarkForReplacements" ) ResourceMarkForReplacement[] resourceMarkForReplacements ) throws TopologyException
	{
		try
		{
			topologyService.markForReplacements( resourceMarkForReplacements );
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	public void markForReplacement( @WebParam( name = "resourceMarkForReplacement" ) ResourceMarkForReplacement resourceMarkForReplacements ) throws TopologyException
	{
		try
		{
			topologyService.markForReplacement( resourceMarkForReplacements );
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	public void updateDeviceResource( @WebParam( name = "deviceResource" ) DeviceResource deviceResource ) throws TopologyException
	{
		try
		{
			topologyService.updateDeviceResource( deviceResource );
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	public List<DeviceResource> getDeviceResources() throws TopologyException
	{
		try
		{
			return topologyService.getDeviceResources();
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	public MapResource createMap( @WebParam( name = "resource" ) MapResource resource, @WebParam( name = "parentResourceId" ) Long parentResourceId, @WebParam( name = "mapData" ) byte[] mapData ) throws TopologyException
	{
		try
		{
			return topologyService.createMap( resource, parentResourceId, mapData );
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	public MapResource updateMap( @WebParam( name = "resource" ) MapResource resource, @WebParam( name = "mapData" ) byte[] mapData ) throws TopologyException
	{
		try
		{
			return topologyService.updateMap( resource, mapData );
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	public List<ResourcePathNode> getResourcePath( @WebParam( name = "resourceId" ) Long resourceId ) throws TopologyException
	{
		try
		{
			return topologyService.getResourcePath( resourceId );
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	public List<com.marchnetworks.command.common.topology.data.Resource> getResourcesForUser( @WebParam( name = "resourceRootType" ) ResourceRootType type, @WebParam( name = "resourceTypeFilter" ) ResourceType[] resourceTypeFilter ) throws TopologyException
	{
		try
		{
			return topologyService.getResourcesForUser( type, resourceTypeFilter );
		}
		catch ( AccessDeniedException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	public void massRegister( @WebParam( name = "registrationInformation" ) List<MassRegistrationInfo> registrationInformation, @WebParam( name = "securityToken" ) String securityToken, @WebParam( name = "rootFolder" ) Long rootFolder )
	{
		topologyService.massRegister( registrationInformation, securityToken, rootFolder );
	}

	public List<com.marchnetworks.command.common.topology.data.Resource> getArchiverResources()
	{
		return topologyService.getArchiverResources();
	}
}
