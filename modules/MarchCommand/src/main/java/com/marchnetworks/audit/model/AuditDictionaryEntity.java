package com.marchnetworks.audit.model;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.common.utils.CompressionUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table( name = "AUDIT_KEY_VALUE_STORE" )
public class AuditDictionaryEntity
{
	@Id
	@Column( name = "ENTRY_KEY" )
	private Integer key;
	@Lob
	@Column( name = "VALUE" )
	private byte[] value;

	public byte[] getValue()
	{
		return value;
	}

	public void setValue( byte[] value )
	{
		this.value = value;
	}

	public String readValue()
	{
		return CommonAppUtils.encodeToUTF8String( CompressionUtils.decompress( value ) );
	}

	public void setValue( String value )
	{
		if ( value != null )
		{
			byte[] compressedValue = CompressionUtils.compress( CommonAppUtils.encodeStringToBytes( value ) );
			setValue( compressedValue );
		}
	}

	public Integer getKey()
	{
		return key;
	}

	public void setKey( Integer key )
	{
		this.key = key;
	}
}
