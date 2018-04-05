package io.amelia.storage.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.amelia.lang.StorageException;
import io.amelia.storage.driver.entries.BaseEntry;
import io.amelia.storage.driver.entries.DirectoryEntry;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.netty.buffer.Unpooled;

public class IODriver<Entry extends BaseEntry> extends StorageDriver<Entry>
{
	private final File baseDirectory;

	public IODriver( File baseDirectory, Supplier<Entry> entryMaker )
	{
		super( entryMaker );

		Objs.notFalse( baseDirectory.isDirectory() );

		this.baseDirectory = baseDirectory;
	}

	@Override
	public Entry getEntry( String localName ) throws StorageException.Error
	{
		return toEntry( new File( baseDirectory, localName ) );
	}

	@Override
	public Stream<Entry> streamEntries( String regexPattern )
	{
		return IO.recursiveFiles( baseDirectory, Strs.countMatches( regexPattern, '/' ), regexPattern ).stream().map( this::toEntryWithoutException ).filter( Objs::isNotNull );
	}

	public Entry toEntry( File file ) throws StorageException.Error
	{
		Entry entry = entryMaker.get();

		if ( !file.exists() )
			throw StorageException.error( "Entry was not found!" );

		if ( entry instanceof DirectoryEntry && !file.isDirectory() )
			throw StorageException.error( "File is not valid for DirectoryEntry!" );

		entry.setName( file.getName() );

		try
		{
			entry.setContent( Unpooled.wrappedBuffer( IO.readStreamToNIOBuffer( new FileInputStream( file ) ) ) );
		}
		catch ( IOException e )
		{
			throw StorageException.error( e );
		}

		entry.meta.put( BaseEntry.LAST_MODIFIED, String.valueOf( file.lastModified() ) );

		return entry;
	}

	private Entry toEntryWithoutException( File file )
	{
		try
		{
			return toEntry( file );
		}
		catch ( Exception e )
		{
			return null;
		}
	}
}
