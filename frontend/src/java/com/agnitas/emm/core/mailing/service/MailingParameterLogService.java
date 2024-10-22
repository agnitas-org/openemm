/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.agnitas.emm.core.useractivitylog.UserAction;

import com.agnitas.emm.core.mailing.bean.ComMailingParameter;

public interface MailingParameterLogService {
    
    List<UserAction> getMailingParametersChangeLog(int mailingId, Map<Integer, ComMailingParameter> parametersOld, Map<Integer, ComMailingParameter> parametersNew);
    
    UserAction getMailingParameterChangeLog(int mailingId, int parameterId, ComMailingParameter parameterOld, ComMailingParameter parameterNew);
    
    UserAction getMailingParameterCreateLog(int mailingId, ComMailingParameter parameterNew);
    
    UserAction getMailingParameterDeleteLog(Collection<Integer> ids);
    
    List<String> getParameterChanges(ComMailingParameter oldParameter, ComMailingParameter newParameter);
}
