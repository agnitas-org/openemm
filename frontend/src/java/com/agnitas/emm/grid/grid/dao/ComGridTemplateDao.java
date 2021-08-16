package com.agnitas.emm.grid.grid.dao;

import java.util.Set;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface ComGridTemplateDao {
	// dummy interface
	
	Set<Integer> deleteByMailingID(@VelocityCheck int companyID, int mailingID);

}
