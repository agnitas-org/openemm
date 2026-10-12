/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.action.dao;

import java.util.List;


import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import org.springframework.transaction.annotation.Transactional;

public interface EmmActionOperationDao {
	
	List<AbstractActionOperationParameters> getOperations(int actionId, int companyId);

	List<ActionOperationType> getOperationsTypes(int actionId, int companyId);

	void saveOperation(AbstractActionOperationParameters operation);
	
	void deleteOperation(AbstractActionOperationParameters operation);

    @Transactional
    void deleteByActionId(int actionId, int companyId);

	void deleteOperations(int actionID, int companyId);
	
}
