package com.marchnetworks.command.api.provider;

public interface ContentProvider<O, I>
{
	O getContent( I paramI );
}
