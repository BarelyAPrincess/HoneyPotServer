package com.marchnetworks.esm.common.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.esm.common.model.MemberEntity;

import java.util.List;

public interface MemberDAO extends GenericDAO<MemberEntity, Long>
{
	MemberEntity findMemberByName( String paramString );

	MemberEntity deleteMember( MemberEntity paramMemberEntity );

	List<MemberEntity> findGroupByName( List<String> paramList );

	List<MemberEntity> findAllMembersByRootResource( Long paramLong );

	List<MemberEntity> findAllMembersByProfileId( Long paramLong );

	List<MemberEntity> findAllMembersByProfileIdsDetached( List<Long> paramList );

	List<String> findMembersNames( String... paramVarArgs );

	List<MemberEntity> findGroupsByIds( List<Long> paramList );

	List<MemberEntity> findByGroupId( Long paramLong );

	List<MemberEntity> findAllMembersWithPersonalResource();
}
