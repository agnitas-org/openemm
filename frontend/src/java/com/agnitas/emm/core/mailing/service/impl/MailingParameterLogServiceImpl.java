/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mailing.service.MailingParameterLogService;

@Service("mailingParameterLogService")
public class MailingParameterLogServiceImpl implements MailingParameterLogService {
    
    private static final String PLACEHOLDER_EMPTY = "empty";
    
    @Override
	public List<UserAction> getMailingParametersChangeLog(int mailingId, Map<Integer, MailingParameter> parametersOld, Map<Integer, MailingParameter> parametersNew) {
		List<UserAction> userActions = new ArrayList<>();
		
		CollectionUtils.union(parametersOld.keySet(), parametersNew.keySet()).forEach(p -> {
			List<String> changes = getParameterChanges(parametersOld.get(p), parametersNew.get(p));
			if (!changes.isEmpty()) {
				userActions.add(new UserAction("edit mailing parameter",
						String.format("Mailing ID (%d). Mailing parameter ID (%d) %n%s", mailingId, p,
								StringUtils.join(changes, "; "))));
			}
		});
		
		return userActions;
	}
    
    @Override
    public UserAction getMailingParameterChangeLog(int mailingId, int parameterId, MailingParameter parameterOld, MailingParameter parameterNew) {
        UserAction userAction = null;

        List<String> changes = getParameterChanges(parameterOld, parameterNew);
        if (!changes.isEmpty()) {
            userAction = new UserAction("edit mailing parameter",
                    String.format("Mailing ID (%d) %n Mailing parameter ID (%d) %n%s", mailingId, parameterId,
                            StringUtils.join(changes, "\n")));
        }
        
        return userAction;
    }
    
    @Override
    public UserAction getMailingParameterCreateLog(int mailingId, MailingParameter parameterNew) {
        UserAction userAction = null;

        List<String> changes = getParameterChanges(null, parameterNew);
        if (!changes.isEmpty()) {
            userAction = new UserAction("create mailing parameter",
                    String.format("Mailing ID (%d). Mailing parameter ID (%d) %n%s", mailingId, parameterNew.getMailingInfoID(),
                            StringUtils.join(changes, "; ")));
        }
        
        return userAction;
    }
    
    @Override
    public UserAction getMailingParameterDeleteLog(Collection<Integer> ids) {
		return new UserAction("delete mailing parameters",
				String.format("Delete mailing parameter with IDs: (%s)", StringUtils.join(ids, ", ")));
    }
    
    @Override
	public List<String> getParameterChanges(MailingParameter oldParameter, MailingParameter newParameter) {
		List<String> changes = new ArrayList<>();
		
		if (oldParameter != null && newParameter != null) {
			String nameOld = StringUtils.defaultIfEmpty(oldParameter.getName(), PLACEHOLDER_EMPTY);
			String nameNew = StringUtils.defaultIfEmpty(newParameter.getName(), PLACEHOLDER_EMPTY);
			if (!StringUtils.equals(nameOld, nameNew)) {
				changes.add(String.format("Changed name from '%s' to '%s'.", nameOld, nameNew));
			}
			
			String valueOld = StringUtils.defaultIfEmpty(oldParameter.getValue(), PLACEHOLDER_EMPTY);
			String valueNew = StringUtils.defaultIfEmpty(newParameter.getValue(), PLACEHOLDER_EMPTY);
			if (!StringUtils.equals(valueOld, valueNew)) {
				changes.add(String.format("Changed value from '%s' to '%s'.", valueOld, valueNew));
			}
			
			String descriptionOld = StringUtils.defaultIfEmpty(oldParameter.getDescription(), PLACEHOLDER_EMPTY);
			String descriptionNew = StringUtils.defaultIfEmpty(newParameter.getDescription(), PLACEHOLDER_EMPTY);
			if (!StringUtils.equals(descriptionOld, descriptionNew)) {
				changes.add(String.format("Changed description from '%s' to '%s'.", descriptionOld, descriptionNew));
			}
			int oldMailingId = oldParameter.getMailingID();
			int newMailingId = newParameter.getMailingID();
			if (oldMailingId != newMailingId) {
				changes.add(String.format("Changed mailing from id = '%d' to id = '%d'.", oldMailingId, newMailingId));
			}
		} else if (oldParameter != null) {
			String nameOld = StringUtils.defaultIfEmpty(oldParameter.getName(), PLACEHOLDER_EMPTY);
			String valueOld = StringUtils.defaultIfEmpty(oldParameter.getValue(), PLACEHOLDER_EMPTY);
			
			changes.add(String.format("Deleted parameter '%s' = '%s'.", nameOld, valueOld));
		} else if (newParameter != null) {
			String nameNew = StringUtils.defaultIfEmpty(newParameter.getName(), PLACEHOLDER_EMPTY);
			String valueNew = StringUtils.defaultIfEmpty(newParameter.getValue(), PLACEHOLDER_EMPTY);
			
			changes.add(String.format("Added parameter '%s' = '%s'.", nameNew, valueNew));
		}
		return changes;
	}
}
