package com.marchnetworks.management.certificate;

import com.marchnetworks.common.service.CertificationService;
import com.marchnetworks.common.spring.ApplicationContextSupport;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CertificateServiceServlet extends HttpServlet
{
	private static final long serialVersionUID = -4992241666922085871L;
	private static Logger LOG = LoggerFactory.getLogger( CertificateServiceServlet.class );

	public void init( ServletConfig servletConfig ) throws ServletException
	{
		super.init( servletConfig );
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		doDownload( request, response );
	}

	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		doPost( request, response );
	}

	private void doDownload( HttpServletRequest req, HttpServletResponse resp ) throws IOException
	{
		LOG.debug( "Sending RootCert" );

		ServletOutputStream op = resp.getOutputStream();

		req.getParameterMap();
		String path = req.getRequestURI();
		String[] param = path.split( "/" );
		String originalFilename = param[3];

		CertificationService cs = ( CertificationService ) ApplicationContextSupport.getBean( "certificationService" );
		byte[] bRootCert = cs.getRootCertCache();

		resp.setContentType( "application/x-x509-ca-cert" );
		resp.setHeader( "Cache-Control", "max-age=0" );
		resp.setHeader( "Content-Disposition", "attachment; filename=\"" + originalFilename + "\"" );

		op.write( bRootCert );
		op.flush();
		op.close();
	}
}
