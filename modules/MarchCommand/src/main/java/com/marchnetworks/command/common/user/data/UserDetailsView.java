package com.marchnetworks.command.common.user.data;

public class UserDetailsView
{
	private String fullname;

	private String telephone;

	private String email;

	private String manager;

	private String position;

	private String principalName;

	private String distinguishedName;

	private String languagePreference;
	private boolean admin;
	private boolean active;
	private boolean cardLogout;
	private String certificateId;

	public boolean getCardLogout()
	{
		return cardLogout;
	}

	public void setCardLogout( boolean cardLogout )
	{
		this.cardLogout = cardLogout;
	}

	public String getFullname()
	{
		return fullname;
	}

	public void setFullname( String fullname )
	{
		this.fullname = fullname;
	}

	public String getTelephone()
	{
		return telephone;
	}

	public void setTelephone( String telephone )
	{
		this.telephone = telephone;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail( String email )
	{
		this.email = email;
	}

	public String getManager()
	{
		return manager;
	}

	public void setManager( String manager )
	{
		this.manager = manager;
	}

	public String getPosition()
	{
		return position;
	}

	public void setPosition( String position )
	{
		this.position = position;
	}

	public boolean isAdmin()
	{
		return admin;
	}

	public void setAdmin( boolean admin )
	{
		this.admin = admin;
	}

	public String getLanguagePreference()
	{
		return languagePreference;
	}

	public void setLanguagePreference( String languagePreference )
	{
		this.languagePreference = languagePreference;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive( boolean active )
	{
		this.active = active;
	}

	public String getPrincipalName()
	{
		return principalName;
	}

	public void setPrincipalName( String principalName )
	{
		this.principalName = principalName;
	}

	public String getDistinguishedName()
	{
		return distinguishedName;
	}

	public void setDistinguishedName( String distinguishedName )
	{
		this.distinguishedName = distinguishedName;
	}

	public String getCertificateId()
	{
		return certificateId;
	}

	public void setCertificateId( String certificateId )
	{
		this.certificateId = certificateId;
	}

	public void updateLdapDetails( UserDetailsView detailsView )
	{
		email = detailsView.getEmail();
		fullname = detailsView.getFullname();
		manager = detailsView.getManager();
		position = detailsView.getPosition();
		telephone = detailsView.getTelephone();
		principalName = detailsView.getPrincipalName();
		distinguishedName = detailsView.getDistinguishedName();
	}
}
