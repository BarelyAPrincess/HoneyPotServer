package io.amelia.march;

import java.io.IOException;
import java.nio.file.Path;

import io.amelia.foundation.Kernel;
import io.amelia.support.IO;

public class Helper
{
	public static Path getWebDirectory() throws IOException
	{
		Path dir = Kernel.getPath( Kernel.PATH_STORAGE ).resolve( "march/web" );
		IO.forceCreateDirectory( dir );
		return dir;
	}
}
