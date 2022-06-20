/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.JavaMailService;
import com.agnitas.messages.I18nString;

public class ComSupportAction extends DispatchAction {
	private static final transient Logger logger = LogManager.getLogger(ComSupportAction.class);

	protected ConfigService configService;
	
	protected JavaMailService javaMailService;
	
	private String formNotFoundEmailTemplate;
	private String urlParameterTemplate;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	@Required
	public void setJavaMailService(JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}

	public ActionForward formSupport(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		ComSupportForm supportForm = (ComSupportForm) form;
		String supportAddress = configService.getValue(ConfigValue.Mailaddress_Support);

		String companyIdValue = getReportedParameterValue( "agnCI", supportForm);
		String formNameValue = getReportedParameterValue( "agnFN", supportForm);

		ActionMessages actionMessages = new ActionMessages();
		
		if( companyIdValue == null || companyIdValue.equals( "") || formNameValue == null || formNameValue.equals( "")) {
			logger.warn("formSupport: couldn't send error report");
			
			actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("FormNotFoundSendMissingInformation"));
			saveErrors(request, actionMessages);

			request.setAttribute("MESSAGE_BODY", "");
		} else {
			String messageBody = buildMessageBody(supportForm);
			request.setAttribute("MESSAGE_BODY", messageBody);
			
			if (javaMailService.sendEmail(0, supportAddress, I18nString.getLocaleString("FormNotFoundTitle", "de"), messageBody, messageBody)) {
				actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("FormNotFoundSent"));
				saveMessages(request, actionMessages);
			} else {
				logger.warn("formSupport: couldn't send error report");
	
				actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("FormNotFoundSendFailed"));
				saveErrors(request, actionMessages);
			}
		}
		
		
		return mapping.findForward("form_not_found_sent");
	}
	
	private String getReportedParameterValue( String parameterName, ComSupportForm supportForm) {
		// Ok, that's not an efficient method, but the data organization in ComSupportForm requires it :(
		for (int i = 0; i < supportForm.numParameter(); i++) {
			if (supportForm.getParameterName(i).equals(parameterName)) {
				return supportForm.getParameterValue(i);
			}
		}
		
		return null;
	}
	
	private String buildMessageBody(ComSupportForm form) {
		String paramList = buildParameterList(form);
		
		return this.formNotFoundEmailTemplate.replaceAll("%URL%", form.getUrl()).replaceAll("%PARAMLIST%", paramList);
	}
	
	private String buildParameterList(ComSupportForm form) {
		StringBuffer buffer = new StringBuffer();
		
		for( int index = 0; index < form.numParameter(); index++) {
			String param = this.urlParameterTemplate.replaceAll("%PARAM%", form.getParameterName(index)).replaceAll("%VALUE%", form.getParameterValue(index));
			buffer.append(param);
		}
		
		return buffer.toString();
	}

	public void setFormNotFoundEmailTemplate(String formNotFoundEmailTemplate) {
		this.formNotFoundEmailTemplate = formNotFoundEmailTemplate;
	}
	
	public void setUrlParameterTemplate(String urlParameterTemplate) {
		this.urlParameterTemplate = urlParameterTemplate;
	}
}
