package com.marchnetworks.esm.common.model;

import com.marchnetworks.command.common.user.data.UserDetailsView;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "USERDETAILS" )
public class UserDetailsEntity implements Serializable
{
	private static final long serialVersionUID = 5308848749368866342L;
	@Id
	@GeneratedValue
	private Long id;
	@Column( name = "FULLNAME", length = 255, nullable = true )
	private String fullname;
	@Column( name = "TELEPHONE", length = 255, nullable = true )
	private String telephone;
	@Column( name = "EMAIL", length = 255, nullable = true )
	private String email;
	@Column( name = "MANAGER", length = 255, nullable = true )
	private String manager;
	@Column( name = "POSITION", length = 255, nullable = true )
	private String position;
	@Column( name = "LANGUAGE_PREFERENCE", length = 100, nullable = true )
	private String languagePreference;
	@Column( name = "PRINCIPAL_NAME", length = 255 )
	private String principalName;
	@Column( name = "DISTINGUISHED_NAME", length = 300 )
	private String distinguishedName;
	@Column( name = "ACTIVE" )
	@Enumerated( EnumType.STRING )
	private boolean active;
	@Column( name = "ADMIN_ROLE" )
	private boolean superAdmin;
	@Column( name = "CARD_LOGOUT", nullable = false, columnDefinition = "tinyint default '0'" )
	private boolean cardLogout;
	@Column( name = "CERT_FILEOBJECT_ID", nullable = true )
	private String certificate;

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public String getFullname()
	{
		return fullname;
	}

	public void setFullname( String fullname )
	{
		this.fullname = fullname;
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

	public boolean isSuperAdmin()
	{
		return superAdmin;
	}

	public void setSuperAdmin( boolean superAdmin )
	{
		this.superAdmin = superAdmin;
	}

	public String getLanguagePreference()
	{
		return languagePreference;
	}

	public void setLanguagePreference( String languagePreference )
	{
		this.languagePreference = languagePreference;
	}

	public UserDetailsEntity()
	{
	}

	public UserDetailsEntity( String fullname, String telephone, String email, String manager, String position )
	{
		this.fullname = fullname;
		this.telephone = telephone;
		this.email = email;
		this.manager = manager;
		this.position = position;
	}

	public boolean equals( Object obj )
	{
		if ( obj == null )
		{
			return false;
		}
		if ( getClass() != obj.getClass() )
		{
			return false;
		}
		UserDetailsEntity other = ( UserDetailsEntity ) obj;
		if ( fullname == null ? fullname != null : !fullname.equals( fullname ) )
		{
			return false;
		}
		if ( telephone == null ? telephone != null : !telephone.equals( telephone ) )
		{
			return false;
		}
		if ( email == null ? email != null : !email.equals( email ) )
		{
			return false;
		}
		if ( manager == null ? manager != null : !manager.equals( manager ) )
		{
			return false;
		}
		if ( position == null ? position != null : !position.equals( position ) )
		{
			return false;
		}
		if ( active != active )
		{
			return false;
		}
		if ( superAdmin != superAdmin )
		{
			return false;
		}
		return true;
	}

	public int hashCode()
	{
		int hash = 7;
		hash = 89 * hash + ( fullname != null ? fullname.hashCode() : 0 );
		hash = 89 * hash + ( telephone != null ? telephone.hashCode() : 0 );
		hash = 89 * hash + ( email != null ? email.hashCode() : 0 );
		hash = 89 * hash + ( manager != null ? manager.hashCode() : 0 );
		hash = 89 * hash + ( position != null ? position.hashCode() : 0 );
		hash = 89 * hash + ( superAdmin ? 1 : 0 );
		return hash;
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append( " Fullname=" ).append( fullname ).append( " Telephone=" ).append( telephone ).append( " Email=" ).append( email );
		return buf.toString();
	}

	public UserDetailsView toDataObject()
	{
		UserDetailsView userView = new UserDetailsView();
		if ( isActive() )
		{
			userView.setActive( isActive() );
		}

		userView.setAdmin( isSuperAdmin() );
		userView.setEmail( getEmail() );
		userView.setFullname( getFullname() );
		userView.setManager( getManager() );
		userView.setPosition( getPosition() );
		userView.setTelephone( getTelephone() );
		userView.setLanguagePreference( getLanguagePreference() );
		userView.setPrincipalName( getPrincipalName() );
		userView.setDistinguishedName( getDistinguishedName() );
		userView.setCardLogout( getCardLogout() );
		userView.setCertificateId( getCertificate() );

		return userView;
	}

	public void setPrincipalName( String principalName )
	{
		this.principalName = principalName;
	}

	public String getPrincipalName()
	{
		return principalName;
	}

	public void readFromDataObject( UserDetailsView detailsView )
	{
		if ( detailsView == null )
		{
			return;
		}

		setActive( detailsView.isActive() );
		setSuperAdmin( detailsView.isAdmin() );
		setEmail( detailsView.getEmail() );
		setFullname( detailsView.getFullname() );
		setManager( detailsView.getManager() );
		setPosition( detailsView.getPosition() );
		setTelephone( detailsView.getTelephone() );
		setPrincipalName( detailsView.getPrincipalName() );
		setDistinguishedName( detailsView.getDistinguishedName() );
		setCardLogout( detailsView.getCardLogout() );
		setCertificate( detailsView.getCertificateId() );

		if ( ( detailsView.getLanguagePreference() != null ) && ( detailsView.getLanguagePreference().length() != 0 ) )
		{
			setLanguagePreference( detailsView.getLanguagePreference() );
		}
	}

	public void updateLdapInfo( UserDetailsView detailsView )
	{
		if ( detailsView == null )
		{
			return;
		}

		setEmail( detailsView.getEmail() );
		setFullname( detailsView.getFullname() );
		setManager( detailsView.getManager() );
		setPosition( detailsView.getPosition() );
		setTelephone( detailsView.getTelephone() );
		setPrincipalName( detailsView.getPrincipalName() );
		setDistinguishedName( detailsView.getDistinguishedName() );
	}

	public String getDistinguishedName()
	{
		return distinguishedName;
	}

	public void setDistinguishedName( String distinguishedName )
	{
		this.distinguishedName = distinguishedName;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive( boolean active )
	{
		this.active = active;
	}

	public boolean getCardLogout()
	{
		return cardLogout;
	}

	public void setCardLogout( boolean cardLogout )
	{
		this.cardLogout = cardLogout;
	}

	public String getCertificate()
	{
		return certificate;
	}

	public void setCertificate( String certificate )
	{
		this.certificate = certificate;
	}
}
