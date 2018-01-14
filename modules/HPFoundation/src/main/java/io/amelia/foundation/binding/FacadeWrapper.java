package io.amelia.foundation.binding;

import java.util.HashMap;
import java.util.Map;

import io.amelia.foundation.facades.FacadePriority;
import io.amelia.foundation.facades.FacadeService;

class FacadeWrapper
{
	final Map<FacadePriority, FacadeService> registeredFacades = new HashMap<>();
}
