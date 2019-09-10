package com.marchnetworks.command.common.transaction;

import com.marchnetworks.command.common.ExceptionChain;

import java.sql.SQLException;

import javax.persistence.PersistenceException;
import javax.resource.spi.ResourceAllocationException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;

import org.hibernate.StaleStateException;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.UnexpectedRollbackException;

public class TransactionExceptionTranslator
{
	public static final ExceptionChain[] RETRY_EXCEPTIONS = {new ExceptionChain( TransientDataAccessException.class ), new ExceptionChain( new Class[] {StaleStateException.class} ), new ExceptionChain( new Class[] {org.hibernate.OptimisticLockException.class} ), new ExceptionChain( new Class[] {LockAcquisitionException.class} ), new ExceptionChain( new Class[] {PersistenceException.class, LockAcquisitionException.class} ), new ExceptionChain( new Class[] {UnexpectedRollbackException.class, RollbackException.class, javax.persistence.OptimisticLockException.class, StaleStateException.class} ), new ExceptionChain( UnexpectedRollbackException.class, RollbackException.class, PersistenceException.class, LockAcquisitionException.class )};

	public static final ExceptionChain[] RESTART_EXCEPTIONS = {new ExceptionChain( GenericJDBCException.class, SQLException.class, ResourceAllocationException.class ), new ExceptionChain( new Class[] {PersistenceException.class, GenericJDBCException.class, SQLException.class, ResourceAllocationException.class} ), new ExceptionChain( new Class[] {TransactionSystemException.class, SystemException.class, XAException.class} )};

	public static final ExceptionChain[] EXPECTED_EXCEPTIONS = {new ExceptionChain( AccessDeniedException.class )};

	public static Exception translateException( Exception ex )
	{
		for ( ExceptionChain chain : RETRY_EXCEPTIONS )
		{
			if ( chain.match( ex ) )
			{
				return new DatabaseRetryException( ex );
			}
		}

		for ( ExceptionChain chain : RESTART_EXCEPTIONS )
		{
			if ( chain.match( ex ) )
			{
				return new DatabaseFailureException( ex );
			}
		}

		for ( ExceptionChain chain : EXPECTED_EXCEPTIONS )
		{
			if ( chain.match( ex ) )
			{
				return new ExternalExpectedException( ex );
			}
		}

		return new UnrecoverableException( ex );
	}
}
