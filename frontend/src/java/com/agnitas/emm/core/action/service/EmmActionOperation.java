/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service;

import java.util.Map;

import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;

public interface EmmActionOperation {
	
	/**
	 * Execute the given action operation.
	 * 
	 * @param operation operation to execute
	 * @param params context parameters
	 * @param errors errors reporting class
	 * 
	 * @return <code>true</code> if execution was successful
	 * 
	 * @throws Exception on errors during execution
	 */
	boolean execute(final AbstractActionOperationParameters operation, final Map<String, Object> params, final EmmActionOperationErrors errors) throws Exception;

    ActionOperationType processedType();

}
