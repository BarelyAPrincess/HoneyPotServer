package com.marchnetworks.command.api.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MetricsDisplayUtils
{
	public static final String TOP_SUCCESS_COLUMN = "<b>Top success</b>";
	public static final String TOP_FAILURE_COLUMN = "<b>Top failure</b>";

	public static List<String> getNameValueHeaders()
	{
		return Arrays.asList( new String[] {"Name", "Value"} );
	}

	public static List<String> getNameCountHeaders()
	{
		return Arrays.asList( new String[] {"Name", "Count"} );
	}

	public static List<String> getMinMaxAvgHeaders()
	{
		return Arrays.asList( new String[] {"Name", "Min", "Max", "Avg", "Total", "Sum"} );
	}

	public static List<List<String>> getMinMaxAvgRows( Map<String, MinMaxAvg> rows )
	{
		List<List<String>> result = new ArrayList();
		Set<Entry<String, MinMaxAvg>> set = rows.entrySet();
		for ( Entry<String, MinMaxAvg> entry : set )
		{
			MinMaxAvg value = ( MinMaxAvg ) entry.getValue();
			List<String> row = Arrays.asList( new String[] {( String ) entry.getKey(), String.valueOf( value.getMin() ), String.valueOf( value.getMax() ), value.getAvgString(), String.valueOf( value.getTotal() ), String.valueOf( value.getSum() )} );
			result.add( row );
		}
		return result;
	}
}
