package com.marchnetworks.security.saml;

import com.marchnetworks.app.service.OsgiService;
import com.marchnetworks.command.api.security.AuthorizationContent;
import com.marchnetworks.command.api.security.CommandAuthenticationDetails;
import com.marchnetworks.command.api.security.SamlException;
import com.marchnetworks.command.api.security.SamlException.SamlExceptionTypeEnum;
import com.marchnetworks.command.api.security.SecurityTokenCoreService;
import com.marchnetworks.command.api.security.TokenContributor;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.AlarmSourceResource;
import com.marchnetworks.command.common.topology.data.AudioOutputResource;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.SwitchResource;
import com.marchnetworks.command.common.user.data.RightEnum;
import com.marchnetworks.common.service.CertificationService;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.common.utils.ServerUtils;
import com.marchnetworks.license.LicenseService;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.model.DeviceLicenseInfo;
import com.marchnetworks.management.instrumentation.DeviceCapabilityService;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;

import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.jdom.DefaultJDOMFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMFactory;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.x500.X500PrivateCredential;

public class SecurityTokenServiceImpl implements SecurityTokenService, SecurityTokenCoreService
{
	static
	{
		Init.init();
	}

	private static final Logger LOG = LoggerFactory.getLogger( SecurityTokenServiceImpl.class );
	private static final String TOKEN_SERVICE_URL = "/Command/securityToken/";
	private static final String NO_RESOURCE = "undefined";

	public SecurityTokenServiceImpl()
	{
		notBeforeTime = -5;
		notAfterTime = 10;
		jDomFactory = new DefaultJDOMFactory();
	}

	private int notBeforeTime;

	private int notAfterTime;

	private JDOMFactory jDomFactory;

	public String getServerSecurityToken( String deviceId ) throws SamlException
	{
		return getServerSecurityToken( deviceId, notAfterTime );
	}

	public String getServerSecurityToken( String deviceId, int length ) throws SamlException
	{
		DeviceResource deviceResource = topologyService.getDeviceResourceByDeviceId( deviceId );

		if ( deviceResource == null )
		{
			throw new SamlException( "Could not find a DeviceResource mapped to the DeviceId provided: " + deviceId, SamlExceptionTypeEnum.BAD_REQUEST );
		}
		return createSecurityToken( deviceResource, deviceResource.createResourceList(), "_server_", length, deviceResource.getDeviceView().getTimeDelta().longValue() );
	}

	public String getUserSecurityToken( String deviceId, String tokenFormat ) throws SamlException
	{
		DeviceResource deviceResource = topologyService.getDeviceResourceByDeviceId( deviceId );
		if ( deviceResource == null )
		{
			throw new SamlException( "Could not find a DeviceResource mapped to the DeviceId provided: " + deviceId, SamlExceptionTypeEnum.BAD_REQUEST );
		}
		return getUserSecurityToken( deviceResource, tokenFormat, null );
	}

	private CertificationService certService;

	private ResourceTopologyServiceIF topologyService;

	private DeviceCapabilityService deviceCapabilityService;

	private LicenseService licenseService;

	private OsgiService osgiService;

	public String getUserSecurityToken( Long deviceResourceId, String tokenFormat, Long archiverResourceId ) throws SamlException
	{
		DeviceResource deviceResource = topologyService.getDeviceResource( deviceResourceId );
		if ( deviceResource == null )
		{
			throw new SamlException( "Error when querying for Topology resource of deviceId " + deviceResourceId, SamlExceptionTypeEnum.BAD_REQUEST );
		}
		DeviceResource archiverResource = null;
		if ( archiverResourceId != null )
		{
			archiverResource = topologyService.getDeviceResource( archiverResourceId );
			if ( archiverResource == null )
			{
				throw new SamlException( "Error when querying for Topology resource of archiverId " + archiverResourceId, SamlExceptionTypeEnum.BAD_REQUEST );
			}
		}
		return getUserSecurityToken( deviceResource, tokenFormat, archiverResource );
	}

