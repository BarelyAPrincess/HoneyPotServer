package com.marchnetworks.monitoring.metrics.dao;

import com.marchnetworks.command.api.metrics.MetricSnapshot;

public abstract interface MetricsDAO
{
	public abstract void update( MetricSnapshot paramMetricSnapshot );

	public abstract MetricSnapshot find();

	public abstract void delete();

	public abstract void saveCurrent( MetricSnapshot paramMetricSnapshot );
}

