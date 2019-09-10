package com.marchnetworks.monitoring.metrics.util;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.command.api.metrics.BucketMinMaxAvg;
import com.marchnetworks.command.api.metrics.Metric;
import com.marchnetworks.command.api.metrics.MetricSnapshot;
import com.marchnetworks.command.api.metrics.MinMaxAvg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MetricsGraphMinMaxAvg
{
	public static DecimalFormat formatter = new DecimalFormat( "#0.0" );

	public static void main( String[] args )
	{
		// TODO Temp on both windows and linux
		String directory = "/tmp/logs";
		String graphMetric = "device.tasks";
		List<String> subMetrics = Arrays.asList( "UpdateConnectionState", "UpdateConnectionTime" );
		Map<String, Map<String, MinMaxAvgGraph>> graphs = new LinkedHashMap<String, Map<String, MinMaxAvgGraph>>();

		File file = new File( directory );
		String[] names = file.list();

		for ( String name : names )
		{
			File folder = new File( directory + File.separator + name );
			if ( folder.isDirectory() )
			{
				File[] files = folder.listFiles();

				for ( File logFile : files )
				{
					if ( logFile.getName().equals( "metrics.json" ) )
					{

						String json = readFileToString( logFile.getPath() );

						json = "[" + json.substring( 0, json.length() - 2 ) + "]";

						List<MetricSnapshot> result = com.marchnetworks.common.serialization.CoreJsonSerializer.collectionFromJson( json, new TypeToken<ArrayList<MetricSnapshot>>()
						{
						} );
						Map<String, MinMaxAvgGraph> currentGraphs = new LinkedHashMap<String, MinMaxAvgGraph>();
						for ( String subMetric : subMetrics )
						{
							currentGraphs.put( subMetric, new MinMaxAvgGraph() );
						}

						for ( Iterator i$ = result.iterator(); i$.hasNext(); )
						{
							MetricSnapshot snapshot = ( MetricSnapshot ) i$.next();

							if ( snapshot != null )
							{

								List<Metric> metrics = snapshot.getMetrics();
								for ( Metric metric : metrics )
									if ( ( ( metric instanceof BucketMinMaxAvg ) ) && ( metric.getName().equals( graphMetric ) ) )
									{
										BucketMinMaxAvg bucketMinMaxAvg = ( BucketMinMaxAvg ) metric;

										for ( i$ = subMetrics.iterator(); i$.hasNext(); )
										{
											String subMetric = ( String ) i$.next();
											for ( Entry<String, MinMaxAvg> minMaxAvgEntry : bucketMinMaxAvg.getAverages().entrySet() )
											{
												if ( minMaxAvgEntry.getKey().equals( subMetric ) )
												{
													MinMaxAvgGraph graph = currentGraphs.get( subMetric );
													MinMaxAvg minMaxAvg = minMaxAvgEntry.getValue();

													graph.addMax( minMaxAvg.getMax() );
													graph.addAverage( minMaxAvg.getAvg() );
													graph.addDate( snapshot.getTimeString() );
													break;
												}
											}
										}
									}
							}
						}

						Iterator i$;
						graphs.put( folder.getName(), currentGraphs );
					}
				}
			}
		}

		String result = "";

		for ( Entry<String, Map<String, MinMaxAvgGraph>> entry : graphs.entrySet() )
		{
			String logName = entry.getKey();
			Map<String, MinMaxAvgGraph> currentGraphs = entry.getValue();

			result = result + logName + "\r\n";

			for ( Entry<String, MinMaxAvgGraph> currentGraphEntry : currentGraphs.entrySet() )
			{
				String graphName = currentGraphEntry.getKey();
				MinMaxAvgGraph graph = currentGraphEntry.getValue();

				result = result + graphName + "\r\n";
				result = result + getListLine( graph.getDates() );
				result = result + getListLine( graph.getAverages() );
				result = result + getListLine( graph.getMaxes() );
			}
			result = result + " \r\n";
		}

		String outputFile = directory + File.separator + graphMetric + " results.csv";
		writeStringToFile( outputFile, result );
		System.out.println( "Results written to " + outputFile );
	}

	public static <T> String getListLine( List<T> list )
	{
		String result = "";
		for ( int i = 0; i < list.size(); i++ )
		{
			T object = list.get( i );
			if ( ( object instanceof Double ) )
			{
				result = result + formatter.format( object );
			}
			else
			{
				result = result + list.get( i );
			}
			if ( i < list.size() - 1 )
			{
				result = result + ",";
			}
		}
		result = result + "\r\n";
		return result;
	}

	public static String readFileToString( String path )
	{
		try
		{
			FileInputStream fileInputStream = new FileInputStream( path );
			Throwable localThrowable2 = null;
			try
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream( 8192 );
				byte[] buffer = new byte[' '];
				int length = 0;
				while ( ( length = fileInputStream.read( buffer ) ) != -1 )
				{
					baos.write( buffer, 0, length );
				}
				byte[] byteArray = baos.toByteArray();

				return new String( byteArray, "UTF-8" );
			}
			catch ( Throwable localThrowable1 )
			{
				localThrowable2 = localThrowable1;
				throw localThrowable1;

			}
			finally
			{

				if ( fileInputStream != null )
					if ( localThrowable2 != null )
						try
						{
							fileInputStream.close();
						}
						catch ( Throwable x2 )
						{
							localThrowable2.addSuppressed( x2 );
						}
					else
					{
						fileInputStream.close();
					}
			}
		}
		catch ( IOException e )
		{
			System.out.println( "File " + path + " could not be read" );
		}

		return null;
	}

	public static boolean writeStringToFile( String path, String text )
	{
		try
		{
			PrintWriter out = new PrintWriter( path );
			Throwable localThrowable2 = null;
			try
			{
				out.println( text );
				return true;
			}
			catch ( Throwable localThrowable3 )
			{
				localThrowable2 = localThrowable3;
				throw localThrowable3;
			}
			finally
			{
				if ( out != null )
					if ( localThrowable2 != null )
						try
						{
							out.close();
						}
						catch ( Throwable x2 )
						{
							localThrowable2.addSuppressed( x2 );
						}
					else
					{
						out.close();
					}
			}
		}
		catch ( IOException e )
		{
			System.out.println( "File " + path + " could not be written" );
		}

		return false;
	}
}

