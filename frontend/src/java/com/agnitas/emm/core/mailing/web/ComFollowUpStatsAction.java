/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.web;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.util.MessageResources;

import com.agnitas.emm.core.mailing.service.ComFollowUpStatsService;

public class ComFollowUpStatsAction extends DispatchAction {
	private static final transient Logger logger = Logger.getLogger(ComFollowUpStatsAction.class);	
	private ComFollowUpStatsService followUpStatsService;	
	
	public ActionForward stats(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		PrintWriter responseWriter = response.getWriter();
		int companyID = Integer.parseInt(request.getParameter("companyID")); 
		int mailingID = Integer.parseInt(request.getParameter("mailingID"));
		int baseMail = Integer.parseInt(request.getParameter("baseMailID"));
		boolean useTargetGroups = false;
		useTargetGroups = Boolean.parseBoolean(request.getParameter("useTargetGroups"));		
		responseWriter.print("CompanyID: " + request.getParameter("companyID") );
		responseWriter.print("mailingID: " + request.getParameter("mailingID") );
		String sessionID = request.getParameter("jsessionid");
		
		// call calculation
		startCalculation(mailingID, baseMail, companyID, sessionID, useTargetGroups);		
		return null;
	}
	
	
	public ActionForward checkResult(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		MessageResources messageResources = getResources(request);	// for messages
		if (logger.isInfoEnabled()) logger.info("checkResult was called.");
		PrintWriter responseWriter = response.getWriter();
//		responseWriter.println("Hello + " + System.currentTimeMillis());
		int mailingID = Integer.parseInt(request.getParameter("mailingID"));
		String sessionID = request.getSession().getId();
		String text1 = messageResources.getMessage(request.getLocale(), "RecipientsFollowupXplain1");
		String text2 = messageResources.getMessage(request.getLocale(), "RecipientsFollowupXplain2");
		String result = followUpStatsService.checkStats(mailingID, sessionID);
		if (result == null) {			
			 result = messageResources.getMessage(request.getLocale(), "CalculationProgress");
		} else {
			result = text1 + " <b> " + result + "</b> " + text2;
		}
		responseWriter.println(result);
//		followUpStatsService.getStats(mailingID, baseMail, companyID, useTargetGroups);
		return null;
	}
	
	public void startCalculation(int followupID, int baseMailID, @VelocityCheck int companyID, String sessionID, boolean useTargetGroups) {
		followUpStatsService.startCalculation(followupID, baseMailID, companyID, sessionID, useTargetGroups);
	}
	
	public void setFollowUpStatsService(ComFollowUpStatsService followUpStatsService) {
		this.followUpStatsService = followUpStatsService;
	}	
}
