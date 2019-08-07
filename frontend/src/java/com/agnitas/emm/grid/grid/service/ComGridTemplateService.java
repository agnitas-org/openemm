package com.agnitas.emm.grid.grid.service;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComMailing;
import com.agnitas.emm.grid.grid.beans.ComGridTemplate;
import com.agnitas.emm.grid.grid.beans.ComTemplateSettings;
import org.agnitas.emm.core.velocity.VelocityCheck;

public interface ComGridTemplateService {

    ComGridTemplate getGridTemplate(int templateId, @VelocityCheck int companyId);

    ComTemplateSettings getGridTemplateSettings(int templateId);
    
	ComMailing createMailing(ComAdmin admin, int templateId, MailingCreationOptions options) throws Exception;
	
	void deleteRecycledChildren(@VelocityCheck int companyId);
}