	private String getUserSecurityToken( DeviceResource deviceResource, String tokenFormat, DeviceResource archiverResource ) throws SamlException
	{
		String username = CommonAppUtils.getUsernameFromSecurityContext();

		DeviceLicenseInfo license = licenseService.getDeviceLicense( Long.valueOf( deviceResource.getDeviceId() ) );
		if ( ( license != null ) && ( license.getRevoked() ) )
		{
			LOG.error( "Saml token cannot be generated because the license is expired" );
			throw new SamlException( "Expired License for device : " + deviceResource.getIdAsString(), SamlExceptionTypeEnum.BAD_REQUEST );
		}

		if ( "token.1".equalsIgnoreCase( tokenFormat ) )
		{
			return createSecurityToken( username );
		}

		if ( !licenseService.isIdentifiedAndLicensedSession() )
		{
			throw new SamlException( "Security token was requested for user " + username + " with unidentified client or invalid session", SamlExceptionTypeEnum.UNAUTHORIZED );
		}
		LOG.debug( "Token requested from user {} for device id {}", new Object[] {username, deviceResource.getDeviceId()} );

		List<Resource> deviceResourceList = new ArrayList();
		CommandAuthenticationDetails sessionDetails = CommonUtils.getAuthneticationDetails();
		try
		{
			deviceResourceList = topologyService.getResources( new Long[] {deviceResource.getId()}, -1 );

			deviceResourceList = licenseService.filterAppResources( sessionDetails.getAppId(), deviceResource.getId(), deviceResourceList );
		}
		catch ( LicenseException e )
		{
			throw new SamlException( "Error looking up resources for App " + sessionDetails.getAppId() + " when asking for security token, Exception:" + e.getMessage(), SamlExceptionTypeEnum.UNAUTHORIZED );
		}
		catch ( TopologyException e )
		{
			throw new SamlException( "Error looking up resources in topology when asking for security token, Exception:" + e.getMessage(), SamlExceptionTypeEnum.UNAUTHORIZED );
		}
		if ( deviceResourceList.isEmpty() )
		{
			throw new SamlException( "User does not have access to deviceId " + deviceResource.getId(), SamlExceptionTypeEnum.UNAUTHORIZED );
		}
		Long timeDelta = deviceResource.getDeviceView().getTimeDelta();
		if ( archiverResource != null )
		{
			timeDelta = archiverResource.getDeviceView().getTimeDelta();
		}
		return createSecurityToken( deviceResource, deviceResourceList, username, notAfterTime, timeDelta.longValue() );
	}

	private String createSecurityToken( DeviceResource rootDevice, List<Resource> deviceResourceList, String username, int length, long timeDelta ) throws SamlException
	{
		String responseXmlString = createSamlResponse( rootDevice, deviceResourceList, username, length, timeDelta );
		return signAndWrapToken( responseXmlString );
	}

	private String createSecurityToken( String userName ) throws SamlException
	{
		String notBefore = SamlTokenUtils.getFormattedConditionTime( notBeforeTime, Long.valueOf( 0L ) );
		String notOnOrAfter = SamlTokenUtils.getFormattedConditionTime( notAfterTime, Long.valueOf( 0L ) );

		String samlResponse = genStartSAMLResponse( userName, notBefore, notOnOrAfter );
		Document responseDoc = SamlTokenUtils.createJdomDoc( samlResponse );
		Element assertionElement = responseDoc.getRootElement().getChild( "Assertion", SamlTokenUtils.SAML_NS );

		Element authzStatement = buildAuthDecisionElement( "undefined" );
		assertionElement.addContent( authzStatement );
		resolveUserRolesForToken( authzStatement, userName );

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try
		{
			new XMLOutputter( Format.getPrettyFormat() ).output( responseDoc, bout );
		}
		catch ( IOException e )
		{
			throw new SamlException( e, SamlExceptionTypeEnum.INTERNAL_SERVER_ERROR );
		}
		String responseXmlString = CommonAppUtils.encodeToUTF8String( bout.toByteArray() );
		return signAndWrapToken( responseXmlString );
	}

