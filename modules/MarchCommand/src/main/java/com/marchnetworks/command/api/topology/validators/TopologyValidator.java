package com.marchnetworks.command.api.topology.validators;

import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.LinkResource;

public interface TopologyValidator
{
	void validateLinkResource( LinkResource paramLinkResource, Long paramLong ) throws TopologyException;
}
