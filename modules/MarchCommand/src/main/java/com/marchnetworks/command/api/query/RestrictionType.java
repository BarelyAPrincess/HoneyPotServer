package com.marchnetworks.command.api.query;

public enum RestrictionType
{
	EQUAL,
	NOT_EQUAL,
	IN,
	IS_NOT_EMPTY,
	CONTAINS,
	NOT_IN,
	GREATER_THAN,
	LESS_THAN;

	private RestrictionType()
	{
	}
}
