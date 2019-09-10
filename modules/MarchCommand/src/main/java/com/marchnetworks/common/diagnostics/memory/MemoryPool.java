package com.marchnetworks.common.diagnostics.memory;

public enum MemoryPool
{
	CODE_CACHE( new String[] {"Code Cache"} ),
	EDEN( new String[] {"Eden Space"} ),
	SURVIVOR( new String[] {"Survivor Space"} ),
	TENURED( new String[] {"Tenured Gen", "Old Gen"} ),
	PERM_GEN( new String[] {"Perm Gen"} );

	public static MemoryPool fromValue( String name )
	{
		for ( MemoryPool c : values() )
		{
			for ( String poolName : c.getNames() )
			{
				if ( name.contains( poolName ) )
					return c;
			}
		}

		return null;
	}

	private String[] names;

	private MemoryPool( String... names )
	{
		this.names = names;
	}

	public String[] getNames()
	{
		return names;
	}
}
