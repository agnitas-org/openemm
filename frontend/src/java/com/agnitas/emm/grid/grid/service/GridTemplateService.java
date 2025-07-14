package com.agnitas.emm.grid.grid.service;


import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.grid.grid.beans.GridTemplate;
import com.agnitas.emm.grid.grid.beans.GridTemplateSettings;

public interface GridTemplateService {

    GridTemplate getGridTemplate(int templateId, int companyId);

    GridTemplateSettings getGridTemplateSettings(int templateId, Admin admin);
    
	Mailing createMailing(Admin admin, int templateId, MailingCreationOptions options) throws Exception;
	
	void deleteRecycledChildren(int companyId);

    GridTemplate getGridTemplateWithLinks(int templateId, int companyId);
}