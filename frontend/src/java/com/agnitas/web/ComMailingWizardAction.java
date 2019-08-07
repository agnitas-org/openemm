/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.Collection;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.Mailing;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.MailingWizardAction;
import org.agnitas.web.MailingWizardForm;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.agnitas.beans.ComTarget;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.ComMailing.MailingContentType;
import com.agnitas.web.forms.ComTargetForm;

/**
 * Implementation of <strong>Action</strong> that handles Mailings
 */
public final class ComMailingWizardAction extends MailingWizardAction {

	public static final String ACTION_NEW_TARGET = "newTarget";
	public static final String ACTION_NEW_FIELD = "newField";
	public static final String ACTION_ADD_TARGET = "addTarget";
	/**
	 * 
	 */
	@Override
	public ActionForward target(ActionMapping mapping, ActionForm form,
			HttpServletRequest req, HttpServletResponse res) throws Exception {
		if (!AgnUtils.isUserLoggedIn(req)) {
			return mapping.findForward("logon");
		}
		MailingWizardForm aForm = (MailingWizardForm) form;
		Mailing mailing = aForm.getMailing();
		

		MediatypeEmail param = mailing.getEmailParam();
		param.setOnepixel(aForm.getEmailOnepixel());
        prepareTargetPage(req);
		if (aForm.getTargetID() != 0) {
			Collection<Integer> targetGroups = mailing.getTargetGroups();

			if (targetGroups == null) {
				targetGroups = new HashSet<>();
				mailing.setTargetGroups(targetGroups);
			}
			ComTarget aTarget = targetDao.getTarget(aForm.getTargetID(), AgnUtils.getCompanyID(req));
			if (!targetGroups.contains(aForm.getTargetID()) && aTarget != null && aTarget.getDeleted() == 0) {
				targetGroups.add(aForm.getTargetID());
			}
		}

		if (aForm.getRemoveTargetID() != 0) {
			Collection<Integer> aList = aForm.getMailing().getTargetGroups();

			if (aList != null) {
				aList.remove(aForm.getRemoveTargetID());
			}
			return mapping.getInputForward();
		}

		// for the case if the target mode was changed we need to re-generate target expression
        if (aForm.getTargetID() == 0 && aForm.getRemoveTargetID() == 0 && CollectionUtils.isNotEmpty(mailing.getTargetGroups())) {
            mailing.updateTargetExpression();
        }
		return mapping.findForward("next");
	}

	public ActionForward addTarget(ActionMapping mapping, ActionForm form,
                                   HttpServletRequest request, HttpServletResponse response){
        if (!AgnUtils.isUserLoggedIn(request)) {
            return mapping.findForward("logon");
        }
        MailingWizardForm aForm = (MailingWizardForm) form;
        Mailing mailing = aForm.getMailing();
        request.setAttribute("isEnableTrackingVeto", configService.getBooleanValue(ConfigValue.EnableTrackingVeto, AgnUtils.getCompanyID(request)));

        MediatypeEmail param = mailing.getEmailParam();
        param.setOnepixel(aForm.getEmailOnepixel());
        prepareTargetPage(request);
        

        Collection<Integer> targetGroups = mailing.getTargetGroups();

        if (targetGroups == null) {
            targetGroups = new HashSet<>();
            mailing.setTargetGroups(targetGroups);
        }
        ComTarget aTarget = targetDao.getTarget(aForm.getTargetID(), AgnUtils.getCompanyID(request));
        if (!targetGroups.contains(aForm.getTargetID()) && aTarget != null && aTarget.getDeleted() == 0) {
            targetGroups.add(aForm.getTargetID());
        }
        return mapping.getInputForward();
    }
	
	public ActionForward newTarget(ActionMapping mapping, ActionForm form,
			HttpServletRequest req, HttpServletResponse res) throws Exception {
		if (!AgnUtils.isUserLoggedIn(req)) {
			return mapping.findForward("logon");
			
		}
        ComTargetForm cForm = (ComTargetForm)req.getSession().getAttribute("targetForm");
        if (cForm != null) {
            cForm.clearRules();
            cForm.setTargetID(0);
        }

		return mapping.findForward("newTarget");
	}
	
	public ActionForward newField(ActionMapping mapping, ActionForm form,
			HttpServletRequest req, HttpServletResponse res) throws Exception {
		if (!AgnUtils.isUserLoggedIn(req)) {
			return mapping.findForward("logon");
		}

		return mapping.findForward("newField");
	}

    @Override
    public ActionForward mailtype(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res) throws Exception {
        if (!AgnUtils.isUserLoggedIn(req)) {
			return mapping.findForward("logon");
		}

        return super.mailtype(mapping, form, req, res);
    }
	private MailingContentType mailingContentType;
    
	public MailingContentType getMailingContentType() throws Exception {
		return mailingContentType;
	}

	public void setMailingContentType(MailingContentType mailingContentType) {
		this.mailingContentType = mailingContentType;
	}

	public boolean isMailingContentTypeAdvertising() {
		return mailingContentType == null || mailingContentType == MailingContentType.advertising;
	}

	public void setMailingContentTypeAdvertising(boolean mailingContentTypeAdvertising) {
		mailingContentType = mailingContentTypeAdvertising ? MailingContentType.advertising : MailingContentType.transaction;
	}
}
