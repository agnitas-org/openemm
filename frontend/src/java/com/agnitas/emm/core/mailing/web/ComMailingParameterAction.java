/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils;
import org.agnitas.web.BaseDispatchAction;
import org.agnitas.web.StrutsActionBase;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import com.agnitas.emm.core.mailing.forms.ComMailingParameterForm;
import com.agnitas.emm.core.mailing.service.ComMailingParameterService;
import com.agnitas.emm.core.mailing.service.MailingParameterLogService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.service.ComWebStorage;

public class ComMailingParameterAction extends BaseDispatchAction {
	/** The logger. */
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ComMailingParameterAction.class);
	private ComMailingParameterService mailingParameterService;
	private MailingService mailingService;
	private WebStorage webStorage;
	
	private MailingParameterLogService mailingParameterLogService;
	

	/**
	 * Action for displaying list of mailing parameters. Returns either page with all parameters for current company
	 * or page with filtered list of parameters according to the {@code parameterQuery} and {@code mailingQuery}.
	 *
	 * @param mapping  template mapping of current Action
	 * @param form     form of current Action
	 * @param request  request object
	 * @param response response object
	 * @return corresponding template
	 */
	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		final ComAdmin admin = AgnUtils.getAdmin(request);
		AgnUtils.setAdminDateTimeFormatPatterns(request);

		// parameter name or description
		final String parameterQuery = request.getParameter("parameterSearchQuery");

		// first part of mailing id or full mailing id
		final String mailingQuery = request.getParameter("mailingSearchQuery");

		ComMailingParameterForm parameterForm = (ComMailingParameterForm) form;
		List<ComMailingParameter> parameters;

		if (CollectionUtils.isEmpty(parameterForm.getColumnwidthsList())) {
			parameterForm.setColumnwidthsList(getInitializedColumnWidthList(4));
		}

		if (StringUtils.isNotBlank(parameterQuery) || NumberUtils.isNumber(mailingQuery)) {
			parameters = mailingParameterService.getParametersBySearchQuery(admin.getCompanyID(), parameterQuery, mailingQuery);
			// saving parameters for the next request
			parameterForm.setParameterSearchQuery(parameterQuery);
			parameterForm.setMailingSearchQuery(mailingQuery);
		} else {
			parameters = mailingParameterService.getAllParameters(admin.getCompanyID(), admin);
		}

		FormUtils.syncNumberOfRows(webStorage, ComWebStorage.MAILING_PARAMETER_OVERVIEW, parameterForm);

		request.setAttribute("mailingParameter", parameters);

		return mapping.findForward("list");
	}

	/**
	 * Returns JSON object with filtered mailing parameters by search query. Necessary for autocomplete.
	 *
	 * @param mapping  template mapping of current Action
	 * @param form     form of current Action
	 * @param request  request object
	 * @param response response object
	 * @return JSON object with list of mailing parameters
	 */
	public ActionForward search(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		final String parameterQuery = request.getParameter("parameterSearchQuery");
		final String mailingQuery = request.getParameter("mailingSearchQuery");
		final ComAdmin admin = AgnUtils.getAdmin(request);
		
		assert (admin != null);
		
		final List<ComMailingParameter> parameters = mailingParameterService.getParametersBySearchQuery(admin.getCompanyID(),
				parameterQuery, mailingQuery);

		HttpUtils.responseJson(response, HttpUtils.toJson(parameters));
		return null;
	}

	//form filled with values of the parameter
	public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		ComMailingParameter parameter = mailingParameterService.getParameter(((ComMailingParameterForm)form).getMailingInfoID(), AgnUtils.getAdmin(request));
		loadMailings(request);
		fillFormWithData((ComMailingParameterForm) form, parameter);
		return mapping.findForward("view");
	}
	
	//show empty form to enter new parameter values
	public ActionForward newParameter(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		((ComMailingParameterForm)form).resetFormValues(mapping, request);
		((ComMailingParameterForm)form).setCompanyID(AgnUtils.getCompanyID(request));
		loadMailings(request);
		return mapping.findForward("view");
	}
	
	//save a (new?) parameter
	public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		ActionMessages actionMessages = new ActionMessages();
		ActionErrors errors = new ActionErrors();
		
		// load Data...
		ComAdmin admin = AgnUtils.getAdmin(request);
		int adminId = admin.getAdminID();
		ComMailingParameter parameter = fillBeanWithData((ComMailingParameterForm) form);
		parameter.setCompanyID(AgnUtils.getCompanyID(request));
		parameter.setCreationAdminID(adminId);
		parameter.setChangeAdminID(adminId);

		boolean result;
		if (parameter.getMailingInfoID() > 0) {
			ComMailingParameter oldParameter = mailingParameterService.getParameter(parameter.getMailingInfoID(), admin);
			result = mailingParameterService.updateParameter(parameter, admin);
			writeUserActivityLog(admin, mailingParameterLogService.getMailingParameterChangeLog(parameter.getMailingID(), parameter.getMailingInfoID(), oldParameter, parameter));
		} else {
			result = mailingParameterService.insertParameter(parameter, admin);
			writeUserActivityLog(admin, mailingParameterLogService.getMailingParameterCreateLog(parameter.getMailingID(), parameter));
		}
		
		if (result) {
			list(mapping, form, request, response);
			showSavedMessage(request);
			return mapping.findForward("list");
		} else {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error"));
            saveErrors(request, actionMessages);
			return mapping.findForward("view");
		}
	}
	
	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		ActionErrors errors = new ActionErrors();
		ComAdmin admin = AgnUtils.getAdmin(request);
		int mailingInfoID = ((ComMailingParameterForm) form).getMailingInfoID();
		boolean result = mailingParameterService.deleteParameter(mailingInfoID, admin);
		final ComMailingParameter parameter = mailingParameterService.getParameter(mailingInfoID, admin);
		
		writeUserActivityLog(admin, mailingParameterLogService.getMailingParameterDeleteLog(mailingInfoID, parameter));

		if (result) {
			list(mapping, form, request, response);
			showSavedMessage(request, "default.selection.deleted");
			return mapping.findForward("list");
		} else {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error"));
			return mapping.findForward("view");
		}
	}

    public ActionForward deleteConfirm(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ComMailingParameter parameter = mailingParameterService.getParameter(((ComMailingParameterForm)form).getMailingInfoID(), AgnUtils.getAdmin(request));
        fillFormWithData((ComMailingParameterForm) form, parameter);
        return mapping.findForward("delete");
    }

    public ActionForward deleteCancelled(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        if(((ComMailingParameterForm) form).getPreviousAction() == StrutsActionBase.ACTION_VIEW){
            return view(mapping, form, request, response);
        }
        return list(mapping, form, request, response);
    }

    public ActionForward confirmDelete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ComMailingParameterForm mailingParameterForm = (ComMailingParameterForm) form;
        ComMailingParameter parameter = mailingParameterService.getParameter(mailingParameterForm.getMailingInfoID(), AgnUtils.getAdmin(request));
        mailingParameterForm.setParameterName(parameter.getName());
        mailingParameterForm.setAction("delete");

        return mapping.findForward("delete");
    }
	
	/**
	 * This method fills the given form with the data from the upload-object.
	 * Warning! The Form you pass as parameter will be changed!
	 * @param form
	 * @param mailingParameter
	 * @return
	 */
	private ActionForm fillFormWithData(ComMailingParameterForm form, ComMailingParameter mailingParameter) {
		form.setMailingInfoID(mailingParameter.getMailingInfoID());
		form.setMailingID(mailingParameter.getMailingID());
		form.setCompanyID(mailingParameter.getCompanyID());
		form.setParameterName(mailingParameter.getName());
		form.setValue(mailingParameter.getValue());
		form.setDescription(mailingParameter.getDescription());
		form.setCreation_date(mailingParameter.getCreationDate());
		form.setChange_date(mailingParameter.getChangeDate());
		form.setCreation_admin_id(mailingParameter.getCreationAdminID());
		form.setChange_admin_id(mailingParameter.getChangeAdminID());
		return form;
	}
	
	private ComMailingParameter fillBeanWithData(ComMailingParameterForm form) {
		ComMailingParameter parameter = new ComMailingParameter();
		parameter.setMailingInfoID(form.getMailingInfoID());
		parameter.setMailingID(form.getMailingID());
		parameter.setCompanyID(form.getCompanyID());
		parameter.setName(form.getParameterName());
		parameter.setValue(form.getValue());
		parameter.setDescription(form.getDescription());
		parameter.setCreationDate(form.getCreation_date());
		parameter.setChangeDate(form.getChange_date());
		parameter.setCreationAdminID(form.getCreation_admin_id());
		parameter.setChangeAdminID(form.getChange_admin_id());
		return parameter;
	}

	protected void loadMailings(HttpServletRequest req) {
		List<LightweightMailing> mailings = mailingService.getAllMailingNames(AgnUtils.getAdmin(req));
		req.setAttribute("mailings", mailings);
	}

	@Required
	public void setMailingParameterService(ComMailingParameterService mailingParameterService) {
		this.mailingParameterService = mailingParameterService;
	}

	@Required
	public void setMailingService(MailingService mailingService) {
		this.mailingService = mailingService;
	}

	@Required
	public void setWebStorage(WebStorage webStorage) {
		this.webStorage = webStorage;
	}
	
	@Required
	public void setMailingParameterLogService(MailingParameterLogService mailingParameterLogService) {
		this.mailingParameterLogService = mailingParameterLogService;
	}
}
