package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.ViewMediaCell;
import com.marchnetworks.command.common.topology.data.ViewResource;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import java.util.ArrayList;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table( name = "VIEW_RESOURCE" )
public class ViewResourceEntity extends LinkResourceEntity
{
	@Lob
	@Column( name = "VIEW_META_DATA" )
	private byte[] viewMetaData;
	@Lob
	@Column( name = "CELLS_META_DATA" )
	private byte[] cellGridMetaData;

	public ViewResourceEntity()
	{
	}

	public ViewResourceEntity( ViewResource dataObject )
	{
		super( dataObject );
	}

	protected Resource newDataObject()
	{
		ViewResource viewResourceDTO = new ViewResource();
		super.newDataObject( viewResourceDTO );

		viewResourceDTO.setViewMetaData( getViewMetaData() );
		ViewMediaCell[] cellsData = ( ViewMediaCell[] ) CoreJsonSerializer.fromJson( getCellGridMetaData(), ViewMediaCell[].class );
		if ( cellsData != null )
			viewResourceDTO.setCells( cellsData );
		return viewResourceDTO;
	}

	public void readFromDataObject( Resource dataObject )
	{
		super.readFromDataObject( dataObject );

		ViewResource viewDataObject = ( ViewResource ) dataObject;
		setViewMetaData( viewDataObject.getViewMetaData() );
		setCellGridMetaData( CoreJsonSerializer.toJson( viewDataObject.getCells() ) );
		setLinkedResourceIds( viewDataObject.getLinkedResourceIds() == null ? new ArrayList() : Arrays.asList( viewDataObject.getLinkedResourceIds() ) );
	}

	public Class<ViewResource> getDataObjectClass()
	{
		return ViewResource.class;
	}

	protected String getViewMetaData()
	{
		return CommonAppUtils.encodeToUTF8String( viewMetaData );
	}

	protected void setViewMetaData( String viewMetaData )
	{
		this.viewMetaData = ( viewMetaData != null ? CommonAppUtils.encodeStringToBytes( viewMetaData ) : new byte[0] );
	}

	protected String getCellGridMetaData()
	{
		return CommonAppUtils.encodeToUTF8String( cellGridMetaData );
	}

	protected void setCellGridMetaData( String cellGridMetaData )
	{
		this.cellGridMetaData = CommonAppUtils.encodeStringToBytes( cellGridMetaData );
	}

	public boolean isContainer()
	{
		return true;
	}
}

