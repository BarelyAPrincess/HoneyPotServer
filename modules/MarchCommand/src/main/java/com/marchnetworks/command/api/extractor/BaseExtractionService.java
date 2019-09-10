package com.marchnetworks.command.api.extractor;

import com.marchnetworks.command.api.extractor.data.ImageDownloadJob;
import com.marchnetworks.command.api.extractor.data.ImageDownloadScheduleJob;
import com.marchnetworks.command.api.extractor.data.MediaDownloadJob;
import com.marchnetworks.command.api.extractor.data.TransactionJob;
import com.marchnetworks.command.common.extractor.data.Parameter;
import com.marchnetworks.command.common.extractor.data.image.ImageInfo;

import java.util.List;
import java.util.Map;

public abstract interface BaseExtractionService
{
	public abstract Long getExtractorIdForDevice( Long paramLong );

	public abstract boolean extractorExists( Long paramLong );

	public abstract boolean extractorExists();

	public abstract void updateMediaJobs( List<MediaDownloadJob> paramList );

	public abstract void updateMediaJobsAsync( List<MediaDownloadJob> paramList );

	public abstract Map<Long, Boolean> removeMediaJobs( List<MediaDownloadJob> paramList );

	public abstract void removeMediaJobsAsync( List<MediaDownloadJob> paramList );

	public abstract void updateDataCollectionJobs( List<Long> paramList );

	public abstract void removeDataCollectionJob( Long paramLong );

	public abstract void updateTransactionJobs( List<TransactionJob> paramList );

	public abstract void removeTransactionJob( TransactionJob paramTransactionJob );

	public abstract void updateImageDownloadScheduleJobs( List<ImageDownloadScheduleJob> paramList );

	public abstract void removeImageDownloadScheduleJob( Long paramLong );

	public abstract List<String> updateImageDownloadJobs( List<ImageDownloadJob> paramList );

	public abstract void updateImageDownloadJobsAsync( List<ImageDownloadJob> paramList );

	public abstract byte[] getImage( Long paramLong1, String paramString1, Long paramLong2, Long paramLong3, Long paramLong4, String paramString2 );

	public abstract byte[] getImageExact( Long paramLong1, String paramString1, Long paramLong2, String paramString2 );

	public abstract List<ImageInfo> getImageInfo( Long paramLong1, String paramString1, Long paramLong2, Long paramLong3, String paramString2 );

	public abstract void setParameter( Parameter paramParameter );

	public abstract void sendParameterAsync( Parameter paramParameter );
}
