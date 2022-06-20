/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.ecs.service;

import java.util.Map;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.dao.ComRecipientDao;

/**
 * Service class that provides recipients list for ecs-page
 */
public interface EcsRecipientsProvider {

	/**
	 * Gets all "test" and "admin" recipients for mailing's mailing list.
	 * The map entry is "recipientId" -> "recipientFirstName recipientLastName &lt;recipientEmail&gt;"
	 *
	 * @param mailingId mailing id
	 * @param companyId company id
	 * @return test and admin recipients for mailing's mailing list.
	 */
	public Map<Integer, String> getTestAndAdminRecipients(int mailingId, @VelocityCheck int companyId);

	/**
	 * Setter for recipient Dao
	 *
	 * @param recipientDao recipient Dao
	 */
	public void setRecipientDao(ComRecipientDao recipientDao);

}
