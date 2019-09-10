package com.marchnetworks.management.topology;

import com.marchnetworks.command.api.query.Criteria;
import com.marchnetworks.command.common.device.data.MassRegistrationInfo;
import com.marchnetworks.command.common.topology.ResourceRootType;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.ChannelLinkResource;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DefaultRootResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.LinkResource;
import com.marchnetworks.command.common.topology.data.MapResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.ResourceAssociation;
import com.marchnetworks.command.common.topology.data.ResourceMarkForReplacement;
import com.marchnetworks.command.common.topology.data.ResourcePathNode;
import com.marchnetworks.management.topology.data.ResourceType;

import java.util.List;
import java.util.Set;

public interface ResourceTopologyServiceIF
{
	void createAssociation( ResourceAssociation paramResourceAssociation ) throws TopologyException;

	Resource createChannelLinkResource( ChannelResource paramChannelResource, Long paramLong1, Long paramLong2 ) throws TopologyException;

	MapResource createMap( MapResource paramMapResource, Long paramLong, byte[] paramArrayOfByte ) throws TopologyException;

	Resource createPersonalResource( String paramString );

	Resource createResource( Resource paramResource, Long paramLong, String paramString ) throws TopologyException;

	void createResources( Resource[] paramArrayOfResource ) throws TopologyException;

	List<Long> findAllRootDeviceResourcesIds();

	Long getAlarmSourceResourceId( Long paramLong );

	List<DeviceResource> getAllDeviceResources();

	List<Resource> getArchiverResources();

	List<String> getChannelIdsFromDevice( Long paramLong ) throws TopologyException;

	List<ChannelLinkResource> getChannelLinkResources( String paramString1, String paramString2 );

	ChannelResource getChannelResource( String paramString1, String paramString2 );

	ChannelResource getChannelResource( Long paramLong, String paramString );

	Long getChannelResourceId( String paramString1, String paramString2 );

	DefaultRootResource getDefaultRootResource( String paramString );

	DefaultRootResource[] getDefaultRootResources();

	DeviceResource getDeviceResource( Long paramLong );

	DeviceResource getDeviceResourceByDeviceId( String paramString );

	List<ResourcePathNode> getDeviceResourcePath( String paramString );

	List<DeviceResource> getDeviceResources() throws TopologyException;

	List<Long> getDeviceResourcesFromIdSet( Set<Long> paramSet );

	List<Resource> getFilteredResourceList( Long paramLong, Criteria paramCriteria ) throws TopologyException;

	String getFirstChannelIdFromDevice( String paramString );

	ChannelResource getFirstChannelResourceFromDevice( String paramString );

	Resource getFirstResource( Criteria paramCriteria );

	Resource getFirstResourceByRoot( ResourceRootType paramResourceRootType, Criteria paramCriteria );

	List<Resource> getGenericResources( ResourceRootType paramResourceRootType, String paramString, String... paramVarArgs ) throws TopologyException;

	List<LinkResource> getLinkResources( Long paramLong );

	Resource getResource( Long paramLong ) throws TopologyException;

	Resource getResource( Long paramLong, int paramInt ) throws TopologyException;

	Long getResourceIdByDeviceId( String paramString );

	List<ResourcePathNode> getResourcePath( Long paramLong );

	List<ResourcePathNode> getResourcePath( Long paramLong, Class<?>... paramVarArgs );

	String getResourcePathString( Resource paramResource, Long paramLong );

	String getResourcePathString( Long paramLong );

	List<Resource> getResources( Long[] paramArrayOfLong, int paramInt ) throws TopologyException;

	List<Resource> getResources( Long[] paramArrayOfLong, int paramInt, Set<Long> paramSet ) throws TopologyException;

	List<Resource> getResources( Class<?>... paramVarArgs );

	<T extends Resource> List<T> getResources( Criteria paramCriteria );

	List<Resource> getResourcesForUser( String paramString, ResourceRootType paramResourceRootType, Criteria paramCriteria, boolean paramBoolean ) throws TopologyException;

	List<Resource> getResourcesForUser( ResourceRootType paramResourceRootType, ResourceType[] paramArrayOfResourceType ) throws TopologyException;

	List<Resource> getRootResources( ResourceRootType paramResourceRootType ) throws TopologyException;

	boolean isChild( Long paramLong1, Long paramLong2 );

	boolean isOnPath( Set<Long> paramSet1, Set<Long> paramSet2 );

	void markForReplacement( ResourceMarkForReplacement paramResourceMarkForReplacement ) throws TopologyException;

	void markForReplacements( ResourceMarkForReplacement[] paramArrayOfResourceMarkForReplacement ) throws TopologyException;

	List<MassRegistrationInfo> massRegister( List<MassRegistrationInfo> paramList, String paramString, Long paramLong );

	DeviceResource registerDeviceResource( Long paramLong, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6 ) throws TopologyException;

	void removeGenericResources( String paramString ) throws TopologyException;

	boolean removeResource( Long paramLong ) throws TopologyException;

	void removeResources( Long[] paramArrayOfLong ) throws TopologyException;

	void removeResources( Long[] paramArrayOfLong, boolean paramBoolean ) throws TopologyException;

	void removeUserPersonalRoot( Resource paramResource ) throws TopologyException;

	void reregisterDeviceResource( Long paramLong, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6 ) throws TopologyException;

	void retryReplacement( Long paramLong ) throws TopologyException;

	void stopMassRegistration( Long paramLong ) throws TopologyException;

	void unregisterDeviceById( String paramString ) throws TopologyException;

	void updateAssociation( ResourceAssociation paramResourceAssociation1, ResourceAssociation paramResourceAssociation2 ) throws TopologyException;

	void updateAssociations( ResourceAssociation[] paramArrayOfResourceAssociation1, ResourceAssociation[] paramArrayOfResourceAssociation2 ) throws TopologyException;

	void updateDeviceResource( DeviceResource paramDeviceResource ) throws TopologyException;

	MapResource updateMap( MapResource paramMapResource, byte[] paramArrayOfByte ) throws TopologyException;

	Resource updateResource( Resource paramResource ) throws TopologyException;
}

