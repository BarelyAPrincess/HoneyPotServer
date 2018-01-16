package io.amelia.foundation.parcel;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a complete registration of receivers and sending available at each {@link ParcelChannel}.
 */
class ApplicationRegistration
{
	private final Map<String, Object> registered = new HashMap<>();
}
