/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.SafeString;
import org.agnitas.web.EmmActionAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.UserFormDao;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.web.forms.ComEmmActionForm;

public class ComEmmActionAction extends EmmActionAction {
    private static final Logger logger = Logger.getLogger(ComEmmActionAction.class);

    private UserFormDao userFormDao;

    public static final int ACTION_BULK_CONFIRM_DELETE = 11;
    public static final int ACTION_BULK_DELETE = 12;
    public static final int ACTION_CLONE = 13;
    public static final int ACTION_SHOW_FORMS = 14;
    public static final int ACTION_SAVE_ACTIVENESS = 15;

    @Override
    public String subActionMethodName(int subAction) {
        switch (subAction) {
            case ACTION_BULK_CONFIRM_DELETE:
                return "bulk_confirm_delete";
            case ACTION_BULK_DELETE:
                return "bulk_delete";
            case ACTION_CLONE:
                return "clone";
            case ACTION_SHOW_FORMS:
                return "view_forms";
            case ACTION_SAVE_ACTIVENESS:
                return "save_activeness";
            default:
                return super.subActionMethodName(subAction);
        }
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res) throws Exception {
        ComEmmActionForm emmActionForm = (ComEmmActionForm) form;
        ComAdmin admin = AgnUtils.getAdmin(req);

        assert admin != null;

        switch (emmActionForm.getAction()) {
            case ACTION_BULK_CONFIRM_DELETE:
                if (emmActionForm.getBulkIds().size() == 0) {
                    ActionMessages errors = new ActionMessages();
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("bulkAction.nothing.action"));
                    saveErrors(req, errors);
                    emmActionForm.setAction(ACTION_LIST);
                    return super.execute(mapping, form, req, res);
                } else {
                    emmActionForm.setAction(ACTION_BULK_DELETE);
                    return mapping.findForward("bulk_delete_confirm");
                }

            case ACTION_BULK_DELETE:
                if (deleteEmmActionsBulk(emmActionForm, req)) {
                    ActionMessages messages = new ActionMessages();
                    messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
                    saveMessages(req, messages);
                }
                emmActionForm.setAction(ACTION_LIST);
                return super.execute(mapping, form, req, res);

            case ACTION_CLONE:
                ActionForward destination;
                emmActionForm.setAction(ACTION_VIEW);
                destination = super.execute(mapping, form, req, res);
                emmActionForm.setShortname(SafeString.getLocaleString("mailing.CopyOf", (Locale) req.getSession().getAttribute(Globals.LOCALE_KEY)) + " " + emmActionForm.getShortname());
                emmActionForm.setActionID(0);

                // An operations should be cloned, not referenced
                List<AbstractActionOperationParameters> operations = emmActionForm.getActions();
                if (operations != null) {
                    for (AbstractActionOperationParameters operation : operations) {
                        operation.setId(0);
                        operation.setActionId(0);
                    }
                }
                return destination;

            case ACTION_SHOW_FORMS:
                emmActionForm.setUsedByFormsNames(userFormDao.getUserFormNamesByActionID(admin.getCompanyID(), emmActionForm.getActionID()));
                emmActionForm.setUsedByImportNames(userFormDao.getImportNamesByActionID(admin.getCompanyID(), emmActionForm.getActionID()));
                return mapping.findForward("view_forms");

            case ACTION_SAVE_ACTIVENESS:
                if (saveActiveness(admin, emmActionForm)) {
                    ActionMessages messages = new ActionMessages();
                    messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
                    saveMessages(req, messages);
                }
                emmActionForm.setAction(ACTION_LIST);
                return super.execute(mapping, form, req, res);

            default:
                return super.execute(mapping, form, req, res);
        }
    }

    private boolean saveActiveness(ComAdmin admin, ComEmmActionForm form) {
        List<UserAction> userActions = new ArrayList<>();

        boolean isSomethingChanged = emmActionService.setActiveness(form.getActivenessMap(), admin.getCompanyID(), userActions);
        for (UserAction userAction : userActions) {
            writeUserActivityLog(admin, userAction, logger);
        }

        return isSomethingChanged;
    }

    private boolean deleteEmmActionsBulk(ComEmmActionForm form, HttpServletRequest req) {
        Set<Integer> ids = form.getBulkIds();
        if (CollectionUtils.isNotEmpty(ids)) {
            final ComAdmin admin = AgnUtils.getAdmin(req);
            final int companyId = AgnUtils.getCompanyID(req);

            Map<Integer, String> descriptions = new HashMap<>();
            for (int actionId : ids) {
                descriptions.put(actionId, emmActionService.getEmmActionName(actionId, companyId));
            }

            emmActionService.bulkDelete(ids, companyId);

            for (int actionId : ids) {
                writeUserActivityLog(admin, "delete action", descriptions.get(actionId));
            }
            return true;
        }
        return false;
    }

    @Required
    public void setUserFormDao(UserFormDao userFormDao) {
        this.userFormDao = userFormDao;
    }
}
