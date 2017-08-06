package io.amelia.foundation;

public abstract class VendorRegistrar extends RegistrarBase
{
	private VendorMeta meta;

	public VendorRegistrar( VendorMeta meta )
	{
		this.meta = meta;
	}

	public VendorMeta getVendorMeta()
	{
		return meta;
	}

	public String getName()
	{
		return meta.getName();
	}
}
