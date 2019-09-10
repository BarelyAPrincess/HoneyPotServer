package com.marchnetworks.management.topology.service;

import com.marchnetworks.command.api.extractor.BaseExtractionService;
import com.marchnetworks.command.api.extractor.GpsExtractionService;
import com.marchnetworks.command.api.extractor.TransactionExtractionService;
import com.marchnetworks.command.api.extractor.data.ImageDownloadJob;
import com.marchnetworks.command.api.extractor.data.ImageDownloadScheduleJob;
import com.marchnetworks.command.api.extractor.data.ImageDownloadTag;
import com.marchnetworks.command.api.extractor.data.Job;
import com.marchnetworks.command.api.extractor.data.MediaDownloadJob;
import com.marchnetworks.command.api.extractor.data.TransactionJob;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.api.query.Criteria;
import com.marchnetworks.command.api.query.Restrictions;
import com.marchnetworks.command.api.rest.DeviceManagementConstants;
import com.marchnetworks.command.api.rest.DeviceRestException;
import com.marchnetworks.command.api.schedule.ScheduleException;
import com.marchnetworks.command.api.security.DeviceSessionCoreService;
import com.marchnetworks.command.api.security.SamlException;
import com.marchnetworks.command.api.serialization.JsonSerializer;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.DeviceEventsEnum;
import com.marchnetworks.command.common.device.data.ChannelState;
import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.command.common.device.data.DataEncoderView;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.command.common.device.data.TextEncoderView;
import com.marchnetworks.command.common.extractor.data.Channel;
import com.marchnetworks.command.common.extractor.data.ExtractionJobType;
import com.marchnetworks.command.common.extractor.data.ExtractorOperationResult;
import com.marchnetworks.command.common.extractor.data.ExtractorsOperationEnum;
import com.marchnetworks.command.common.extractor.data.Parameter;
import com.marchnetworks.command.common.extractor.data.RecorderAuth;
import com.marchnetworks.command.common.extractor.data.RecorderJob;
import com.marchnetworks.command.common.extractor.data.RecorderUpdate;
import com.marchnetworks.command.common.extractor.data.ResultState;
import com.marchnetworks.command.common.extractor.data.SetParameterResult;
import com.marchnetworks.command.common.extractor.data.State;
import com.marchnetworks.command.common.extractor.data.UpdateRecorderInfoResult;
import com.marchnetworks.command.common.extractor.data.datacollection.DataCollectionJob;
import com.marchnetworks.command.common.extractor.data.datacollection.GpsPoint;
import com.marchnetworks.command.common.extractor.data.image.AdditionalTarget;
import com.marchnetworks.command.common.extractor.data.image.Extension;
import com.marchnetworks.command.common.extractor.data.image.ImageExtractionJob;
import com.marchnetworks.command.common.extractor.data.image.ImageInfo;
import com.marchnetworks.command.common.extractor.data.image.ScheduledImageExtractionJob;
import com.marchnetworks.command.common.extractor.data.media.MediaExtractionJob;
import com.marchnetworks.command.common.extractor.data.transaction.Transaction;
import com.marchnetworks.command.common.extractor.data.transaction.TransactionChannel;
import com.marchnetworks.command.common.extractor.data.transaction.TransactionExtractionJob;
import com.marchnetworks.command.common.extractor.rest.ExtractorRestClient;
import com.marchnetworks.command.common.schedule.data.Schedule;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.ChannelLinkResource;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.common.diagnostics.metrics.MetricsHelper;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.events.DeviceConnectionStateChangeEvent;
import com.marchnetworks.management.instrumentation.events.DeviceIpChangedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRestartEvent;
import com.marchnetworks.management.instrumentation.events.ExtractorJobEvent;
import com.marchnetworks.management.topology.ArchiverAssociationService;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.management.topology.events.ArchiverAssociationRemovedEvent;
import com.marchnetworks.management.topology.events.ArchiverAssociationUpdatedEvent;
import com.marchnetworks.schedule.service.ScheduleService;
import com.marchnetworks.security.saml.SecurityTokenService;
import com.marchnetworks.server.event.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ExtractionServiceImpl implements BaseExtractionService, GpsExtractionService, TransactionExtractionService, InitializationListener, EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( BaseExtractionService.class );

	public ExtractionServiceImpl()
	{
		extractorsParams = new HashSet();

		transactionExtractions = new HashMap();
		scheduledImageDownloadExtractions = new HashMap();
		dataCollectionExtractions = new HashMap();

		mediaExtractions = new HashMap();

		extractorsMap = new HashMap();
	}

	private Set<Parameter> extractorsParams;
	private Map<Long, List<TransactionExtractionJob>> transactionExtractions;
	private Map<Long, List<ScheduledImageExtractionJob>> scheduledImageDownloadExtractions;
	private Map<Long, List<DataCollectionJob>> dataCollectionExtractions;
	private Map<Long, List<MediaExtractionJob>> mediaExtractions;
	private Map<Long, ExtractorInfo> extractorsMap;

	public void onAppInitialized()
	{
		setUpExtractor( ExtractorsOperationEnum.INITIALIZATION, null );
	}

	public String getListenerName()
	{
		return ExtractionServiceImpl.class.getSimpleName();
	}

	public void process( Event event )
	{
		if ( ( event instanceof DeviceRegistrationEvent ) )
		{
			DeviceRegistrationEvent registrationEvent = ( DeviceRegistrationEvent ) event;
			processDeviceRegistration( registrationEvent.getResourceId(), registrationEvent.getRegistrationStatus().name() );
		}
		else if ( ( event instanceof DeviceIpChangedEvent ) )
		{
			DeviceIpChangedEvent ipChangedEvent = ( DeviceIpChangedEvent ) event;
			processDeviceIpChanged( Long.valueOf( ipChangedEvent.getDeviceResourceId() ) );
		}
		else if ( ( event instanceof DeviceRestartEvent ) )
		{
			DeviceRestartEvent restartEvent = ( DeviceRestartEvent ) event;
			processDeviceRestart( restartEvent.getDeviceId() );
		}
		else if ( ( event instanceof DeviceConnectionStateChangeEvent ) )
		{
			DeviceConnectionStateChangeEvent stateChangeEvent = ( DeviceConnectionStateChangeEvent ) event;
			processDeviceConnectStateChange( stateChangeEvent.getDeviceId(), stateChangeEvent.getConnectState() );
		}
		else if ( ( event instanceof ArchiverAssociationUpdatedEvent ) )
		{
			ArchiverAssociationUpdatedEvent associationUpdatedEvent = ( ArchiverAssociationUpdatedEvent ) event;
			processAssociationUpdated( associationUpdatedEvent.getArchiverResourceId(), associationUpdatedEvent.getDeviceResourceIds() );
		}
		else if ( ( event instanceof ArchiverAssociationRemovedEvent ) )
		{
			ArchiverAssociationRemovedEvent associationRemovedEvent = ( ArchiverAssociationRemovedEvent ) event;
			processAssociationRemoved( associationRemovedEvent.getArchiverResourceId(), associationRemovedEvent.getDeviceResourceIds() );
		}
		else if ( ( event instanceof ExtractorJobEvent ) )
		{
			ExtractorJobEvent jobEvent = ( ExtractorJobEvent ) event;
			processJobStateUpdate( jobEvent );
		}
	}

	public Long getExtractorIdForDevice( Long deviceResourceId )
	{
		if ( hasSingleExtractor() )
		{
			return ( Long ) CollectionUtils.getNextFromSet( extractorsMap.keySet() );
		}

		return archiverAssociationService.getPrimaryArchiverByDeviceresourceId( deviceResourceId );
	}

	public boolean extractorExists( Long extractorId )
	{
		return extractorsMap.containsKey( extractorId );
	}

	public boolean extractorExists()
	{
		return !extractorsMap.isEmpty();
	}

	public void updateTransactionJobs( List<TransactionJob> transactionJobs )
	{
		if ( transactionJobs.isEmpty() )
		{
			return;
		}
		Map<Long, List<TransactionJob>> transactionJobsMap = groupJobsByExtractor( transactionJobs );
		synchronized ( transactionExtractions )
		{
			for ( Entry<Long, List<TransactionJob>> entry : transactionJobsMap.entrySet() )
			{
				Long extractorId = ( Long ) entry.getKey();
				List<TransactionJob> jobList = ( List ) entry.getValue();
				boolean firstTimeAddingJobs = ( ( List ) transactionExtractions.get( extractorId ) ).isEmpty();

				List<TransactionExtractionJob> extractionJobs = new ArrayList( jobList.size() );
				for ( TransactionJob transactionJob : jobList )
				{
					TransactionExtractionJob extractionJob = createTransactionExtractionJob( transactionJob );
					if ( extractionJob != null )
					{
						extractionJobs.add( extractionJob );
					}
				}
				if ( firstTimeAddingJobs )
				{
					sendSetExtractionJobs( extractorId, ExtractionJobType.TRANSACTION, extractionJobs );
				}
				else
					sendUpdateExtractionJobs( extractorId, ExtractionJobType.TRANSACTION, extractionJobs );
			}
		}
	}

	public void removeTransactionJob( TransactionJob job )
	{
		Iterator<TransactionExtractionJob> iterator;
		TransactionExtractionJob transactionJob;
		Iterator<TransactionChannel> it;
		synchronized ( transactionExtractions )
		{
			Long extractorId = getExtractorIdForDevice( job.getDeviceResourceId() );
			List<TransactionExtractionJob> jobsList = ( List ) transactionExtractions.get( extractorId );
			for ( iterator = jobsList.iterator(); iterator.hasNext(); )
			{
				transactionJob = ( TransactionExtractionJob ) iterator.next();
				if ( transactionJob.getSiteId().equals( job.getSiteId() ) )
				{
					for ( it = transactionJob.getChannels().iterator(); it.hasNext(); )
					{
						TransactionChannel channel = ( TransactionChannel ) it.next();
						if ( channel.getTerminalId().equals( job.getTerminalId() ) )
						{
							LOG.info( "Removed channel for local job, id:" + transactionJob.getId() + ", channel id:" + channel.getId() );
							it.remove();
							if ( transactionJob.getChannels().isEmpty() )
							{
								LOG.info( "Removed local job, id:" + transactionJob.getId() );
								iterator.remove();
								sendDeleteExtractionJobs( transactionJob.getExtractorId(), ExtractionJobType.TRANSACTION, Collections.singletonList( transactionJob.getId() ) );
								transactionJob.getRefreshTimer().cancel( true );
							}
							else
							{
								sendUpdateExtractionJobs( transactionJob.getExtractorId(), ExtractionJobType.TRANSACTION, Collections.singletonList( transactionJob ) );
							}
							return;
						}
					}
				}
			}
		}
	}

	public void updateImageDownloadScheduleJobs( List<ImageDownloadScheduleJob> imageDownloadScheduleJobs )
	{
		if ( imageDownloadScheduleJobs.isEmpty() )
		{
			return;
		}
		List<ScheduledImageExtractionJob> extractionJobs = new ArrayList();
		synchronized ( scheduledImageDownloadExtractions )
		{
			for ( ImageDownloadScheduleJob imageScheduleJob : imageDownloadScheduleJobs )
			{
				extractionJobs.addAll( createScheduledImageExtractionJobs( imageScheduleJob ) );
			}
		}
		Map<Long, List<ScheduledImageExtractionJob>> extractionJobsMap = new HashMap<>( extractorsMap.size() );

		for ( ScheduledImageExtractionJob extractionJob : extractionJobs )
		{
			List<ScheduledImageExtractionJob> jobList = extractionJobsMap.get( extractionJob.getExtractorId() );

			if ( jobList == null )
			{
				jobList = new ArrayList<>();
				extractionJobsMap.put( extractionJob.getExtractorId(), jobList );
			}

			jobList.add( extractionJob );
		}

		for ( Entry<Long, List<ScheduledImageExtractionJob>> entry : extractionJobsMap.entrySet() )
			sendUpdateExtractionJobs( entry.getKey(), ExtractionJobType.IMAGE, entry.getValue() );
	}

	public void removeImageDownloadScheduleJob( Long siteId )
	{
		Map<Long, List<String>> jobsToDelete = new HashMap( 1 );
		String siteIdString = siteId.toString() + "_";
		Iterator<ScheduledImageExtractionJob> iterator;
		synchronized ( scheduledImageDownloadExtractions )
		{
			for ( List<ScheduledImageExtractionJob> jobsList : scheduledImageDownloadExtractions.values() )
			{
				for ( iterator = jobsList.iterator(); iterator.hasNext(); )
				{
					ScheduledImageExtractionJob imageExtractionJob = ( ScheduledImageExtractionJob ) iterator.next();
					if ( imageExtractionJob.getId().startsWith( siteIdString ) )
					{
						iterator.remove();
						imageExtractionJob.getRefreshTimer().cancel( true );
						List<String> ids = ( List ) jobsToDelete.get( imageExtractionJob.getExtractorId() );
						if ( ids == null )
						{
							ids = new ArrayList( 1 );
							jobsToDelete.put( imageExtractionJob.getExtractorId(), ids );
						}
						ids.add( imageExtractionJob.getId() );
					}
				}
			}
		}

		for ( Entry<Long, List<String>> entry : jobsToDelete.entrySet() )
		{
			sendDeleteExtractionJobs( ( Long ) entry.getKey(), ExtractionJobType.IMAGE, ( List ) entry.getValue() );
		}
	}

	public List<String> updateImageDownloadJobs( List<ImageDownloadJob> imageDownloadJobs )
	{
		List<String> result = new ArrayList<>( imageDownloadJobs.size() );
		Map<Long, List<ImageExtractionJob>> extractorJobsMap = new HashMap<>( 1 );
		Map<Long, ImageExtractionJob> extractionJobsPerDevice = new HashMap<>();
		ChannelLinkResource channelLinkResource = null;

		for ( ImageDownloadJob imageDownloadJob : imageDownloadJobs )
		{
			try
			{
				channelLinkResource = ( ChannelLinkResource ) topologyService.getResource( imageDownloadJob.getChannelResourceId() );
			}
			catch ( TopologyException e )
			{
				LOG.warn( "Channel {} could not be found when creating ImageExtractionJob.", imageDownloadJob.getChannelResourceId() );
			}

			if ( channelLinkResource == null )
				continue;

			ChannelResource channelResource = topologyService.getChannelResource( channelLinkResource.getDeviceResourceId(), channelLinkResource.getChannelId() );
			if ( ( channelResource == null ) || ( channelResource.getChannelView().getChannelState().equals( ChannelState.DISABLED ) ) )
			{
				LOG.info( "Channel {} from device resource {} is disabled. Not creating ImageExtractionJob. ", channelLinkResource.getChannelId(), channelLinkResource.getDeviceResourceId() );
			}
			else
			{
				DeviceResource deviceResource = getDeviceResource( channelLinkResource.getDeviceResourceId() );
				if ( deviceResource == null )
				{
					LOG.warn( "Device {} could not be found when creating ImageExtractionJob.", channelLinkResource.getDeviceResourceId().toString() );
				}
				else
				{
					Long extractorId = imageDownloadJob.getExtractorId() == null ? getExtractorIdForDevice( deviceResource.getId() ) : imageDownloadJob.getExtractorId();
					if ( extractorId == null )
					{
						LOG.warn( "No Archiver set to device {}. Won't proceed creating ImageExtractionJob.", channelLinkResource.getDeviceResourceId().toString() );
					}
					else
					{
						ImageExtractionJob extractionJob = ( ImageExtractionJob ) extractionJobsPerDevice.get( deviceResource.getId() );
						StringBuilder sb = new StringBuilder();
						if ( extractionJob == null )
						{
							extractionJob = new ImageExtractionJob( imageDownloadJob.isManualDownload(), imageDownloadJob.getTag().getTagValue() );

							sb.append( imageDownloadJob.getTag().getTagValue() ).append( "_" ).append( deviceResource.getIdAsString() ).append( "_" ).append( System.currentTimeMillis() );
							extractionJob.setId( sb.toString() );

							extractionJob.setTimeoutPeriod( imageDownloadJob.getJobTimeout() == null ? 172800000L : imageDownloadJob.getJobTimeout().intValue() );
							fillInRecorderDetails( extractionJob, extractorId, deviceResource );

							result.add( extractionJob.getId() );
							extractionJobsPerDevice.put( deviceResource.getId(), extractionJob );

							List<ImageExtractionJob> extractorJobList = ( List ) extractorJobsMap.get( extractorId );
							if ( extractorJobList == null )
							{
								extractorJobList = new ArrayList();
								extractorJobsMap.put( extractorId, extractorJobList );
							}
							extractorJobList.add( extractionJob );
						}

						extractionJob.addRequest( channelLinkResource.getChannelId(), imageDownloadJob.getTimestamps() );
					}
				}
			}
		}
		for ( Entry<Long, List<ImageExtractionJob>> entry : extractorJobsMap.entrySet() )
		{
			sendUpdateExtractionJobs( ( Long ) entry.getKey(), ExtractionJobType.IMAGE, ( List ) entry.getValue() );
		}
		return result;
	}

	public void updateImageDownloadJobsAsync( final List<ImageDownloadJob> imageDownloadJobs )
	{
		taskScheduler.executeNow( new Runnable()
		{
			public void run()
			{
				updateImageDownloadJobs( imageDownloadJobs );
			}
		} );
	}

	public byte[] getImage( Long deviceResourceId, String channelId, Long rts, Long earlyMs, Long lateMs, String tag )
	{
		Long extractorId = getExtractorIdForDevice( deviceResourceId );
		if ( extractorId == null )
		{
			LOG.warn( "No Archiver set to device {}. Won't proceed to get image.", deviceResourceId );
			return new byte[0];
		}
		try
		{
			ExtractorInfo extractorInfo = ( ExtractorInfo ) extractorsMap.get( extractorId );
			return extractorInfo.getExtractorRestClient().getImage( deviceResourceId + "_" + channelId, rts, earlyMs, lateMs, tag );
		}
		catch ( DeviceRestException e )
		{
			LOG.warn( "Error when trying to get image from Archiver {}. Details: {}", extractorId, e.getMessage() );
		}
		return new byte[0];
	}

	public byte[] getImageExact( Long deviceResourceId, String channelId, Long rts, String tag )
	{
		Long extractorId = getExtractorIdForDevice( deviceResourceId );
		if ( extractorId == null )
		{
			LOG.warn( "No Archiver set to device {}. Won't proceed to get image by timestamp.", deviceResourceId );
			return new byte[0];
		}
		try
		{
			ExtractorInfo extractorInfo = ( ExtractorInfo ) extractorsMap.get( extractorId );
			return extractorInfo.getExtractorRestClient().getImageExact( deviceResourceId + "_" + channelId, rts, tag );
		}
		catch ( DeviceRestException e )
		{
			LOG.warn( "Error when trying to get image by timestamp from Archiver {}. Details: {}", extractorId, e.getMessage() );
		}
		return new byte[0];
	}

	public List<ImageInfo> getImageInfo( Long deviceResourceId, String channelId, Long start, Long end, String tag )
	{
		Long extractorId = getExtractorIdForDevice( deviceResourceId );
		if ( extractorId == null )
		{
			LOG.warn( "No Archiver set to device {}. Won't proceed to get image.", deviceResourceId );
			return Collections.emptyList();
		}
		try
		{
			ExtractorInfo extractorInfo = ( ExtractorInfo ) extractorsMap.get( extractorId );
			return extractorInfo.getExtractorRestClient().getImageInfo( deviceResourceId + "_" + channelId, start, end, tag );
		}
		catch ( DeviceRestException e )
		{
			LOG.warn( "Error when trying to get image details from Archiver {}. Details: {}", extractorId, e.getMessage() );
		}
		return Collections.emptyList();
	}

	public void updateMediaJobs( List<MediaDownloadJob> mediaJobs )
	{
		if ( mediaJobs.isEmpty() )
		{
			return;
		}

		Map<Long, List<MediaDownloadJob>> mediaJobsMap = groupJobsByExtractor( mediaJobs );

		synchronized ( mediaExtractions )
		{
			for ( Entry<Long, List<MediaDownloadJob>> entry : mediaJobsMap.entrySet() )
			{
				Long extractorId = ( Long ) entry.getKey();
				List<MediaDownloadJob> jobList = ( List ) entry.getValue();
				boolean firstTimeAddingJobs = ( ( List ) mediaExtractions.get( extractorId ) ).isEmpty();

				List<MediaExtractionJob> mediaExtractionJobs = new ArrayList( jobList.size() );
				for ( MediaDownloadJob mediaJob : jobList )
				{
					MediaExtractionJob extractionJob = createMediaExtractionJob( mediaJob );
					if ( extractionJob != null )
					{
						mediaExtractionJobs.add( extractionJob );
					}
				}
				if ( firstTimeAddingJobs )
				{
					sendSetExtractionJobs( extractorId, ExtractionJobType.MEDIA, mediaExtractionJobs );
				}
				else
				{
					sendUpdateExtractionJobs( extractorId, ExtractionJobType.MEDIA, mediaExtractionJobs );
				}
			}
		}
	}

	public void updateMediaJobsAsync( final List<MediaDownloadJob> mediaJobs )
	{
		if ( mediaJobs.isEmpty() )
		{
			return;
		}
		taskScheduler.executeNow( new Runnable()
		{
			public void run()
			{
				updateMediaJobs( mediaJobs );
			}
		} );
	}

	public Map<Long, Boolean> removeMediaJobs( List<MediaDownloadJob> jobs )
	{
		Map<Long, List<String>> mediaJobsMap = new HashMap( extractorsMap.size() );
		for ( MediaDownloadJob job : jobs )
		{
			Long key = job.getExtractorId();
			String value = job.getJobId();
			if ( extractorExists( key ) )
			{

				List<String> list = ( List ) mediaJobsMap.get( key );
				if ( list == null )
				{
					list = new ArrayList();
					mediaJobsMap.put( key, list );
				}
				list.add( value );
			}
		}
		Map<Long, Boolean> results = new HashMap();
		for ( Entry<Long, List<String>> entry : mediaJobsMap.entrySet() )
		{
			Long extractorId = ( Long ) entry.getKey();
			List<String> jobIds = ( List ) entry.getValue();
			boolean result = sendDeleteExtractionJobs( extractorId, ExtractionJobType.MEDIA, jobIds );
			if ( result )
			{
				Iterator<MediaExtractionJob> iterator;
				synchronized ( mediaExtractions )
				{
					for ( iterator = ( ( List ) mediaExtractions.get( extractorId ) ).iterator(); iterator.hasNext(); )
					{
						MediaExtractionJob job = ( MediaExtractionJob ) iterator.next();
						if ( jobIds.contains( job.getId() ) )
						{
							iterator.remove();
						}
					}
				}
			}
			results.put( entry.getKey(), Boolean.valueOf( result ) );
		}
		return results;
	}

	public void removeMediaJobsAsync( final List<MediaDownloadJob> jobs )
	{
		taskScheduler.executeNow( new Runnable()
		{
			public void run()
			{
				removeMediaJobs( jobs );
			}
		} );
	}

	public void updateDataCollectionJobs( List<Long> deviceResourceIds )
	{
		Map<Long, List<Long>> devicesByExtractor = groupDevicesByExtractor( deviceResourceIds );
		for ( Entry<Long, List<Long>> entry : devicesByExtractor.entrySet() )
		{
			processDataCollectionJobs( ( Long ) entry.getKey(), ( List ) entry.getValue() );
		}
	}

	public void removeDataCollectionJob( Long deviceResourceId )
	{
		if ( !hasSingleExtractor() )
		{
			return;
		}

		String deletedJobId = null;
		Long extractorId = null;
		Iterator<DataCollectionJob> iterator;
		synchronized ( dataCollectionExtractions )
		{
			for ( Entry<Long, List<DataCollectionJob>> entry : dataCollectionExtractions.entrySet() )
			{
				for ( iterator = ( ( List ) entry.getValue() ).iterator(); iterator.hasNext(); )
				{
					DataCollectionJob dataCollectionJob = ( DataCollectionJob ) iterator.next();
					if ( deviceResourceId.equals( Long.valueOf( dataCollectionJob.getRecorderId() ) ) )
					{
						iterator.remove();
						dataCollectionJob.getRefreshTimer().cancel( true );

						extractorId = dataCollectionJob.getExtractorId();
						deletedJobId = dataCollectionJob.getId();
						break;
					}
				}
			}
		}
		if ( deletedJobId != null )
		{
			sendDeleteExtractionJobs( extractorId, ExtractionJobType.DATA_COLLECTION, Collections.singletonList( deletedJobId ) );
		}
	}

	public List<GpsPoint> getGpsPoints( Long extractorId )
	{
		try
		{
			ExtractorInfo extractorInfo = ( ExtractorInfo ) extractorsMap.get( extractorId );
			return extractorInfo.getExtractorRestClient().getGpsData();
		}
		catch ( DeviceRestException e )
		{
			LOG.warn( "Error when trying to get gps points from Archiver {}, reason:{}", extractorId, e.getMessage() );
		}
		return Collections.emptyList();
	}

	public List<Transaction> getTransactions( Long extractorId )
	{
		try
		{
			ExtractorInfo extractorInfo = ( ExtractorInfo ) extractorsMap.get( extractorId );
			return extractorInfo.getExtractorRestClient().getTransactions();
		}
		catch ( DeviceRestException e )
		{
			LOG.warn( "Error when trying to get transactions from Archiver: {}, reason:{} ", extractorId, e.getMessage() );
		}
		return Collections.emptyList();
	}

	public long getLastTransactionTime( Long extractorId )
	{
		ExtractorInfo extractorInfo = ( ExtractorInfo ) extractorsMap.get( extractorId );
		if ( extractorInfo != null )
		{
			return extractorInfo.getExtractorRestClient().getLastTransactionTime();
		}
		return 0L;
	}

	public long getLastDeserializeTime( Long extractorId )
	{
		ExtractorInfo extractorInfo = ( ExtractorInfo ) extractorsMap.get( extractorId );
		if ( extractorInfo != null )
		{
			return extractorInfo.getExtractorRestClient().getLastDeserializeTime();
		}
		return 0L;
	}

	public void setParameter( Parameter parameter )
	{
		extractorsParams.remove( parameter );
		extractorsParams.add( parameter );
	}

	public void sendParameterAsync( Parameter parameter )
	{
		setParameter( parameter );
		for ( final Long extractorId : extractorsMap.keySet() )
		{
			taskScheduler.executeNow( new Runnable()
			{
				public void run()
				{
					ExtractionServiceImpl.this.setParameters( extractorId );
				}
			} );
		}
	}

	protected boolean hasSingleExtractor()
	{
		return extractorsMap.size() == 1;
	}

	private void setParameters( Long extractorId )
	{
		if ( extractorsParams.isEmpty() )
		{
			return;
		}
		try
		{
			ExtractorInfo extractorInfo = ( ExtractorInfo ) extractorsMap.get( extractorId );
			List<SetParameterResult> results = extractorInfo.getExtractorRestClient().setParameter( new ArrayList( extractorsParams ) );
			for ( SetParameterResult result : results )
			{
				if ( !result.isSuccess() )
				{
					LOG.info( "Error setting " + result.getPath() + " parameter on Archiver " + extractorId + ", reason:" + result.getFailReason() );
				}
			}
		}
		catch ( DeviceRestException e )
		{
			LOG.info( "Unable to set parameters on Archiver {}, reason: {}", extractorId, e.getMessage() );
		}
	}

	private void updateRecorderState( DeviceResource deviceResource, ConnectState connectState )
	{
		Long extractorId = getExtractorIdForDevice( deviceResource.getId() );
		if ( extractorId == null )
		{
			LOG.warn( "No Archiver found for device {} when updating recorder state.", deviceResource.getDeviceId() );
			return;
		}
		RecorderUpdate recorderUpdate = new RecorderUpdate( deviceResource.getIdAsString(), connectState.toString() );
		String deviceUrl = getDeviceAddress( deviceResource );
		recorderUpdate.setUrl( deviceUrl );
		LOG.info( "Sending recorder state " + connectState.name() + " for device " + deviceResource.getDeviceId() + " to Archiver " + extractorId );
		sendRecorderStateUpdate( extractorId, Collections.singletonList( recorderUpdate ) );
	}

	private void updateRecorderTokens( Long extractorId, List<Long> deviceResourceIds )
	{
		List<RecorderUpdate> recorderUpdateList = new ArrayList( deviceResourceIds.size() );
		for ( Long id : deviceResourceIds )
		{
			DeviceResource deviceResource = getDeviceResource( id );
			if ( ( deviceResource != null ) && ( !extractorExists( id ) ) )
			{

				String deviceUrl = getDeviceAddress( deviceResource );
				String securityToken = getSecurityToken( deviceResource.getDeviceId() );
				RecorderAuth recorderAuth = buildRecorderAuth( securityToken );
				String state = deviceResource.getDeviceView().getConnectState().toString();
				RecorderUpdate recorderUpdate = new RecorderUpdate( deviceResource.getIdAsString(), deviceUrl, recorderAuth, state );
				recorderUpdateList.add( recorderUpdate );
			}
		}
		sendRecorderStateUpdate( extractorId, recorderUpdateList );
	}

	private void updateRecorderTokensByExtractor( final Long extractorId )
	{
		List<Long> deviceResourceIds = new ArrayList();
		if ( hasSingleExtractor() )
		{
			deviceResourceIds = topologyService.findAllRootDeviceResourcesIds();
		}
		else
		{
			deviceResourceIds.addAll( archiverAssociationService.getAssociatedDeviceResourceIdsByArchiverId( extractorId ) );
		}
		updateRecorderTokens( extractorId, deviceResourceIds );

		ExtractorInfo extractorInfo = ( ExtractorInfo ) extractorsMap.get( extractorId );
		Future<?> timer = extractorInfo.getTokenRefreshTimer();
		if ( timer != null )
		{
			timer.cancel( true );
		}

		timer = taskScheduler.schedule( new Runnable()
		{

			public void run()
			{
				ExtractionServiceImpl.this.updateRecorderTokensByExtractor( extractorId );
			}
		}, 7L, TimeUnit.DAYS );

		extractorInfo.setTokenRefreshTimer( timer );
	}

	private void processDeviceRegistration( Long deviceResourceId, String registrationStatus )
	{
		if ( registrationStatus.equals( RegistrationStatus.REGISTERED.name() ) )
		{
			DeviceResource deviceResource = getDeviceResource( deviceResourceId );
			if ( ( deviceResource != null ) && ( "1001".equals( deviceResource.getDeviceView().getFamily() ) ) )
			{
				setUpExtractor( ExtractorsOperationEnum.CREATE, deviceResourceId );
			}
			else
			{
				Long extractorId = getExtractorIdForDevice( deviceResourceId );
				if ( extractorId != null )
				{
					updateRecorderTokens( extractorId, Collections.singletonList( deviceResourceId ) );
				}
			}
		}
		else if ( ( registrationStatus.equals( RegistrationStatus.UNREGISTERED.name() ) ) && ( extractorExists( deviceResourceId ) ) )
		{
			setUpExtractor( ExtractorsOperationEnum.DELETE, deviceResourceId );
		}
	}

	private void processDeviceIpChanged( Long deviceResourceId )
	{
		DeviceResource deviceResource = topologyService.getDeviceResource( deviceResourceId );
		if ( extractorExists( deviceResourceId ) )
		{
			ExtractorRestClient restClient = ( ( ExtractorInfo ) extractorsMap.get( deviceResourceId ) ).getExtractorRestClient();
			restClient.setAddress( deviceResource.getDeviceView().getRegistrationAddress() );
			LOG.info( "Archiver {} registration address updated with {}", deviceResourceId, deviceResource.getDeviceView().getRegistrationAddress() );
		}
		else
		{
			Long extractorId = getExtractorIdForDevice( deviceResourceId );
			if ( extractorId != null )
			{
				updateRecorderState( deviceResource, deviceResource.getDeviceView().getConnectState() );
			}
		}
	}

	private void processDeviceRestart( String deviceId )
	{
		DeviceResource deviceResource = topologyService.getDeviceResourceByDeviceId( deviceId );
		if ( ( deviceResource != null ) && ( extractorExists( deviceResource.getId() ) ) )
		{
			resync( deviceResource.getId() );
		}
	}

	private void processDeviceConnectStateChange( String deviceId, ConnectState connectState )
	{
		DeviceResource deviceResource = topologyService.getDeviceResourceByDeviceId( deviceId );
		if ( ( deviceResource != null ) && ( extractorExists( deviceResource.getId() ) ) && ( connectState == ConnectState.ONLINE ) )
		{
			resync( deviceResource.getId() );
		}
		else
		{
			updateRecorderState( deviceResource, connectState );
		}
	}

	private void setUpExtractor( ExtractorsOperationEnum operation, Long id )
	{
		if ( operation == ExtractorsOperationEnum.INITIALIZATION )
		{
			List<Resource> resources = topologyService.getArchiverResources();
			if ( !resources.isEmpty() )
			{
				for ( Resource res : resources )
				{
					DeviceResource deviceRes = ( DeviceResource ) res;
					LOG.info( "Archiver initialized, deviceResource id:{}", deviceRes.getId() );
					extractorAdded( deviceRes );
				}
			}
		}
		else if ( operation == ExtractorsOperationEnum.DELETE )
		{
			ExtractorInfo extractorInfo = ( ExtractorInfo ) extractorsMap.remove( id );
			if ( extractorInfo != null )
			{
				Future<?> timer = extractorInfo.getTokenRefreshTimer();
				if ( timer != null )
				{
					timer.cancel( true );
				}

				transactionExtractions.remove( id );
				dataCollectionExtractions.remove( id );
				scheduledImageDownloadExtractions.remove( id );
				mediaExtractions.remove( id );
				LOG.info( "Archiver removed, deviceResource id:{}", id );
			}
		}
		else if ( operation == ExtractorsOperationEnum.CREATE )
		{
			DeviceResource deviceRes = getDeviceResource( id );
			extractorAdded( deviceRes );
			updateRecorderTokensByExtractor( id );
			LOG.info( "Archiver initialized, deviceResource id:{}", deviceRes.getId() );
		}
	}

	private void sendRecorderStateUpdate( Long extractorId, List<RecorderUpdate> recorderUpdates )
	{
		try
		{
			List<UpdateRecorderInfoResult> results = null;
			ExtractorRestClient restClient = ( ( ExtractorInfo ) extractorsMap.get( extractorId ) ).getExtractorRestClient();
			results = restClient.updateRecorderInfo( recorderUpdates );
			for ( UpdateRecorderInfoResult result : results )
			{
				if ( result.getFailReason() != null )
				{
					LOG.error( "Error when sending recorder state update to Archiver {}, reason:{}", extractorId, result.getFailReason() );
				}
			}
		}
		catch ( DeviceRestException e )
		{
			LOG.error( "Unable to send recorder state update to Archiver {}, Exception: {}", extractorId, e.getMessage() );
		}
	}

	private <E extends RecorderJob> void sendSetExtractionJobs( Long extractorId, ExtractionJobType jobType, List<E> jobs )
	{
		try
		{
			ExtractorRestClient extractorRestClient = ( ( ExtractorInfo ) extractorsMap.get( extractorId ) ).getExtractorRestClient();
			ExtractorOperationResult[] results = extractorRestClient.setJobs( jobType, jobs );
			for ( ExtractorOperationResult result : results )
			{
				if ( !ResultState.SUCCESS.getValue().equals( result.getResultState() ) )
				{
					LOG.info( "Error setting jobs on extractor " + extractorId + " result:" + result + ", job type: " + jobType );
				}
			}
		}
		catch ( DeviceRestException e )
		{
			LOG.info( "Unable to set jobs on extractor " + extractorId + " with job type:" + jobType + ", Exception: " + e.getMessage() );
		}
	}

	private <E extends RecorderJob> void sendUpdateExtractionJobs( Long extractorId, ExtractionJobType jobType, List<E> jobs )
	{
		try
		{
			ExtractorRestClient extractorRestClient = ( ( ExtractorInfo ) extractorsMap.get( extractorId ) ).getExtractorRestClient();
			ExtractorOperationResult[] results = extractorRestClient.updateJobs( jobType, jobs );
			for ( ExtractorOperationResult result : results )
			{
				if ( !ResultState.SUCCESS.getValue().equals( result.getResultState() ) )
				{
					LOG.info( "Error updating jobs on extractor " + extractorId + " result:" + result + ", job type: " + jobType );
				}
			}
		}
		catch ( DeviceRestException e )
		{
			LOG.info( "Unable to udpate jobs on extractor " + extractorId + " with job type:" + jobType + ", Exception: " + e.getMessage() );
		}
	}

	private boolean sendDeleteExtractionJobs( Long extractorId, ExtractionJobType jobType, List<String> jobIds )
	{
		try
		{
			ExtractorRestClient extractorRestClient = ( ( ExtractorInfo ) extractorsMap.get( extractorId ) ).getExtractorRestClient();
			ExtractorOperationResult[] results = extractorRestClient.deleteJobs( jobType, jobIds );
			for ( ExtractorOperationResult result : results )
			{
				if ( !ResultState.SUCCESS.getValue().equals( result.getResultState() ) )
				{
					LOG.info( "Error deleting extractor job: {}", result );
				}
			}
			return true;
		}
		catch ( DeviceRestException e )
		{
			LOG.info( "Unable to delete extractor job, Exception: {}", e.getMessage() );
		}
		return false;
	}

	private DeviceResource getDeviceResource( Long deviceResourceId )
	{
		DeviceResource deviceResource = topologyService.getDeviceResource( deviceResourceId );
		if ( deviceResource == null )
		{
			LOG.debug( "Unable to find DeviceResource id: " + deviceResourceId );
		}
		return deviceResource;
	}

	private ChannelResource getChannelResource( Long channelResourceId )
	{
		try
		{
			return ( ChannelResource ) topologyService.getResource( channelResourceId );
		}
		catch ( TopologyException e )
		{
			LOG.warn( "Error getting Terminal text channel, id: {}. Could not proceed removing transaction job.", channelResourceId );
		}
		return null;
	}

	private String getDeviceAddress( DeviceResource deviceResource )
	{
		DeviceView device = deviceResource.getDeviceView();
		String address = device.getRegistrationAddress();

		address = "https://" + address;
		if ( !address.endsWith( "/" ) )
		{
			address = address + "/";
		}
		return address;
	}

	private RecorderAuth buildRecorderAuth( String token )
	{
		RecorderAuth recorderAuth = new RecorderAuth();
		recorderAuth.setAuthType( "token" );
		String encodedToken = CommonAppUtils.stringToBase64( token );
		recorderAuth.setAuthData( encodedToken );
		return recorderAuth;
	}

	private String getSecurityToken( String deviceId )
	{
		String token = null;
		try
		{
			token = securityTokenService.getServerSecurityToken( deviceId, 20170 );
		}
		catch ( SamlException e )
		{
			LOG.error( "Unable to obtain security token, Exception: " + e.getMessage(), e );
		}
		return token;
	}

	private void refreshJob( RecorderJob job, ExtractionJobType jobType )
	{
		DeviceResource deviceResource = getDeviceResource( Long.valueOf( job.getRecorderId() ) );
		if ( deviceResource != null )
		{
			LOG.info( "Refreshing job, id:{} type:{}", job.getId(), jobType.toString() );

			job.setRestart( true );
			sendUpdateExtractionJobs( job.getExtractorId(), jobType, Collections.singletonList( job ) );
			job.setRestart( false );

			job.setRefreshTimer( setJobRefreshTimer( job, jobType ) );
		}
	}

	private Future<?> setJobRefreshTimer( final RecorderJob job, final ExtractionJobType jobType )
	{
		Future<?> result = taskScheduler.schedule( new Runnable()
		{

			public void run()
			{
				ExtractionServiceImpl.this.refreshJob( job, jobType );
			}
		}, 900L, TimeUnit.MINUTES );

		return result;
	}

	private <T extends Job> Map<Long, List<T>> groupJobsByExtractor( List<T> jobs )
	{
		if ( jobs == null )
		{
			return Collections.emptyMap();
		}
		Map<Long, List<T>> jobsByExtractorMap = new HashMap( extractorsMap.size() );

		if ( hasSingleExtractor() )
		{
			Long extractorId = ( Long ) CollectionUtils.getNextFromSet( extractorsMap.keySet() );
			List<T> jobsList = new ArrayList( jobs.size() );
			for ( T job : jobs )
			{
				Long jobExtractorId = job.getExtractorId();
				if ( ( jobExtractorId != null ) && ( !extractorExists( jobExtractorId ) ) )
				{
					LOG.debug( "Job {} is set to archiver {} which no longer exists. Skipping...", job.getJobId(), jobExtractorId );
				}
				else
				{
					job.setExtractorId( extractorId );
					jobsList.add( job );
				}
			}
			if ( !jobsList.isEmpty() )
			{
				jobsByExtractorMap.put( extractorId, jobsList );
			}
			return jobsByExtractorMap;
		}

		for ( T job : jobs )
		{
			if ( ( job.getExtractorId() != null ) && ( !extractorExists( job.getExtractorId() ) ) )
			{
				LOG.debug( "Job {} is set to archiver {} which no longer exists. Skipping...", job.getJobId(), job.getExtractorId() );

			}
			else if ( ( job instanceof TransactionJob ) )
			{
				ChannelResource channelResource = getChannelResource( ( ( TransactionJob ) job ).getTextChannelId() );
				Long extractorId = getExtractorIdForDevice( channelResource.getParentResourceId() );
				if ( extractorId == null )
				{
					LOG.warn( "No Archiver set to device {}. Won't proceed updating transaction job.", channelResource.getParentResourceId() );
				}
				else
				{
					job.setExtractorId( extractorId );
				}
			}
			else
			{
				List<T> jobsList = ( List ) jobsByExtractorMap.get( job.getExtractorId() );
				if ( jobsList == null )
				{
					jobsList = new ArrayList();
					jobsByExtractorMap.put( job.getExtractorId(), jobsList );
				}
				jobsList.add( job );
			}
		}
		return jobsByExtractorMap;
	}

	private Map<Long, List<Long>> groupDevicesByExtractor( List<Long> deviceResourceIds )
	{
		Map<Long, List<Long>> devicesByExtractor = new HashMap( extractorsMap.size() );
		if ( hasSingleExtractor() )
		{
			Long extractorId = ( Long ) CollectionUtils.getNextFromSet( extractorsMap.keySet() );
			devicesByExtractor.put( extractorId, deviceResourceIds );
			return devicesByExtractor;
		}

		Long extractorForDevice = null;
		for ( Long deviceResourceId : deviceResourceIds )
		{
			extractorForDevice = getExtractorIdForDevice( deviceResourceId );
			if ( extractorForDevice != null )
			{
				List<Long> deviceIdList = ( List ) devicesByExtractor.get( extractorForDevice );
				if ( deviceIdList == null )
				{
					deviceIdList = new ArrayList();
					devicesByExtractor.put( extractorForDevice, deviceIdList );
				}
				deviceIdList.add( deviceResourceId );
			}
		}
		return devicesByExtractor;
	}

	private TaskScheduler taskScheduler;
	private ResourceTopologyServiceIF topologyService;
	private ArchiverAssociationService archiverAssociationService;
	private SecurityTokenService securityTokenService;
	private DeviceSessionCoreService deviceSessionCoreService;
	private ScheduleService scheduleService;

	private void processDataCollectionJobs( Long extractorId, List<Long> deviceResourceIds )
	{
		synchronized ( dataCollectionExtractions )
		{
			boolean firstTimeAddingJobs = ( ( List ) dataCollectionExtractions.get( extractorId ) ).isEmpty();
			List<DataCollectionJob> updatedJobs = new ArrayList( deviceResourceIds.size() );
			for ( Long deviceResourceId : deviceResourceIds )
			{
				DataCollectionJob job = createDataCollectionJob( extractorId, deviceResourceId );
				if ( job != null )
				{
					updatedJobs.add( job );
				}
			}
			if ( firstTimeAddingJobs )
			{
				sendSetExtractionJobs( extractorId, ExtractionJobType.DATA_COLLECTION, updatedJobs );
			}
			else
			{
				sendUpdateExtractionJobs( extractorId, ExtractionJobType.DATA_COLLECTION, updatedJobs );
			}
		}
	}

	private void removeDataCollectionJobs( Long extractorId, List<Long> deviceResourceIds )
	{
		List<String> deletedJobs = new ArrayList();
		Iterator<DataCollectionJob> iterator;
		synchronized ( dataCollectionExtractions )
		{
			List<DataCollectionJob> collectionJobs = ( List ) dataCollectionExtractions.get( extractorId );
			for ( iterator = collectionJobs.iterator(); iterator.hasNext(); )
			{
				DataCollectionJob dataCollectionJob = ( DataCollectionJob ) iterator.next();
				if ( deviceResourceIds.contains( Long.valueOf( dataCollectionJob.getRecorderId() ) ) )
				{
					iterator.remove();
					dataCollectionJob.getRefreshTimer().cancel( true );
					deletedJobs.add( dataCollectionJob.getId() );
				}
			}
		}
		sendDeleteExtractionJobs( extractorId, ExtractionJobType.DATA_COLLECTION, deletedJobs );
	}

	private MediaExtractionJob createMediaExtractionJob( MediaDownloadJob job )
	{
		Long extractorId = job.getExtractorId() != null ? job.getExtractorId() : getExtractorIdForDevice( job.getDeviceId() );
		if ( extractorId == null )
		{
			LOG.warn( "No Archiver set to device {}. Won't proceed creating transaction job.", job.getDeviceId() );
			return null;
		}

		MediaExtractionJob extractionJob = null;
		List<MediaExtractionJob> mediaJobsList = ( List ) mediaExtractions.get( extractorId );
		for ( MediaExtractionJob mediaExtractionJob : mediaJobsList )
		{
			if ( mediaExtractionJob.getId().equals( job.getJobId() ) )
			{
				extractionJob = mediaExtractionJob;
				break;
			}
		}

		if ( extractionJob == null )
		{
			extractionJob = new MediaExtractionJob();

			extractionJob.setId( job.getJobId() );
			extractionJob.setReferenceId( job.getReferenceId() );
			extractionJob.setTimeoutPeriod( job.getJobTimeout() == null ? 172800000L : job.getJobTimeout().intValue() );

			DeviceResource deviceResource = getDeviceResource( job.getDeviceId() );
			if ( deviceResource == null )
			{
				LOG.warn( "Device resource {} not found in topology. Will not create/update media job for it. Job id {}.", job.getDeviceId(), job.getJobId() );
				return null;
			}
			fillInRecorderDetails( extractionJob, extractorId, deviceResource );
			mediaJobsList.add( extractionJob );
		}

		for ( Iterator<Channel> iterator = job.getChannels().iterator(); iterator.hasNext(); )
		{
			Channel channel = ( Channel ) iterator.next();
			ChannelResource channelResource = topologyService.getChannelResource( job.getDeviceId(), channel.getId() );
			if ( channelResource == null )
			{
				LOG.warn( "Channel resource not found in topology. Removing channel {} from device {} for Job id {}.", new Object[] {channel.getId(), job.getDeviceId(), job.getJobId()} );
				iterator.remove();
			}
			else
			{
				DataEncoderView[] dataEncoders = channelResource.getChannelView().getData();
				if ( dataEncoders != null )
				{
					for ( DataEncoderView dataEncoder : dataEncoders )
						if ( ( dataEncoder.getCodec().equals( "dp2" ) ) || ( dataEncoder.getCodec().equals( "dp3" ) ) )
						{
							iterator.remove();
							break;
						}
				}
			}
		}
		extractionJob.setChannels( job.getChannels() );
		extractionJob.setStartTime( job.getStartTime().longValue() );
		extractionJob.setEndTime( job.getEndTime().longValue() );
		extractionJob.setPaused( job.getState() == State.PAUSED );
		if ( job.getCompletionState() != null )
		{
			extractionJob.setCompletionState( job.getCompletionState().getValue() );
		}
		extractionJob.setState( job.getState().getValue() );
		if ( job.getRestart() != null )
		{
			extractionJob.setRestart( job.getRestart().booleanValue() );
		}
		else
		{
			extractionJob.setRestart( job.getState() != State.COMPLETE );
		}
		return extractionJob;
	}

	private TransactionExtractionJob createTransactionExtractionJob( TransactionJob job )
	{
		ChannelResource channelResource = getChannelResource( job.getTextChannelId() );
		if ( channelResource == null )
		{
			LOG.warn( "Terminal text channel no longer exists in topology. Device resource id:{} channel id: {} ", job.getDeviceResourceId(), job.getTextChannelId() );
			return null;
		}

		TextEncoderView[] textEncoders = channelResource.getChannelView().getText();
		String protocolName;

		if ( ( textEncoders != null ) && ( textEncoders.length > 0 ) )
		{
			protocolName = textEncoders[0].getProtocolName();
		}
		else
		{
			LOG.warn( "Terminal text channel does not have any text encoders, Device resource id: {} channel id: {}", job.getDeviceResourceId(), job.getTextChannelId() );
			return null;
		}

		DeviceResource deviceResource = getDeviceResource( channelResource.getParentResourceId() );
		job.setJobId( job.getSiteId() + "_" + deviceResource.getIdAsString() );

		TransactionChannel channel = new TransactionChannel();
		channel.setId( channelResource.getChannelId() );
		channel.setTerminalId( job.getTerminalId() );
		channel.setProtocolName( protocolName );
		channel.setSectorId( "" );

		List<TransactionExtractionJob> transactionJobList = ( List ) transactionExtractions.get( job.getExtractorId() );
		TransactionExtractionJob extractionJob = null;
		for ( TransactionExtractionJob transactionJob : transactionJobList )
		{
			if ( transactionJob.getId().equals( job.getJobId() ) )
			{
				extractionJob = transactionJob;
				break;
			}
		}
		if ( extractionJob == null )
		{
			extractionJob = new TransactionExtractionJob();
			extractionJob.setId( job.getJobId() );
			extractionJob.setSiteId( job.getSiteId() );

			extractionJob.setCustomerId( job.getCustomerId() );
			List<TransactionChannel> channelList = new ArrayList( 1 );
			channelList.add( channel );
			extractionJob.setChannels( channelList );
			extractionJob.setTimeoutPeriod( job.getJobTimeout() == null ? 172800000L : job.getJobTimeout().intValue() );
			fillInRecorderDetails( extractionJob, job.getExtractorId(), deviceResource );

			transactionJobList.add( extractionJob );
			extractionJob.setRefreshTimer( setJobRefreshTimer( extractionJob, ExtractionJobType.TRANSACTION ) );

			LOG.info( "Created local job, id: " + extractionJob.getId() );
		}
		else
		{
			boolean addChannel = true;
			for ( Iterator<TransactionChannel> iterator = extractionJob.getChannels().iterator(); iterator.hasNext(); )
			{
				TransactionChannel c = ( TransactionChannel ) iterator.next();
				String id = c.getId();
				String terminalId = c.getTerminalId();

				if ( ( terminalId.equals( job.getTerminalId() ) ) && ( !id.equals( channelResource.getChannelId() ) ) )
				{
					LOG.info( "Removed channel for local job, id:" + extractionJob.getId() + ", channel id:" + c.getId() );
					iterator.remove();
					break;
				}
				if ( terminalId.equals( job.getTerminalId() ) )
				{
					addChannel = false;
					break;
				}
			}

			if ( addChannel )
			{
				extractionJob.getChannels().add( channel );
				LOG.info( "Added channel for local job, id: " + extractionJob.getId() + ", channel id:" + channel.getId() );
			}
		}
		return extractionJob;
	}

	private List<ScheduledImageExtractionJob> createScheduledImageExtractionJobs( ImageDownloadScheduleJob job )
	{
		List<ScheduledImageExtractionJob> result = new ArrayList();

		Map<Long, List<String>> deviceAndChannelsMap = new HashMap();

		Criteria criteria = new Criteria( ChannelLinkResource.class );
		criteria.add( Restrictions.in( "id", job.getChannelResourceIds() ) );
		List<Resource> channelLinkResources = topologyService.getResources( criteria );

		for ( Resource resource : channelLinkResources )
		{
			ChannelLinkResource channelLink = ( ChannelLinkResource ) resource;

			ChannelResource channelResource = topologyService.getChannelResource( channelLink.getDeviceResourceId(), channelLink.getChannelId() );
			if ( ( channelResource == null ) || ( channelResource.getChannelView().getChannelState().equals( ChannelState.DISABLED ) ) )
			{
				LOG.info( "Channel {} from device resource {} is disabled. Not adding channel to ScheduledImageExtractionJob. ", channelLink.getChannelId(), channelLink.getDeviceResourceId() );
			}
			else
			{
				List<String> channelIds = ( List ) deviceAndChannelsMap.get( channelLink.getDeviceResourceId() );
				if ( channelIds == null )
				{
					channelIds = new ArrayList();
					deviceAndChannelsMap.put( channelLink.getDeviceResourceId(), channelIds );
				}
				channelIds.add( channelLink.getChannelId() );
			}
		}

		for ( Long deviceResourceId : deviceAndChannelsMap.keySet() )
		{
			DeviceResource deviceResource = getDeviceResource( deviceResourceId );
			if ( deviceResource == null )
			{
				LOG.warn( "Device {} for Site id {} could not be found when creating ScheduledImageExtractionJob.", new Object[] {deviceResourceId, job.getSiteId()} );
			}
			else
			{
				Schedule jobSchedule = null;
				try
				{
					jobSchedule = scheduleService.getById( job.getScheduleId() );
				}
				catch ( ScheduleException e )
				{
					LOG.warn( "Schedule {} for Site id {} could not be found when creating ScheduledImageExtractionJob. Details {}", new Object[] {job.getScheduleId(), job.getSiteId(), e.getMessage()} );
				}

				if ( jobSchedule == null )
					continue;

				Long extractorId = job.getExtractorId() == null ? getExtractorIdForDevice( deviceResourceId ) : job.getExtractorId();
				if ( extractorId == null )
				{
					LOG.warn( "No Archiver set to device {}. Won't proceed creating scheduled image download job.", deviceResourceId );
				}
				else
				{
					List<ScheduledImageExtractionJob> scheduledJobsList = ( List ) scheduledImageDownloadExtractions.get( extractorId );
					String jobId = job.getSiteId().toString() + "_" + deviceResourceId.toString();

					ScheduledImageExtractionJob scheduledImageExtractionJob = null;
					for ( ScheduledImageExtractionJob scheduledExtractionJob : scheduledJobsList )
					{
						if ( scheduledExtractionJob.getId().equals( jobId ) )
						{
							scheduledImageExtractionJob = scheduledExtractionJob;
							break;
						}
					}
					if ( scheduledImageExtractionJob == null )
					{
						scheduledImageExtractionJob = new ScheduledImageExtractionJob();
						scheduledImageExtractionJob.setId( jobId );

						fillInRecorderDetails( scheduledImageExtractionJob, extractorId, deviceResource );
						scheduledImageExtractionJob.setTimeoutPeriod( job.getJobTimeout() == null ? 172800000L : job.getJobTimeout().intValue() );
						scheduledImageExtractionJob.setTag( ImageDownloadTag.OPERATIONS_AUDIT.getTagValue() );
						scheduledImageExtractionJob.setRefreshTimer( setJobRefreshTimer( scheduledImageExtractionJob, ExtractionJobType.IMAGE ) );

						scheduledJobsList.add( scheduledImageExtractionJob );
					}
					else
					{
						scheduledImageExtractionJob.clearSchedules();
						scheduledImageExtractionJob.clearChannels();
					}

					scheduledImageExtractionJob.setAdditionalTargets( null );
					if ( job.isHighResolution() )
					{
						List<AdditionalTarget> targetsList = new ArrayList( 1 );
						targetsList.add( new Extension( "High" ) );
						scheduledImageExtractionJob.setAdditionalTargets( targetsList );
					}

					scheduledImageExtractionJob.setTimeZone( job.getTimezone() );
					scheduledImageExtractionJob.addSchedules( jobSchedule.getIntervals(), job.getDownloadFrequency() );
					scheduledImageExtractionJob.addChannels( ( List ) deviceAndChannelsMap.get( deviceResourceId ) );
					result.add( scheduledImageExtractionJob );
				}
			}
		}
		return result;
	}

	private DataCollectionJob createDataCollectionJob( Long extractorId, Long deviceResourceId )
	{
		DeviceResource deviceResource = topologyService.getDeviceResource( deviceResourceId );
		if ( deviceResource == null )
		{
			LOG.info( "Device resource {} not found in topology. Will not create/update data collection job for it.", deviceResourceId );
			return null;
		}

		if ( extractorId == null )
		{
			extractorId = getExtractorIdForDevice( deviceResourceId );
			if ( extractorId == null )
			{
				LOG.info( "Device {} does not have an Archiver association. Won't try to set up a data collection job.", deviceResourceId );
				return null;
			}
		}
		List<DataCollectionJob> extractorJobs = ( List ) dataCollectionExtractions.get( extractorId );
		DataCollectionJob collectionJob = null;

		for ( DataCollectionJob extractorJob : extractorJobs )
		{
			if ( extractorJob.getId().equals( deviceResourceId.toString() ) )
			{
				collectionJob = extractorJob;
				break;
			}
		}
		if ( collectionJob == null )
		{
			collectionJob = new DataCollectionJob();
			collectionJob.setId( deviceResource.getIdAsString() );
			collectionJob.setTimeoutPeriod( 172800000L );
			fillInRecorderDetails( collectionJob, extractorId, deviceResource );
			collectionJob.setRestart( true );

			extractorJobs.add( collectionJob );
			collectionJob.setRefreshTimer( setJobRefreshTimer( collectionJob, ExtractionJobType.DATA_COLLECTION ) );
		}

		if ( DeviceManagementConstants.isR5_GT_Device( deviceResource.getDeviceView().getFamily(), deviceResource.getDeviceView().getModel() ) )
		{
			for ( Resource res : deviceResource.createFilteredResourceList( new Class[] {ChannelResource.class} ) )
			{
				ChannelResource channelResource = ( ChannelResource ) res;
				if ( ( channelResource.getChannelView().getChannelState() != ChannelState.DISABLED ) && ( channelResource.getChannelView().getData() != null ) && ( channelResource.getChannelView().getData().length > 0 ) )
				{
					DataEncoderView dataEncoder = channelResource.getChannelView().getData()[0];
					if ( "nmea".equals( dataEncoder.getCodec() ) )
					{
						collectionJob.addGpsCollection( channelResource.getChannelId() );
						break;
					}
				}
			}
		}

		fillInRecorderDetails( collectionJob, extractorId, deviceResource );
		return collectionJob;
	}

	private void fillInRecorderDetails( RecorderJob extractionJob, Long extractorId, DeviceResource deviceResource )
	{
		extractionJob.setRecorderId( deviceResource.getIdAsString() );
		extractionJob.setRecorderUrl( getDeviceAddress( deviceResource ) );
		extractionJob.setExtractorId( extractorId );
	}

	private ExtractorRestClient getExtractorRestClient( String address, String deviceId )
	{
		JsonSerializer jsonSerializer = ( JsonSerializer ) ApplicationContextSupport.getBean( "jsonSerializerService" );
		return new ExtractorRestClient( address, deviceId, deviceSessionCoreService, jsonSerializer, MetricsHelper.metrics );
	}

	private void resync( Long deviceResourceId )
	{
		updateRecorderTokensByExtractor( deviceResourceId );

		sendSetExtractionJobs( deviceResourceId, ExtractionJobType.IMAGE, ( List ) scheduledImageDownloadExtractions.get( deviceResourceId ) );

		synchronized ( transactionExtractions )
		{
			if ( !( ( List ) transactionExtractions.get( deviceResourceId ) ).isEmpty() )
			{
				sendSetExtractionJobs( deviceResourceId, ExtractionJobType.TRANSACTION, ( List ) transactionExtractions.get( deviceResourceId ) );
			}
		}
		synchronized ( dataCollectionExtractions )
		{
			if ( !( ( List ) dataCollectionExtractions.get( deviceResourceId ) ).isEmpty() )
			{
				sendSetExtractionJobs( deviceResourceId, ExtractionJobType.DATA_COLLECTION, ( List ) dataCollectionExtractions.get( deviceResourceId ) );
			}
		}
		synchronized ( mediaExtractions )
		{
			if ( !( ( List ) mediaExtractions.get( deviceResourceId ) ).isEmpty() )
			{
				sendSetExtractionJobs( deviceResourceId, ExtractionJobType.MEDIA, ( List ) mediaExtractions.get( deviceResourceId ) );
			}
		}

		setParameters( deviceResourceId );
	}

	private void extractorAdded( DeviceResource extractor )
	{
		DeviceView deviceView = extractor.getDeviceView();
		ExtractorRestClient extractorRestClient = getExtractorRestClient( deviceView.getRegistrationAddress(), deviceView.getDeviceId() );
		extractorsMap.put( extractor.getId(), new ExtractorInfo( extractorRestClient ) );

		transactionExtractions.put( extractor.getId(), new ArrayList() );
		dataCollectionExtractions.put( extractor.getId(), new ArrayList() );
		scheduledImageDownloadExtractions.put( extractor.getId(), new ArrayList() );
		mediaExtractions.put( extractor.getId(), new ArrayList() );
	}

	private void processAssociationUpdated( Long extractorId, Long[] deviceResourceIds )
	{
		if ( deviceResourceIds != null )
		{
			processDataCollectionJobs( extractorId, Arrays.asList( deviceResourceIds ) );
			updateRecorderTokens( extractorId, Arrays.asList( deviceResourceIds ) );
		}
	}

	private void processAssociationRemoved( Long extractorId, Long[] deviceResourceIds )
	{
		removeDataCollectionJobs( extractorId, Arrays.asList( deviceResourceIds ) );
	}

	private void processJobStateUpdate( ExtractorJobEvent extractorJobEvent )
	{
		String eventPath = extractorJobEvent.getNotificationInfo().getPath();
		if ( ( !eventPath.equals( DeviceEventsEnum.EXTRACTOR_MEDIA_JOB.getPath() ) ) && ( !eventPath.equals( DeviceEventsEnum.EXTRACTOR_TRANSACTION_JOB.getPath() ) ) )
		{
			return;
		}

		Long extractorId = Long.valueOf( extractorJobEvent.getInfo( "CES_DEVICE_RESOURCE_ID" ) );
		String jobId = extractorJobEvent.getSource();
		String state = ( String ) extractorJobEvent.getValue();
		String completionState = extractorJobEvent.getInfo( "CompletionState" );

		RecorderJob extractionJob = null;
		if ( eventPath.equals( DeviceEventsEnum.EXTRACTOR_MEDIA_JOB.getPath() ) )
		{
			synchronized ( mediaExtractions )
			{
				List<MediaExtractionJob> mediaJobs = ( List ) mediaExtractions.get( extractorId );
				for ( MediaExtractionJob mediaJob : mediaJobs )
				{
					if ( mediaJob.getId().equals( jobId ) )
					{
						extractionJob = mediaJob;
						break;
					}
				}
			}
		}
		else
		{
			synchronized ( transactionExtractions )
			{
				List<TransactionExtractionJob> transactionJobs = ( List ) transactionExtractions.get( extractorId );
				for ( TransactionExtractionJob transactionJob : transactionJobs )
				{
					if ( transactionJob.getId().equals( jobId ) )
					{
						extractionJob = transactionJob;
						break;
					}
				}
			}
		}

		if ( extractionJob != null )
		{
			extractionJob.setState( state );
			extractionJob.setCompletionState( completionState );
			extractionJob.setRestart( !state.equals( State.COMPLETE.getValue() ) );
		}
	}

	private class ExtractorInfo
	{
		private ExtractorRestClient extractorRestClient;
		private Future<?> tokenRefreshTimer;

		public ExtractorInfo( ExtractorRestClient extractorRestClient )
		{
			this.extractorRestClient = extractorRestClient;
		}

		public Future<?> getTokenRefreshTimer()
		{
			return tokenRefreshTimer;
		}

		public void setTokenRefreshTimer( Future<?> tokenRefreshTimer )
		{
			this.tokenRefreshTimer = tokenRefreshTimer;
		}

		public ExtractorRestClient getExtractorRestClient()
		{
			return extractorRestClient;
		}
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}

	public void setTopologyService( ResourceTopologyServiceIF topologyCoreService )
	{
		topologyService = topologyCoreService;
	}

	public void setArchiverAssociationCoreService( ArchiverAssociationService archiverAssociationCoreService )
	{
		archiverAssociationService = archiverAssociationCoreService;
	}

	public void setSecurityTokenCoreService( SecurityTokenService securityTokenCoreService )
	{
		securityTokenService = securityTokenCoreService;
	}

	public void setDeviceSessionCoreService( DeviceSessionCoreService deviceSessionCoreService )
	{
		this.deviceSessionCoreService = deviceSessionCoreService;
	}

	public void setScheduleService( ScheduleService scheduleService )
	{
		this.scheduleService = scheduleService;
	}
}

