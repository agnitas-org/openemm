package com.agnitas.emm.grid.grid.dao;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface ComGridTemplateDao {
	// dummy interface
	
	void deleteByMailingID(@VelocityCheck int companyID, int mailingID);

}