package com.marchnetworks.command.api.topology;

import com.marchnetworks.command.api.query.Criteria;
import com.marchnetworks.command.common.device.data.MassRegistrationInfo;
import com.marchnetworks.command.common.topology.ResourceRootType;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.ChannelLinkResource;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.LinkResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.ResourceAssociation;
import com.marchnetworks.command.common.topology.data.ResourcePathNode;

import java.util.List;

public interface TopologyCoreService
{
	Resource createResource( Resource paramResource, Long paramLong, String paramString ) throws TopologyException;

	void createResources( Resource[] paramArrayOfResource ) throws TopologyException;

	Resource getResource( Long paramLong ) throws TopologyException;

	Resource getResource( Long paramLong, int paramInt ) throws TopologyException;

	Resource updateResource( Resource paramResource ) throws TopologyException;

	boolean removeResource( Long paramLong ) throws TopologyException;

	void removeResources( Long[] paramArrayOfLong ) throws TopologyException;

	void createAssociation( ResourceAssociation paramResourceAssociation ) throws TopologyException;

	void updateAssociation( ResourceAssociation paramResourceAssociation1, ResourceAssociation paramResourceAssociation2 ) throws TopologyException;

	void updateAssociations( ResourceAssociation[] paramArrayOfResourceAssociation1, ResourceAssociation[] paramArrayOfResourceAssociation2 ) throws TopologyException;

	DeviceResource registerDeviceResource( Long paramLong, String paramString1, String paramString2, String paramString3, String paramString4 ) throws TopologyException;

	List<Resource> getResourcesForUser( String paramString, ResourceRootType paramResourceRootType, Criteria paramCriteria, boolean paramBoolean ) throws TopologyException;

	List<Resource> getGenericResources( ResourceRootType paramResourceRootType, String paramString, String... paramVarArgs ) throws TopologyException;

	List<ChannelLinkResource> getChannelLinkResources( String paramString1, String paramString2 );

	List<Resource> getResources( Class<?>... paramVarArgs );

	<T extends Resource> List<T> getResources( Criteria paramCriteria );

	Resource getFirstResource( Criteria paramCriteria );

	List<LinkResource> getLinkResources( Long paramLong );

	List<Resource> getFilteredResourceList( Long paramLong, Criteria paramCriteria ) throws TopologyException;

	List<ResourcePathNode> createGroupResources( Long paramLong, List<String> paramList, boolean paramBoolean ) throws TopologyException;

	Long getResourceIdByDeviceId( String paramString );

	List<String> getChannelIdsFromDevice( Long paramLong ) throws TopologyException;

	boolean removeGenericResource( String paramString1, String paramString2 ) throws TopologyException;

	Resource createChannelLinkResource( ChannelResource paramChannelResource, Long paramLong1, Long paramLong2 ) throws TopologyException;

	List<Long> findUserTerritoryRootIds( String paramString, ResourceRootType paramResourceRootType ) throws TopologyException;

	boolean hasRootAccess( String paramString, ResourceRootType paramResourceRootType ) throws TopologyException;

	boolean hasResourceAccess( String paramString, Long paramLong ) throws TopologyException;

	boolean hasResourceAccess( List<Long> paramList, Long paramLong ) throws TopologyException;

	List<DeviceResource> getAllDeviceResources();

	ChannelResource getChannelResource( String paramString1, String paramString2 );

	DeviceResource getDeviceResource( Long paramLong );

	ChannelResource getChannelResource( Long paramLong, String paramString );

	List<Long> getLinkResourceIds( Long paramLong );

	String getResourcePathString( Resource paramResource, Long paramLong );

	String getResourcePathString( Long paramLong );

	List<MassRegistrationInfo> massRegister( List<MassRegistrationInfo> paramList, String paramString, Long paramLong );

	void stopMassRegistration( Long paramLong ) throws TopologyException;

	DeviceResource findDeviceByStationId( String paramString );

	List<MassRegistrationInfo> prepareMassRegister( List<MassRegistrationInfo> paramList, String paramString, Long paramLong );

	void doDeviceRegister( List<MassRegistrationInfo> paramList );

	DeviceResource getDeviceResourceByDeviceId( String paramString );

	boolean isRegisteringDevice();

	Resource getFirstResourceByRoot( ResourceRootType paramResourceRootType, Criteria paramCriteria );
}
