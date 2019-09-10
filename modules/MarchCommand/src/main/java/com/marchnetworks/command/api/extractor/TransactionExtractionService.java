package com.marchnetworks.command.api.extractor;

import com.marchnetworks.command.common.extractor.data.transaction.Transaction;

import java.util.List;

public interface TransactionExtractionService extends BaseExtractionService
{
	List<Transaction> getTransactions( Long paramLong );

	long getLastTransactionTime( Long paramLong );

	long getLastDeserializeTime( Long paramLong );
}
