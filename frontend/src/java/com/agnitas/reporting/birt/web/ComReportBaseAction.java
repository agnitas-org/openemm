/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.web;

import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.Mailinglist;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.StrutsActionBase;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.mailinglist.service.ComMailinglistService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.reporting.birt.service.ComMailingBIRTStatService;

public class ComReportBaseAction extends StrutsActionBase {
	
    class BirtUrlParams {
    	/**
       	 * start of report period calculated on the other date parameters.
       	 */
       	String reportStartDate;
    	/**
       	 * end of report period calculated on the other date parameters.
       	 */
       	String reportEndDate;
       	/**
       	 * display hour-by-hour or day-by-day
       	 */
       	boolean hourScale;
    }
	
	protected String publicKeyFilename;
    protected ComMailingBIRTStatService birtservice;
	protected ComTargetService targetService;
	protected ComMailinglistService mailinglistService;
	private MailinglistApprovalService mailinglistApprovalService;
	  
    public void setPublicKeyFilename(String publicKeyFilename) {
		this.publicKeyFilename = publicKeyFilename;
	}
    
    @Required
    public final void setMailinglistApprovalService(final MailinglistApprovalService service) {
    	this.mailinglistApprovalService = Objects.requireNonNull(service, "Mailinglist approval service is null");
    }
  
    public void setBirtservice(ComMailingBIRTStatService birtservice) {
		this.birtservice = birtservice;
	}

	@Required
	public void setTargetService(ComTargetService targetService) {
		this.targetService = targetService;
	}

	@Required
	public void setMailinglistService(ComMailinglistService mailinglistService) {
		this.mailinglistService = mailinglistService;
	}

	public List<TargetLight> getTargetLights(HttpServletRequest request) {
		return targetService.getTargetLights(AgnUtils.getCompanyID(request));
	}

    public List<Mailinglist> getMailingListsNames(HttpServletRequest request) {
		return mailinglistApprovalService.getEnabledMailinglistsNamesForAdmin(AgnUtils.getAdmin(request));
	}
}
