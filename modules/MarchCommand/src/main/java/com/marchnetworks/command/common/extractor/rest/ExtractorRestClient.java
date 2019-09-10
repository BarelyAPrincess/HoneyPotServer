package com.marchnetworks.command.common.extractor.rest;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.command.api.metrics.MetricsCoreService;
import com.marchnetworks.command.api.rest.DeviceRestClient;
import com.marchnetworks.command.api.rest.DeviceRestException;
import com.marchnetworks.command.api.security.DeviceSessionCoreService;
import com.marchnetworks.command.api.serialization.JsonSerializer;
import com.marchnetworks.command.common.extractor.data.ExtractionJobType;
import com.marchnetworks.command.common.extractor.data.ExtractorOperationResult;
import com.marchnetworks.command.common.extractor.data.Parameter;
import com.marchnetworks.command.common.extractor.data.RecorderJob;
import com.marchnetworks.command.common.extractor.data.RecorderUpdate;
import com.marchnetworks.command.common.extractor.data.SetParameterResult;
import com.marchnetworks.command.common.extractor.data.UpdateRecorderInfoResult;
import com.marchnetworks.command.common.extractor.data.datacollection.GpsPoint;
import com.marchnetworks.command.common.extractor.data.image.ImageInfo;
import com.marchnetworks.command.common.extractor.data.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

public class ExtractorRestClient extends DeviceRestClient
{
	private JsonSerializer serializer;
	private long getTransactionTime = 0L;
	private long deserializeTime = 0L;

	public ExtractorRestClient( String address, String deviceId, DeviceSessionCoreService deviceSessionCoreService, JsonSerializer serializer, MetricsCoreService metricsService )
	{
		super( address, deviceId, deviceSessionCoreService, metricsService );
		this.serializer = serializer;
	}

	public <T extends RecorderJob> ExtractorOperationResult[] updateJobs( ExtractionJobType jobType, List<T> jobs ) throws DeviceRestException
	{
		String data = serializer.toJson( jobs );
		String result = httpRequest( "/rawdevice/Extractor." + jobType.getType() + ".UpdateJobs", "POST", data ).getResponseAsString();
		return ( ExtractorOperationResult[] ) serializer.fromJson( result, ExtractorOperationResult[].class );
	}

	public <T extends RecorderJob> ExtractorOperationResult[] setJobs( ExtractionJobType jobType, List<T> jobs ) throws DeviceRestException
	{
		String data = serializer.toJson( jobs );
		String result = httpRequest( "/rawdevice/Extractor." + jobType.getType() + ".SetJobs", "POST", data ).getResponseAsString();
		return ( ExtractorOperationResult[] ) serializer.fromJson( result, ExtractorOperationResult[].class );
	}

	public ExtractorOperationResult[] deleteJobs( ExtractionJobType jobType, List<String> jobIds ) throws DeviceRestException
	{
		String data = serializer.toJson( jobIds );
		String result = httpRequest( "/rawdevice/Extractor." + jobType.getType() + ".DeleteJobs", "POST", data ).getResponseAsString();

		return ( ExtractorOperationResult[] ) serializer.fromJson( result, ExtractorOperationResult[].class );
	}

	public List<SetParameterResult> setParameter( List<Parameter> parameters ) throws DeviceRestException
	{
		String data = serializer.toJson( parameters );
		String result = httpRequest( "/rawdevice/Extractor.SetParameters", "POST", data ).getResponseAsString();

		return serializer.collectionFromJson( result, new TypeToken<ArrayList<SetParameterResult>>()
		{
		} );
	}

	public List<UpdateRecorderInfoResult> updateRecorderInfo( List<RecorderUpdate> recorderUpdates ) throws DeviceRestException
	{
		String data = serializer.toJson( recorderUpdates );
		String result = httpRequest( "/rawdevice/Extractor.UpdateRecorderInfo", "POST", data ).getResponseAsString();

		return serializer.collectionFromJson( result, new TypeToken<ArrayList<UpdateRecorderInfoResult>>()
		{
		} );
	}

	public List<GpsPoint> getGpsData() throws DeviceRestException
	{
		String result = httpRequest( "/rawdevice/Extractor." + ExtractionJobType.DATA_COLLECTION.getType() + ".GetGpsData", "GET" ).getResponseAsString();
		List<GpsPoint> gpsPoints = serializer.collectionFromJson( result, new TypeToken<ArrayList<GpsPoint>>()
		{
		} );
		return gpsPoints;
	}

	public List<ImageInfo> getImageInfo( String channelId, Long start, Long end, String tags ) throws DeviceRestException
	{
		String path = "/rawdevice/Extractor." + ExtractionJobType.IMAGE.getType() + ".GetImageInfo" + "?channel=" + channelId + "&start=" + start + "&end=" + end + "&tags=" + tags;
		String result = httpRequest( path, "GET" ).getResponseAsString();

		List<ImageInfo> imageInfoList = serializer.collectionFromJson( result, new TypeToken<ArrayList<ImageInfo>>()
		{
		} );
		return imageInfoList;
	}

	public byte[] getImage( String channelId, Long rts, Long earlyMs, Long lateMs, String tags ) throws DeviceRestException
	{
		String path = "/rawdevice/Extractor." + ExtractionJobType.IMAGE.getType() + ".GetImage" + "?channel=" + channelId + "&rts=" + rts + "&tags=" + tags;
		if ( ( earlyMs != null ) && ( lateMs != null ) )
		{
			path = path + "&earlyMs=" + earlyMs + "&lateMs=" + lateMs;
		}
		byte[] result = httpRequest( path, "GET" ).getResponse();
		return result;
	}

	public byte[] getImageExact( String channelId, Long rts, String tags ) throws DeviceRestException
	{
		String path = "/rawdevice/Extractor." + ExtractionJobType.IMAGE.getType() + ".GetImageExact" + "?channel=" + channelId + "&rts=" + rts + "&tag=" + tags;

		byte[] result = httpRequest( path, "GET" ).getResponse();
		return result;
	}

	public List<Transaction> getTransactions() throws DeviceRestException
	{
		long start = System.currentTimeMillis();
		String result = httpRequest( "/rawdevice/Extractor." + ExtractionJobType.TRANSACTION.getType() + ".GetTransactions", "GET" ).getResponseAsString();
		getTransactionTime = ( System.currentTimeMillis() - start );

		start = System.currentTimeMillis();
		List<Transaction> transactions = serializer.collectionFromJson( result, new TypeToken<ArrayList<Transaction>>()
		{
		} );
		deserializeTime = ( System.currentTimeMillis() - start );
		return transactions;
	}

	public long getLastTransactionTime()
	{
		return getTransactionTime;
	}

	public long getLastDeserializeTime()
	{
		return deserializeTime;
	}
}
