package com.marchnetworks.command.api.user.rightexport;

import java.util.List;

public abstract interface UserRightTemplateProvider
{
	public abstract List<UserRightExportTemplate> getUserRightTemplates();
}

