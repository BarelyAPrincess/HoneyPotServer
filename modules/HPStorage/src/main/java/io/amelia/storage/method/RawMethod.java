package io.amelia.storage.method;

import java.io.IOException;

import io.amelia.lang.StorageException;
import io.amelia.storage.driver.entries.BytesEntry;
import io.amelia.storage.driver.entries.StorageAbilities;
import io.amelia.support.IO;
import io.netty.buffer.ByteBuf;

public class RawMethod extends StorageMethod
{
	private ByteBuf buffer;

	public RawMethod( StorageMethod parent, String localName ) throws StorageException.Error
	{
		super( parent, localName );

		StorageAbilities.requireRawBytes( driver );

		try
		{
			buffer = ( ( BytesEntry ) driver ).getBytes( IO.buildPath( parent.getPath(), localName ) );
		}
		catch ( IOException e )
		{
			throw new StorageException.Error( e );
		}
	}

	public ByteBuf getByteBuf()
	{
		return buffer;
	}
}
