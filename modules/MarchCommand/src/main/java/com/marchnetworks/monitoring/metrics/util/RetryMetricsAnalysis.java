package com.marchnetworks.monitoring.metrics.util;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.command.api.metrics.Metric;
import com.marchnetworks.command.api.metrics.MetricSnapshot;
import com.marchnetworks.command.api.metrics.RetryAction;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RetryMetricsAnalysis
{
	public static void main( String[] args )
	{
		String directory = "/tmp/logs";
		String retryMetric = "transaction";

		DecimalFormat formatter = new DecimalFormat( "#0.0" );
		File file = new File( directory );
		String[] names = file.list();

		Map<String, Map<Long, Long>> retriesMap = new HashMap<String, Map<Long, Long>>();

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
						Map<Long, Long> totals = new HashMap();
						totals.put( Long.valueOf( 1L ), Long.valueOf( 0L ) );
						totals.put( Long.valueOf( 2L ), Long.valueOf( 0L ) );
						totals.put( Long.valueOf( 3L ), Long.valueOf( 0L ) );
						totals.put( Long.valueOf( 4L ), Long.valueOf( 0L ) );
						totals.put( Long.valueOf( 5L ), Long.valueOf( 0L ) );

						String json = readFileToString( logFile.getPath() );

						json = "[" + json.substring( 0, json.length() - 2 ) + "]";

						List<MetricSnapshot> result = ( List ) CoreJsonSerializer.collectionFromJson( json, new TypeToken()
						{
						} );

						for ( MetricSnapshot snapshot : result )
						{
							if ( snapshot != null )
							{

								List<Metric> metrics = snapshot.getMetrics();
								for ( Metric metric : metrics )
								{
									if ( metric.getName().equals( retryMetric ) )
									{
										RetryAction retryAction = ( RetryAction ) metric;
										Map<Long, Long> retries = retryAction.getRetries();

										for ( Long key : totals.keySet() )
										{
											Long valueTotal = totals.get( key );
											Long newValue = retries.get( key );
											if ( newValue != null )
												totals.put( key, valueTotal + newValue );
										}
									}
								}
							}
						}

						retriesMap.put( folder.getName(), totals );
					}
				}
			}
		}

		String result = "Date,Name,Retry1,Retry2,Retry3,Retry4,Retry5,Gain1,Gain2,Gain3,Gain4\r\n";

		for ( Entry<String, Map<Long, Long>> entry : retriesMap.entrySet() )
		{
			String key = ( String ) entry.getKey();
			int dateStart = key.indexOf( "_" ) + 1;
			int dateEnd = key.lastIndexOf( "_" );
			String date = key.substring( dateStart, dateEnd );
			String name = key.substring( dateEnd + 1 );

			result = result + date + "," + name + ",";

			Map<Long, Long> retries = ( Map ) entry.getValue();

			for ( Long value : retries.values() )
			{
				result = result + value + ",";
			}
			List<Long> values = new ArrayList( retries.values() );
			Long total = ( Long ) retries.get( Long.valueOf( 1L ) );

			for ( int i = 0; i < values.size(); i++ )
			{
				Long current = ( Long ) values.get( i );
				if ( i < values.size() - 1 )
				{
					Long next = ( Long ) values.get( i + 1 );
					Long diff = Long.valueOf( current.longValue() - next.longValue() );
					double gain = 0.0D;
					if ( total.longValue() > 0L )
					{
						gain = diff.longValue() / total.longValue() * 100.0D;
					}
					result = result + formatter.format( gain );
					if ( i < values.size() - 2 )
					{
						result = result + ",";
					}
				}
			}
			result = result + "\r\n";
		}

		String outputFile = directory + "\\results.csv";
		writeStringToFile( outputFile, result );
		System.out.println( "Results written to " + outputFile );
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

