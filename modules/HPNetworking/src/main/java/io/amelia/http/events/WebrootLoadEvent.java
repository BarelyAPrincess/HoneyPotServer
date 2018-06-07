package io.amelia.http.events;

import io.amelia.events.Cancellable;
import io.amelia.foundation.events.ApplicationEvent;
import io.amelia.http.webroot.Webroot;

public class WebrootLoadEvent extends ApplicationEvent implements Cancellable
{
	Webroot webroot;

	public WebrootLoadEvent( Webroot webroot )
	{
		this.webroot = webroot;
	}

	public Webroot getWebroot()
	{
		return webroot;
	}
}
