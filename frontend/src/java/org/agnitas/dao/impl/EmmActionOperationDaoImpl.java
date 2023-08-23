/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.agnitas.beans.factory.ActionOperationFactory;
import org.agnitas.dao.EmmActionOperationDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.dao.ActionOperationDao;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;

public class EmmActionOperationDaoImpl extends BaseDaoImpl implements EmmActionOperationDao, InitializingBean {

	private static final transient Logger logger = LogManager.getLogger(EmmActionOperationDaoImpl.class);

	private ActionOperationFactory actionOperationFactory;

	
	// TODO Change type of key to ActionOperationType (-> Application Context!)
	private Map<String, ActionOperationDao> daos = new HashMap<>();

	@Override
	public void afterPropertiesSet() throws Exception {
		// nothing to do
	}

	@Override
	public List<AbstractActionOperationParameters> getOperations(int actionId, int companyId) {
		List<Map<String, Object>> list = select(logger, "SELECT action_operation_id, action_id, company_id, type FROM actop_tbl WHERE action_id = ? AND company_id = ? ORDER BY action_operation_id", actionId, companyId);
		List<AbstractActionOperationParameters> resultList = new ArrayList<>(list.size());
		for (Map<String, Object> row : list) {
			String type = (String) row.get("type");
			AbstractActionOperationParameters actionOperation = actionOperationFactory.newActionOperation(type);
			actionOperation.setId(((Number) row.get("action_operation_id")).intValue());
			actionOperation.setActionId(((Number) row.get("action_id")).intValue());
			actionOperation.setCompanyId(((Number) row.get("company_id")).intValue());
			ActionOperationDao dao = daos.get(type);
			// check for subtable dao
			if (dao != null) {
				dao.getOperation(actionOperation);
			}
			resultList.add(actionOperation);
		}
		return resultList;
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public void saveOperation(AbstractActionOperationParameters operation) {
		ActionOperationDao dao = daos.get(operation.getOperationType().getName());
		if (operation.getId() == 0) {
			if (isOracleDB()) {
				operation.setId(selectInt(logger, "SELECT actop_tbl_seq.NEXTVAL FROM DUAL"));
				update(logger, "INSERT INTO actop_tbl (action_operation_id, action_id, company_id, type) VALUES (?, ?, ?, ?)", operation.getId(), operation.getActionId(), operation.getCompanyId(), operation.getOperationType().getName());
			} else {
				int newActionOperationId = insertIntoAutoincrementMysqlTable(logger, "action_operation_id", "INSERT INTO actop_tbl (action_id, company_id, type) VALUES (?, ?, ?)", operation.getActionId(), operation.getCompanyId(), operation.getOperationType().getName());
				operation.setId(newActionOperationId);
			}
			
			// check for subtable dao
			if (dao != null) {
				dao.saveOperation(operation);
			}
		} else {
			// update only subtable
			if (dao != null) {
				dao.updateOperation(operation);
			}
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteOperation(AbstractActionOperationParameters operation) {
		ActionOperationDao dao = daos.get(operation.getOperationType().getName());
		if (dao != null) {
			dao.deleteOperation(operation);
		}
		update(logger, "DELETE FROM actop_tbl WHERE action_operation_id = ? AND company_id = ?", operation.getId(), operation.getCompanyId());
	}

	@Override
	public void deleteOperations(int actionID, int companyId) {
		List<AbstractActionOperationParameters> operations = getOperations(actionID, companyId);
		for (AbstractActionOperationParameters operation : operations) {
			deleteOperation(operation);
		}
	}

	public void setActionOperationFactory(ActionOperationFactory actionOperationFactory) {
		this.actionOperationFactory = actionOperationFactory;
	}

	public void setDaos(Map<String, ActionOperationDao> daos) {
		this.daos = daos;
	}
}
