/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.web.forms;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.util.DbUtilities;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.commons.lang.ArrayUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import com.agnitas.reporting.birt.web.ComMailingBIRTStatAction;

public class ComMailingBIRTStatForm extends StrutsFormBase {

	private static final long serialVersionUID = -829099167129536174L;
	private int action = -1;
	
	private int mailingID;
	
	private int previousMailingID = -1;
	
	private int urlID;
		
	private String startDate_localized; // user enters a date according to his localization eg. dd.mm.yyyy
	
	private String endDate_localized; // user enters a date according to his localization eg. dd.mm.yyyy
	
	private String startDate;	// we use the iso-scheme yyyy-mm-dd as a parameter for our reports
	
	private String endDate; // we use the iso-scheme yyyy-mm-dd as a parameter for our reports
	
	private String month;
	
	private String year;
		
	private String day;
	
	private String[] availableTargets ;
	
	private String[] selectedTargets ;	
	
	private boolean targetSelectsReset;
	
	private String shortname;
	
	private boolean includeAdminAndTestMails;

    private int showReportOnly;

	private boolean isMailingGrid;

	private int workflowId;

	private int templateId;

	private boolean isMailingUndoAvailable;

	private String searchQueryText;

	private boolean searchNameChecked;

	private boolean searchDescriptionChecked;

	private String[] additionalFields = ArrayUtils.EMPTY_STRING_ARRAY;

	private int[] filteredMailingLists = ArrayUtils.EMPTY_INT_ARRAY;

	private int[] filteredTargetGroups = ArrayUtils.EMPTY_INT_ARRAY;

	@Override
	public ActionErrors formSpecificValidate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();

		if (getAction() == ComMailingBIRTStatAction.ACTION_LIST) {
			if (searchNameChecked || searchDescriptionChecked) {
				for (String error : DbUtilities.validateFulltextSearchQueryText(searchQueryText)) {
					errors.add("invalid_search_query", new ActionMessage(error));
				}
			}
		}

		return errors;
	}

	@Override
	public void reset(ActionMapping map, HttpServletRequest request) {
		super.reset(map, request);

		setNumberOfRows(-1);
		setAdditionalFields(ArrayUtils.EMPTY_STRING_ARRAY);
	}

	public boolean isSearchNameChecked() {
		return searchNameChecked;
	}

	public void setSearchNameChecked(boolean searchNameChecked) {
		this.searchNameChecked = searchNameChecked;
	}

	public boolean isSearchDescriptionChecked() {
		return searchDescriptionChecked;
	}

	public void setSearchDescriptionChecked(boolean searchDescriptionChecked) {
		this.searchDescriptionChecked = searchDescriptionChecked;
	}

	public String getSearchQueryText() {
		return searchQueryText;
	}

	public void setSearchQueryText(String searchQueryText) {
		this.searchQueryText = searchQueryText;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public int getMailingID() {
		return mailingID;
	}

	public void setMailingID(int mailingID) {
		this.mailingID = mailingID;
	}

	public int getPreviousMailingID() {
		return previousMailingID;
	}

	public void setPreviousMailingID(int previousMailingID) {
		this.previousMailingID = previousMailingID;
	}

	public int getUrlID() {
		return urlID;
	}

	public void setUrlID(int urlID) {
		this.urlID = urlID;
	}

	public String getStartDate_localized() {
		return startDate_localized;
	}

	public void setStartDate_localized(String startDate_localized) {
		this.startDate_localized = startDate_localized;
	}

	public String getEndDate_localized() {
		return endDate_localized;
	}

	public void setEndDate_localized(String endDate_localized) {
		this.endDate_localized = endDate_localized;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}	

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String[] getAvailableTargets() {
		return availableTargets;
	}

	public void setAvailableTargets(String[] availableTargets) {
		this.availableTargets = availableTargets;
	}

	public String[] getSelectedTargets() {
		return selectedTargets;
	}

	public void setSelectedTargets(String[] selectedTargets) {
		this.selectedTargets = selectedTargets;
	}

	public boolean isTargetSelectsReset() {
		return targetSelectsReset;
	}

	public void setTargetSelectsReset(boolean targetSelectsReset) {
		this.targetSelectsReset = targetSelectsReset;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public boolean isIncludeAdminAndTestMails() {
		return includeAdminAndTestMails;
	}

	public void setIncludeAdminAndTestMails(boolean includeAdminAndTestMails) {
		this.includeAdminAndTestMails = includeAdminAndTestMails;
	}
	
	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}
	
	public void setShowReportOnly( int showReportOnly) {
		this.showReportOnly = showReportOnly;
	}

	public int getShowReportOnly() {
		return this.showReportOnly;
	}

	public boolean getIsMailingGrid() {
		return isMailingGrid;
	}

	public void setIsMailingGrid(boolean isMailingGrid) {
		this.isMailingGrid = isMailingGrid;
	}

	public int getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(int workflowId) {
		this.workflowId = workflowId;
	}

	public int getTemplateId() {
		return templateId;
	}

	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}

	public boolean getIsMailingUndoAvailable() {
		return isMailingUndoAvailable;
	}

	public void setIsMailingUndoAvailable(boolean isMailingUndoAvailable) {
		this.isMailingUndoAvailable = isMailingUndoAvailable;
	}

	public String[] getAdditionalFields() {
		return additionalFields;
	}

	public void setAdditionalFields(String[] additionalFields) {
		this.additionalFields = additionalFields;
	}

	public Set<String> getAdditionalFieldsSet() {
		if (ArrayUtils.isEmpty(additionalFields)) {
			return Collections.emptySet();
		}

		return new HashSet<>(Arrays.asList(additionalFields));
	}

	public List<Integer> getFilteredMailingListsAsList() {
		if (ArrayUtils.isEmpty(filteredMailingLists)) {
			return Collections.emptyList();
		}

		return Arrays.stream(filteredMailingLists)
				.boxed()
				.collect(Collectors.toList());
	}

	public int[] getFilteredMailingLists() {
		return filteredMailingLists;
	}

	public void setFilteredMailingLists(int[] filteredMailingLists) {
		this.filteredMailingLists = filteredMailingLists;
	}

	public List<Integer> getFilteredTargetGroupsAsList() {
		if (ArrayUtils.isEmpty(filteredTargetGroups)) {
			return Collections.emptyList();
		}

		return Arrays.stream(filteredTargetGroups)
				.boxed()
				.collect(Collectors.toList());
	}

	public int[] getFilteredTargetGroups() {
		return filteredTargetGroups;
	}

	public void setFilteredTargetGroups(int[] filteredTargetGroups) {
		this.filteredTargetGroups = filteredTargetGroups;
	}
}
