package com.marchnetworks.license.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.license.model.AppLicenseEntity;

import java.util.List;

public interface AppLicenseDAO extends GenericDAO<AppLicenseEntity, Long>
{
	AppLicenseEntity findOneByAppId( String paramString );

	List<AppLicenseEntity> findAllByAppId( String paramString );

	AppLicenseEntity findByLicenseId( String paramString );

	List<AppLicenseEntity> findAllExcludeId( Long paramLong );
}
