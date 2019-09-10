package com.marchnetworks.web.test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.marchnetworks.command.api.query.Criteria;
import com.marchnetworks.command.api.query.Restrictions;
import com.marchnetworks.command.common.device.DeviceEventsEnum;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.events.GenericDeviceAuditEvent;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.management.user.UserService;
import com.marchnetworks.server.event.EventRegistry;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet( name = "AuditLoadTest", urlPatterns = {"/AuditLoadTest"} )
public class AuditLoadTestServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		int events = 10000;
		if ( request.getParameter( "eventCount" ) != null )
		{
			events = Integer.valueOf( request.getParameter( "eventCount" ) );
		}
		List<String> auditEvents = new ArrayList<String>();
		Collections.addAll( auditEvents, DeviceEventsEnum.CHANNEL_LIVEREQUEST.getPath(), DeviceEventsEnum.CHANNEL_PTZ_CONTROL.getPath(), DeviceEventsEnum.CHANNEL_ARCHIVEREQUEST.getPath() );
		ResourceTopologyServiceIF topologyService = ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyServiceProxy_internal" );
		UserService userService = ( UserService ) ApplicationContextSupport.getBean( "userServiceProxy_internal" );
		Criteria criteria = new Criteria( DeviceResource.class );
		criteria.add( Restrictions.eq( "deviceView.registrationStatus", RegistrationStatus.REGISTERED ) );
		criteria.add( Restrictions.eq( "deviceView.parentDeviceId", null ) );
		criteria.add( Restrictions.ne( "deviceView.family", "1001" ) );
		List<Resource> rootDevices = topologyService.getResources( criteria );

		Multimap<Long, ChannelResource> channelsMap = ArrayListMultimap.create();
		for ( Iterator i$ = rootDevices.iterator(); i$.hasNext(); )
		{
			Resource rootDevice = ( Resource ) i$.next();
			for ( Resource resource : rootDevice.createResourceList() )
			{
				if ( ( resource instanceof ChannelResource ) )
				{
					channelsMap.put( rootDevice.getId(), ( ChannelResource ) resource );
				}
			}
		}

		List<MemberView> users = userService.listAllMembers();
		List<String> usersIpAddresses = new ArrayList( users.size() );
		for ( int i = 0; i < users.size(); i++ )
		{
			int ipStart = ( int ) ( Math.random() * 255.0D );
			if ( ipStart == 0 )
			{
				ipStart = 10;
			}
			int ipEnd = ( int ) ( Math.random() * 255.0D );
			if ( ipEnd == 0 )
			{
				ipEnd = 1;
			}
			String randomIp = ipStart + ".168.2." + ipEnd;
			usersIpAddresses.add( randomIp );
		}

		EventRegistry ev = ( EventRegistry ) ApplicationContextSupport.getBean( "eventRegistry" );
		StringBuilder userPairBuilder = new StringBuilder();
		for ( int i = 0; i < events; i++ )
		{
			Collections.shuffle( auditEvents );
			String event = ( String ) auditEvents.get( 0 );

			Collections.shuffle( rootDevices );
			DeviceResource rootDevice = ( DeviceResource ) rootDevices.get( 0 );

			List<ChannelResource> channels = ( List ) channelsMap.get( rootDevice.getId() );
			Collections.shuffle( channels );
			ChannelResource channel = ( ChannelResource ) channels.get( 0 );

			Collections.shuffle( users );
			Collections.shuffle( usersIpAddresses );

			Pair pair = new Pair();
			pair.setName( "details" );
			userPairBuilder.delete( 0, userPairBuilder.length() );
			userPairBuilder.append( ( ( MemberView ) users.get( 0 ) ).getName() ).append( "@" ).append( ( String ) usersIpAddresses.get( 0 ) );
			pair.setValue( userPairBuilder.toString() );
			GenericDeviceAuditEvent gda = new GenericDeviceAuditEvent( rootDevice.getDeviceId(), event, channel.getChannelView().getChannelId(), "inuse", new Pair[] {pair}, System.currentTimeMillis() );
			ev.send( gda );
		}
		PrintWriter outputWriter = response.getWriter();
		outputWriter.println( events + " channel user audits for " + rootDevices.size() + " managed devices generated." );
	}
}
