/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service;

import java.util.Date;
import java.util.TimeZone;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.components.form.MailingSendForm;
import com.agnitas.emm.core.components.form.MailingTestSendForm;
import com.agnitas.emm.core.mailing.forms.MailingIntervalSettingsForm;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.web.mvc.Popups;

public interface MailingSendService {

    int FAIRNESS_PERIOD_IN_MINUTES = 5;
    int ADMIN_TARGET_SINGLE_RECIPIENT = -1;

    boolean deactivateMailing(Mailing mailing, int companyId, boolean isWorkflowDriven);

    void unlockMailing(Mailing mailing);

    SimpleServiceResult saveIntervalSettings(MailingIntervalSettingsForm intervalSettings, Mailing mailing, Admin admin);

    void deactivateIntervalMailing(int mailingId, Admin admin);

    SimpleServiceResult activateIntervalMailing(MailingIntervalSettingsForm intervalSettings, int mailingId, Admin admin);

    void sendTestMailing(Mailing mailing, MailingTestSendForm form, Admin admin, Popups popups) throws Exception;

    @Deprecated // Use #canSendOrActivateMailing instead
    boolean isWorldMailingCanBeSend(Admin admin, Mailing mailing) throws Exception;

    boolean canSendOrActivateMailing(Admin admin, Mailing mailing) throws Exception;

    boolean saveEncryptedState(int mailingId, int companyId, boolean isEncryptedSend);

    boolean cancelMailingDelivery(int mailingID, int companyId);

    void sendEmail(Admin admin, String senderDomain);

    void checkIfMailingCanBeSend(Mailing mailing, Date sendDate, TimeZone timeZone) throws Exception;

    void sendWorldMailing(Admin admin, MailingSendForm form, Popups popups, Mailing mailing, Date sendDate) throws Exception;

    void sendAdminMailing(Mailing mailing, Admin admin, MailingTestSendForm form, Popups popups) throws Exception;

    void activateDateBasedMailing(Admin admin, Mailing mailing, Popups popups, Date sendDate, int autoImportId);

    void activateActionBasedMailing(Admin admin, Mailing mailing, Popups popups);

    boolean isLimitationForSendExists(int companyId);
}
