package io.amelia.foundation;

import io.amelia.lang.ConfigException;

public interface ConfigRegistryLoader
{
	void loadConfig( ConfigMap config ) throws ConfigException.Error;
}
