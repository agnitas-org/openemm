/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.userform.trackablelinks.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.util.AgnUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.UserFormDao;
import com.agnitas.reporting.birt.external.web.filter.BirtInterceptingFilter;
import com.agnitas.reporting.birt.util.URLUtils;
import com.agnitas.userform.bean.UserForm;

/**
 * Action class for user formula statistics
 */
public class ComTrackableUserFormLinkStatAction extends DispatchAction {
	private static final transient Logger logger = Logger.getLogger(ComTrackableUserFormLinkStatAction.class);

	protected ConfigService configService;
    protected UserFormDao userFormDao;

	/**
	 * For retrieving the statistics for trackable user links
	 *
	 * @param mapping - action mapping
	 * @param form - action form
	 * @param request - HTTP request object
	 * @param response - HTTP response object
	 * @return
	 * @throws Exception
	 */
	public ActionForward statistics(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		if (logger.isDebugEnabled()) {
			logger.debug("Starting action method statistics");
		}
		
		ActionForward destination = mapping.findForward("stat");
		
		if (!AgnUtils.isUserLoggedIn(request)) {
			return mapping.findForward("logon");
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Finished action method statistics");
		}
		request.setAttribute("sec", URLUtils.encodeURL(BirtInterceptingFilter.createSecurityToken(configService, AgnUtils.getAdmin(request).getCompanyID())));
		request.setAttribute("language", getLocale(request).getLanguage());
        ComTrackableUserFormLinkStatForm linkStatForm = (ComTrackableUserFormLinkStatForm) form;
        UserForm userForm = userFormDao.getUserForm(linkStatForm.getFormID(), AgnUtils.getCompanyID(request));
        request.setAttribute("userForm", userForm);

		return destination;
	}

	@Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

	@Required
    public void setUserFormDao(UserFormDao userFormDao) {
        this.userFormDao = userFormDao;
    }
}
