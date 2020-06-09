/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.agnitas.emm.core.useractivitylog.UserAction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service("recipientLogService")
public class RecipientLogServiceImpl implements RecipientLogService {
    
    @Override
    public UserAction getRecipientFieldsBulkChangeLog(int targetId, int mailinglistId, Map<String, Object> affectedFields) {
        List<String> changes = new ArrayList<>();
        UserAction userAction = null;
        
        for(Map.Entry<String, Object> field: affectedFields.entrySet()) {
            changes.add(String.format("Name: %s, Value: %s;", field.getKey(),
                    StringUtils.defaultString((String) field.getValue(),"emptied")));
        }
        
        if (changes.size() > 0) {
            userAction = new UserAction("recipient change bulk", String.format("Target ID: %d, Mailinglist ID: %d, Affected fields: %n%s",
                    targetId, mailinglistId, StringUtils.join(changes, "\n")));
        }
        
        return userAction;
    }

}
