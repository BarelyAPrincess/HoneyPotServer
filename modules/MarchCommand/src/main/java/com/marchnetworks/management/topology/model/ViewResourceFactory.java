package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.TopologyExceptionTypeEnum;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.ViewMediaCell;
import com.marchnetworks.command.common.topology.data.ViewResource;
import com.marchnetworks.common.utils.XmlUtils;

import java.nio.charset.Charset;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ViewResourceFactory extends AbstractResourceFactory
{
	public ResourceEntity newResource( Resource resourceData ) throws TopologyException
	{
		ViewResource viewData = ( ViewResource ) resourceData;
		ViewResourceEntity viewResource = new ViewResourceEntity( viewData );

		if ( ( viewData.getCells() != null ) && ( viewData.getCells().length > 0 ) )
		{
			for ( ViewMediaCell cell : viewData.getCells() )
			{
				Document doc = XmlUtils.getDocumentFromString( cell.getCellMetaData(), Charset.forName( "utf-16" ) );
				if ( ( doc == null ) || ( doc.getDocumentElement() == null ) )
				{
					throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Invalid metadata format set for cell" );
				}
				String channelId = XmlUtils.getValueFromAttributeMap( "channelId", doc.getDocumentElement().getAttributes() );
				if ( channelId == null )
				{
					throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Invalid metadata format set for cell" );
				}
			}
		}
		viewResource.setName( resourceData.getName() );

		return viewResource;
	}

	public void onCreateAssociation( ResourceEntity resource, ResourceEntity parentResource ) throws TopologyException
	{
		if ( !( parentResource instanceof GroupEntity ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "View resource " + resource.getIdAsString() + " cannot be put under " + parentResource.getName() );
		}
	}
}

