package com.marchnetworks.management.config.model;

import java.io.IOException;

public interface ModifySnapshot
{
	void setFirmwareVersion( String paramString );

	void setModel( String paramString );

	void readConfigData( String paramString ) throws IOException;
}
