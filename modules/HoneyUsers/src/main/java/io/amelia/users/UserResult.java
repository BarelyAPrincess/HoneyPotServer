package io.amelia.users;

import javax.annotation.Nonnull;

import io.amelia.lang.DescriptiveReason;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.UserException;

public class UserResult implements UserPrincipal
{
	@Nonnull
	private final String uuid;
	@Nonnull
	private DescriptiveReason descriptiveReason = DescriptiveReason.NULL;
	private Throwable throwable = null;
	private UserContext userContext = null;

	public UserResult( @Nonnull String uuid )
	{
		this.uuid = uuid;
	}

	@Nonnull
	public DescriptiveReason getDescriptiveReason()
	{
		return descriptiveReason;
	}

	public ReportingLevel getReportingLevel()
	{
		return descriptiveReason.getReportingLevel();
	}

	public UserException.Error getThrowable()
	{
		return new UserException.Error( userContext, descriptiveReason, throwable );
	}

	public UserContext getUser()
	{
		return userContext;
	}

	public boolean hasException()
	{
		return throwable != null;
	}

	public boolean hasResult()
	{
		return descriptiveReason != DescriptiveReason.NULL;
	}

	@Override
	public String name()
	{
		return userContext.name();
	}

	public void reset()
	{
		descriptiveReason = DescriptiveReason.NULL;
		throwable = null;
	}

	public void setDescriptiveReason( @Nonnull DescriptiveReason descriptiveReason )
	{
		this.descriptiveReason = descriptiveReason;
	}

	public void setThrowable( Throwable throwable )
	{
		this.throwable = throwable;
	}

	public void setUser( UserContext userContext )
	{
		if ( !uuid.equals( userContext.uuid() ) )
			throw new IllegalArgumentException( "UserContext did not match the uuid this UserResult was constructed with." );

		this.userContext = userContext;
	}

	@Override
	public String uuid()
	{
		return userContext.uuid();
	}
}
