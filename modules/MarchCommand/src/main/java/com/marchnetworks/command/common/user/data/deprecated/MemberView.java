package com.marchnetworks.command.common.user.data.deprecated;

import com.marchnetworks.command.common.user.data.MemberTypeEnum;
import com.marchnetworks.command.common.user.data.ProfileView;
import com.marchnetworks.command.common.user.data.UserDetailsView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@Deprecated
public class MemberView
{
	private String name;
	private ProfileView profile;
	private UserDetailsView detailsView;
	private List<MemberResourceView> resources = new ArrayList();
	private Boolean termsAccepted;
	private MemberTypeEnum type;
	private Set<String> groups;

	public String getName()
	{
		return name;
	}

	public void setName( String username )
	{
		name = username;
	}

	public ProfileView getProfile()
	{
		return profile;
	}

	public void setProfile( ProfileView profile )
	{
		this.profile = profile;
	}

	@XmlElement( required = true )
	public MemberTypeEnum getType()
	{
		return type;
	}

	public void setType( MemberTypeEnum type )
	{
		this.type = type;
	}

	public UserDetailsView getDetailsView()
	{
		return detailsView;
	}

	public void setDetailsView( UserDetailsView detailsView )
	{
		this.detailsView = detailsView;
	}

	public List<MemberResourceView> getResources()
	{
		return resources;
	}

	public void setResources( List<MemberResourceView> resources )
	{
		this.resources = resources;
	}

	@XmlElement( required = true )
	public Boolean getTermsAccepted()
	{
		return Boolean.valueOf( ( termsAccepted != null ) && ( termsAccepted.booleanValue() ) );
	}

	public void setTermsAccepted( Boolean tosAccepted )
	{
		termsAccepted = tosAccepted;
	}

	@XmlTransient
	public Set<String> getGroups()
	{
		return groups;
	}

	public void setGroups( Set<String> groups )
	{
		this.groups = groups;
	}
}
