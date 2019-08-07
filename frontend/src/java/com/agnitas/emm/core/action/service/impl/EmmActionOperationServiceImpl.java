/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationService;

public class EmmActionOperationServiceImpl implements EmmActionOperationService, InitializingBean {

	// TODO: Change type of key to ActionOperationType (-> Spring context!)
	private Map<String, EmmActionOperation> operations = new HashMap<>();

	@Override
	public void afterPropertiesSet() throws Exception {
		// nothing to do
	}

	@Override
	public boolean executeOperation(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors errors) throws Exception {
		final boolean result = operations.get(operation.getOperationType().getName()).execute(operation, params, errors);
		
		return result && errors.isEmpty();
	}

	public void setOperations(Map<String, EmmActionOperation> operations) {
		this.operations = operations;
	}

}
