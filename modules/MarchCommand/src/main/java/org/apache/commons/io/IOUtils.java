package org.apache.commons.io;

import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

import io.amelia.support.IO;

public class IOUtils
{
	public static void copy( FileInputStream in, ServletOutputStream out ) throws IOException
	{
		IO.copy( in, out );
	}
}
