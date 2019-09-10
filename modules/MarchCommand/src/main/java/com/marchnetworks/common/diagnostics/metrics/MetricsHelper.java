package com.marchnetworks.common.diagnostics.metrics;

import com.marchnetworks.command.api.metrics.MetricsCoreService;
import com.marchnetworks.common.spring.ApplicationContextSupport;

public class MetricsHelper
{
	public static MetricsCoreService metrics = ( MetricsCoreService ) ApplicationContextSupport.getBean( "metricsService" );
}
