/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

/**
 * Title:        Optimization
 * Copyright:    Copyright (c) AGNITAS AG
 * Company:      AGNITAS AG
 */

package com.agnitas.mailing.autooptimization.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.mailing.autooptimization.beans.impl.AutoOptimizationLight;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.mailing.autooptimization.beans.ComOptimization;

public interface ComOptimizationDao {

	ComOptimization get(int optimizationID, @VelocityCheck int companyID);

	/**
	 * Returns final_mailing_id for the AutoOptimization which contains mailing with ID = oneOfTheSplitMailingID.
	 * @param oneOfTheSplitMailingID one of the mailing for AutoOptimization, in auto_optimization_tbl this is group1_id or group2_id...
	 * @return final mailing ID
	 */
    int getFinalMailingID(@VelocityCheck int companyID, int workflowID, int oneOfTheSplitMailingID);
    int getFinalMailingId(@VelocityCheck int companyId, int workflowId);
	
	AutoOptimizationLight getAutoOptimizationLight(@VelocityCheck int companyId, int workflowId);
	
	int save(ComOptimization optimization);
	
	boolean delete(ComOptimization optimization);

	/**
	 * Retrieve all entities except ones created by workflow manager.
	 * @param campaignID
	 * @param companyID
	 * @return
	 */
	List<ComOptimization> list(int campaignID, @VelocityCheck int companyID);

	/**
	 * Retrieve only entries created by workflow manager.
	 * @param workflowId
	 * @param companyID
	 * @return
	 */
	List<ComOptimization> listWorkflowManaged(int workflowId, @VelocityCheck int companyID);

	Map<Integer, String> getGroups(int campaignID, @VelocityCheck int companyID , int optimizationID);
	
	Map<Integer, Integer> getDueOnDateOptimizations(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);
	
	List<ComOptimization> getDueOnThresholdOptimizationCandidates(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);
	
	int deleteByCompanyID(@VelocityCheck int companyID);
	
	int countByCompanyID(@VelocityCheck int companyID);

	List<ComOptimization> getOptimizationsForCalendar(@VelocityCheck int companyId, Date startDate, Date endDate);

	List<ComOptimization> getOptimizationsForCalendar_New(@VelocityCheck int companyId, Date startDate, Date endDate);
    
    int getOptimizationByFinalMailingId(int finalMailingId, @VelocityCheck int companyId);
}
