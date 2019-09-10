package com.marchnetworks.web.filters;

import com.marchnetworks.command.api.servlet.LocalConnectionFilter;

import javax.servlet.annotation.WebFilter;

@WebFilter( asyncSupported = true, servletNames = {"WatchdogServlet", "AlarmTest", "AlarmLoadTest", "AudioOutputTest", "AuditLoadTest", "DeviceStatsTest", "EventTest", "DeviceLoadTest", "ArchiverAssociationTest", "TestSchedule", "TestNotification", "TestUserRights", "AppTest", "TestAudit", "TestDiagnostic", "TestDevice", "Metrics"} )
public class CommandLocalConnectionFilter extends LocalConnectionFilter
{
}
