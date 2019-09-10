package com.marchnetworks.command.common.scheduling;

public class SerialTask implements Runnable
{
	private String id;

	private Runnable runnable;

	private FixedPoolSerialExecutor executor;

	public SerialTask( String id, Runnable runnable, FixedPoolSerialExecutor executor )
	{
		this.id = id;
		this.runnable = runnable;
		this.executor = executor;
	}

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public Runnable getRunnable()
	{
		return runnable;
	}

	public void setRunnable( Runnable runnable )
	{
		this.runnable = runnable;
	}

	public String getName()
	{
		return runnable.getClass().getSimpleName();
	}

	public void run()
	{
		long start = System.currentTimeMillis();
		try
		{
			runnable.run();
		}
		finally
		{
			long end = System.currentTimeMillis();
			executor.notifyTaskComplete( this, end - start );
		}
	}
}
