package com.marchnetworks.management.topology.util;

import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;

import java.util.Arrays;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.Semaphore;

public class SimulatedDevicesKiller extends RecursiveAction
{
	private static final long serialVersionUID = 1L;
	private static final ResourceTopologyServiceIF topologyService = ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyServiceProxy_internal" );

	private static final int MAX_KILLS_FOR_A_SINGLE_THREAD = 20;
	private Long[] dyingResources;
	private Semaphore semaphore;

	public SimulatedDevicesKiller( Long[] dyingResources, Semaphore semaphore )
	{
		this.dyingResources = dyingResources;
		this.semaphore = semaphore;
	}

	protected void compute()
	{
		if ( dyingResources.length < 20 )
		{
			try
			{
				semaphore.acquire();
				topologyService.removeResources( dyingResources );
				semaphore.release();
			}
			catch ( TopologyException e )
			{
				e.printStackTrace();
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			int halfWay = Math.round( dyingResources.length / 2 );
			invokeAll( new SimulatedDevicesKiller( Arrays.copyOfRange( dyingResources, 0, halfWay ), semaphore ), new SimulatedDevicesKiller( Arrays.copyOfRange( dyingResources, halfWay, dyingResources.length ), semaphore ) );
		}
	}
}

