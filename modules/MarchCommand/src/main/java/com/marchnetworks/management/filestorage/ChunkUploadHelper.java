package com.marchnetworks.management.filestorage;

import com.marchnetworks.command.common.Base64;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.DateUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChunkUploadHelper
{
	private static Logger LOG = LoggerFactory.getLogger( ChunkUploadHelper.class );

	public static final String HDR_STARTUPLOAD = "X-StartChunkUpload";
	public static final String HDR_FILESIZE = "X-FileSize";
	public static final String HDR_FILEHASH = "X-FileHash";
	public static final String HDR_NUMCHUNKS = "X-NumChunks";
	public static final String HDR_CHUNKINDEX = "X-ChunkIndex";
	public static final String HDR_CHUNKSIZE = "X-ChunkSize";
	public static final String HDR_CHUNKHASH = "X-ChunkHash";
	public static final String TRANSFER_SESSION = "TransferSession";
	public static final String HDR_TRANSFER_SESSION = "X-TransferSession";
	public static final String DEFAULT_DIR = System.getProperty( "user.dir" ) + File.separator + "fileStorageTmp";

	protected ChunkUploadServlet m_Owner;
	protected Random m_Random;
	protected String m_Dir;
	protected HashMap<Integer, UploadSession> m_Uploads;
	protected RunCleanupTask m_RunCleanupTask;

	public ChunkUploadHelper( ChunkUploadServlet owner )
	{
		this( owner, DEFAULT_DIR );
	}

	public ChunkUploadHelper( ChunkUploadServlet owner, String tempDirLocation )
	{
		m_Owner = owner;
		m_Random = new Random();
		m_Dir = tempDirLocation;

		File folder = new File( m_Dir );
		if ( !folder.exists() )
		{
			folder.mkdirs();
		}
		m_Uploads = new HashMap();

		TaskScheduler ts = ( TaskScheduler ) ApplicationContextSupport.getBean( "taskScheduler" );
		m_RunCleanupTask = new RunCleanupTask();
		ts.scheduleAtFixedRate( m_RunCleanupTask, 0L, 12L, TimeUnit.HOURS );
	}

	public boolean isChunkStart( HttpServletRequest request )
	{
		String s = request.getHeader( "X-StartChunkUpload" );
		return ( s != null ) && ( s.equalsIgnoreCase( "true" ) );
	}

	public boolean isChunkContinue( HttpServletRequest request )
	{
		String s = request.getHeader( "X-TransferSession" );
		return ( s != null ) && ( !s.isEmpty() );
	}

	public boolean handleChunkUpload( HttpServletRequest request, HttpServletResponse response, Object userObject ) throws IOException
	{
		if ( isChunkStart( request ) )
		{
			startUploadSession( request, response, userObject );
			return true;
		}
		if ( isChunkContinue( request ) )
		{

			continueUploadSession( request, response );
			return true;
		}
		return false;
	}

	public void startUploadSession( HttpServletRequest req, HttpServletResponse resp, Object userObject ) throws IOException
	{
		UploadSession up = new UploadSession();
		int sessionId = 0;

		userObject = userObject;

		String err = initCheckHeaders( req, up );
		if ( !err.isEmpty() )
		{
			genErrorResponse( resp, ChunkUploadError.START_UPLOAD_HEADERS, "Error parsing startUploadSession headers: " + err, req.getRemoteAddr() );
			return;
		}

		if ( m_Owner.fileExists( up.sName ) )
		{
			genErrorResponse( resp, ChunkUploadError.FILE_EXISTS, "File already exists: name=" + up.sName + " path=" + up.sPath, req.getRemoteAddr() );
			return;
		}

		up.lLastContactTime = DateUtils.getCurrentUTCTimeInMillis();
		up.lCurrentChunk = 0L;

		sessionId = newSession( up );
		if ( sessionId < 0 )
		{
			LOG.error( "Couldn't find unique SessionId in 100 tries!!" );

			genErrorResponse( resp, ChunkUploadError.SESSION_GENERATION, "Could not generate SessionId", req.getRemoteAddr() );
			return;
		}

		up.tmpFile = new File( m_Dir + File.separator + sessionId );

		rplyOK( resp, sessionId );
		LOG.debug( "Upload session {} started.", Integer.valueOf( sessionId ) );
	}

	public void continueUploadSession( HttpServletRequest req, HttpServletResponse resp ) throws IOException
	{
		ChunkUploadParams cup = new ChunkUploadParams();
		UploadSession up = null;

		byte[] bCalcHash = null;
		InputStream is = null;
		FileOutputStream fos = null;

		String err = contCheckHeaders( req, cup );
		if ( !err.isEmpty() )
		{
			if ( err.equals( "cancel" ) )
			{
				LOG.debug( "Upload SessionId=" + cup.SessionId + " cancelled." );
				deleteSession( cup.SessionId, true );
				PrintWriter out = resp.getWriter();
				out.println( "TransferSession=" + cup.SessionId );
				out.println( "Cancelled" );
				return;
			}

			genErrorResponse( resp, ChunkUploadError.CONTINUE_UPLOAD_HEADERS, "Error parsing continueUploadSession headers: " + err, req.getRemoteAddr() );
			return;
		}

		up = findSession( cup.SessionId );
		if ( up == null )
		{
			genErrorResponse( resp, ChunkUploadError.SESSION_NOT_FOUND, "No transfer session " + cup.SessionId + " found", req.getRemoteAddr() );
			return;
		}

		if ( up.lCurrentChunk + 1L != cup.ChunkIndex )
		{
			genErrorResponse( resp, ChunkUploadError.CHUNK_ORDER, "SessionId=" + cup.SessionId + " Chunk sent out of order. Cancelling upload", req.getRemoteAddr() );
			deleteSession( cup.SessionId, true );
			return;
		}
		up.lCurrentChunk += 1L;

		is = req.getInputStream();

		fos = new FileOutputStream( up.tmpFile, up.lCurrentChunk != 1L );
		try
		{
			bCalcHash = copyStream( is, fos );
		}
		finally
		{
			fos.close();
		}
		is.close();

		if ( !Arrays.equals( cup.ChunkHash, bCalcHash ) )
		{
			genErrorResponse( resp, ChunkUploadError.CHUNK_HASH, "SessionId=" + cup.SessionId + " Error in transmitting data: Checksum fail on chunk " + cup.ChunkIndex + "/" + up.lNumChunks + ". Cancelling session", req.getRemoteAddr() );

			deleteSession( cup.SessionId, true );
			return;
		}

		up.lLastContactTime = DateUtils.getCurrentUTCTimeInMillis();

		if ( up.lCurrentChunk != up.lNumChunks )
		{
			rplyOK( resp, cup.SessionId );
		}
		else
		{
			bCalcHash = calcHashOfFile( up.tmpFile );
			if ( !Arrays.equals( up.bFileHash, bCalcHash ) )
			{
				genErrorResponse( resp, ChunkUploadError.FILE_HASH, "SessionId=" + cup.SessionId + " Error in transmitting data: Checksum fail. Cancelling session", req.getRemoteAddr() );

				deleteSession( cup.SessionId, true );
				return;
			}

			if ( !m_Owner.saveFile( resp, up.sName, up.tmpFile, up.userObject ) )
			{
				LOG.error( "UploadError: RemoteAddr=" + req.getRemoteAddr() + " Error: Could not save chunk-uploaded file" );
				deleteSession( cup.SessionId, true );
				return;
			}

			LOG.debug( "Upload cup.SessionId=" + cup.SessionId + " finished" );
		}
	}

	private String initCheckHeaders( HttpServletRequest req, UploadSession up )
	{
		String fileName = req.getHeader( "X-fileName" );
		String filePath = req.getHeader( "X-filePath" );
		String fileSize = req.getHeader( "X-FileSize" );
		String numChunks = req.getHeader( "X-NumChunks" );
		String fileHash = req.getHeader( "X-FileHash" );

		if ( isEmpty( fileName ) )
			return "No X-fileName";
		up.sName = fileName;

		if ( filePath == null )
			filePath = "";
		up.sPath = filePath;

		if ( isEmpty( fileSize ) )
			return "No X-FileSize";
		up.lFileSize = Long.valueOf( fileSize );
		if ( ( up.lFileSize < 0L ) || ( up.lFileSize > 1073741824L ) )
		{
			return "X-FileSize is too large: " + fileSize;
		}

		if ( isEmpty( numChunks ) )
			return "No X-NumChunks";
		up.lNumChunks = Long.valueOf( numChunks );
		if ( up.lNumChunks < 0L )
		{
			return "Invalid X-NumChunks: " + numChunks;
		}

		if ( isEmpty( fileHash ) )
			return "No X-FileHash";
		try
		{
			up.bFileHash = Base64.decode( fileHash );
		}
		catch ( IOException e )
		{
			return "Invalid X-FileHash";
		}

		return "";
	}

	private String contCheckHeaders( HttpServletRequest req, ChunkUploadParams cup )
	{
		String sSessionId = req.getHeader( "X-TransferSession" );
		String sChunkIndex = req.getHeader( "X-ChunkIndex" );
		String sChunkHash = req.getHeader( "X-ChunkHash" );

		if ( isEmpty( sSessionId ) )
			return "No X-TransferSession";
		cup.SessionId = Integer.valueOf( sSessionId );

		String sCancel = req.getHeader( "X-CancelSession" );
		if ( ( sCancel != null ) && ( sCancel.equalsIgnoreCase( "true" ) ) )
		{
			return "cancel";
		}

		if ( isEmpty( sChunkIndex ) )
			return "No X-ChunkIndex";
		cup.ChunkIndex = Long.valueOf( sChunkIndex );

		if ( isEmpty( sChunkHash ) )
			return "No X-ChunkHash";
		try
		{
			cup.ChunkHash = Base64.decode( sChunkHash );
		}
		catch ( IOException e )
		{
			return "Invalid X-ChunkHash";
		}

		return "";
	}

	private synchronized int newSession( UploadSession up )
	{
		int result = 0;
		int i;

		for ( i = 0; i < 100; i++ )
		{
			result = m_Random.nextInt( 32768 );

			result %= 32768;

			if ( ( result != 0 ) && !m_Uploads.containsKey( result ) )
				break;
		}

		if ( i > 99 )
		{
			return -1;
		}

		m_Uploads.put( result, up );
		return result;
	}

	private synchronized UploadSession findSession( int sessionId )
	{
		return m_Uploads.get( sessionId );
	}

	private synchronized void deleteSession( int sessionId, boolean deleteFile )
	{
		UploadSession up = m_Uploads.remove( sessionId );
		if ( ( deleteFile ) && ( up.tmpFile.exists() ) )
			up.tmpFile.delete();
	}

	private void rplyOK( HttpServletResponse resp, int sessionId ) throws IOException
	{
		resp.setStatus( 202 );
		PrintWriter out = resp.getWriter();
		out.print( "TransferSession=" + sessionId + String.format( "%n", new Object[0] ) );
	}

	protected boolean isEmpty( String s )
	{
		if ( s == null )
			return true;
		return s.isEmpty();
	}

	protected byte[] copyStream( InputStream is, OutputStream os ) throws IOException
	{
		byte[] buf = new byte['က'];
		MessageDigest md;

		try
		{
			md = MessageDigest.getInstance( "SHA-1" );
		}
		catch ( NoSuchAlgorithmException e )
		{
			LOG.error( "Can't get MD5 hash algorithm: ", e );
			return null;
		}

		md.reset();
		int read = is.read( buf );
		while ( read > 0 )
		{
			md.update( buf, 0, read );
			os.write( buf, 0, read );
			read = is.read( buf );
		}

		return md.digest();
	}

	protected void genErrorResponse( HttpServletResponse resp, ChunkUploadError code, String msg, String remoteAddress ) throws IOException
	{
		LOG.error( "UploadError: RemoteAddr={} Error: {}", remoteAddress, msg );
		genErrorResponse_Common( resp, code.toString(), msg );
	}

	protected void genErrorResponse_Common( HttpServletResponse resp, String code, String msg ) throws IOException
	{
		resp.setHeader( "x-reason", code );
		resp.setStatus( 400 );
		PrintWriter out = resp.getWriter();
		out.print( msg );
		out.close();
	}

	protected class RunCleanupTask implements Runnable
	{
		protected RunCleanupTask()
		{
		}

		public void run()
		{
			RunCleanup();
		}
	}

	public void RunCleanup()
	{
		LOG.debug( "Cleaning up any stale sessions or unused temporary files" );

		long STALETIME = 36000000L;
		List<Integer> removeList = new ArrayList<Integer>();

		long now = DateUtils.getCurrentUTCTimeInMillis();
		synchronized ( this )
		{
			for ( Map.Entry<Integer, UploadSession> e : m_Uploads.entrySet() )
			{
				UploadSession up = e.getValue();
				if ( now - up.lLastContactTime > 36000000L )
					removeList.add( e.getKey() );
			}
		}

		for ( Iterator<Integer> i$ = removeList.iterator(); i$.hasNext(); )
		{
			int sessionId = i$.next();
			deleteSession( sessionId, true );
		}

		File dir = new File( m_Dir );
		String[] files = dir.list();
		for ( String filename : files )
		{

			if ( ( !filename.equals( "." ) ) && ( !filename.equals( ".." ) ) )
			{

				Integer sessionid = parseInt( filename );
				if ( sessionid != null )
				{

					if ( !m_Uploads.containsKey( sessionid ) )
					{
						File del = new File( m_Dir + File.separator + sessionid.toString() );

						if ( del.isFile() )
							del.delete();
					}
				}
			}
		}
	}

	public Integer parseInt( String data )
	{
		Integer val = null;
		try
		{
			val = Integer.valueOf( Integer.parseInt( data ) );
		}
		catch ( NumberFormatException localNumberFormatException )
		{
		}
		return val;
	}

	protected byte[] calcHashOfFile( File f )
	{
		byte[] buf = new byte['က'];
		MessageDigest md;

		try
		{
			md = MessageDigest.getInstance( "SHA-1" );
		}
		catch ( NoSuchAlgorithmException e )
		{
			LOG.error( "Can't get MD5 hash algorithm: ", e );
			return null;
		}

		FileInputStream fis;

		try
		{
			fis = new FileInputStream( f );
		}
		catch ( FileNotFoundException e )
		{
			return null;
		}

		md.reset();
		try
		{
			int read = fis.read( buf );
			while ( read > 0 )
			{
				md.update( buf, 0, read );
				read = fis.read( buf );
			}

			return md.digest();
		}
		catch ( IOException e )
		{
			return null;
		}
		finally
		{
			try
			{
				fis.close();
			}
			catch ( IOException localIOException3 )
			{
			}
		}
	}

	protected class ChunkUploadParams
	{
		public int SessionId;
		public long ChunkIndex;
		public byte[] ChunkHash;

		protected ChunkUploadParams()
		{
		}
	}

	protected class UploadSession
	{
		public long lLastContactTime;
		public long lFileSize;
		public long lNumChunks;
		public long lCurrentChunk;
		public byte[] bFileHash;
		public String sName;
		public String sPath;
		public File tmpFile;
		public Object userObject;

		protected UploadSession()
		{
		}
	}
}
