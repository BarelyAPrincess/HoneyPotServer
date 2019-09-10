package com.marchnetworks.common.system.parameter.model;

import com.marchnetworks.command.common.CommonAppUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table( name = "PARAMETER_SETTING" )
public class ParameterSettingEntity
{
	@Id
	@Column( name = "NAME" )
	private String parameterName;
	@Lob
	@Column( name = "VALUE", length = 2000000 )
	private byte[] parameterValue;

	public String getParameterName()
	{
		return parameterName;
	}

	public String getParameterValue()
	{
		return CommonAppUtils.encodeToUTF8String( parameterValue );
	}

	public void setParameterName( String parameterName )
	{
		this.parameterName = parameterName;
	}

	public void setParameterValue( String parameterValue )
	{
		this.parameterValue = CommonAppUtils.encodeStringToBytes( parameterValue );
	}
}
