package com.marchnetworks.command.common.extractor.data.transaction;

import com.marchnetworks.command.common.extractor.data.Channel;

public class TransactionChannel extends Channel
{
	private String ProtocolName;
	private String TerminalId;

	public String getProtocolName()
	{
		return ProtocolName;
	}

	public void setProtocolName( String protocolName )
	{
		this.ProtocolName = protocolName;
	}

	public String getTerminalId()
	{
		return TerminalId;
	}

	public void setTerminalId( String terminalId )
	{
		this.TerminalId = terminalId;
	}
}
