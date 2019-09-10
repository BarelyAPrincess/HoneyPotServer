package com.marchnetworks.monitoring.metrics.event;

import com.marchnetworks.command.api.metrics.input.MetricInput;
import com.marchnetworks.common.event.Event;

public class MetricsEvent extends Event
{
	private MetricInput metricInput;

	public MetricsEvent( MetricInput metricInput )
	{
		super( MetricsEvent.class.getName() );
		this.metricInput = metricInput;
	}

	public MetricInput getMetricInput()
	{
		return metricInput;
	}

	public void setMetricInput( MetricInput metricInput )
	{
		this.metricInput = metricInput;
	}
}

