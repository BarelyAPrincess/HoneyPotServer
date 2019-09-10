package com.marchnetworks.management.file;

import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.management.data.ChannelDeviceModel;
import com.marchnetworks.management.data.ModelVersionsForUPG;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FirmwareConfiguration
{
	private static final String XML_PROPERTY_FILENAME = "filename";
	private static final String XML_PROPERTY_VERSION = "version";
	private static final String XML_PROPERTY_MINVERSION = "minversion";
	private static final String XML_PROPERTY_MAXVERSION = "maxversion";
	private static final String XML_PROPERTY_MODEL = "model";
	private static final String XML_PROPERTY_TYPE = "type";
	private static final String XML_PROPERTY_UPGRADE = "upgrade";
	private static final String XML_PROPERTY_FILETYPE = "filetype";
	private static final String XML_PROPERTY_DISPLAYVERSION = "displayversion";
	private static final String XML_PROPERTY_VERSIONLIST = "versionlist";
	private static final String XML_PROPERTY_AGENTVERSION = "agentversion";
	private static final String XML_PROPERTY_UPGRADELIST = "upgradelist";
	private static final String XML_PROPERTY_UPG = "upg";
	private static final String XML_PROPERTY_RESTART = "restart";
	private static final String XML_PROPERTY_CCM = "CCM";
	private static final String XML_PROPERTY_DEVICE = "device";
	private static final String XML_PROPERTY_MANUFACTURERID = "manufacturerId";
	private static final String XML_PROPERTY_MODELNAME = "modelName";
	private static final String XML_PROPERTY_MODELID = "modelId";
	private static final String XML_PROPERTY_SUBMODELID = "submodelId";
	private static final String PROPERTY_SEPARATOR = ";";
	private String fileName;
	private String version;
	private String minVersion;
	private String maxVersion;
	private String family;
	private String model;
	private String filetype;
	private String displayversion;
	private String versionlist;
	private String agentversion;
	private boolean upgradeElement;
	private List<ModelVersionsForUPG> modelVersionList = new ArrayList();

	private String restart;
	private String manufacturerId = "";
	private List<ChannelDeviceModel> channelModelList = new ArrayList();

	public FirmwareConfiguration()
	{
		upgradeElement = false;
	}

	public boolean readFirmwareConfiguration( InputStream inputStream )
	{
		try
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler()
			{
				boolean filenameFlag = false;
				boolean versionFlag = false;
				boolean minVersionFlag = false;
				boolean maxVersionFlag = false;
				boolean modelFlag = false;
				boolean typeFlag = false;
				boolean upgradeFlag = false;

				boolean filetypeFlag = false;
				boolean displayversionFlag = false;
				boolean versionlistFlag = false;
				boolean agentVersionFlag = false;
				boolean upgradelistFlag = false;
				boolean upgFlag = false;
				boolean restartFlag = false;

				boolean ccmFlag = false;
				boolean deviceFlag = false;
				boolean manufacturerIdFlag = false;
				boolean modelNameFlag = false;
				boolean modelIdFlag = false;
				boolean submodelIdFlag = false;

				public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException
				{
					if ( qName.equalsIgnoreCase( "upgrade" ) )
					{
						upgradeFlag = true;
					}

					if ( qName.equalsIgnoreCase( "filename" ) )
					{
						filenameFlag = true;
					}

					if ( qName.equalsIgnoreCase( "version" ) )
					{
						versionFlag = true;
					}

					if ( qName.equalsIgnoreCase( "minversion" ) )
					{
						minVersionFlag = true;
					}

					if ( qName.equalsIgnoreCase( "maxversion" ) )
					{
						maxVersionFlag = true;
					}

					if ( qName.equalsIgnoreCase( "model" ) )
					{
						modelFlag = true;
					}

					if ( qName.equalsIgnoreCase( "type" ) )
					{
						typeFlag = true;
					}

					if ( qName.equalsIgnoreCase( "filetype" ) )
					{
						filetypeFlag = true;
					}
					if ( qName.equalsIgnoreCase( "displayversion" ) )
					{
						displayversionFlag = true;
					}
					if ( qName.equalsIgnoreCase( "versionlist" ) )
					{
						versionlistFlag = true;
					}
					if ( qName.equalsIgnoreCase( "agentversion" ) )
					{
						agentVersionFlag = true;
					}
					if ( qName.equalsIgnoreCase( "upgradelist" ) )
					{
						upgradelistFlag = true;
					}
					if ( qName.equalsIgnoreCase( "upg" ) )
					{
						modelVersionList.add( new ModelVersionsForUPG() );
						upgFlag = true;
					}
					if ( qName.equalsIgnoreCase( "restart" ) )
					{
						restartFlag = true;
					}

					if ( qName.equalsIgnoreCase( "CCM" ) )
					{
						ccmFlag = true;
					}

					if ( qName.equalsIgnoreCase( "device" ) )
					{
						channelModelList.add( new ChannelDeviceModel() );
						deviceFlag = true;
					}

					if ( qName.equalsIgnoreCase( "manufacturerId" ) )
					{
						manufacturerIdFlag = true;
					}

					if ( qName.equalsIgnoreCase( "modelName" ) )
					{
						modelNameFlag = true;
					}

					if ( qName.equalsIgnoreCase( "modelId" ) )
					{
						modelIdFlag = true;
					}

					if ( qName.equalsIgnoreCase( "submodelId" ) )
					{
						submodelIdFlag = true;
					}
				}

				public void endElement( String uri, String localName, String qName ) throws SAXException
				{
					if ( qName.equalsIgnoreCase( "upgradelist" ) )
					{
						upgradelistFlag = false;
					}
					if ( qName.equalsIgnoreCase( "upg" ) )
					{
						upgFlag = false;
					}

					if ( qName.equalsIgnoreCase( "CCM" ) )
					{
						ccmFlag = false;
					}
					if ( qName.equalsIgnoreCase( "device" ) )
					{
						deviceFlag = false;
					}
				}

				public void characters( char[] ch, int start, int length ) throws SAXException
				{
					if ( upgradeFlag )
					{
						setUpgradeElement( true );
					}

					if ( filenameFlag )
					{
						setFileName( new String( ch, start, length ) );
						filenameFlag = false;
					}

					if ( versionFlag )
					{
						setVersion( new String( ch, start, length ) );
						versionFlag = false;
					}

					if ( minVersionFlag )
					{
						String upgMinVersion = new String( ch, start, length );
						if ( ( upgradelistFlag ) && ( upgFlag ) )
						{
							( ( ModelVersionsForUPG ) modelVersionList.get( modelVersionList.size() - 1 ) ).setMinversion( upgMinVersion );
						}
						else
						{
							setMinVersion( upgMinVersion );
						}
						minVersionFlag = false;
					}

					if ( maxVersionFlag )
					{
						String upgMaxVersion = new String( ch, start, length );
						if ( ( upgradelistFlag ) && ( upgFlag ) )
						{
							( ( ModelVersionsForUPG ) modelVersionList.get( modelVersionList.size() - 1 ) ).setMaxversion( upgMaxVersion );
						}
						else
						{
							setMaxVersion( upgMaxVersion );
						}
						maxVersionFlag = false;
					}

					if ( modelFlag )
					{
						String upgFamily = FirmwareConfiguration.this.convertModel( new String( ch, start, length ) );
						if ( ( upgradelistFlag ) && ( upgFlag ) )
						{
							( ( ModelVersionsForUPG ) modelVersionList.get( modelVersionList.size() - 1 ) ).setFamily( upgFamily );
						}
						else
						{
							setFamily( upgFamily );
						}
						modelFlag = false;
					}

					if ( typeFlag )
					{
						String upgModel = FirmwareConfiguration.this.convertModel( new String( ch, start, length ) );
						if ( ( upgradelistFlag ) && ( upgFlag ) )
						{
							( ( ModelVersionsForUPG ) modelVersionList.get( modelVersionList.size() - 1 ) ).setModel( upgModel );
						}
						else
						{
							setModel( upgModel );
						}
						typeFlag = false;
					}

					if ( filetypeFlag )
					{
						setFiletype( new String( ch, start, length ) );
						filetypeFlag = false;
					}

					if ( displayversionFlag )
					{
						setDisplayversion( new String( ch, start, length ) );
						displayversionFlag = false;
					}

					if ( versionlistFlag )
					{
						setVersionlist( new String( ch, start, length ) );
						versionlistFlag = false;
					}

					if ( agentVersionFlag )
					{
						setAgentVersion( new String( ch, start, length ) );
						agentVersionFlag = false;
					}

					if ( restartFlag )
					{
						setRestart( new String( ch, start, length ) );
						restartFlag = false;
					}

					if ( ccmFlag )
					{
						if ( manufacturerIdFlag )
						{
							setManufacturerId( new String( ch, start, length ) );
							manufacturerIdFlag = false;
						}

						if ( deviceFlag )
						{
							if ( modelNameFlag )
							{
								( ( ChannelDeviceModel ) channelModelList.get( channelModelList.size() - 1 ) ).setModelName( new String( ch, start, length ) );
								modelNameFlag = false;
							}
							if ( modelIdFlag )
							{
								( ( ChannelDeviceModel ) channelModelList.get( channelModelList.size() - 1 ) ).setModelId( new String( ch, start, length ) );
								modelIdFlag = false;
							}
							if ( submodelIdFlag )
							{
								( ( ChannelDeviceModel ) channelModelList.get( channelModelList.size() - 1 ) ).setSubmodelId( new String( ch, start, length ) );
								submodelIdFlag = false;
							}

						}
					}
				}
			};
			saxParser.parse( inputStream, handler );
		}
		catch ( Exception localException )
		{
		}

		return upgradeElement;
	}

	public String getFileName()
	{
		return fileName;
	}

	public String getVersion()
	{
		return version;
	}

	public String getMinVersion()
	{
		return minVersion;
	}

	public String getMaxVersion()
	{
		return maxVersion;
	}

	public String getModel()
	{
		return model;
	}

	public String getFamily()
	{
		return family;
	}

	public String getFiletype()
	{
		return filetype;
	}

	public String getDisplayversion()
	{
		return displayversion;
	}

	public String getVersionlist()
	{
		return versionlist;
	}

	public String getAgentVersion()
	{
		return agentversion;
	}

	public String getUpgradeListString()
	{
		StringBuilder sb = new StringBuilder();
		boolean needSeparator = false;
		for ( ModelVersionsForUPG upg : modelVersionList )
		{
			if ( needSeparator )
			{
				sb.append( ";" );
			}
			else
			{
				needSeparator = true;
			}
			sb.append( CoreJsonSerializer.toJson( upg ) );
		}

		return sb.toString();
	}

	public String getRestart()
	{
		return restart;
	}

	public String getManufacturerId()
	{
		return manufacturerId;
	}

	public String getCCMDeviceModels()
	{
		if ( channelModelList.isEmpty() )
		{
			return null;
		}
		return CoreJsonSerializer.toJson( channelModelList );
	}

	public void setFileName( String fileName )
	{
		this.fileName = fileName;
	}

	public void setVersion( String version )
	{
		this.version = version;
	}

	public void setMinVersion( String minVersion )
	{
		this.minVersion = minVersion.trim();
	}

	public void setMaxVersion( String maxVersion )
	{
		this.maxVersion = maxVersion.trim();
	}

	public void setFamily( String family )
	{
		this.family = family;
	}

	private String convertModel( String model )
	{
		StringBuilder modelDecimal = new StringBuilder( "" );
		model = model.trim();
		if ( model.indexOf( ',' ) > 0 )
		{
			for ( String retval : model.split( "," ) )
			{
				if ( modelDecimal.length() >= 1 )
				{
					modelDecimal.append( "," );
				}

				if ( retval.indexOf( '-' ) > 0 )
				{
					String[] retval2 = retval.split( "-" );
					modelDecimal.append( CommonUtils.toDecimalString( retval2[0] ) );
					modelDecimal.append( "-" );
					modelDecimal.append( CommonUtils.toDecimalString( retval2[1] ) );
				}
				else
				{
					modelDecimal.append( CommonUtils.toDecimalString( retval ) );
				}
			}
		}
		else if ( model.indexOf( '-' ) > 0 )
		{
			if ( modelDecimal.length() >= 1 )
			{
				modelDecimal.append( "," );
			}

			String[] retval4 = model.split( "-" );
			modelDecimal.append( CommonUtils.toDecimalString( retval4[0] ) );
			modelDecimal.append( "-" );
			modelDecimal.append( CommonUtils.toDecimalString( retval4[1] ) );
		}
		else
		{
			if ( modelDecimal.length() >= 1 )
			{
				modelDecimal.append( "," );
			}
			modelDecimal.append( CommonUtils.toDecimalString( model ) );
		}

		return modelDecimal.toString();
	}

	public void setModel( String model )
	{
		this.model = model;
	}

	public void setUpgradeElement( boolean upgradeElement )
	{
		this.upgradeElement = upgradeElement;
	}

	public void setFiletype( String filetype )
	{
		this.filetype = filetype;
	}

	public void setDisplayversion( String displayversion )
	{
		this.displayversion = displayversion;
	}

	public void setVersionlist( String versionlist )
	{
		this.versionlist = versionlist.trim();
	}

	public void setAgentVersion( String agentVersion )
	{
		agentversion = agentVersion.trim();
	}

	public void setRestart( String restart )
	{
		this.restart = restart;
	}

	public void setManufacturerId( String manufacturerId )
	{
		this.manufacturerId = manufacturerId;
	}
}

