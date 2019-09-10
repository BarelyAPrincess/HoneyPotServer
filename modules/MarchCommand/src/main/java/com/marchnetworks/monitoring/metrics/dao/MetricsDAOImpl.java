package com.marchnetworks.monitoring.metrics.dao;

import com.marchnetworks.command.api.metrics.MetricSnapshot;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.utils.DateUtils;

public class MetricsDAOImpl implements MetricsDAO
{
	public static final String FILE = "..\\logs\\metricsCurrent.json";

	public MetricSnapshot find()
	{
		MetricSnapshot result = null;
		String json = CommonAppUtils.readFileToString( "..\\logs\\metricsCurrent.json" );

		if ( json != null )
		{
			result = ( MetricSnapshot ) CoreJsonSerializer.fromJson( json, MetricSnapshot.class );
		}

		return result;
	}

	public void delete()
	{
		CommonAppUtils.clearFile( "..\\logs\\metricsCurrent.json" );
	}

	public void update( MetricSnapshot snapshot )
	{
		String json = CoreJsonSerializer.toJsonIndented( snapshot );
		CommonAppUtils.writeStringToFile( "..\\logs\\metricsCurrent.json", json );
	}

	public void saveCurrent( MetricSnapshot snapshot )
	{
		String json = CoreJsonSerializer.toJsonIndented( snapshot );
		String file = "..\\logs\\metrics-" + DateUtils.getFileTimestampFromMillis( System.currentTimeMillis() ) + ".json";
		CommonAppUtils.writeStringToFile( file, json );
	}
}

