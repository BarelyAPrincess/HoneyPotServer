package io.amelia.foundation;

public interface VendorRegistrar extends RegistrarBase
{
	default String getName()
	{
		return getVendorMeta().getName();
	}

	VendorMeta getVendorMeta();
}
