/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.logger;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.DateUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MailingSendLogWriter {

    private static final Logger logger = LogManager.getLogger(MailingSendLogWriter.class);
    private final UserActivityLogService userActivityLogService;

    @Autowired
    public MailingSendLogWriter(UserActivityLogService userActivityLogService) {
        this.userActivityLogService = userActivityLogService;
    }

    public void writeIntervalMailingActivationLog(Admin admin, Mailing mailing, Date sendDate, String actionDescription) {
        writeUserActivityLog(admin, "do activate mailing", String.format("Mailing type: %s, next start at: %s. %s",
                mailing.getMailingType().name(),
                DateUtilities.getDateTimeFormat(DateFormat.MEDIUM, DateFormat.SHORT, Locale.UK).format(sendDate),
                getRegularMailingDescription(mailing, actionDescription)));
    }

    public void writeIntervalMailingActivationSimpleLog(Mailing mailing, Admin admin) {
        String actionDescription = String.format("Mailing type: %s. %s",
                mailing.getMailingType().name(),
                "send to world recipients"
        );

        writeUserActivityLog(admin, "do activate mailing", actionDescription);
    }

    public void writeActionBasedActivationLog(Admin admin, Mailing mailing) {
        writeUserActivityLog(admin, "do activate mailing", String.format("Mailing type: %s. %s", mailing.getMailingType().name(), getTriggerMailingDescription(mailing)));
    }

    public void writeDateBasedActivationLog(Admin admin, Mailing mailing, Date sendDate) {
        writeUserActivityLog(admin, "do activate mailing", String.format("Mailing type: %s, at: %s. %s",
                mailing.getMailingType().name(),
                DateUtilities.getDateFormat(DateFormat.SHORT, Locale.UK).format(sendDate),
                getTriggerMailingDescription(mailing)));
    }

    public void writeScheduleMailingLog(Admin admin, Mailing mailing, Date sendDate) {
        String sendActionDescription = "send to world recipients";

        writeUserActivityLog(admin, "do schedule mailing", String.format("Mailing type: %s, at: %s. %s",
                mailing.getMailingType().name(),
                DateUtilities.getDateTimeFormat(DateFormat.MEDIUM, DateFormat.SHORT, Locale.UK).format(sendDate),
                getRegularMailingDescription(mailing, sendActionDescription)));
    }

    public void writeCancelIntervalMailingLog(Admin admin, Mailing mailing) {
        writeUserActivityLog(admin, "do cancel mailing",
                String.format("Mailing type: %s. %s", mailing.getMailingType().name(), getTriggerMailingDescription(mailing)));
    }

    public void writeUserActivityLog(Admin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, action, description, logger);
    }
    
    public void writeUserActivityLog(Admin admin, UserAction userAction) {
        userActivityLogService.writeUserActivityLog(admin, userAction, logger);
    }

    public void writeIntervalChangesLog(Admin admin, String oldInterval, String newInterval) {
        oldInterval = StringUtils.defaultString(oldInterval);
        newInterval = StringUtils.defaultString(newInterval);

        if (!oldInterval.equals(newInterval)) {
            String actionDescription = String.format("mailing edit interval from %s to %s; ", oldInterval, newInterval);
            writeUserActivityLog(admin, "edit mailing settings", actionDescription);
        }
    }

    private String getRegularMailingDescription(Mailing mailing, String sendActionDescription) {
        return String.format(
                "%s (%d) (delivery type: %s)",
                mailing.getShortname(),
                mailing.getId(),
                sendActionDescription
        );
    }

    private String getTriggerMailingDescription(Mailing mailing) {
        return String.format("%s (%d)", mailing.getShortname(), mailing.getId());
    }
}
