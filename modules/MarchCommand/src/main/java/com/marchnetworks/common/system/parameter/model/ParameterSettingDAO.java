package com.marchnetworks.common.system.parameter.model;

import com.marchnetworks.command.common.dao.GenericDAO;

import java.util.List;

public abstract interface ParameterSettingDAO extends GenericDAO<ParameterSettingEntity, String>
{
	public abstract boolean isParameterSettingEmpty();

	public abstract List<ParameterSettingEntity> findAllByName( String... paramVarArgs );
}
