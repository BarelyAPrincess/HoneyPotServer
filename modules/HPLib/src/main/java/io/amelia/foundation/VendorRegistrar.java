package io.amelia.foundation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class VendorRegistrar extends RegistrarBase
{
	private VendorMeta meta;

	public VendorRegistrar( Class<?> cls )
	{
		super( cls );
	}

	public VendorMeta getMeta()
	{
		return meta;
	}

	public class VendorMeta extends HashMap<String, String>
	{
		public List<String> getAuthors()
		{
			return Arrays.asList( get( "authors" ).split( "|" ) );
		}
	}
}
