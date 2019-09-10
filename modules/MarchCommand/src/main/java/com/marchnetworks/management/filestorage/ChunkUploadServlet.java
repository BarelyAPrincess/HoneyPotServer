package com.marchnetworks.management.filestorage;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public abstract interface ChunkUploadServlet
{
	public abstract boolean fileExists( String paramString );

	public abstract boolean saveFile( HttpServletResponse paramHttpServletResponse, String paramString, File paramFile, Object paramObject ) throws IOException;
}
