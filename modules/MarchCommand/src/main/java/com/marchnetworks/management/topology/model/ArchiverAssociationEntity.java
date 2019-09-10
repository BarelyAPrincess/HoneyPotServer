package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.topology.data.ArchiverAssociation;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.utils.CommonUtils;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table( name = "ARCHIVER_ASSOCIATION" )
public class ArchiverAssociationEntity
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Column( name = "ARCHIVER_RESOURCEID" )
	private Long archiverResourceId;
	@Lob
	@Column( name = "PRIMARY_ASSOCIATED_DEVICEID" )
	private byte[] primaryAssociatedDeviceString;
	@Version
	@Column( name = "VERSION" )
	private Long version;

	public ArchiverAssociationEntity()
	{
	}

	public ArchiverAssociationEntity( Long archiverResourceId, Long[] deviceIds )
	{
		this.archiverResourceId = archiverResourceId;
		setAssociatedDevices( deviceIds );
	}

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public ArchiverAssociation toDataObject()
	{
		Long[] deviceIdArray = null;

		if ( primaryAssociatedDeviceString != null )
		{
			deviceIdArray = ( Long[] ) getAssociatedDevices().toArray( new Long[getAssociatedDevices().size()] );
		}
		ArchiverAssociation dataObject = new ArchiverAssociation( getArchiverResourceId(), deviceIdArray );
		return dataObject;
	}

	public Long getArchiverResourceId()
	{
		return archiverResourceId;
	}

	public void setArchiverResourceId( Long archiverResourceId )
	{
		this.archiverResourceId = archiverResourceId;
	}

	public Set<Long> getAssociatedDevices()
	{
		if ( primaryAssociatedDeviceString == null )
		{
			return null;
		}
		return CommonUtils.jsonToLongSet( getPrimaryAssociatedDeviceString() );
	}

	public void setAssociatedDevices( Long[] associatedDevicesArray )
	{
		if ( ( associatedDevicesArray == null ) || ( associatedDevicesArray.length == 0 ) )
		{
			primaryAssociatedDeviceString = null;
		}
		else
		{
			setPrimaryAssociatedDeviceString( CommonUtils.arrayToJson( associatedDevicesArray ) );
		}
	}

	public boolean removeFromAssociatedDevices( Long device )
	{
		if ( device == null )
		{
			return false;
		}

		Set<Long> associatedDevicesSet = getAssociatedDevices();
		if ( ( associatedDevicesSet != null ) && ( !associatedDevicesSet.isEmpty() ) )
		{
			boolean removed = associatedDevicesSet.remove( device );
			if ( removed )
			{
				if ( associatedDevicesSet.isEmpty() )
				{
					primaryAssociatedDeviceString = null;
				}
				else
				{
					setPrimaryAssociatedDeviceString( CoreJsonSerializer.toJson( associatedDevicesSet ) );
				}
			}
			return removed;
		}
		return false;
	}

	protected String getPrimaryAssociatedDeviceString()
	{
		return CommonAppUtils.encodeToUTF8String( primaryAssociatedDeviceString );
	}

	protected void setPrimaryAssociatedDeviceString( String associatedDevices )
	{
		primaryAssociatedDeviceString = CommonAppUtils.encodeStringToBytes( associatedDevices );
	}

	public Long getVersion()
	{
		return version;
	}

	public void setVersion( Long version )
	{
		this.version = version;
	}
}

