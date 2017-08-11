package io.amelia.foundation;

public class ClassBasedRegistrar implements RegistrarBase
{
	protected Class<?> cls;

	public ClassBasedRegistrar( Class<?> cls )
	{
		this.cls = cls;
	}

	public String getName()
	{
		return cls == null ? null : cls.getSimpleName();
	}
}
