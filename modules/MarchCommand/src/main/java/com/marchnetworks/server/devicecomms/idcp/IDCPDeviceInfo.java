package com.marchnetworks.server.devicecomms.idcp;

import com.marchnetworks.management.communications.NetworkConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IDCPDeviceInfo
{
	private Map<String, String> modelInfoMap = new HashMap();
	private Map<String, String> nameInfoMap = new HashMap();
	private Map<String, String> netConfigInfoMap = new HashMap();
	private Map<String, String> versionInfoMap = new HashMap();
	private List<NetworkConfiguration> extendedNetConfigList = new ArrayList();

	public Map<String, String> getModelInfoMap()
	{
		return modelInfoMap;
	}

	public Map<String, String> getNameInfoMap()
	{
		return nameInfoMap;
	}

	public Map<String, String> getNetConfigInfoMap()
	{
		return netConfigInfoMap;
	}

	public Map<String, String> getVersionInfoMap()
	{
		return versionInfoMap;
	}

	public List<NetworkConfiguration> getExtendedNetConfigList()
	{
		return extendedNetConfigList;
	}
}

