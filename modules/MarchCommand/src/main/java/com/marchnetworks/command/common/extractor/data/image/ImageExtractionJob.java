package com.marchnetworks.command.common.extractor.data.image;

import java.util.ArrayList;
import java.util.List;

public class ImageExtractionJob extends ImageExtractionJobBase
{
	private boolean IsPriority;
	private List<Request> Requests;

	public ImageExtractionJob()
	{
		IsPriority = false;
		Requests = new ArrayList();
	}

	public ImageExtractionJob( boolean priority, String tag )
	{
		IsPriority = priority;
		Requests = new ArrayList();
		setTag( tag );
	}

	public void addRequest( String channelId, List<Long> times )
	{
		Requests.add( new Request( channelId, ( Long[] ) times.toArray( new Long[times.size()] ) ) );
	}

	class Request
	{
		private String ChannelId;

		private String SectorId;

		private Long[] Times;

		public Request()
		{
		}

		public Request( String channelId, Long[] times )
		{
			ChannelId = channelId;
			Times = times;
		}

		public Long[] getTimes()
		{
			return Times;
		}

		public void setTimes( Long[] times )
		{
			Times = times;
		}

		public String getChannelId()
		{
			return ChannelId;
		}

		public void setChannelId( String channelId )
		{
			ChannelId = channelId;
		}

		public String getSectorId()
		{
			return SectorId;
		}

		public void setSectorId( String sectorId )
		{
			SectorId = sectorId;
		}
	}

	public boolean isIsPriority()
	{
		return IsPriority;
	}

	public void setIsPriority( boolean isPriority )
	{
		IsPriority = isPriority;
	}

	public List<Request> getRequests()
	{
		return Requests;
	}

	public void setRequests( List<Request> requests )
	{
		Requests = requests;
	}
}
