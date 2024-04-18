/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.service;

import static org.agnitas.emm.core.recipient.RecipientUtils.getSalutationByGenderId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.agnitas.emm.core.recipient.RecipientUtils;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.emm.core.recipient.dto.RecipientDto;
import com.agnitas.emm.core.recipient.dto.SaveRecipientDto;
import com.agnitas.emm.core.service.RecipientFieldService.RecipientStandardField;

@Service("recipientLogService")
public class RecipientLogServiceImpl implements RecipientLogService {

    private static final Logger logger = LogManager.getLogger(RecipientLogServiceImpl.class);

    @Override
    public UserAction getRecipientFieldsBulkChangeLog(int targetId, int mailinglistId, Map<String, Object> affectedFields) {
        List<String> changes = new ArrayList<>();
        UserAction userAction = null;
        
        for (Map.Entry<String, Object> field: affectedFields.entrySet()) {
            changes.add(String.format("Name: %s, Value: %s;", field.getKey(),
                    StringUtils.defaultString((String) field.getValue(),"emptied")));
        }
        
        if (changes.size() > 0) {
            userAction = new UserAction("recipient change bulk", String.format("Target ID: %d, Mailinglist ID: %d, Affected fields: %n%s",
                    targetId, mailinglistId, StringUtils.join(changes, "\n")));
        }
        
        return userAction;
    }

    @Override
    public UserAction getRecipientChangesLog(Admin admin, RecipientDto existedRecipient, SaveRecipientDto recipient) {
        List<String> changes = new ArrayList<>();
        UserAction userAction = null;
        try {

            //log column number changes
            if (existedRecipient.getGender() != recipient.getGender()) {
                changes.add(String.format(
                        "Salutation changed from %s to %s.",
                        getSalutationByGenderId(existedRecipient.getGender()), getSalutationByGenderId(recipient.getGender())
                ));
            }

            //log title changes
            if (!StringUtils.equals(existedRecipient.getTitle(), recipient.getTitle())) {
                changes.add(getFieldChangeLog("Title", existedRecipient.getTitle(), recipient.getTitle()));
            }

            //log firstname changes
            if (!StringUtils.equals(existedRecipient.getFirstname(), recipient.getFirstname())) {
                changes.add(getFieldChangeLog("First name", existedRecipient.getFirstname(), recipient.getFirstname()));
            }

            //log lastname changes
            if (!StringUtils.equals(existedRecipient.getLastname(), recipient.getLastname())) {
                changes.add(getFieldChangeLog("Last name", existedRecipient.getLastname(), recipient.getLastname()));
            }

            //log email changes
            if (!StringUtils.equals(existedRecipient.getEmail(), recipient.getEmail())) {
                changes.add(String.format(
                        "Email changed from %s to %s.",
                        existedRecipient.getEmail(), recipient.getEmail()
                ));
            }

            //log mailType changes
            if (existedRecipient.getMailtype() != recipient.getMailtype()) {
                changes.add(String.format(
                        "Mailtype changed from %d to %d.",
                        existedRecipient.getMailtype(), recipient.getMailtype()
                ));
            }

            //log tracking veto changes
            if (existedRecipient.isTrackingVeto() != recipient.isTrackingVeto()) {
                changes.add(String.format(
                        "Tracking veto is %s.",
                        recipient.isTrackingVeto() ? "activated" : "deactivated"
                ));
            }

            for (ProfileField column : existedRecipient.getDbColumns().values()) {
                String columnName = column.getColumn();

                if (!RecipientStandardField.getAllRecipientStandardFieldColumnNames().contains(columnName) && !RecipientUtils.hasSupplementalSuffix(columnName)) {
                    String existingValue = existedRecipient.getColumnFormattedValue(admin, columnName);
                    String newValue = recipient.getStringValue(columnName);
                    if (!StringUtils.equals(existingValue, newValue)) {
                        changes.add(getFieldChangeLog("Recipient " + columnName, existingValue, newValue));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Log Recipient changes error" + e);
        }

        if (changes.size() > 0) {
            userAction = new UserAction("edit recipient", String.format("%s%n%s",
                    RecipientUtils.getRecipientDescription(existedRecipient.getId(), existedRecipient.getFirstname(), existedRecipient.getLastname(), existedRecipient.getEmail()),
                    StringUtils.join(changes, "\n")));
        }

        return userAction;
    }

    private String getFieldChangeLog(String title, String value, String newValue) {
        if (StringUtils.isNotEmpty(value) && StringUtils.isNotEmpty(newValue)) {
            return String.format("%s changed from %s to %s", title, value, newValue);
        } else if(StringUtils.isNotEmpty(value)) {
            return String.format("%s %s removed", title, value);
        } else if(StringUtils.isNotEmpty(newValue)) {
            return String.format("%s %s added", title, newValue);
        }

        return "";
    }
}
