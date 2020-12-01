/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.mailing.autooptimization.web.forms;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.mailing.autooptimization.beans.ComOptimization;
import com.agnitas.mailing.autooptimization.beans.impl.ComOptimizationImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HtmlUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

public class ComOptimizationForm extends ActionForm {
	private static final transient Logger logger = Logger.getLogger(ComOptimizationForm.class);

	/**
	 * generated serial versionUID
	 */
	private static final long serialVersionUID = 2832237399927441262L;

	private static final Pattern THRESHOLD_PATTERN = Pattern.compile("[0-9]*");

	// form is backuped by a domain object
	private ComOptimization optimization = new ComOptimizationImpl();

	// state used for gui, handles problem of a really new optimization or
	// errors which occurred while saving the optimization
	private boolean newOptimization = true;

	private String thresholdString;

	private String resultSendDateAsString;
	private String testMailingsSendDateAsString;
    private String targetExpression;
    private int targetMode;
    private int splitSize;
    private String reportUrl;
    private int previousAction;

	@Override
	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {

		String method = mapping.getParameter(); // struts-config.xml <action ...
												// parameter ="method">
		String action = request.getParameter(method);

		ActionErrors errors = new ActionErrors();

		if ("save".equals(action)) {
			// validate the shortname of the optimization
			String regex = "[0-9A-Za-z]{3,}";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(optimization.getShortname());

			if (!matcher.find()) {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						"mailing.autooptimization.errors.noshortname"));
			}

