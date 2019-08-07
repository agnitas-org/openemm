/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.dao.impl;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.operations.ActionOperationActivateDoubleOptInParameters;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public class ActionOperationActivateDoubleOptInDaoImpl extends AbstractActionOperationDaoImpl<ActionOperationActivateDoubleOptInParameters> {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ActionOperationActivateDoubleOptInDaoImpl.class);

	@Override
	protected void processGetOperation(ActionOperationActivateDoubleOptInParameters operation) {
		List<Map<String, Object>> result = select(logger, "SELECT for_all_lists, media_type FROM actop_activate_doi_tbl WHERE action_operation_id = ?", operation.getId());
		if (result.size() == 0) {
			operation.setForAllLists(false);
			processSaveOperation(operation);
		} else if (result.size() == 1) {
			final Map<String, Object> map = result.get(0);
			operation.setForAllLists(((Number) map.get("for_all_lists")).intValue() != 0);
			operation.setMediaType(extractMediaType(map));
		} else {
			throw new IncorrectResultSizeDataAccessException("Invalid multiple data found in actop_activate_doi_tbl for action_operation_id: " + operation.getId(), 1, result.size());
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processSaveOperation(ActionOperationActivateDoubleOptInParameters operation) {
		update(logger, 
				"INSERT INTO actop_activate_doi_tbl (action_operation_id, for_all_lists, media_type) VALUES (?,?,?)", 
				operation.getId(), 
				operation.isForAllLists() ? 1 : 0, 
				operation.getMediaType().getMediaCode());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processUpdateOperation(ActionOperationActivateDoubleOptInParameters operation) {
		update(logger, 
				"UPDATE actop_activate_doi_tbl SET for_all_lists = ?, media_type = ? WHERE action_operation_id = ?", 
				operation.isForAllLists() ? 1 : 0, 
				operation.getMediaType().getMediaCode(), 
				operation.getId());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processDeleteOperation(ActionOperationActivateDoubleOptInParameters operation) {
		update(logger, "DELETE FROM actop_activate_doi_tbl WHERE action_operation_id = ?", operation.getId());
	}
	
	/**
	 * Converts numeric representation of media type to {@link MediaTypes}. If numeric representation
	 * is <code>null</code>, <code>null</code> is returned.
	 * 
	 * @param map result map
	 * 
	 * @return {@link MediaTypes} or <code>null</code>
	 */
	private static final MediaTypes extractMediaType(final Map<String, Object> map) {
		final Object value = map.get("media_type");
		
		return value != null
				? MediaTypes.getMediaTypeForCode(((Number) value).intValue())
				: null;
	}
}
