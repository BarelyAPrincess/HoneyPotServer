package com.marchnetworks.esm.common.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.esm.common.model.ProfileEntity;

public interface ProfileDAO extends GenericDAO<ProfileEntity, Long>
{
	ProfileEntity findSuperAdminProfile();

	ProfileEntity findByName( String paramString );
}
