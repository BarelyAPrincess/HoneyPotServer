package com.marchnetworks.common.system;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.common.config.AppConfig;
import com.marchnetworks.common.config.AppConfigImpl;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.device.ServerServiceException;
import com.marchnetworks.common.system.parameter.model.ParameterSettingDAO;
import com.marchnetworks.common.system.parameter.model.ParameterSettingEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;

class ServerParameterStoreServiceImpl implements ServerParameterStoreServiceIF
{
	private static final Logger LOG = LoggerFactory.getLogger( ServerParameterStoreServiceImpl.class );

	protected AppConfig m_AppConfig = AppConfigImpl.getInstance();

	private ParameterSettingDAO parameterSettingDAO;

	public void init( ContextRefreshedEvent ev )
	{
		Object o = ev.getSource();
		if ( !( o instanceof WebApplicationContext ) )
		{
			return;
		}
		InputStream is = getManifestFileStream( ( WebApplicationContext ) o );
		if ( is == null )
		{
			return;
		}
		try
		{
			Manifest m = new Manifest( is );
			Attributes atrs = m.getMainAttributes();

			String sBuildDate = atrs.getValue( "Build-Date" );
			String sBuildNum = atrs.getValue( "Build-Number" );
			String sPackNum = m_AppConfig.getProperty( ConfigProperty.PACKAGE_NUMBER );

			LOG.info( "Running server version {}", sBuildNum );
			storeParameter( "ServerManifest.Build-Date", sBuildDate );
			storeParameter( "ServerManifest.Build-Number", sBuildNum );
			storeParameter( "ServerManifest.Package-Number", sPackNum );
		}
		catch ( Exception e )
		{
			LOG.warn( "Error grabbing server build date and number from MANIFEST.MF:" + e.toString() );
		}
	}

	public String getParameterValue( String parameterName )
	{
		ParameterSettingEntity parameterSetting = ( ParameterSettingEntity ) parameterSettingDAO.findById( parameterName );
		return parameterSetting != null ? parameterSetting.getParameterValue() : null;
	}

	public String getParameterValueService( String parameterName ) throws ServerServiceException
	{
		String param = m_AppConfig.getProperty( parameterName );
		if ( param == null )
		{
			if ( !ServerParameterStoreConstants.PUBLIC_PARAMETERS.contains( parameterName ) )
			{
				throw new ServerServiceException( "Property " + parameterName + " is not an allowed parameter" );
			}

			param = ( String ) ServerParameterStoreConstants.PUBLIC_PARAMETERS_VALUES.get( parameterName );
			if ( !CommonAppUtils.isNullOrEmptyString( param ) )
			{
				return param;
			}

			ParameterSettingEntity parameterSetting = ( ParameterSettingEntity ) parameterSettingDAO.findById( parameterName );
			return parameterSetting != null ? parameterSetting.getParameterValue() : null;
		}

		return param;
	}

	public Map<String, String> getParametersValues( String... parameterNames ) throws ServerServiceException
	{
		if ( parameterNames == null )
		{
			return null;
		}

		List<ParameterSettingEntity> parameters = parameterSettingDAO.findAllByName( parameterNames );
		Map<String, String> results = new HashMap( parameters.size() );
		for ( ParameterSettingEntity parameterSettingEntity : parameters )
		{
			results.put( parameterSettingEntity.getParameterName(), parameterSettingEntity.getParameterValue() );
		}
		return results;
	}

	public void storeParameter( String parameterName, String parameterValue ) throws ServerServiceException
	{
		if ( ( parameterName == null ) || ( parameterValue == null ) )
		{
			throw new ServerServiceException( "Parameter Name and Value can't be null" );
		}

		ParameterSettingEntity parameterSetting = ( ParameterSettingEntity ) parameterSettingDAO.findById( parameterName );
		if ( parameterSetting == null )
		{
			parameterSetting = new ParameterSettingEntity();
			parameterSetting.setParameterName( parameterName );
			parameterSettingDAO.create( parameterSetting );
		}
		parameterSetting.setParameterValue( parameterValue );
	}

	public void removeParameter( String parameterName )
	{
		if ( parameterName == null )
		{
			return;
		}
		parameterSettingDAO.deleteDetached( parameterName );
	}

	private FileInputStream getManifestFileStream( WebApplicationContext webContext )
	{
		FileInputStream manifestFile = null;

		ServletContext sc = webContext.getServletContext();

		String manifestFullPath = sc.getRealPath( "/META-INF/MANIFEST.MF" );
		try
		{
			manifestFile = new FileInputStream( manifestFullPath );
		}
		catch ( FileNotFoundException fnfe )
		{
			LOG.warn( "Manifest file not found or can't be read at {}. Error details: {}", new Object[] {manifestFullPath, fnfe} );
		}

		return manifestFile;
	}

	public void setParameterSettingDAO( ParameterSettingDAO parameterSettingDAO )
	{
		this.parameterSettingDAO = parameterSettingDAO;
	}
}
