/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.dao.impl;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.operations.ActionOperationContentViewParameters;

import java.util.Map;

public class ActionOperationContentViewDaoImpl extends AbstractActionOperationDaoImpl<ActionOperationContentViewParameters> {

	@Override
	protected void processGetOperation(ActionOperationContentViewParameters operation) {
		Map<String, Object> row = selectSingleRow("select tag_name from actop_content_view_tbl where action_operation_id=?", operation.getId());
		operation.setTagName((String) row.get("tag_name"));
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processSaveOperation(ActionOperationContentViewParameters operation) {
		update("insert into actop_content_view_tbl (action_operation_id, tag_name) values (?,?)",
				operation.getId(),
				operation.getTagName());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processUpdateOperation(ActionOperationContentViewParameters operation) {
		update("update actop_content_view_tbl set tag_name=? where action_operation_id=?",
				operation.getTagName(),
				operation.getId());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processDeleteOperation(ActionOperationContentViewParameters operation) {
		update("delete from actop_content_view_tbl where action_operation_id=?", operation.getId());
	}

}
