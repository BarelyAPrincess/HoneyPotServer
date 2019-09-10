package com.marchnetworks.command.common.extractor.data;

public enum ExtractionJobType
{
	TRANSACTION( "Transaction" ),
	IMAGE( "Image" ),
	MEDIA( "Media" ),
	DATA_COLLECTION( "DataCollection" );

	private String type;

	private ExtractionJobType( String type )
	{
		this.type = type;
	}

	public String getType()
	{
		return type;
	}
}
