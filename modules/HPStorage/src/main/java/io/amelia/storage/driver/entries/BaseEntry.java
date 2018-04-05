package io.amelia.storage.driver.entries;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import io.amelia.support.Objs;
import io.netty.buffer.ByteBuf;

public abstract class BaseEntry
{
	public static String LAST_MODIFIED = "last-modified";

	public volatile Map<String, String> meta = new HashMap<>();
	private ByteBuf content;
	private String name;

	public String getName()
	{
		return name;
	}

	public void setName( @Nonnull String name )
	{
		Objs.nonEmpty( name );

		this.name = name;
	}

	public void setContent( ByteBuf content )
	{
		this.content = content;
	}
}
