package com.marchnetworks.command.spring.security;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AppAuthenticationEntryPoint implements AuthenticationEntryPoint, InitializingBean
{
	public void afterPropertiesSet() throws Exception
	{
	}

	public void commence( HttpServletRequest request, HttpServletResponse response, AuthenticationException authException ) throws IOException, ServletException
	{
		HttpServletResponse httpResponse = response;
		httpResponse.sendError( 401, authException.getMessage() );
	}
}
