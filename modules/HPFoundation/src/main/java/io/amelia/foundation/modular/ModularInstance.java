package io.amelia.foundation.modular;

import io.amelia.foundation.RegistrarBase;
import io.amelia.lang.ApplicationException;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;

public abstract class ModularInstance extends RegistrarBase
{
	public ModularInstance()
	{
		setClass( getModularClass() );
	}

	public Logger getLogger()
	{
		return LogBuilder.get( getModularClass() );
	}

	public abstract Class<?> getModularClass();

	public String getName()
	{
		return getModularClass().getSimpleName();
	}

	public boolean isEnabled()
	{
		return true;
	}

	public void onModuleDestroy( Object[] args )
	{

	}

	public void onModuleDisable() throws ApplicationException
	{

	}

	public void onModuleEnable() throws ApplicationException
	{

	}

	public void onModuleLoad() throws ApplicationException
	{

	}
}
