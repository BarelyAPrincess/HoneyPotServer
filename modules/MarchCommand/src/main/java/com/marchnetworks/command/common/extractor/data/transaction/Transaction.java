package com.marchnetworks.command.common.extractor.data.transaction;

import java.math.BigDecimal;

public abstract class Transaction
{
	protected String siteId;
	protected String terminalId;
	protected Long transactionNumber;
	protected String transactionTypeCode;
	protected BigDecimal amount;
	protected Boolean isExternalTimeUTC;
	protected Long externalStart;
	protected Long externalEnd;
	protected Long internalStartUTC;
	protected Long internalEndUTC;
	protected String protocolId;
	protected String extStr1;
	protected String extStr2;
	protected String extStr3;
	protected Boolean extBool1;
	protected Boolean extBool2;
	protected Boolean extBool3;
	protected BigDecimal extNum1;
	protected BigDecimal extNum2;
	protected BigDecimal extNum3;

	public String getSiteId()
	{
		return siteId;
	}

	public void setSiteId( String siteId )
	{
		this.siteId = siteId;
	}

	public String getTerminalId()
	{
		return terminalId;
	}

	public void setTerminalId( String terminalId )
	{
		this.terminalId = terminalId;
	}

	public Long getTransactionNumber()
	{
		return transactionNumber;
	}

	public void setTransactionNumber( Long transactionNumber )
	{
		this.transactionNumber = transactionNumber;
	}

	public String getTransactionTypeCode()
	{
		return transactionTypeCode;
	}

	public void setTransactionTypeCode( String transactionTypeCode )
	{
		this.transactionTypeCode = transactionTypeCode;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public void setAmount( BigDecimal amount )
	{
		this.amount = amount;
	}

	public Boolean getIsExternalTimeUTC()
	{
		return isExternalTimeUTC;
	}

	public void setIsExternalTimeUTC( Boolean isExternalTimeUTC )
	{
		this.isExternalTimeUTC = isExternalTimeUTC;
	}

	public Long getExternalStart()
	{
		return externalStart;
	}

	public void setExternalStart( Long externalStart )
	{
		this.externalStart = externalStart;
	}

	public Long getExternalEnd()
	{
		return externalEnd;
	}

	public void setExternalEnd( Long externalEnd )
	{
		this.externalEnd = externalEnd;
	}

	public Long getInternalStartUTC()
	{
		return internalStartUTC;
	}

	public void setInternalStartUTC( Long internalStartUTC )
	{
		this.internalStartUTC = internalStartUTC;
	}

	public Long getInternalEndUTC()
	{
		return internalEndUTC;
	}

	public void setInternalEndUTC( Long internalEndUTC )
	{
		this.internalEndUTC = internalEndUTC;
	}

	public String getProtocolId()
	{
		return protocolId;
	}

	public void setProtocolId( String protocolId )
	{
		this.protocolId = protocolId;
	}

	public String getExtStr1()
	{
		return extStr1;
	}

	public void setExtStr1( String extStr1 )
	{
		this.extStr1 = extStr1;
	}

	public String getExtStr2()
	{
		return extStr2;
	}

	public void setExtStr2( String extStr2 )
	{
		this.extStr2 = extStr2;
	}

	public String getExtStr3()
	{
		return extStr3;
	}

	public void setExtStr3( String extStr3 )
	{
		this.extStr3 = extStr3;
	}

	public Boolean getExtBool1()
	{
		return extBool1;
	}

	public void setExtBool1( Boolean extBool1 )
	{
		this.extBool1 = extBool1;
	}

	public Boolean getExtBool2()
	{
		return extBool2;
	}

	public void setExtBool2( Boolean extBool2 )
	{
		this.extBool2 = extBool2;
	}

	public Boolean getExtBool3()
	{
		return extBool3;
	}

	public void setExtBool3( Boolean extBool3 )
	{
		this.extBool3 = extBool3;
	}

	public BigDecimal getExtNum1()
	{
		return extNum1;
	}

	public void setExtNum1( BigDecimal extNum1 )
	{
		this.extNum1 = extNum1;
	}

	public BigDecimal getExtNum2()
	{
		return extNum2;
	}

	public void setExtNum2( BigDecimal extNum2 )
	{
		this.extNum2 = extNum2;
	}

	public BigDecimal getExtNum3()
	{
		return extNum3;
	}

	public void setExtNum3( BigDecimal extNum3 )
	{
		this.extNum3 = extNum3;
	}
}
