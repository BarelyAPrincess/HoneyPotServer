package com.marchnetworks.management.topology.model;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.topology.data.ContainerItem;
import com.marchnetworks.command.common.topology.data.LinkResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table( name = "LINK_RESOURCE" )
public abstract class LinkResourceEntity extends ResourceEntity
{
	@Column( name = "LINKED_RESOURCE_IDS", length = 4000 )
	protected String linkedResourceIds;
	@Lob
	@Column( name = "CONTAINER_ITEMS" )
	private byte[] containerItemsString;

	public LinkResourceEntity()
	{
	}

	public LinkResourceEntity( LinkResource dataObject )
	{
		super( dataObject );
	}

	protected void newDataObject( LinkResource linkResource )
	{
		List<Long> linkedIds = getLinkedResourceIds();
		linkResource.setLinkedResourceIds( ( Long[] ) linkedIds.toArray( new Long[linkedIds.size()] ) );
		linkResource.setContainerItems( getContainerItems() );
	}

	public void readFromDataObject( Resource dataObject )
	{
		super.readFromDataObject( dataObject );

		LinkResource linkDataObject = ( LinkResource ) dataObject;
		setLinkedResourceIds( linkDataObject.getLinkedResourceIds() == null ? new ArrayList() : Arrays.asList( linkDataObject.getLinkedResourceIds() ) );
		setContainerItems( linkDataObject.getContainerItems() );
	}

	public List<Long> getLinkedResourceIds()
	{
		List<Long> linkedIds = new ArrayList();
		if ( linkedResourceIds != null )
		{
			String[] serializedIds = ( String[] ) CoreJsonSerializer.fromJson( linkedResourceIds, String[].class );
			for ( String id : serializedIds )
			{
				linkedIds.add( Long.valueOf( Long.parseLong( id ) ) );
			}
		}
		return linkedIds;
	}

	protected void setLinkedResourceIds( List<Long> linkedResourceIds )
	{
		this.linkedResourceIds = null;
		if ( linkedResourceIds != null )
		{
			String[] idsAsString = new String[linkedResourceIds.size()];
			for ( int i = 0; i < linkedResourceIds.size(); i++ )
			{
				idsAsString[i] = ( ( Long ) linkedResourceIds.get( i ) ).toString();
			}
			this.linkedResourceIds = CoreJsonSerializer.toJson( idsAsString );
		}
	}

	public List<ContainerItem> getContainerItems()
	{
		if ( containerItemsString != null )
		{
			return CoreJsonSerializer.collectionFromJson( getContainerItemsString(), new TypeToken<ArrayList<ContainerItem>>()
			{
			} );
		}

		return new ArrayList<ContainerItem>( 1 );
	}

	public void setContainerItems( List<ContainerItem> containerItems )
	{
		if ( ( containerItems == null ) || ( containerItems.isEmpty() ) )
		{
			containerItemsString = null;
		}
		else
		{
			setContainerItemsString( CoreJsonSerializer.toJson( containerItems ) );
		}
	}

	protected String getContainerItemsString()
	{
		return CommonAppUtils.encodeToUTF8String( containerItemsString );
	}

	protected void setContainerItemsString( String containerItemsString )
	{
		this.containerItemsString = CommonAppUtils.encodeStringToBytes( containerItemsString );
	}

	public abstract boolean isContainer();
}

