package com.agnitas.emm.grid.grid.service;


import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.grid.grid.beans.ComGridTemplate;
import com.agnitas.emm.grid.grid.beans.ComTemplateSettings;

public interface ComGridTemplateService {

    ComGridTemplate getGridTemplate(int templateId, int companyId);

    ComTemplateSettings getGridTemplateSettings(int templateId, Admin admin);
    
	Mailing createMailing(Admin admin, int templateId, MailingCreationOptions options) throws Exception;
	
	void deleteRecycledChildren(int companyId);

    ComGridTemplate getGridTemplateWithLinks(int templateId, int companyId);
}