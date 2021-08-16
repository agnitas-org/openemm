/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.emm.core.target.exception.UnknownTargetGroupIdException;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.MailingWizardAction;
import org.agnitas.web.MailingWizardForm;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.web.QueryBuilderTargetGroupForm;

/**
 * Implementation of <strong>Action</strong> that handles Mailings
 */
public class ComMailingWizardAction extends MailingWizardAction {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ComMailingWizardAction.class);

	public static final String ACTION_NEW_TARGET = "newTarget";
	public static final String ACTION_ADD_TARGET = "addTarget";

	/**
     * If the user is not logged in - forwards to login page.
     * Saves openrate-measure property to mailing email param. Loads campaigns, mailinglists and target-groups to
     * request. Adds target to mailing targets list if needed. Removes target from mailing target list if needed.
     * Updates target expression (re-generates String representing selected targets IDs of mailing). Forwards to
     * "next" (currently the page for managing textmodules)
     *
     * @param mapping The ActionMapping used to select this instance
     * @param form data for the action filled by the jsp
     * @param request request from jsp
     * @param response response
     * @return destination specified in struts-config.xml to forward to next jsp
     */
	public ActionForward target(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		if (!AgnUtils.isUserLoggedIn(request)) {
			return mapping.findForward("logon");
		}

		MailingWizardForm aForm = (MailingWizardForm) form;
		Mailing mailing = aForm.getMailing();

		MediatypeEmail param = mailing.getEmailParam();
		param.setOnepixel(aForm.getEmailOnepixel());

		if (aForm.getAssignTargetGroups()) {
            mailing.setTargetExpression(TargetExpressionUtils.makeTargetExpression(aForm.getTargetGroups(), mailing.getTargetMode() != Mailing.TARGET_MODE_OR));
        }

		return mapping.findForward("next");
	}

	public ActionForward addTarget(ActionMapping mapping, ActionForm form,
                                   HttpServletRequest request, HttpServletResponse response) {
        if (!AgnUtils.isUserLoggedIn(request)) {
            return mapping.findForward("logon");
        }

        ActionMessages errors = new ActionErrors();
        try {
			MailingWizardForm aForm = (MailingWizardForm) form;
			int newTargetId = aForm.getAddTargetID();

			Collection<Integer> targetGroups = CollectionUtils.emptyIfNull(aForm.getTargetGroups());
			if (newTargetId > 0 && !targetGroups.contains(newTargetId)) {
				ComTarget target = targetService.getTargetGroup(aForm.getAddTargetID(), AgnUtils.getCompanyID(request));
				if (target.getDeleted() == 0) {
					targetGroups.add(newTargetId);
				}
				aForm.setTargetGroups(targetGroups);
			}

			return targetView(mapping, form, request, response);
		} catch (UnknownTargetGroupIdException e) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.unknownTargetGroup"));
		} catch (Exception e) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("Error"));
		}

        return mapping.findForward("messages");
    }
	
	public ActionForward newTarget(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (!AgnUtils.isUserLoggedIn(request)) {
			return mapping.findForward("logon");
		}

		ComAdmin admin = AgnUtils.getAdmin(request);

        QueryBuilderTargetGroupForm targetForm = (QueryBuilderTargetGroupForm) request.getSession().getAttribute("editTargetForm");
        if (targetForm != null) {
			targetForm.setTargetID(0);
			targetForm.setShortname("");
			targetForm.setDescription("");
			targetForm.setFormat("qb");
			targetForm.setEql("");
			targetForm.setQueryBuilderRules("[]");
		}

		request.setAttribute("mailTrackingAvailable", AgnUtils.isMailTrackingAvailable(admin));
		request.setAttribute("queryBuilderFilters", filterListBuilder.buildFilterListJson(admin));

		return mapping.findForward("newTarget");
	}

}
