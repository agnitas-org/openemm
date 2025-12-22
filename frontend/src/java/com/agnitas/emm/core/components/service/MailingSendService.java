/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingSendOptions;
import com.agnitas.emm.core.components.form.MailingTestSendForm;
import com.agnitas.emm.core.mailing.forms.MailingIntervalSettingsForm;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public interface MailingSendService {

    enum DeliveryType {
        WORLD,
        TEST,
        ADMIN
    }

    int FAIRNESS_PERIOD_IN_MINUTES = 5;

    SimpleServiceResult isRequiredDataAndComponentsExists(Mailing mailing);

    ServiceResult<UserAction> sendTestMailing(Mailing mailing, MailingTestSendForm form, Admin admin);

    ServiceResult<UserAction> sendWorldMailing(Mailing mailing, MailingSendOptions sendOptions, Admin admin);

    ServiceResult<UserAction> sendAdminMailing(Mailing mailing, MailingSendOptions sendOptions);

    ServiceResult<UserAction> activateDateBasedMailing(Mailing mailing, MailingSendOptions sendOptions, Admin admin);

    ServiceResult<UserAction> activateActionBasedMailing(Mailing mailing, MailingSendOptions sendOptions);

    SimpleServiceResult activateIntervalMailing(MailingIntervalSettingsForm intervalSettings, int autoImportId, Mailing mailing, Admin admin);

    boolean isLimitationForSendExists(int companyId);

    ServiceResult<UserAction> sendMailing(Mailing mailing, MailingSendOptions sendOptions, Admin admin);

    boolean deactivateMailing(Mailing mailing, int companyId, boolean isWorkflowDriven);

    void unlockMailing(Mailing mailing);

    void deactivateIntervalMailing(int mailingId, Admin admin);

    boolean deactivateIntervalMailing(Mailing mailing);
        
    boolean canSendOrActivateMailing(Admin admin, Mailing mailing);

    boolean isMailingActiveOrSent(Mailing mailing);

    boolean cancelMailingDelivery(int mailingID, int companyId);

    void sendEmail(Admin admin, String senderDomain);

    void checkIfMailingCanBeSend(Mailing mailing, Date sendDate, TimeZone timeZone);

    List<Integer> getAvailableSendingSpeedOptions(int companyID);

	void clearTestActionsData(int mailingID, int companyID);

    void validateForTestRun(MailingTestSendForm form, int mailingId, int companyId);

    boolean updateDoubleCheckOnSending(int companyId, int mailingID, boolean doubleCheck);

}