	private String signAndWrapToken( String unsignedToken ) throws SamlException
	{
		X500PrivateCredential commandCredential = null;
		try
		{
			commandCredential = certService.getCommandCredential();
		}
		catch ( Exception e )
		{
			LOG.warn( "Failed to get command signing authority: ", e );
		}
		if ( commandCredential == null )
		{
			throw new SamlException( "not local command credential", SamlExceptionTypeEnum.UNAUTHORIZED );
		}

		String signedSamlResponse = XmlDigitalSigner.signXML( unsignedToken, commandCredential.getCertificate().getPublicKey(), commandCredential.getPrivateKey() );
		try
		{
			Canonicalizer canonicalizer = Canonicalizer.getInstance( "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments" );

			byte[] canon = canonicalizer.canonicalize( CommonAppUtils.encodeStringToBytes( signedSamlResponse ) );
			return CommonAppUtils.encodeToUTF8String( canon );
		}
		catch ( Exception e )
		{
			LOG.warn( "Failed to Canonicalize SAML token: ", e );
			throw new SamlException( "failed to canonicalize SAML token ", SamlExceptionTypeEnum.INTERNAL_SERVER_ERROR );
		}
	}

	private String createSamlResponse( DeviceResource device, List<Resource> deviceResourceList, String userName, int length, long timeDelta ) throws SamlException
	{
		String notBefore = SamlTokenUtils.getFormattedConditionTime( notBeforeTime, Long.valueOf( timeDelta ) );
		String notOnOrAfter = SamlTokenUtils.getFormattedConditionTime( length, Long.valueOf( timeDelta ) );

		String samlResponse = genStartSAMLResponse( userName, notBefore, notOnOrAfter );
		Document responseDoc = SamlTokenUtils.createJdomDoc( samlResponse );
		Element assertionElement = responseDoc.getRootElement().getChild( "Assertion", SamlTokenUtils.SAML_NS );

		if ( ( device.getDeviceView().getConnectState().equals( ConnectState.OFFLINE ) ) || ( deviceCapabilityService.isCapabilityEnabled( Long.parseLong( device.getDeviceId() ), "token.2", false ) ) )
		{
			for ( Resource resource : deviceResourceList )
			{
				Element authzStatement = null;
				if ( ( resource instanceof DeviceResource ) )
				{
					DeviceResource deviceResource = ( DeviceResource ) resource;
					if ( !deviceResource.isRootDevice() )
					{
						continue;
					}
					StringBuilder sb = new StringBuilder( "device:" );
					sb.append( deviceResource.getIdAsString() ).append( ":" ).append( deviceResource.getDeviceId() );
					authzStatement = buildAuthDecisionElement( sb.toString() );
					resolveUserRolesForToken( authzStatement, userName, "device:" );
				}
				else
				{
					if ( ( resource instanceof ChannelResource ) )
					{

						ChannelResource channelResource = ( ChannelResource ) resource;
						authzStatement = buildAuthDecisionElement( "channel:" + channelResource.getChannelId() );
						resolveUserRolesForToken( authzStatement, userName, "channel:" );
						assertionElement.addContent( authzStatement );

						if ( CommonAppUtils.isNullOrEmptyString( channelResource.getChannelView().getPtzDomeIdentifier() ) )
							continue;
						authzStatement = buildAuthDecisionElement( "ptz:" + channelResource.getChannelView().getPtzDomeIdentifier() );
						resolveUserRolesForToken( authzStatement, userName, "ptz:" );
						assertionElement.addContent( authzStatement );
						continue;
					}

					if ( ( resource instanceof AlarmSourceResource ) )
					{
						AlarmSourceResource alarmResource = ( AlarmSourceResource ) resource;
						authzStatement = buildAuthDecisionElement( "alarmsource:" + alarmResource.getAlarmSource().getDeviceAlarmSourceId() );
						resolveUserRolesForToken( authzStatement, userName, "alarmsource:" );
					}
					else if ( ( resource instanceof SwitchResource ) )
					{
						SwitchResource switchResource = ( SwitchResource ) resource;
						authzStatement = buildAuthDecisionElement( "switch:" + switchResource.getSwitchView().getSwitchId() );
						setEmptyActionElement( authzStatement );
					}
					else if ( ( resource instanceof AudioOutputResource ) )
					{
						AudioOutputResource audioOutputResource = ( AudioOutputResource ) resource;
						authzStatement = buildAuthDecisionElement( "audiooutput:" + audioOutputResource.getAudioOutputView().getAudioOutputId() );
						setEmptyActionElement( authzStatement );
					}
				}
				if ( authzStatement != null )
				{
					assertionElement.addContent( authzStatement );
				}
			}
		}
		else
		{
			Element authzStatement = buildAuthDecisionElement( device.getDeviceId() );
			assertionElement.addContent( authzStatement );
			resolveUserRolesForToken( authzStatement, userName );
		}

		if ( !"_server_".equals( userName ) )
		{
			List<TokenContributor> tokenContributors = getOsgiService().getServices( TokenContributor.class );
			for ( TokenContributor contributor : tokenContributors )
			{
				AuthorizationContent authorizationContent = contributor.getAuthorizationContent( userName, device.getId() );
				if ( authorizationContent != null )
				{
					Element authDecisionElement = buildAuthDecisionElement( authorizationContent.getResource() );
					addActionElements( authDecisionElement, authorizationContent.getRights() );

					assertionElement.addContent( authDecisionElement );
				}
			}
		}

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try
		{
			new XMLOutputter( Format.getPrettyFormat() ).output( responseDoc, bout );
		}
		catch ( IOException e )
		{
			throw new SamlException( e, SamlExceptionTypeEnum.INTERNAL_SERVER_ERROR );
		}
		samlResponse = CommonAppUtils.encodeToUTF8String( bout.toByteArray() );
		LOG.debug( "Created SAML response {}.", samlResponse );
		return samlResponse;
	}

