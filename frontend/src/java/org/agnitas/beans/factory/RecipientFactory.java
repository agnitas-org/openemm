/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.factory;

import org.agnitas.beans.Recipient;

/**
 * Abstract factory for recipients.
 */
public interface RecipientFactory {
	
	/**
	 * Creates a new recipient, but does not load profile field data.
	 * 
	 * @return new recipient.
	 */
    public Recipient newRecipient();
	
	/**
	 * Creates a new recipient. Profile field structure is loaded, if company ID > 0.
	 * 
	 * @param companyID company ID of new recipient
	 * 
	 * @return new recipient
	 */
	public Recipient newRecipient(final int companyID);
}
