package com.marchnetworks.command.api.security;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class CommandAuthenticationDetails extends WebAuthenticationDetails
{
	private static final long serialVersionUID = 1204439708202501429L;
	private boolean identified = false;
	private String appId;
	private String challengeNonce;
	private Map<String, Object> params = new HashMap<String, Object>();

	public CommandAuthenticationDetails( HttpServletRequest request )
	{
		super( request );
	}

	public boolean isIdentified()
	{
		return identified;
	}

	public void setIdentified( boolean identified )
	{
		this.identified = identified;
	}

	public String getChallengeNonce()
	{
		return challengeNonce;
	}

	public void setChallengeNonce( String challengeNonce )
	{
		this.challengeNonce = challengeNonce;
	}

	public String getAppId()
	{
		return appId;
	}

	public void setAppId( String appId )
	{
		this.appId = appId;
	}

	public Map<String, Object> getParams()
	{
		return params;
	}

	public void addParam( String key, Object value )
	{
		params.put( key, value );
	}
}
