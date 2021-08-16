/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.web.MailingSendForm;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.mailing.bean.MailingDependentType;

public class ComMailingSendForm extends MailingSendForm {
	private static final long serialVersionUID = -2719144223604027921L;

	private String statusmailRecipients = "";
	private boolean statusmailOnErrorOnly = false;
	private String followupFor = "";
	private String followUpType = "";
	private String reportSendEmail;
	private int followUpCount;
	private int workflowId;

	private boolean reportSendAfter24h;
	private boolean reportSendAfter48h;
	private boolean reportSendAfter1Week;

	private boolean recipientReportSendSendingTime;
	private boolean recipientReportSendAfter24h;
	private boolean recipientReportSendAfter48h;
	private boolean recipientReportSendAfter1Week;

	private int adminTargetGroupID;

	private int percentsComplete;

	private int autoExportId;

	/**
	 * Holds value of property doublechecking.
	 */
	private boolean doublechecking;

	/**
	 * Holds value of property doublechecking.
	 */
	private boolean skipempty;

	/**
	 * Mailing's current workStatus
	 */
	private String workStatus = "";

	private int templateId;
	private boolean isMailingGrid = false;
	private boolean isMailingUndoAvailable;

	private Map<String, String> styles;
	List<AutoExport> autoExports;

	private String[] mailingTestRecipients;
	private String[] filterTypes;
	private List<Dependent<MailingDependentType>> dependents;

	@Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		super.reset(mapping, request);

