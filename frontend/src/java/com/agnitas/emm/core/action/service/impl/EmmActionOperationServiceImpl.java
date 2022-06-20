/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationService;
import com.agnitas.emm.core.action.service.UnknownEmmActionExecutor;

public class EmmActionOperationServiceImpl implements EmmActionOperationService, InitializingBean {
	
	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(EmmActionOperationServiceImpl.class);

	private List<EmmActionOperation> executors = new ArrayList<>();

	public void setExecutors(List<EmmActionOperation> executors) {
		this.executors = executors;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// nothing to do
	}

	@Override
	public boolean executeOperation(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors errors) throws Exception {
		EmmActionOperation executor = getExecutor(operation.getOperationType());
		final boolean result = executor.execute(operation, params, errors);

		if(!result || !errors.isEmpty()) {
			LOGGER.error(String.format(
					"Action operation %d (type %s, company %d, action %d) returned result %b and action errors %s",
					operation.getId(),
					operation.getOperationType(),
					operation.getCompanyId(),
					operation.getActionId(),
					result,
					errors));
		}
		
		return result && errors.isEmpty();
	}

	public EmmActionOperation getExecutor(ActionOperationType type) throws UnknownEmmActionExecutor {
		for (EmmActionOperation executor: executors) {
			if (executor.processedType() == type) {
				return executor;
			}
		}

		throw new UnknownEmmActionExecutor(type);
	}
}
