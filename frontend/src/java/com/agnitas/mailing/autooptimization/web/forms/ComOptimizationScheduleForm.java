/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.web.forms;

import java.text.ParseException;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;

import org.agnitas.emm.core.commons.util.Constants;
import org.agnitas.emm.core.commons.util.DateUtil;
import org.agnitas.util.AgnUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import static org.agnitas.emm.core.commons.util.Constants.DATE_PATTERN_FULL;

/**
 * Form for starting an 'autooptimization'.
 * 
 */

public class ComOptimizationScheduleForm extends ActionForm {
	private static final transient Logger logger = Logger.getLogger(ComOptimizationScheduleForm.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -7524586757665417350L;

	private int optimizationID;
	private int campaignID;
	private String resultSendDateAsString;
	private String testMailingsSendDateAsString;
	

	 /**
	 * Hey, it's an 'autooptimization'. So you need its ID
	 */

	public int getOptimizationID() {
		return optimizationID;
	}

	public void setOptimizationID(int optimizationID) {
		this.optimizationID = optimizationID;
	}

	public String getResultSendDateAsString() {
		return resultSendDateAsString;
	}

	public void setResultSendDateAsString(String resultSendDateAsString) {
		this.resultSendDateAsString = resultSendDateAsString;
	}

	public String getTestMailingsSendDateAsString() {
		return testMailingsSendDateAsString;
	}

	public void setTestMailingsSendDateAsString(String testMailingsSendDateAsString) {
		this.testMailingsSendDateAsString = testMailingsSendDateAsString;
	}

	
	@Override
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {

		
		String method = mapping.getParameter() ; // struts-config.xml <action ... parameter ="method">
		String action =	request.getParameter(method);
		
		if("showSchedule".equals(action)) {
			return super.validate(mapping, request);
		}
		
		ActionErrors errors = new ActionErrors();

		if (!AgnUtils.isDateValid(resultSendDateAsString, DATE_PATTERN_FULL)) {
			errors.add( ActionMessages.GLOBAL_MESSAGE,  new ActionMessage("mailing.autooptimization.errors.resultsenddate" , Constants.DATE_PATTERN_FULL));
		}

		if (!AgnUtils.isDateValid(testMailingsSendDateAsString, DATE_PATTERN_FULL)) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.autooptimization.errors.resultsenddate" ,Constants.DATE_PATTERN_FULL));
		}

		if (!errors.isEmpty()) { // something is wrong with the supplied 'dates' , it doesn't make sense to parse dates from them.
			return errors;
		}

		Date testmailingsSenddate = null;
		Date resultSenddate = null;
		try {
			testmailingsSenddate = DateUtil.parseFullDate(testMailingsSendDateAsString);
			resultSenddate = DateUtil.parseFullDate(resultSendDateAsString);
		} catch (ParseException e) {
			logger.error("Error occured: " + e.getMessage(), e);
		}
			
		Date now = new Date();

		if (resultSenddate == null) {
			throw new RuntimeException("resultSenddate was null");
		}
		
		if (!resultSenddate.after(testmailingsSenddate)) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
					"mailing.autooptimization.errors.result_is_not_after_test"));
		}

		if (now.after(resultSenddate)) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
					"mailing.autooptimization.errors.resultsenddate_is_not_in_future"));
		}

		if (now.after(testmailingsSenddate)) {
			errors
					.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
					"mailing.autooptimization.errors.testmailingssenddate_is_not_infuture"));
		}
		return errors;
	}

	public int getCampaignID() {
		return campaignID;
	}

	public void setCampaignID(int campaignID) {
		this.campaignID = campaignID;
	}
	

}
