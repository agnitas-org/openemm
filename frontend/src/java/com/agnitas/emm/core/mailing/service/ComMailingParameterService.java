/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import java.util.List;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;

public interface ComMailingParameterService {
	List<ComMailingParameter> getAllParameters(@VelocityCheck int companyID, final ComAdmin admin);

	List<ComMailingParameter> getMailingParameters(@VelocityCheck int companyId, int mailingId);

	List<ComMailingParameter> getParametersBySearchQuery(int companyID, String searchQuery, String mailingId);

	ComMailingParameter getParameter(int mailingInfoID, final ComAdmin admin);

	boolean insertParameter(ComMailingParameter parameter, final ComAdmin admin);

	boolean updateParameter(ComMailingParameter parameter, final ComAdmin admin);

	boolean deleteParameter(int mailingInfoID, final ComAdmin admin);

	boolean updateParameters(@VelocityCheck int companyID, int mailingID, List<ComMailingParameter> parameterList, int adminId);

	boolean updateParameters(@VelocityCheck int companyID, int mailingID, List<ComMailingParameter> parameterList, int adminId, List<UserAction> userActions);
}
