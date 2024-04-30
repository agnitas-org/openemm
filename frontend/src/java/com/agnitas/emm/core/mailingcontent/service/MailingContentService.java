/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.service;

import java.util.List;

import com.agnitas.service.ServiceResult;
import com.agnitas.beans.Mailing;
import org.agnitas.emm.core.useractivitylog.UserAction;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;

public interface MailingContentService {
    ServiceResult<List<UserAction>> updateDynContent(Mailing mailing, DynTagDto dynTagDto, Admin admin) throws Exception;
    DynTagDto getDynTag(int companyId, int dynNameId);
	void buildDependencies(int mailingID, int companyID) throws Exception;
}
