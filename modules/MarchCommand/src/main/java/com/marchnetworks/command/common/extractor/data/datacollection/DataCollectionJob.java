package com.marchnetworks.command.common.extractor.data.datacollection;

import com.marchnetworks.command.common.extractor.data.RecorderJob;

public class DataCollectionJob extends RecorderJob
{
	private LogCollectionSettings LogCollectionSettings;
	private GpsCollectionSettings GpsCollectionSettings;

	public DataCollectionJob()
	{
		LogCollectionSettings = new LogCollectionSettings();
	}

	public void addGpsCollection( String channelId )
	{
		GpsCollectionSettings = new GpsCollectionSettings( channelId );
	}

	class LogCollectionSettings
	{
		private boolean Enable = true;
		private int NumLogSets = 0;
		private int MinFrequencyHours = 0;

		public LogCollectionSettings()
		{
		}

		public boolean isEnable()
		{
			return Enable;
		}

		public void setEnable( boolean enable )
		{
			Enable = enable;
		}

		public int getNumLogSets()
		{
			return NumLogSets;
		}

		public void setNumLogSets( int numLogSets )
		{
			NumLogSets = numLogSets;
		}

		public int getMinFrequencyHours()
		{
			return MinFrequencyHours;
		}

		public void setMinFrequencyHours( int minFrequencyHours )
		{
			MinFrequencyHours = minFrequencyHours;
		}
	}

	class GpsCollectionSettings
	{
		private boolean Enable = true;
		private String ChannelId;

		public GpsCollectionSettings( String channelId )
		{
			ChannelId = channelId;
		}

		public boolean isEnable()
		{
			return Enable;
		}

		public void setEnable( boolean enable )
		{
			Enable = enable;
		}

		public String getChannelId()
		{
			return ChannelId;
		}

		public void setChannelId( String channelId )
		{
			ChannelId = channelId;
		}
	}

	public LogCollectionSettings getLogCollectionSettings()
	{
		return LogCollectionSettings;
	}

	public void setLogCollectionSettings( LogCollectionSettings logCollectionSettings )
	{
		LogCollectionSettings = logCollectionSettings;
	}

	public GpsCollectionSettings getGpsCollectionSettings()
	{
		return GpsCollectionSettings;
	}

	public void setGpsCollectionSettings( GpsCollectionSettings gpsCollectionSettings )
	{
		GpsCollectionSettings = gpsCollectionSettings;
	}
}