		styles = new HashMap<>();
	}

	public int getAutoExportId() {
		return autoExportId;
	}

	public void setAutoExportId(int autoExportId) {
		this.autoExportId = autoExportId;
	}

	/**
     * Getter for property doublechecking.
     * @return Value of property doublechecking.
     */
    public boolean isDoublechecking() {
        return this.doublechecking;
    }

    /**
     * Setter for property doublechecking.
     * @param doublechecking New value of property doublechecking.
     */
    public void setDoublechecking(boolean doublechecking) {
        this.doublechecking = doublechecking;
    }

	public String	getFollowupFor()	{
		return followupFor;
	}

	public void	setFollowupFor(String followupFor)	{
		this.followupFor=followupFor;
	}

    private String maxRecipients = "0";

	private int generationOptimization;

	public String getMaxRecipients() {
		return maxRecipients;
	}

	public void setMaxRecipients(final String maxRecipients) {
		this.maxRecipients = StringUtils.isBlank(maxRecipients) ? "0" : maxRecipients;
	}

	public String getStatusmailRecipients() {
		return statusmailRecipients;
	}

	public void setStatusmailRecipients(String statusmailRecipients) {
		if (statusmailRecipients != null) {
			this.statusmailRecipients = statusmailRecipients.toLowerCase();
		} else {
			this.statusmailRecipients = statusmailRecipients;
		}
	}

	public boolean isStatusmailOnErrorOnly() {
		return statusmailOnErrorOnly;
	}

	public void setStatusmailOnErrorOnly(boolean statusmailOnErrorOnly) {
		this.statusmailOnErrorOnly = statusmailOnErrorOnly;
	}

	public boolean isSkipempty() {
		return skipempty;
	}

	public void setSkipempty(boolean skipempty) {
		this.skipempty = skipempty;
	}
	
	public String getWorkStatus() {
		return workStatus;
	}

	public void setWorkStatus(String workStatus) {
		this.workStatus = workStatus;
	}

	/**
	 * Returns the amount of mailings who will get this follow-up mailing.
	 * @return
	 */
	public int getFollowUpStat() {
		return followUpCount;
	}
	
	/**
	 * sets the amunt of mailings who will get the follow-up mailing
	 */
	public void setFollowUpStat(int followUpCount) {
		this.followUpCount = followUpCount;
	}
	
	/**
	 * returns the Follow-Up Type of this mailing. This can be
	 * clicker, non-clicker, opener, non-opener. The definition can
	 * be found in Mailing.java (e.g. FollowUpType.TYPE_FOLLOWUP_OPENER = "opener")
	 * 
	 * @return
	 */
	public String getFollowUpType() {
		return followUpType;
	}

	public String getReportSendEmail() {
		return reportSendEmail;
	}

	public void setReportSendEmail(String reportSendEmail) {
		this.reportSendEmail = reportSendEmail;
	}

	/**
	 * sets the followUp-type.
	 * possible values are "clicker", "non-clicker", "opener", "non-opener" but its better
	 * to use the constant definition from Mailing.java like
	 * FollowUpType.TYPE_FOLLOWUP_OPENER
	 * @param followUpType
	 */
	public void setFollowUpType(String followUpType) {
		this.followUpType = followUpType;
	}

	public boolean isReportSendAfter24h() {
		return reportSendAfter24h;
	}

	public void setReportSendAfter24h(boolean reportSendAfter24h) {
		this.reportSendAfter24h = reportSendAfter24h;
	}

	public boolean isReportSendAfter48h() {
		return reportSendAfter48h;
	}

	public void setReportSendAfter48h(boolean reportSendAfter48h) {
		this.reportSendAfter48h = reportSendAfter48h;
	}

	public boolean isReportSendAfter1Week() {
		return reportSendAfter1Week;
	}

	public void setReportSendAfter1Week(boolean reportSendAfter1Week) {
		this.reportSendAfter1Week = reportSendAfter1Week;
	}

	public boolean isRecipientReportSendSendingTime() {
		return recipientReportSendSendingTime;
	}

	public void setRecipientReportSendSendingTime(boolean recipientReportSendSendingTime) {
		this.recipientReportSendSendingTime = recipientReportSendSendingTime;
	}

	public boolean isRecipientReportSendAfter24h() {
		return recipientReportSendAfter24h;
	}

	public void setRecipientReportSendAfter24h(boolean recipientReportSendAfter24h) {
		this.recipientReportSendAfter24h = recipientReportSendAfter24h;
	}

	public boolean isRecipientReportSendAfter48h() {
		return recipientReportSendAfter48h;
	}

	public void setRecipientReportSendAfter48h(boolean recipientReportSendAfter48h) {
		this.recipientReportSendAfter48h = recipientReportSendAfter48h;
	}

	public boolean isRecipientReportSendAfter1Week() {
		return recipientReportSendAfter1Week;
	}

	public void setRecipientReportSendAfter1Week(boolean recipientReportSendAfter1Week) {
		this.recipientReportSendAfter1Week = recipientReportSendAfter1Week;
	}

	public void setAdminTargetGroupID(int targetID) {
		this.adminTargetGroupID = targetID;
	}
	
	public int getAdminTargetGroupID() {
		return this.adminTargetGroupID;
	}

    public int getPercentsComplete() {
        return percentsComplete;
    }

    public void setPercentsComplete(int percentsComplete) {
        this.percentsComplete = percentsComplete;
    }

    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }

	@Override
	public ActionErrors formSpecificValidate(ActionMapping mapping, HttpServletRequest req) {
		ActionErrors errors = new ActionErrors();
		// prevent validate here the action MailingSendAction.ACTION_CONFIRM_SEND_WORLD
		//validation is implemented in execute action
		return errors;
	}

    public String getMailingSubject(){
        Mailing mailing = getMailing();
        MediatypeEmail mediatype = (MediatypeEmail) mailing.getMediatypes().get(0);
        String subject = mediatype.getSubject();
        return subject;
    }

	public int getTemplateId() {
		return templateId;
	}

	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}

	public boolean isIsMailingGrid() {
		return isMailingGrid;
	}

	public void setMailingGrid(boolean mailingGrid) {
		isMailingGrid = mailingGrid;
	}

	/**
	 * Getter for property isTemplate.
	 *
	 * @return Value of property isTemplate.
	 */
	@Override
	public boolean isIsTemplate() {
		return this.isTemplate;
	}

	/**
	 * Setter for property isTemplate.
	 *
	 * @param isTemplate New value of property isTemplate.
	 */
	@Override
	public void setIsTemplate(boolean isTemplate) {
		this.isTemplate = isTemplate;
	}

	public Map<String, String> getStyles() {
		if (styles == null) {
			styles = new HashMap<>();
		}
		return styles;
	}
	
	public void setGenerationOptimization(final int mode) {
		this.generationOptimization = mode;
	}
	
	public int getGenerationOptimization() {
		return this.generationOptimization;
	}

	public boolean getIsMailingUndoAvailable() {
		return isMailingUndoAvailable;
	}

	public void setIsMailingUndoAvailable(boolean isMailingUndoAvailable) {
		this.isMailingUndoAvailable = isMailingUndoAvailable;
	}

	public List<AutoExport> getAutoExports() {
		return autoExports;
	}

	public void setAutoExports(List<AutoExport> autoExports) {
		this.autoExports = autoExports;
	}

	public String[] getMailingTestRecipients() {
		return mailingTestRecipients;
	}

	public void setMailingTestRecipients(String[] mailingTestRecipients) {
		this.mailingTestRecipients = mailingTestRecipients;
	}

	public List<Dependent<MailingDependentType>> getDependents() {
		return dependents;
	}

	public void setDependents(List<Dependent<MailingDependentType>> dependents) {
		this.dependents = dependents;
	}

	public void setFilterTypes(String[] filterTypes) {
		this.filterTypes = filterTypes;
	}

	public String[] getFilterTypes() {
		return filterTypes;
	}
}
