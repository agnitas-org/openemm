/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import org.agnitas.emm.core.useractivitylog.UserAction;

public interface ComMailingParameterService {
	List<ComMailingParameter> getAllParameters(int companyID, final Admin admin);

	List<ComMailingParameter> getMailingParameters(int companyId, int mailingId);

	List<ComMailingParameter> getParametersBySearchQuery(int companyID, String searchQuery, String mailingId, DateRange changeDate);

	ComMailingParameter getParameter(int mailingInfoID, final Admin admin);

	boolean saveParameter(ComMailingParameter parameter, final Admin admin);
	
	boolean insertParameter(ComMailingParameter parameter, final Admin admin);

	boolean updateParameter(ComMailingParameter parameter, final Admin admin);

	boolean deleteParameter(int mailingInfoID, final Admin admin);

	boolean updateParameters(int companyID, int mailingID, List<ComMailingParameter> parameterList, int adminId);

	boolean updateParameters(int companyID, int mailingID, List<ComMailingParameter> parameterList, int adminId, List<UserAction> userActions);
}
