package com.marchnetworks.notification.service;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.notification.data.NotificationContentFormat;
import com.marchnetworks.notification.data.NotificationMessage;
import com.marchnetworks.notification.task.MailTask;
import com.marchnetworks.shared.config.CommonConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.activation.CommandMap;
import javax.activation.DataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

public class SendNotificationServiceImpl implements SendNotificationService
{
	private static final Logger LOG = LoggerFactory.getLogger( SendNotificationServiceImpl.class );

	private static final String SMTPAUTH = "mail.smtp.auth";
	private static final String SMTPSTARTTLS = "mail.smtp.starttls.enable";
	private static final String DISABLE = "false";
	private static final String CONTENT_FORMAT_TEXT = "text/plain";
	private static final String CONTENT_FORMAT_HTML = "text/html";
	private CommandMailSenderIF mailSender;
	private CommonConfiguration configuration;
	private TaskScheduler executor;
	boolean isMailConfigured = false;
	boolean isSmsConfigured = false;

	public void setMailSender( CommandMailSenderIF mailSender )
	{
		this.mailSender = mailSender;
	}

	public void setConfiguration( CommonConfiguration configuration )
	{
		this.configuration = configuration;
	}

	public void setExecutor( TaskScheduler executor )
	{
		this.executor = executor;
	}

	void init()
	{
		String smtpServer = configuration.getProperty( ConfigProperty.SMTP_SERVER );
		if ( !CommonAppUtils.isNullOrEmptyString( smtpServer ) )
		{
			String[] SmtpServerTokens = smtpServer.split( ":" );
			mailSender.setHost( SmtpServerTokens[0] );
			if ( SmtpServerTokens.length == 2 )
			{
				try
				{
					Integer port = Integer.valueOf( SmtpServerTokens[1] );
					mailSender.setPort( port.intValue() );
				}
				catch ( NumberFormatException e )
				{
					LOG.error( "Cannot configue SMTP server, the provided port:{} is invalid.", SmtpServerTokens[1] );
					return;
				}
			}

			String username = configuration.getProperty( ConfigProperty.SMTP_USERNAME );

			if ( !CommonAppUtils.isNullOrEmptyString( username ) )
			{
				mailSender.setUsername( username );
				String password = configuration.getSmtpPasswordFromConfig();
				if ( !CommonAppUtils.isNullOrEmptyString( password ) )
				{
					mailSender.setPassword( password );
				}
			}

			Properties mailProperties = new Properties();

			String enableAuth = configuration.getProperty( ConfigProperty.SMTP_AUTHENTICATION );
			if ( enableAuth == null )
			{
				mailProperties.setProperty( "mail.smtp.auth", "false" );
			}
			else
			{
				mailProperties.setProperty( "mail.smtp.auth", enableAuth );
			}

			String enableStarttls = configuration.getProperty( ConfigProperty.SMTP_ENABLESTARTTLS );
			if ( enableStarttls == null )
			{
				mailProperties.setProperty( "mail.smtp.starttls.enable", "false" );
			}
			else
			{
				mailProperties.setProperty( "mail.smtp.starttls.enable", enableStarttls );

				mailProperties.setProperty( "mail.smtp.ssl.trust", SmtpServerTokens[0] );
			}

			mailSender.setJavaMailProperties( mailProperties );

			MailcapCommandMap mc = ( MailcapCommandMap ) CommandMap.getDefaultCommandMap();
			mc.addMailcap( "text/html;; x-java-content-handler=com.sun.mail.handlers.text_html" );
			mc.addMailcap( "text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml" );
			mc.addMailcap( "text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain" );
			mc.addMailcap( "multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed" );
			mc.addMailcap( "message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822" );

			isMailConfigured = true;
			LOG.info( "Smtp Server is ready." );
		}
		else
		{
			LOG.info( "Smtp Server is not configured." );
		}
	}

	public void doSendMessage( NotificationMessage message ) throws MailSendException
	{
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		try
		{
			if ( message.getText() != null )
			{
				if ( message.getContentFormat() == NotificationContentFormat.PLAINTEXT )
				{
					mimeMessage.setContent( message.getText(), "text/plain" );
				}
				else
				{
					mimeMessage.setContent( message.getText(), "text/html" );
				}
			}
			MimeMessageHelper helper = new MimeMessageHelper( mimeMessage, true, "utf-8" );

			helper.setFrom( configuration.getProperty( ConfigProperty.SMTP_SENDER ) );
			if ( message.getTo() == null )
			{
				LOG.warn( "Missing mail receipient, aborting sending email" );
				return;
			}
			helper.setTo( message.getTo() );
			if ( message.getCc() != null )
			{
				helper.setCc( message.getCc() );
			}
			if ( message.getBcc() != null )
			{
				helper.setBcc( message.getBcc() );
			}
			if ( message.getSubject() != null )
			{
				helper.setSubject( message.getSubject() );
			}
			if ( message.getText() != null )
			{
				helper.setText( message.getText(), true );
			}

			if ( !message.getInlines().isEmpty() )
			{
				Set<Entry<String, DataSource>> entrySet = message.getInlines().entrySet();
				for ( Entry<String, DataSource> inline : entrySet )
				{
					helper.addInline( ( String ) inline.getKey(), ( DataSource ) inline.getValue() );
				}
			}

			mailSender.send( mimeMessage );
		}
		catch ( AddressException e )
		{
			LOG.error( "Messaging Address Exception error to {} : {}", new Object[] {message.getTo(), e.getMessage()} );
		}
		catch ( MessagingException e )
		{
			LOG.error( "Messaging error to {} : {}", new Object[] {message.getTo(), e.getMessage()} );
		}
		catch ( MailSendException e )
		{
			LOG.error( "Mail Send Exception error to {} : {}", new Object[] {message.getTo(), e.getMessage()} );
			throw e;
		}
	}

	public void sendMessages( Long notificationId, List<NotificationMessage> messages )
	{
		LOG.debug( "****** Invoke NotificationManager::sendMessage" );

		if ( !isMailConfigured )
		{
			LOG.warn( "SMTP server must be configured before sending notification." );
			return;
		}

		MailTask task = new MailTask( notificationId, messages );
		executor.executeNow( task );
	}
}

