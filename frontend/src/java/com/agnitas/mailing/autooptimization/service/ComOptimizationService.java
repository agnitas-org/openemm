/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.TargetLight;
import com.agnitas.mailing.autooptimization.beans.ComOptimization;
import com.agnitas.mailing.autooptimization.beans.impl.AutoOptimizationLight;
import net.sf.json.JSONArray;
import org.agnitas.beans.impl.MaildropDeleteException;
import org.agnitas.util.beans.impl.SelectOption;

public interface ComOptimizationService {

	boolean delete(ComOptimization optimization) throws MaildropDeleteException;

	String findName(int optimizationId, int companyId);

	int save(ComOptimization optimization);

	ComOptimization get(int optimizationID, int companyID);
	
	int getOptimizationIdByFinalMailing(int finalMailingId, int companyId);

	/**
	 * Retrieve all entities except ones created by workflow manager.
	 */
	List<ComOptimization> list(int campaignID, int companyID);

	/**
	 * Retrieve only entries created by workflow manager.
	 */
	List<ComOptimization> listWorkflowManaged(int workflowId, int companyID);

	/**
	 * chooses the mailing with best open- or clickrate , clones it and sends it to the remaining recipients
	 */
	boolean finishOptimization( ComOptimization optimization ) throws Exception;

	int calculateBestMailing( ComOptimization optimization );

	/**
	 * This method wraps finishOptimizations() to ensure, that to invocations
	 * by the Quartz scheduler won't overlap.
	 */
	void finishOptimizationsSingle(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);

	void finishOptimizations(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);

	List<TargetLight> getTargetGroupList(int companyID);

	/**
	 * get a list of available splittypes ( company specific and application common definitions )
	 * @return a list of string arrays [0] = split type , [1]  = i18n-key
	 */
	List<String[]> getSplitTypeList(int companyID, String splitType, String language);

	int getSplitNumbers(int companyID, String splitType);

	List<SelectOption> getTestMailingList(ComOptimization optimization);

	/**
	 * Get the state from the optimization.
	 *
	 * ComOptimization.STATUS_NOT_STARTED :
	 * The test mailings are not scheduled, or the test mailings are scheduled and have not been created yet
	 *
 	 * ComOptimization.STATUS_EVAL_IN_PROGRESS:
 	 * The statistics for test mailings is generating ...
 	 *
	 * ComOptimization.STATUS_TEST_SEND
	 * The test mailings started to generate or have been send
	 *
	 * ComOptimization.STATUS_FINISHED
	 * The optimization process is done
	 */
	int getState(ComOptimization optimization);

	List<ComOptimization> getDueOnThresholdOptimizations(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);

    List<TargetLight> getTargets(String targetExpression, int companyID);

    List<TargetLight> getChosenTargets(String targetExpression, final int companyID);

	List<ComOptimization> getOptimizationsForCalendar(int companyId, Date startDate, Date endDate);

	JSONArray getOptimizationsAsJson(Admin admin, LocalDate startDate, LocalDate endDate, DateTimeFormatter formatter);

	/**
	 * Return final_mailing_id for the AutoOptimization
	 */
	int getFinalMailingId(int companyId, int workflowId);
	
	AutoOptimizationLight getOptimizationLight(int companyId, int workflowId);
}
