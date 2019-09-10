package com.marchnetworks.management.instrumentation.subscription;

public class AdditionalPrefix
{
	private String prefix;

	private int references;

	public AdditionalPrefix( String prefix )
	{
		this.prefix = prefix;
		references = 1;
	}

	public AdditionalPrefix( String prefix, int references )
	{
		this.prefix = prefix;
		this.references = references;
	}

	public String getPrefix()
	{
		return prefix;
	}

	public void setPrefix( String prefix )
	{
		this.prefix = prefix;
	}

	public int getReferences()
	{
		return references;
	}

	public void setReferences( int references )
	{
		this.references = references;
	}

	public void addReference()
	{
		references += 1;
	}

	public void removeReference()
	{
		references -= 1;
	}

	public boolean isUnreferenced()
	{
		return references <= 0;
	}
}

