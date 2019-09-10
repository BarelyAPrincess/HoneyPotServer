package com.marchnetworks.command.api.diagnostics;

import com.marchnetworks.command.common.diagnostics.IndexFragmentation;

import java.util.List;

public abstract interface IndexFragmentationService
{
	public abstract List<IndexFragmentation> appsIndexFragmentationCheck();
}
