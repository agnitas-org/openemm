/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.web.forms;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.commons.collections4.list.GrowthList;
import org.apache.struts.action.ActionMapping;

public class ComOptimizationAjaxForm extends StrutsFormBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1095507500270455467L;
	
	private int companyID;
	private int campaignID;
	private int optimizationID;
	private String splitType;
	private boolean mailtracking;
	
	
	private List<Integer> excludeMailingIDs;
	
	public int getCompanyID() {
		return companyID;
	}
	public void setCompanyID(@VelocityCheck int companyID) {
		this.companyID = companyID;
	}
	public int getCampaignID() {
		return campaignID;
	}
	public void setCampaignID(int campaignID) {
		this.campaignID = campaignID;
	}
	public int getOptimizationID() {
		return optimizationID;
	}
	public void setOptimizationID(int optimizationID) {
		this.optimizationID = optimizationID;
	}
	
	public int getExcludeMailingID(int index) {
		return excludeMailingIDs.get(index);
	}
	public void setExcludeMailingID(int index, int mailingID) {
		this.excludeMailingIDs.set(index, mailingID);
	}
	
	public List<Integer> getExcludeMailingIDs() {
		return excludeMailingIDs;
	}

	@Override
	public void reset(ActionMapping map, HttpServletRequest request) {
		List<Integer> backupList = new ArrayList<>();
		excludeMailingIDs = GrowthList.growthList(backupList);
	}
	public String getSplitType() {
		return splitType;
	}
	public void setSplitType(String splitType) {
		this.splitType = splitType;
	}
	public boolean isMailtracking() {
		return mailtracking;
	}
	public void setMailtracking(boolean mailtracking) {
		this.mailtracking = mailtracking;
	}
	
}