	private String genStartSAMLResponse( String user, String notBefore, String notOnAfter )
	{
		String s01 = "<samlp:Response ID=\"";
		String s02 = "\" IssueInstant=\"";
		String s03 = "\" Version=\"2.0\"\n\txmlns=\"urn:oasis:names:tc:SAML:2.0:assertion\"\n\txmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\"\n\txmlns:xenc=\"http://www.w3.org/2001/04/xmlenc#\">\n\t<samlp:Status>\n\t\t<samlp:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/>\n\t</samlp:Status>\n\t<Assertion ID=\"";

		String s04 = "\"\n\t\tIssueInstant=\"";
		String s05 = "\" Version=\"2.0\"\n\t\txmlns=\"urn:oasis:names:tc:SAML:2.0:assertion\">\n\t\t<Issuer>https://";

		String s06 = "</Issuer>\n\t\t<Subject>\n\t\t\t<NameID Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\">";

		String s07 = "</NameID>\n\t\t\t<SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\"/>\n";

		String s08 = "\t\t</Subject>\n\t\t<Conditions NotBefore=\"";
		String s09 = "\" NotOnOrAfter=\"";
		String s10 = "\">\n\t\t\t<AudienceRestriction>\n\t\t\t\t<Audience></Audience>\n\t\t\t</AudienceRestriction>\n";
		String s11 = "\t\t</Conditions>\n\t\t<AuthnStatement AuthnInstant=\"";
		String s12 = "\">\n\t\t\t<AuthnContext>\n\t\t\t\t<AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:Password</AuthnContextClassRef>\n\t\t\t</AuthnContext>\n\t\t</AuthnStatement>\n\t</Assertion>\n</samlp:Response>\n";

		StringBuilder sb = new StringBuilder( "<samlp:Response ID=\"" );
		sb.append( SamlTokenUtils.createID() );
		sb.append( "\" IssueInstant=\"" );
		sb.append( SamlTokenUtils.getDateAndTime() );
		sb.append( "\" Version=\"2.0\"\n\txmlns=\"urn:oasis:names:tc:SAML:2.0:assertion\"\n\txmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\"\n\txmlns:xenc=\"http://www.w3.org/2001/04/xmlenc#\">\n\t<samlp:Status>\n\t\t<samlp:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/>\n\t</samlp:Status>\n\t<Assertion ID=\"" );
		sb.append( SamlTokenUtils.createID() );
		sb.append( "\"\n\t\tIssueInstant=\"" );
		sb.append( SamlTokenUtils.getDateAndTime() );
		sb.append( "\" Version=\"2.0\"\n\t\txmlns=\"urn:oasis:names:tc:SAML:2.0:assertion\">\n\t\t<Issuer>https://" );
		sb.append( ServerUtils.getHostName() );
		sb.append( "/Command/securityToken/" );
		sb.append( "</Issuer>\n\t\t<Subject>\n\t\t\t<NameID Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\">" );
		sb.append( user );
		sb.append( "</NameID>\n\t\t\t<SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\"/>\n" );
		sb.append( "\t\t</Subject>\n\t\t<Conditions NotBefore=\"" );
		sb.append( notBefore );
		sb.append( "\" NotOnOrAfter=\"" );
		sb.append( notOnAfter );
		sb.append( "\">\n\t\t\t<AudienceRestriction>\n\t\t\t\t<Audience></Audience>\n\t\t\t</AudienceRestriction>\n" );
		sb.append( "\t\t</Conditions>\n\t\t<AuthnStatement AuthnInstant=\"" );
		sb.append( SamlTokenUtils.getDateAndTime() );
		sb.append( "\">\n\t\t\t<AuthnContext>\n\t\t\t\t<AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:Password</AuthnContextClassRef>\n\t\t\t</AuthnContext>\n\t\t</AuthnStatement>\n\t</Assertion>\n</samlp:Response>\n" );
		return sb.toString();
	}

