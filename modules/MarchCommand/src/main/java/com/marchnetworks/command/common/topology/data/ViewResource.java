package com.marchnetworks.command.common.topology.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ViewResource extends LinkResource
{
	private String viewMetaData;
	private ViewMediaCell[] cells;

	public void update( Resource updatedResource )
	{
		if ( ( updatedResource instanceof ViewResource ) )
		{
			super.update( updatedResource );
			ViewResource updatedView = ( ViewResource ) updatedResource;
			viewMetaData = updatedView.getViewMetaData();
			cells = updatedView.getCells();
		}
	}

	public LinkType getLinkType()
	{
		return LinkType.VIEW;
	}

	public void removeLinkedResource( Resource resource )
	{
		if ( ( resource instanceof ChannelResource ) )
		{
			ChannelResource channel = ( ChannelResource ) resource;

			List<ViewMediaCell> cellsAsList = new ArrayList( Arrays.asList( cells ) );
			for ( Iterator<ViewMediaCell> iterator = cellsAsList.iterator(); iterator.hasNext(); )
			{
				ViewMediaCell cell = ( ViewMediaCell ) iterator.next();
				if ( cell.getChannelId().equals( channel.getChannelId() ) )
				{
					iterator.remove();
				}
			}
			cells = ( ( ViewMediaCell[] ) cellsAsList.toArray( new ViewMediaCell[cellsAsList.size()] ) );
		}
	}

	public String getViewMetaData()
	{
		return viewMetaData;
	}

	public void setViewMetaData( String viewMetaData )
	{
		this.viewMetaData = viewMetaData;
	}

	public ViewMediaCell[] getCells()
	{
		return cells;
	}

	public void setCells( ViewMediaCell[] cells )
	{
		this.cells = cells;
	}
}
