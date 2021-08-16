/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.stat.service;

import java.util.List;

import org.springframework.dao.DataAccessException;

import com.agnitas.emm.core.stat.beans.MailingStatJobDescriptor;
import com.agnitas.emm.core.stat.beans.MailingStatisticTgtGrp;
import com.agnitas.emm.core.stat.service.impl.SummaryStatJobNotExistException;

public interface MailingSummaryStatisticJobService {
	int startSummaryStatisticJob(int mailingId, List<Integer> targetList, Integer recipientsType);
	MailingStatJobDescriptor getStatisticJob(int jobId) throws SummaryStatJobNotExistException;
	MailingStatisticTgtGrp getStatisticTgtGrp(int jobId, int targetGroupId) throws DataAccessException;
	public List<Integer> parseGroupList(String targetGroups) throws NumberFormatException;
	void removeExpiredData();
}
