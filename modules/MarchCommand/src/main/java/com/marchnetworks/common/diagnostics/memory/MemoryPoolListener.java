package com.marchnetworks.common.diagnostics.memory;

public abstract interface MemoryPoolListener
{
	public abstract void handleMemoryThresholdExceeded( MemoryPool paramMemoryPool, double paramDouble );
}
