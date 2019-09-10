package com.marchnetworks.notification.service;

import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

public abstract interface CommandMailSenderIF extends JavaMailSender
{
	public abstract void setHost( String paramString );

	public abstract void setPort( int paramInt );

	public abstract void setUsername( String paramString );

	public abstract void setPassword( String paramString );

	public abstract void setJavaMailProperties( Properties paramProperties );
}

