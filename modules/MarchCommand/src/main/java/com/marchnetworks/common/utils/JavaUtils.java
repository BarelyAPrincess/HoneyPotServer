package com.marchnetworks.common.utils;

import com.marchnetworks.command.common.CommonAppUtils;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class JavaUtils
{
	public static void generateThreadDump()
	{
		StringBuilder dump = new StringBuilder();
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads( true, true );
		for ( ThreadInfo threadInfo : threadInfos )
		{
			dump.append( "\"" + threadInfo.getThreadName() + "\"" + " Id=" + threadInfo.getThreadId() + " " + threadInfo.getThreadState() );

			if ( threadInfo.getLockName() != null )
			{
				dump.append( " on " + threadInfo.getLockName() );
			}
			if ( threadInfo.getLockOwnerName() != null )
			{
				dump.append( " owned by \"" + threadInfo.getLockOwnerName() + "\" Id=" + threadInfo.getLockOwnerId() );
			}

			if ( threadInfo.isSuspended() )
			{
				dump.append( " (suspended)" );
			}
			if ( threadInfo.isInNative() )
			{
				dump.append( " (in native)" );
			}
			dump.append( '\n' );

			StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
			for ( int i = 0; i < stackTraceElements.length; i++ )
			{
				StackTraceElement stackTrace = stackTraceElements[i];
				dump.append( "\t" + stackTrace );
				dump.append( '\n' );

				for ( MonitorInfo monitorInfo : threadInfo.getLockedMonitors() )
				{
					if ( monitorInfo.getLockedStackDepth() == i )
					{
						dump.append( "\t-  locked " + monitorInfo );
						dump.append( '\n' );
					}
				}
			}

			LockInfo[] locks = threadInfo.getLockedSynchronizers();
			if ( locks.length > 0 )
			{
				dump.append( "\n\tNumber of locked synchronizers = " + locks.length );
				dump.append( '\n' );
				for ( LockInfo lock : locks )
				{
					dump.append( "\t- " + lock );
					dump.append( '\n' );
				}
			}
			dump.append( '\n' );
		}
		String dumpString = dump.toString();
		CommonAppUtils.writeStringToFile( "..\\logs\\ThreadDump " + DateUtils.getFileTimestampFromMillis( System.currentTimeMillis() ) + ".tdump", dumpString );
	}
}

