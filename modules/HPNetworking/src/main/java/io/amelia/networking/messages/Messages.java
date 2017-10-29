package io.amelia.networking.messages;

import java.util.function.Supplier;

public class Messages
{
	public static final Supplier<ApplicationInfoMessage> MESSAGE_APPLICATION_INFO = ApplicationInfoMessage::new;

	private Messages()
	{
		// Static Class
	}
}
