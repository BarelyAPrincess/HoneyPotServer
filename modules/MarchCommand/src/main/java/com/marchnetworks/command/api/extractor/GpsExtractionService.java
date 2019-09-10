package com.marchnetworks.command.api.extractor;

import com.marchnetworks.command.common.extractor.data.datacollection.GpsPoint;

import java.util.List;

public abstract interface GpsExtractionService extends BaseExtractionService
{
	public abstract List<GpsPoint> getGpsPoints( Long paramLong );
}
