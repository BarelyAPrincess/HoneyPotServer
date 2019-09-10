package com.marchnetworks.common.diagnostics;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "DIAGNOSTIC_TEST" )
public class DiagnosticTestEntity
{
	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	@Column( name = "ID" )
	protected Long id;
	@Column( name = "VALUE" )
	protected String testValue;

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public String getTestValue()
	{
		return testValue;
	}

	public void setTestValue( String testValue )
	{
		this.testValue = testValue;
	}
}