	private void resolveUserRolesForToken( Element authzStatement, String userName )
	{
		if ( userName.equals( "_server_" ) )
		{
			addServerRole( authzStatement );
			for ( RightEnum right : RightEnum.values() )
			{
				Element action = jDomFactory.element( "Action", SamlTokenUtils.SAML_NS );
				action.setText( "ROLE_" + right );
				authzStatement.addContent( action );
			}
		}
		else if ( ( SecurityContextHolder.getContext() != null ) && ( SecurityContextHolder.getContext().getAuthentication() != null ) )
		{
			for ( GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities() )
			{
				Element action = jDomFactory.element( "Action", SamlTokenUtils.SAML_NS );
				action.setText( authority.getAuthority() );
				authzStatement.addContent( action );
			}
		}
	}

	private void addServerRole( Element authzStatement )
	{
		Element action = jDomFactory.element( "Action", SamlTokenUtils.SAML_NS );
		action.setText( "ROLE_SERVER" );
		authzStatement.addContent( action );
	}

	private void resolveUserRolesForToken( Element authzStatement, String userName, String grantedResourceName )
	{
		List<RightEnum> userProfileRights = new ArrayList();
		if ( userName.equals( "_server_" ) )
		{
			addServerRole( authzStatement );
			userProfileRights.addAll( Arrays.asList( RightEnum.values() ) );
		}
		else if ( ( SecurityContextHolder.getContext() != null ) && ( SecurityContextHolder.getContext().getAuthentication() != null ) )
		{
			for ( GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities() )
			{
				RightEnum right = RightEnum.getRightFromString( authority.getAuthority() );
				if ( right != null )
				{
					userProfileRights.add( right );
				}
			}
		}

		UserRightFilter.filterRolesByResource( grantedResourceName, userProfileRights );
		List<String> userRights = new ArrayList( userProfileRights.size() );
		for ( RightEnum right : userProfileRights )
		{
			userRights.add( right.name() );
		}
		addActionElements( authzStatement, userRights );
	}

