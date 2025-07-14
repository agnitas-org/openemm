/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.dao;

import java.util.List;

import com.agnitas.emm.core.service.RecipientFieldDescription;

public interface RecipientFieldDao {
	List<RecipientFieldDescription> getRecipientFields(int companyID);
	List<String> getFieldNames(int companyId);
	void saveRecipientField(int companyID, RecipientFieldDescription recipientFieldDescription) throws Exception;
	void deleteRecipientField(int companyID, String recipientFieldName);
	boolean isReservedKeyWord(String fieldname);
	boolean hasRecipients(int companyID);
	boolean hasRecipientsWithNullValue(int companyID, String columnName);
	int countCustomerEntries(int companyID);
}
