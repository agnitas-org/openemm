/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.service;

import java.util.Date;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.impl.MaildropDeleteException;
import com.agnitas.mailing.autooptimization.beans.Optimization;

public interface OptimizationService {

	boolean delete(Optimization optimization) throws MaildropDeleteException;

	String findName(int optimizationId, int companyId);

	int save(Optimization optimization);

	int getOptimizationIdByFinalMailing(int finalMailingId, int companyId);

	/**
	 * Retrieve only entries created by workflow manager.
	 */
	List<Optimization> listWorkflowManaged(int workflowId, int companyID);

	/**
	 * This method wraps finishOptimizations() to ensure, that to invocations
	 * by the Quartz scheduler won't overlap.
	 */
	void finishOptimizationsSingle(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);

    List<Optimization> getAutoOptimizations(Admin admin, Date start, Date end);

	/**
	 * Return final_mailing_id for the AutoOptimization
	 */
	int getFinalMailingId(int companyId, int workflowId);
	
}
