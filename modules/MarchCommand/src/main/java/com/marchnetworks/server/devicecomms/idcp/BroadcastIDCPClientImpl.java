package com.marchnetworks.server.devicecomms.idcp;

import com.marchnetworks.common.certification.Utils;
import com.marchnetworks.management.communications.NetworkConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class BroadcastIDCPClientImpl implements IDCPClient
{
	private static final Logger LOG = LoggerFactory.getLogger( BroadcastIDCPClientImpl.class );

	private static final BroadcastIDCPClientImpl INSTANCE = new BroadcastIDCPClientImpl();

	private static final int IDCP_MARKER = 1229210448;

	private int idcpPortNumber;
	private int receiveTimeout;
	private String idcpBroadcastAddress;
	private Map<String, IDCPDeviceInfo> discoveredDevicesInfoCache = new HashMap();

	private DatagramSocket receiveSocket;

	private DatagramSocket socket;

	private InetAddress[] addresses;
	private InetAddress broadcastAddress;

	public static BroadcastIDCPClientImpl getInstance()
	{
		return INSTANCE;
	}

	private void init()
	{
		try
		{
			broadcastAddress = InetAddress.getByName( idcpBroadcastAddress );

			ArrayList<InetAddress> inetAddresses = new ArrayList();
			Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
			while ( nics.hasMoreElements() )
			{
				NetworkInterface aNic = ( NetworkInterface ) nics.nextElement();
				Enumeration<InetAddress> tempAddresses = aNic.getInetAddresses();
				while ( tempAddresses.hasMoreElements() )
				{
					InetAddress tempAddress = ( InetAddress ) tempAddresses.nextElement();
					if ( !tempAddress.toString().contains( ":" ) )
					{
						inetAddresses.add( tempAddress );
					}
				}
			}

			addresses = new InetAddress[inetAddresses.size()];
			for ( int i = 0; i < inetAddresses.size(); i++ )
			{
				addresses[i] = ( ( InetAddress ) inetAddresses.get( i ) );
			}
		}
		catch ( IOException e )
		{
			LOG.warn( "Failed to initialize MultiCastIDCPClient " + e + " " );
		}
	}

	public Map<String, IDCPDeviceInfo> discoverDevices()
	{
		init();
		Map<String, IDCPDeviceInfo> discoveredDevicesInfo = new HashMap();

		if ( addresses != null )
		{
			try
			{
				discoveredDevicesInfo.putAll( getDeviceMap() );
			}
			catch ( DiscovererBusyException e )
			{
				return discoveredDevicesInfoCache;
			}
		}

		updateDiscoverCache( discoveredDevicesInfo );
		return discoveredDevicesInfo;
	}

	private void updateDiscoverCache( Map<String, IDCPDeviceInfo> discoverDataMap )
	{
		discoveredDevicesInfoCache.clear();
		discoveredDevicesInfoCache.putAll( discoverDataMap );
	}

	private Map<String, IDCPDeviceInfo> getDeviceMap() throws DiscovererBusyException
	{
		byte[] bufferPacket = createMulticastPacketData();
		DatagramPacket packet = new DatagramPacket( bufferPacket, bufferPacket.length, broadcastAddress, idcpPortNumber );
		for ( int i = 0; i < addresses.length; i++ )
		{
			try
			{
				socket = new DatagramSocket( idcpPortNumber, addresses[i] );
				socket.setBroadcast( true );
				socket.send( packet );
			}
			catch ( IOException e )
			{
				LOG.warn( "Unable to send Ident packet on UDP port. Port in use or Network problem. Sending cached discover to client." );
				throw new DiscovererBusyException();
			}
			finally
			{
				socket.close();
			}
		}
		try
		{
			receiveSocket = new DatagramSocket( idcpPortNumber );
			receiveSocket.setSoTimeout( receiveTimeout );
			receiveSocket.setReceiveBufferSize( 1400 );
		}
		catch ( SocketException se )
		{
			LOG.warn( "Unable to create socket on UDP port. Port in use or Network problem. Sending cached discover to client." );
			throw new DiscovererBusyException();
		}

		List<byte[]> responseList = new ArrayList();
		try
		{
			for ( int i = 0; i < 1000; i++ )
			{
				byte[] receivingPacket = new byte['ո'];
				packet = new DatagramPacket( receivingPacket, receivingPacket.length );
				receiveSocket.receive( packet );
				responseList.add( packet.getData() );
			}
		}
		catch ( SocketTimeoutException ste )
		{
			LOG.info( "Socket Timeout reached. Stoping listening for responses." );
		}
		catch ( IOException e )
		{
			LOG.warn( "I/O error when trying to receive packets on UDP port. Sending cached discover to client." );
			throw new DiscovererBusyException();
		}
		finally
		{
			receiveSocket.close();
		}
		LOG.debug( "Received " + responseList.size() + " responses." );
		Map<String, IDCPDeviceInfo> tempDiscoverInfoMap = new HashMap();
		int keyIndex = 1;
		for ( byte[] responseData : responseList )
		{
			IDCPDeviceInfo discoveredInfo = parseResponseDataPacket( responseData );

			if ( discoveredInfo.getModelInfoMap().get( "modelInfo" ) != null )
			{
				tempDiscoverInfoMap.put( String.valueOf( keyIndex ), discoveredInfo );
				keyIndex++;
			}
		}
		return tempDiscoverInfoMap;
	}

	private IDCPDeviceInfo parseResponseDataPacket( byte[] data )
	{
		ByteBuffer bb = getLittleEndianByteBuffer( data.length );
		IDCPDeviceInfo deviceInfo = new IDCPDeviceInfo();

		int readIndex = 28;
		int extDataFields = 0;
		try
		{
			bb = ByteBuffer.wrap( data );
			extDataFields = Integer.reverseBytes( bb.getInt( 24 ) );
			LOG.info( "Nr. of Ext. Data Packets:" + extDataFields );

			int dataFieldType = -1;
			int dataFieldSize = 0;
			bb.position( readIndex );
			for ( int i = 0; i < extDataFields; i++ )
			{
				dataFieldType = Short.reverseBytes( bb.getShort() );
				dataFieldSize = Short.reverseBytes( bb.getShort() );
				byte[] dataFieldData = new byte[dataFieldSize];
				bb.get( dataFieldData, 0, dataFieldSize );

				checkExtendedFieldType( deviceInfo, dataFieldType, dataFieldData );
			}
		}
		catch ( IndexOutOfBoundsException e )
		{
			LOG.warn( "Response packet in wrong format" );
		}

		return deviceInfo;
	}

	private void checkExtendedFieldType( IDCPDeviceInfo discoveredDeviceInfo, int dataFieldType, byte[] fieldData )
	{
		IDCPExtendedDataTypes identifiedType = IDCPExtendedDataTypes.fromTypeValue( dataFieldType );
		if ( identifiedType != null )
		{
			LOG.info( identifiedType.name() );

			switch ( identifiedType )
			{
				case IDCP_EXTTYPE_MODEL:
					List<String> modelData = parseModelData( fieldData );
					discoveredDeviceInfo.getModelInfoMap().put( "modelInfo", modelData.get( 0 ) );
					discoveredDeviceInfo.getModelInfoMap().put( "modelNameInfo", modelData.get( 1 ) );
					discoveredDeviceInfo.getModelInfoMap().put( "submodelInfo", modelData.get( 2 ) );
					discoveredDeviceInfo.getModelInfoMap().put( "submodelNameInfo", modelData.get( 3 ) );
					break;

				case IDCP_EXTTYPE_NAME:
					String nameData = parseNameData( fieldData );
					if ( nameData != null )
					{
						discoveredDeviceInfo.getNameInfoMap().put( "nameInfo", nameData );
					}
					break;
				case IDCP_EXTTYPE_NETCFG:
					List<String> netConfigData = parseNetConfigData( fieldData );
					discoveredDeviceInfo.getNetConfigInfoMap().put( "netMacAddressInfo", netConfigData.get( 0 ) );
					discoveredDeviceInfo.getNetConfigInfoMap().put( "netIPAddressInfo", netConfigData.get( 1 ) );
					break;

				case IDCP_EXTTYPE_NETCFG_EXT_IP_LIST:
					List<NetworkConfiguration> extendedNetConfig = parseExtendedNetworkConfigurationData( fieldData );
					discoveredDeviceInfo.getExtendedNetConfigList().addAll( extendedNetConfig );
					break;

				case IDCP_EXTTYPE_VERSION:
					String versionData = parseSoftwareVersionData( fieldData );
					if ( versionData != null )
						discoveredDeviceInfo.getVersionInfoMap().put( "versionInfo", versionData );
					break;
				case IDCP_EXTTYPE_NAME_EXT_FRIENDLY:
					LOG.info( "Type not desired. Ignoring... Type Number:" + dataFieldType );
					break;
				case IDCP_EXTTYPE_NETCFG_EXT:
					LOG.info( "Type not desired. Ignoring... Type Number:" + dataFieldType );
					break;
			}

		}
	}

	private List<String> parseModelData( byte[] fieldData )
	{
		List<String> modelInfo = new ArrayList();
		try
		{
			ByteBuffer bb = getLittleEndianByteBuffer( fieldData.length );
			bb = ByteBuffer.wrap( fieldData );
			int modelValue = Short.reverseBytes( bb.getShort() );
			int subModelValue = Short.reverseBytes( bb.getShort() );

			modelInfo.add( String.valueOf( modelValue ) );
			modelInfo.add( IDCPFamilyModelConstants.getFamilyName( modelValue ) );
			modelInfo.add( String.valueOf( subModelValue ) );
			modelInfo.add( IDCPFamilyModelConstants.getModelName( modelValue, subModelValue ) );

			LOG.debug( "Family Id/Family Name:" + ( String ) modelInfo.get( 0 ) + "/" + ( String ) modelInfo.get( 1 ) );
			LOG.debug( "Model Id/Model name:" + ( String ) modelInfo.get( 2 ) + "/" + ( String ) modelInfo.get( 3 ) );
		}
		catch ( BufferUnderflowException bue )
		{
			LOG.info( "End of Device Model Data info." );
		}
		catch ( IndexOutOfBoundsException e )
		{
			LOG.warn( "Model Data Response packet in wrong format" );
		}

		return modelInfo;
	}

	private String parseNameData( byte[] fieldData )
	{
		String nameInfo = null;
		try
		{
			ByteBuffer bb = getLittleEndianByteBuffer( fieldData.length );
			bb = ByteBuffer.wrap( fieldData );

			byte[] trimmedData = new byte[0];
			for ( int i = 0; i < bb.array().length; i++ )
			{
				byte b = bb.array()[i];
				if ( b == Byte.parseByte( "0" ) )
				{
					trimmedData = new byte[i];
					bb.get( trimmedData, 0, i );
					break;
				}
			}

			nameInfo = new String( trimmedData );
			LOG.info( "Device Name:" + nameInfo );
		}
		catch ( BufferUnderflowException bue )
		{
			LOG.info( "End of Device Model Data info." );
		}
		catch ( IndexOutOfBoundsException e )
		{
			LOG.warn( "Name Data Response packet in wrong format" );
		}
		return nameInfo;
	}

	private List<String> parseNetConfigData( byte[] fieldData )
	{
		List<String> netConfigInfo = new ArrayList();
		try
		{
			ByteBuffer bb = getLittleEndianByteBuffer( fieldData.length );
			bb = ByteBuffer.wrap( fieldData );

			byte[] macAddress = new byte[6];
			bb.get( macAddress, 0, 6 );
			bb.position( 8 );

			StringBuilder sb = new StringBuilder( 18 );
			for ( byte b : macAddress )
			{
				if ( sb.length() > 0 )
					sb.append( ':' );
				sb.append( String.format( "%02x", new Object[] {Byte.valueOf( b )} ) );
			}
			netConfigInfo.add( sb.toString() );

			byte[] ipAddress = new byte[4];
			bb.get( ipAddress, 0, 4 );
			netConfigInfo.add( Utils.ipOctets2String( ipAddress ) );

			LOG.info( "Mac Address:" + ( String ) netConfigInfo.get( 0 ) + "Ip Address:" + ( String ) netConfigInfo.get( 1 ) );
		}
		catch ( BufferUnderflowException bue )
		{
			LOG.info( "End of Net Config info." );
		}
		catch ( IndexOutOfBoundsException e )
		{
			LOG.warn( "Net Config Response packet in wrong format" );
		}
		return netConfigInfo;
	}

	private String parseSoftwareVersionData( byte[] fieldData )
	{
		String swInfo = null;
		try
		{
			ByteBuffer bb = getLittleEndianByteBuffer( fieldData.length );
			bb = ByteBuffer.wrap( fieldData );

			byte[] trimmedData = new byte[0];
			for ( int i = 0; i < bb.array().length; i++ )
			{
				byte b = bb.array()[i];
				if ( b == Byte.parseByte( "0" ) )
				{
					trimmedData = new byte[i];
					bb.get( trimmedData, 0, i );
					break;
				}
			}

			swInfo = new String( trimmedData );
			LOG.info( "Software Version Info:" + swInfo );
		}
		catch ( BufferUnderflowException bue )
		{
			LOG.info( "End of Software Version info." );
		}
		catch ( IndexOutOfBoundsException e )
		{
			LOG.warn( "Software Data Response Packet in wrong format" );
		}
		return swInfo;
	}

	private List<NetworkConfiguration> parseExtendedNetworkConfigurationData( byte[] fieldData )
	{
		List<NetworkConfiguration> extendedNetConfList = new ArrayList();
		try
		{
			ByteBuffer bb = getLittleEndianByteBuffer( fieldData.length );
			bb = ByteBuffer.wrap( fieldData );

			while ( bb.hasRemaining() )
			{
				byte[] ipAddress = new byte[4];
				bb.get( ipAddress );

				byte[] netMask = new byte[4];
				bb.get( netMask );

				String sip = Utils.ipOctets2String( ipAddress );
				LOG.info( "Extended Network Configuration IP address: " + sip );
				extendedNetConfList.add( new NetworkConfiguration( sip, null ) );
			}
		}
		catch ( BufferUnderflowException bue )
		{
			LOG.info( "End of Extended Network Configuration info." );
		}
		catch ( IndexOutOfBoundsException e )
		{
			LOG.warn( "Extended Network Configuration Data Response Packet in wrong format" );
		}

		return extendedNetConfList;
	}

	private ByteBuffer getLittleEndianByteBuffer( int size )
	{
		ByteBuffer bb = ByteBuffer.allocate( size );
		bb.order( ByteOrder.LITTLE_ENDIAN );
		return bb;
	}

	private byte[] createMulticastPacketData()
	{
		ByteBuffer bb = getLittleEndianByteBuffer( 28 );

		bb.putInt( 1229210448 );
		bb.putInt( 0 );
		bb.put( getHostMACAdress() );
		bb.putShort( Short.valueOf( "0" ).shortValue() );
		bb.putInt( 0 );
		bb.putInt( 0 );
		bb.putInt( 0 );

		return bb.array();
	}

	private byte[] getHostMACAdress()
	{
		byte[] hostMac = new byte[6];
		try
		{
			InetAddress hostAddress = InetAddress.getByName( InetAddress.getLocalHost().getHostName() );
			NetworkInterface ni = NetworkInterface.getByInetAddress( hostAddress );
			if ( ni != null )
			{
				hostMac = ni.getHardwareAddress();
				LOG.debug( "Got Host Address" );
			}
		}
		catch ( UnknownHostException e )
		{
			LOG.info( "Unable to get Host Address. Will not be able to use MAC address on IDCP Ident." );
		}
		catch ( SocketException se )
		{
			LOG.info( "Unable to get Network Interface info from Host. Will not be able to use MAC address on IDCP Ident." );
		}

		return hostMac;
	}

	public void setIdcpPortNumber( int idcpPortNumber )
	{
		this.idcpPortNumber = idcpPortNumber;
	}

	public void setIdcpBroadcastAddress( String idcpBroadcastAddress )
	{
		this.idcpBroadcastAddress = idcpBroadcastAddress;
	}

	public void setReceiveTimeout( int receiveTimeout )
	{
		this.receiveTimeout = receiveTimeout;
	}
}

