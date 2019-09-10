package com.marchnetworks.command.api.rest;

public enum DeviceRestErrorEnum
{
	ERROR_UNAUTHORIZED,
	ERROR_SOCKET_TIMEOUT,
	ERROR_SSL_HANDSHAKE,
	ERROR_SERVER_ERROR;

	private DeviceRestErrorEnum()
	{
	}
}