	private Element buildAuthDecisionElement( String resourceValue )
	{
		Element element = jDomFactory.element( "AuthzDecisionStatement", SamlTokenUtils.SAML_NS );
		element.setAttribute( "Decision", "Permit" );
		element.setAttribute( "Resource", resourceValue );
		return element;
	}

	private void setEmptyActionElement( Element authzStatement )
	{
		Element action = jDomFactory.element( "Action", SamlTokenUtils.SAML_NS );
		authzStatement.addContent( action );
	}

	private void addActionElements( Element authzStatement, List<String> actions )
	{
		for ( String action : actions )
		{
			Element actionElement = jDomFactory.element( "Action", SamlTokenUtils.SAML_NS );
			actionElement.setText( "ROLE_" + action );
			authzStatement.addContent( actionElement );
		}
	}

	private static class UserRightFilter
	{
		private static RightEnum[] channelResourceRights = {RightEnum.LIVE_VIDEO, RightEnum.ARCHIVE_VIDEO};

		private static RightEnum[] ptzResourceRights = {RightEnum.PTZ_CONTROL};
		private static RightEnum[] alarmResourceRights = {RightEnum.MANAGE_ALARMS};

		static
		{
			Arrays.sort( channelResourceRights );
			Arrays.sort( alarmResourceRights );
			Arrays.sort( ptzResourceRights );
		}

		static void filterRolesByResource( String resourceName, Collection<RightEnum> userRights )
		{
			if ( resourceName.equals( "device:" ) )
			{
				for ( Iterator<RightEnum> iterator = userRights.iterator(); iterator.hasNext(); )
				{
					RightEnum rightEnum = ( RightEnum ) iterator.next();

					if ( ( Arrays.binarySearch( channelResourceRights, rightEnum ) >= 0 ) || ( Arrays.binarySearch( alarmResourceRights, rightEnum ) >= 0 ) || ( Arrays.binarySearch( ptzResourceRights, rightEnum ) >= 0 ) )
					{

						iterator.remove();
					}
				}
			}
			else
			{
				if ( resourceName.equals( "channel:" ) )
				{
					for ( Iterator<RightEnum> iterator = userRights.iterator(); iterator.hasNext(); )
					{
						RightEnum rightEnum = ( RightEnum ) iterator.next();
						if ( Arrays.binarySearch( channelResourceRights, rightEnum ) < 0 )
							iterator.remove();
					}
				}
				else
				{
					if ( resourceName.equals( "alarmsource:" ) )
					{
						for ( Iterator<RightEnum> iterator = userRights.iterator(); iterator.hasNext(); )
						{
							RightEnum rightEnum = ( RightEnum ) iterator.next();
							if ( Arrays.binarySearch( alarmResourceRights, rightEnum ) < 0 )
							{
								iterator.remove();
							}
						}
					}
					else if ( resourceName.equals( "ptz:" ) )
					{
						for ( Iterator<RightEnum> iterator = userRights.iterator(); iterator.hasNext(); )
						{
							RightEnum rightEnum = ( RightEnum ) iterator.next();
							if ( Arrays.binarySearch( ptzResourceRights, rightEnum ) < 0 )
								iterator.remove();
						}
					}
				}
			}
		}
	}

	public void setNotBeforeTime( int notBeforeTime )
	{
		this.notBeforeTime = notBeforeTime;
	}

	public void setNotAfterTime( int notAfterTime )
	{
		this.notAfterTime = notAfterTime;
	}

	public void setTopologyService( ResourceTopologyServiceIF topologyService )
	{
		this.topologyService = topologyService;
	}

	public void setCertificationService( CertificationService cs )
	{
		certService = cs;
	}

	public void setDeviceCapabilityService( DeviceCapabilityService deviceCapabilityService )
	{
		this.deviceCapabilityService = deviceCapabilityService;
	}

	public void setLicenseService( LicenseService licenseService )
	{
		this.licenseService = licenseService;
	}

	public OsgiService getOsgiService()
	{
		if ( osgiService == null )
		{
			osgiService = ( ( OsgiService ) ApplicationContextSupport.getBean( "osgiManager" ) );
		}
		return osgiService;
	}
}

