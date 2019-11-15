/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.ecs.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.BaseDispatchAction;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.ecs.service.EcsService;
import com.agnitas.ecs.web.forms.EcsForm;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.service.GridServiceWrapper;

public class EcsAction extends BaseDispatchAction {
    private EcsService ecsService;
    private ComMailingBaseService mailingBaseService;

    private GridServiceWrapper gridService;
    
	protected ConfigService configService;
	
	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

    public ActionForward view(ActionMapping mapping, ActionForm actionForm, HttpServletRequest req, HttpServletResponse res) throws Exception {
        ActionMessages errors = getErrors(req);
        EcsForm form = (EcsForm) actionForm;

        final int companyId = AgnUtils.getCompanyID(req);
        final int mailingId = form.getMailingID();
        final ComAdmin admin = AgnUtils.getAdmin(req);

        Map<Integer, String> recipients = ecsService.getTestAndAdminRecipients(mailingId, companyId);
        
        req.setAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(AgnUtils.getAdmin(req), form.getMailingID()));

        form.setTestRecipients(recipients);

        // Default recipient for preview
        if (recipients.isEmpty()) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.preview.no_recipient"));
            form.setSelectedRecipient(0);
        } else {
            if(form.getSelectedRecipient() < 1 || !recipients.containsKey(form.getSelectedRecipient())) {
                form.setSelectedRecipient(recipients.keySet().iterator().next());
            }
        }

        form.setCompanyId(companyId);
        form.setRangeColors(ecsService.getClickStatColors(companyId));
        form.setStatServerUrl(configService.getValue(AgnUtils.getHostName(), ConfigValue.SystemUrl));

        form.setShortname(mailingBaseService.getMailingName(mailingId, companyId));

        int gridTemplateId = gridService.getGridTemplateIdByMailingId(mailingId);
        form.setTemplateId(gridTemplateId);
        form.setIsMailingGrid(gridTemplateId > 0);

        form.setIsMailingUndoAvailable(mailingBaseService.checkUndoAvailable(mailingId));
        form.setWorkflowId(mailingBaseService.getWorkflowId(mailingId, companyId));

        if (!errors.isEmpty()) {
            saveErrors(req, errors);
        }
        writeUserActivityLog(admin, "view ecs", "active tab - heatmap");

        return mapping.findForward("view");
    }

    public ActionForward export(ActionMapping mapping, ActionForm actionForm, HttpServletRequest req, HttpServletResponse res) throws Exception {
        EcsForm form = (EcsForm) actionForm;
        if (!ecsService.exportHeatMap(req, res, form.getMailingID(), form.getSelectedRecipient(), form.getViewMode(), form.getPreviewSize())) {
            res.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
        return null;
    }

    @Required
    public void setEcsService(EcsService ecsService) {
        this.ecsService = ecsService;
    }

    @Required
    public void setMailingBaseService(ComMailingBaseService mailingBaseService) {
        this.mailingBaseService = mailingBaseService;
    }
    
    @Required
    public void setGridService(GridServiceWrapper gridService) {
        this.gridService = gridService;
    }
}