			// groups -> testmailings
			int[] groups = { optimization.getGroup1(),
					optimization.getGroup2(), optimization.getGroup3(),
					optimization.getGroup4(), optimization.getGroup5() };
			// there must be at least 2 groups be selected, we count the groups
			// which are gt 0
			int numberofSelectedGroups = 0;
			for ( int i = 0; i < getSplitSize(); i++) {
				int group = groups[i];
				
				if (group > 0) {
					numberofSelectedGroups++;
				} else {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
							"mailing.autooptimization.errors.unsetgroup"));
					break;
				}
			}

			if (numberofSelectedGroups < 2) {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						"mailing.autooptimization.errors.numberofgroups"));
			}

			// compare the groups ( = mailingIDs ), no double entries are
			// allowed
			Set<Integer> groupsSet = new HashSet<>();

			for ( int i = 0; i < getSplitSize(); i++) {
				int group = groups[i];
				
				if (group > 0) {
					if (!groupsSet.add(group)) {
						errors
								.add(
										ActionMessages.GLOBAL_MESSAGE,
										new ActionMessage(
												"mailing.autooptimization.errors.groupsareidentically"));
						break;
					}
				}
			}

			// validate the threshold
			Matcher thresholdMatcher = THRESHOLD_PATTERN.matcher(StringUtils.defaultString(thresholdString));

			if (!thresholdMatcher.matches()) {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.autooptimization.errors.threshold"));
			}
		}
		
		if( "schedule".equals(action)) {
			ComAdmin admin = AgnUtils.getAdmin(request);
			if (!AgnUtils.isDateValid(resultSendDateAsString, admin.getDateTimeFormat().toPattern())) {
				errors.add( ActionMessages.GLOBAL_MESSAGE,  new ActionMessage("mailing.autooptimization.errors.resultsenddate", admin.getDateTimeFormat().toPattern()));
			}

			if (!AgnUtils.isDateValid(testMailingsSendDateAsString, admin.getDateTimeFormat().toPattern())) {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.autooptimization.errors.resultsenddate", admin.getDateTimeFormat().toPattern()));
			}

			if (!errors.isEmpty()) { // something is wrong with the supplied 'dates' , it doesn't make sense to parse dates from them.
				return errors;
			}

			Date testmailingsSenddate = null;
			Date resultSenddate = null;
			try {
				testmailingsSenddate = admin.getDateTimeFormat().parse(testMailingsSendDateAsString);
				resultSenddate = admin.getDateTimeFormat().parse(resultSendDateAsString);
			} catch (ParseException e) {
				logger.error("Error occurred: " + e.getMessage(), e);
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
		}

		return errors;

	}

	public String getDescription() {
		return optimization.getDescription();
	}

	public void setDescription(String description) {
		optimization.setDescription(description);
	}

	public String getShortname() {
		return optimization.getShortname();
	}

	public void setShortname(String shortname) {
		optimization.setShortname(shortname);
	}

	public int getCampaignID() {
		return optimization.getCampaignID();
	}

	public void setCampaignID(int campaignID) {
		optimization.setCampaignID(campaignID);
	}

	public int getCompanyID() {
		return optimization.getCompanyID();
	}

	public void setCompanyID(@VelocityCheck int companyID) {
		optimization.setCompanyID(companyID);
	}

	public String getSplitType() {
		return optimization.getSplitType();
	}

	public void setSplitType(String splitType) {
		optimization.setSplitType(splitType);
	}

	public String getEvalType() {
        return optimization.getEvalType() != null ? optimization.getEvalType().name():"";
	}

	public void setEvalType(String evalType) {
		optimization.setEvalType(HtmlUtils.parseEnumSafe(WorkflowDecision.WorkflowAutoOptimizationCriteria.class, evalType));
	}

	public int getGroup1() {
		return optimization.getGroup1();
	}

	public void setGroup1(int group1) {
		optimization.setGroup1(group1);
	}

	public int getGroup2() {
		return optimization.getGroup2();
	}

	public void setGroup2(int group2) {
		optimization.setGroup2(group2);
	}

	public int getGroup3() {
		return optimization.getGroup3();
	}

	public void setGroup3(int group3) {
		optimization.setGroup3(group3);
	}

	public int getGroup4() {
		return optimization.getGroup4();
	}

	public void setGroup4(int group4) {
		optimization.setGroup4(group4);
	}

	public int getGroup5() {
		return optimization.getGroup5();
	}

	public void setGroup5(int group5) {
		optimization.setGroup5(group5);
	}

	public int getResultMailingID() {
		return optimization.getResultMailingID();
	}

	public void setResultMailingID(int resultMailingID) {
		optimization.setResultMailingID(resultMailingID);
	}

	public int getOptimizationID() {
		return optimization.getId();
	}

	public void setOptimizationID(int optimizationID) {
		optimization.setId(optimizationID);
	}

	public int getTargetID() {
		return optimization.getTargetID();
	}

	public void setTargetID(int targetID) {
		optimization.setTargetID(targetID);
	}

	public int getMailinglistID() {
		return optimization.getMailinglistID();
	}

	public void setMailinglistID(int mailinglistID) {
		optimization.setMailinglistID(mailinglistID);
	}

	public int getStatus() {
		return optimization.getStatus();
	}

	public void setStatus(int status) {
		optimization.setStatus(status);
	}

	public ComOptimization getOptimization() {
		return optimization;
	}

	public void setOptimization(ComOptimization optimization) {
		this.optimization = optimization;
	}

	public boolean isNewOptimization() {
		return newOptimization;
	}

	public void setNewOptimization(boolean newOptimization) {
		this.newOptimization = newOptimization;
	}

	public String getThresholdString() {
		return thresholdString;
	}

	public void setThresholdString(String thresholdString) {
		this.thresholdString = thresholdString;
	}

	public boolean isDoublechecking() {
		return optimization.isDoubleCheckingActivated();
	}

	public void setDoublechecking(boolean activated) {
		optimization.setDoubleCheckingActivated(activated);
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

    public String getTargetExpression() {
        return targetExpression;
    }

    public void setTargetExpression(String targetExpression) {
        this.targetExpression = targetExpression;
    }

    public int getTargetMode() {
        return targetMode;
    }

    public void setTargetMode(int targetMode) {
        this.targetMode = targetMode;
    }
    
    public int getSplitSize() {
    	return this.splitSize;
    }
    
    public void setSplitSize( int splitSize) {
    	this.splitSize = splitSize;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }

    public int getPreviousAction() {
        return previousAction;
    }

    public void setPreviousAction(int previousAction) {
        this.previousAction = previousAction;
    }
}
