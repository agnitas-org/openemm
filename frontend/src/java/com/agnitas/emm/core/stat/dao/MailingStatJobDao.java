/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.stat.dao;

import java.time.ZonedDateTime;
import java.util.List;

import com.agnitas.emm.core.stat.beans.MailingStatJobDescriptor;

public interface MailingStatJobDao {

	public int createMailingStatJob(MailingStatJobDescriptor job);
	
	/**
	 * Changes job status to given and changeDate to now.
	 * @param status
	 */
	public void updateMailingStatJob(int id, int status, String statusDescription);
	
	public MailingStatJobDescriptor getMailingStatJob(int id);
	
	/**
	 * Looks for jobs with given properties not older than (now - maxAge).
	 * @param mailingId
	 * @param recipientsTyp
	 * @param targetGroups
	 * @param maxAge
	 * @return Descending by creation time sorted jobs list.
	 */
	public List<MailingStatJobDescriptor> findMailingStatJobs(int mailingId, int recipientsTyp, String targetGroups, int maxAgeSeconds);

	public void removeExpiredMailingStatJobs(final ZonedDateTime threshold);
	
	public void deleteMailingStatJob(int id);
	
}
