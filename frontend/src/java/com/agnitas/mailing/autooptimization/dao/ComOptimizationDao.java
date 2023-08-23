/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

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

import com.agnitas.mailing.autooptimization.beans.ComOptimization;

public interface ComOptimizationDao {

	ComOptimization get(int optimizationID, int companyID);

    int getFinalMailingId(int companyId, int workflowId);
	
	AutoOptimizationLight getAutoOptimizationLight(int companyId, int workflowId);

	String findName(int optimizationId, int companyId);

	List<Integer> findTargetDependentAutoOptimizations(int targetGroupId, int companyId);
	
	int save(ComOptimization optimization);
	
	boolean delete(ComOptimization optimization);

	/**
	 * Retrieve all entities except ones created by workflow manager.
	 */
	List<ComOptimization> list(int campaignID, int companyID);

	/**
	 * Retrieve only entries created by workflow manager.
	 */
	List<ComOptimization> listWorkflowManaged(int workflowId, int companyID);

	Map<Integer, String> getGroups(int campaignID, int companyID , int optimizationID);
	
	Map<Integer, Integer> getDueOnDateOptimizations(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);
	
	List<ComOptimization> getDueOnThresholdOptimizationCandidates(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);
	
	int deleteByCompanyID(int companyID);
	
	int countByCompanyID(int companyID);

	List<ComOptimization> getOptimizationsForCalendar(int companyId, Date startDate, Date endDate);

	List<ComOptimization> getOptimizationsForCalendar_New(int companyId, Date startDate, Date endDate);
    
    int getOptimizationByFinalMailingId(int finalMailingId, int companyId);
}
