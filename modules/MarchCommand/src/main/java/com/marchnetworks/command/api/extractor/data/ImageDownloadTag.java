package com.marchnetworks.command.api.extractor.data;

public enum ImageDownloadTag
{
	TRANSACTION( "Transaction" ),
	BUSINESS_RULE( "BusinessRule" ),
	OPERATIONS_AUDIT( "OperationsAudit" ),
	OPERATIONS_AUDIT_HIGH_RES( "OperationsAudit.High" );

	private String tagValue;

	private ImageDownloadTag( String value )
	{
		tagValue = value;
	}

	public String getTagValue()
	{
		return tagValue;
	}

	public static ImageDownloadTag fromTagValue( String tagValue )
	{
		for ( ImageDownloadTag imageDownloadTag : values() )
			if ( imageDownloadTag.getTagValue().equals( tagValue ) )
				return imageDownloadTag;

		return null;
	}
}
