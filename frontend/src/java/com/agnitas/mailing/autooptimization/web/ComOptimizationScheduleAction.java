/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.web;

import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.Company;
import org.agnitas.beans.impl.MaildropDeleteException;
import org.agnitas.emm.core.commons.util.Constants;
import org.agnitas.emm.core.commons.util.DateUtil;
import org.agnitas.emm.core.mailing.MailingAllReadySentException;
import org.agnitas.util.AgnUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;

import com.agnitas.beans.ComAdmin;
import com.agnitas.mailing.autooptimization.beans.ComOptimization;
import com.agnitas.mailing.autooptimization.service.ComOptimizationScheduleService;
import com.agnitas.mailing.autooptimization.service.ComOptimizationService;
import com.agnitas.mailing.autooptimization.service.OptimizationIsFinishedException;
import com.agnitas.mailing.autooptimization.web.forms.ComOptimizationScheduleForm;

public class ComOptimizationScheduleAction extends DispatchAction {
	private static final transient Logger logger = Logger.getLogger(ComOptimizationScheduleAction.class);

	private ComOptimizationService optimizationService;
	private ComOptimizationScheduleService optimizationScheduleService;
	
	

	public void setOptimizationScheduleService(
			ComOptimizationScheduleService optimizationScheduleService) {
		this.optimizationScheduleService = optimizationScheduleService;
	}

	// After the optimization has been saved,  you have to schedule it.
	// Show a screen where you can enter the necessary parameters, e.g. start of the optimization process.
	public ActionForward showSchedule(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		
		ComAdmin admin = AgnUtils.getAdmin(request);
		Company company = admin.getCompany();
				
		ComOptimizationScheduleForm startForm = (ComOptimizationScheduleForm) form;
		ComOptimization optimization = optimizationService.get(startForm.getOptimizationID(), company.getId());
			
		startForm.setCampaignID(optimization.getCampaignID());
		
		if( optimization.getSendDate() != null ) {
			startForm.setResultSendDateAsString( DateUtil.formatDateFull(optimization.getSendDate()) );
		}
		
		if( optimization.getTestMailingsSendDate() != null ) {
			startForm.setTestMailingsSendDateAsString(DateUtil.formatDateFull(optimization.getTestMailingsSendDate()));
		}
		
		return mapping.findForward("schedule");
	}
	
	//  schedule the optimization. Save the senddates of the testmailings, and the senddate of the final mailing. 
	public ActionForward schedule(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		
		ActionMessages errors = new ActionMessages();
		ComAdmin admin = AgnUtils.getAdmin(request);
		Company company = admin.getCompany();
				
		ComOptimizationScheduleForm scheduleForm =  (ComOptimizationScheduleForm) form;
		ComOptimization optimization = optimizationService.get(scheduleForm.getOptimizationID(), company.getId());
		
		try {
			optimization.setTestMailingsSendDate(DateUtil.parseFullDate(scheduleForm.getTestMailingsSendDateAsString()));
			optimization.setSendDate(DateUtil.parseFullDate(scheduleForm.getResultSendDateAsString()));
			optimization.setTestRun(false);
			optimizationScheduleService.scheduleOptimization(optimization);
		} catch (ParseException e) {
			logger.error("Could not parse date : " + scheduleForm.getResultSendDateAsString() , e);			
			errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.autooptimization.errors.resultsenddate", Constants.DATE_PATTERN_FULL));
			saveErrors(request, errors);
		} catch (MailingAllReadySentException e) {
			logger.error("Could not schedule optimization. One of the test mailings has been allready sent ! Optimization-ID: " +optimization.getId());
			errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.autooptimization.errors.schedule"));
		} catch (OptimizationIsFinishedException e) {
			logger.error("Could not schedule optimization. Optimization has not the right state. Optimization-ID: " +optimization.getId() + " Status-ID:  " + optimization.getStatus() );
			errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.autooptimization.errors.schedule"));
		} catch (MaildropDeleteException e) {
			logger.error("Could not schedule optimization. Previous unschedule failed ! Optimization-ID: " +optimization.getId() + " Status-ID:  " + optimization.getStatus() );
			errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.autooptimization.errors.schedule"));
		}
		saveErrors(request, errors);
				
		if( errors.isEmpty() ) {
			ActionMessages actionMessages = new ActionMessages();
			actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
			saveMessages(request, actionMessages);
		}
			
		return mapping.findForward("schedule");
	}
	
	public ActionForward unSchedule(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
	
		ActionMessages errors = new ActionMessages();
		ComAdmin admin = AgnUtils.getAdmin(request);
		Company company = admin.getCompany();
		
		ComOptimizationScheduleForm scheduleForm = (ComOptimizationScheduleForm) form;
		ComOptimization optimization = optimizationService.get(scheduleForm.getOptimizationID(), company.getId());
		
		try {
			optimizationScheduleService.unscheduleOptimization(optimization);
		} catch (MaildropDeleteException e) {
			logger.error("Could not unschedule optimization." +optimization.getId() + " Status-ID:  " + optimization.getStatus() );
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.autooptimization.errors.unschedule"));
		}
		saveErrors(request, errors);
		
		if( errors.isEmpty() ) {
			ActionMessages actionMessages = new ActionMessages();
			actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
			saveMessages(request, actionMessages);
			scheduleForm.reset(mapping, request);
			return mapping.findForward("unscheduleSuccess");
		}		
		return mapping.findForward("schedule");
		
	}
	
	public void setOptimizationService(ComOptimizationService optimizationService) {
		this.optimizationService = optimizationService;
	}
	
}
