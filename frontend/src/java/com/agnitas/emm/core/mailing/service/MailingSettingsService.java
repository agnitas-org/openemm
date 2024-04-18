/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import java.util.Locale;
import java.util.Map;

import org.agnitas.web.forms.WorkflowParameters;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.mailing.forms.MailingSettingsForm;
import com.agnitas.emm.core.mailing.web.MailingSettingsOptions;
import com.agnitas.emm.core.mailingcontent.form.FrameContentForm;
import com.agnitas.web.mvc.Popups;

public interface MailingSettingsService {

    boolean saveFrameContent(Mailing mailing, FrameContentForm form, Admin admin, String sessionId, Popups popups);

    boolean saveSettings(Mailing mailing, MailingSettingsForm form, Admin admin, MailingSettingsOptions options, Popups popups);

    MailingSettingsForm mailingToForm(Mailing mailing, Admin admin);

    Map<String, Object> saveMailingGridInfo(int gridTemplateId, int mailingId, Admin admin);
    
    void setMailingTargetsToForm(MailingSettingsForm form, Mailing mailing);

    void copyTemplateSettingsToMailingForm(Mailing template, MailingSettingsForm form, Integer workflowId, boolean regularTemplate);

    void populateDisabledSettings(Mailing mailing, MailingSettingsForm form, boolean isGrid, Admin admin, WorkflowParameters workflowParams);

    void populateWorkflowDrivenSettings(MailingSettingsForm form, Mailing mailing, Admin admin, WorkflowParameters workflowParams);

    MailingSettingsForm prepareFormForCopy(Mailing origin, Locale locale, Integer workflowId, boolean forFollowUp);

    void removeInvalidTargets(Mailing mailing, Popups popups);
}
