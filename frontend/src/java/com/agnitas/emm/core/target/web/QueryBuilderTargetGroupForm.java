/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.Mailinglist;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Form bean containing all the data of the target group editors.
 */
public final class QueryBuilderTargetGroupForm extends ActionForm {
	
	/** Serial version UID. */
	private static final long serialVersionUID = 2219139308500990657L;
	
	/** ID of viewed target group. */
	private int targetID;
	
	/** Name of target group. */
	private String shortname;
	
	/** Description of target group. */
	private String description;
	
	/** State of checkbox "Use for admin- and test-delivery". */
	private boolean useForAdminAndTestDelivery;
	
	/** Content of EQL editor (if shown). */
	private String eql;
	
	/** QueryBuilder rules as JSON string. */
	private String queryBuilderRules;
	
	/** QueryBuilder filters as JSON string. */
	private String queryBuilderFilters;

	private String method;

	private boolean locked;

	private int mailingId;

	private boolean simpleStructure;

	private String workflowId;

	private String workflowForwardParams;

	/** 
	 * Currently shown format of target group data.
	 * @see TargetgroupViewFormat 
	 */
	private String format;

	private List<LightweightMailing> usedInMailings;

	private int mailinglistId;

	private List<Mailinglist> mailinglists;

	private boolean showStatistic = false;

	private String statisticUrl;
	
	@Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		super.reset(mapping, request);
		
		useForAdminAndTestDelivery = false;
		mailingId = 0;
		targetID = 0;
		workflowId = null;
		workflowForwardParams = null;
		usedInMailings = null;
	}

	protected ActionErrors doFormValidate(final ActionMapping mapping, final HttpServletRequest request, final ActionErrors messages) {
		return messages;
	}

	// ----------------------------------------------------------------------------------------------------- property "targetID"	
	public final void setTargetID(final int id) {
		this.targetID = id;
	}

	public final int getTargetID() {
		return this.targetID;
	}

	// ----------------------------------------------------------------------------------------------------- property "shortname"	
	public final void setShortname(final String shortname) {
		this.shortname = shortname;
	}
	
	public final String getShortname() {
		return this.shortname;
	}

	// ----------------------------------------------------------------------------------------------------- property "description"	
	public final String getDescription() {
		return description;
	}

	public final void setDescription(final String description) {
		this.description = description;
	}

	// ----------------------------------------------------------------------------------------------------- property "useForAdminAndTestDelivery"	
	public final boolean isUseForAdminAndTestDelivery() {
		return useForAdminAndTestDelivery;
	}

	public final void setUseForAdminAndTestDelivery(final boolean useForAdminAndTestDelivery) {
		this.useForAdminAndTestDelivery = useForAdminAndTestDelivery;
	}

	// ----------------------------------------------------------------------------------------------------- property "eql"	
	public final String getEql() {
		return eql;
	}

	public final void setEql(final String eql) {
		this.eql = eql;
	}

	// ----------------------------------------------------------------------------------------------------- property "format"	
	public final String getFormat() {
		return format;
	}

	public final void setFormat(final String format) {
		this.format = format;
	}

	public final void setFormat(final TargetgroupViewFormat format) {
		setFormat(format.code());
	}
	
	// ----------------------------------------------------------------------------------------------------- property "queryBuilderRules" (content format: JSON)
	public final String getQueryBuilderRules() {
		return this.queryBuilderRules;
	}
	
	public void setQueryBuilderRules(final String value) {
		this.queryBuilderRules = value;
	}
	
	// ----------------------------------------------------------------------------------------------------- property "queryBuilderFilters" (content format: JSON)
	public final String getQueryBuilderFilters() {
		return this.queryBuilderFilters;
	}
	
	public void setQueryBuilderFilters(final String value) {
		this.queryBuilderFilters = value;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public int getMailingId() {
		return mailingId;
	}

	public void setMailingId(int mailingId) {
		this.mailingId = mailingId;
	}

	public boolean isSimpleStructure() {
		return simpleStructure;
	}

	public void setSimpleStructure(boolean simpleStructure) {
		this.simpleStructure = simpleStructure;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	public String getWorkflowForwardParams() {
		return workflowForwardParams;
	}

	public void setWorkflowForwardParams(String workflowForwardParams) {
		this.workflowForwardParams = workflowForwardParams;
	}

	public List<LightweightMailing> getUsedInMailings() {
		return usedInMailings;
	}

	public void setUsedInMailings(List<LightweightMailing> usedInMailings) {
		this.usedInMailings = usedInMailings;
	}

	public int getMailinglistId() {
		return mailinglistId;
	}

	public void setMailinglistId(int mailinglistId) {
		this.mailinglistId = mailinglistId;
	}

	public List<Mailinglist> getMailinglists() {
		return mailinglists;
	}

	public void setMailinglists(List<Mailinglist> mailinglists) {
		this.mailinglists = mailinglists;
	}

	public boolean isShowStatistic() {
		return showStatistic;
	}

	public void setShowStatistic(boolean showStatistic) {
		this.showStatistic = showStatistic;
	}

	public String getStatisticUrl() {
		return statisticUrl;
	}

	public void setStatisticUrl(String statisticUrl) {
		this.statisticUrl = statisticUrl;
	}

}
