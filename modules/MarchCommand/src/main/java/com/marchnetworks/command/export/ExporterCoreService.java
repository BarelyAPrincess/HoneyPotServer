package com.marchnetworks.command.export;

import java.util.List;

public interface ExporterCoreService
{
	byte[] exportData( List<String> paramList, List<List<String>> paramList1 ) throws ExporterException;
}
