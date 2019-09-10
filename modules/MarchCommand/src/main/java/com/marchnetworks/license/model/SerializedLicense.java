package com.marchnetworks.license.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SerializedLicense
{
	public AppLicenseType type;
	public String feature;
	public Expiry expiry;
	public boolean commercial;
	public boolean open;
	public long start;
	public long end;
	public int count;
	public ApplicationIdentityToken identity;
	public String serverId;
	public List<String> licenseIds = new ArrayList();
	public Set<Long> resources = new LinkedHashSet();
	public LicenseStatus status;
	public List<String> resourceTypes = new ArrayList();
}
