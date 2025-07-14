/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.useractivitylog.bean.UserAction;

import com.agnitas.emm.core.mailing.bean.MailingParameter;

public interface MailingParameterLogService {
    
    List<UserAction> getMailingParametersChangeLog(int mailingId, Map<Integer, MailingParameter> parametersOld, Map<Integer, MailingParameter> parametersNew);
    
    UserAction getMailingParameterChangeLog(int mailingId, int parameterId, MailingParameter parameterOld, MailingParameter parameterNew);
    
    UserAction getMailingParameterCreateLog(int mailingId, MailingParameter parameterNew);
    
    UserAction getMailingParameterDeleteLog(Collection<Integer> ids);
    
    List<String> getParameterChanges(MailingParameter oldParameter, MailingParameter newParameter);
}
