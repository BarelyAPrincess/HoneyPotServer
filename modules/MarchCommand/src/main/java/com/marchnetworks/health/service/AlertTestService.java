package com.marchnetworks.health.service;

import java.util.Map;

public interface AlertTestService
{
	void generateAlerts( int paramInt );

	void deleteAlerts();

	Long getLastDeviceAlertId();

	void findAlertByDeviceAndDeviceAlertId( String paramString1, String paramString2 );

	int getDeviceAlertCount();

	Map<String, Long> runBenchmark();
}
