/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import org.agnitas.actions.EmmAction;
import org.agnitas.dao.impl.EmmActionDaoImpl;
import org.apache.log4j.Logger;

import com.agnitas.dao.DaoUpdateReturnValueCheck;

public class ComEmmActionDaoImpl extends EmmActionDaoImpl {
	
	/**
	 * The logger.
	 */
	private static final transient Logger logger = Logger.getLogger(ComEmmActionDaoImpl.class);

	@Override
	@DaoUpdateReturnValueCheck
	public int saveEmmAction(EmmAction action) {
		if (action == null || action.getCompanyID() == 0) {
			return 0;
		} else {
			try {
				if (action.getId() == 0) {
					if (isOracleDB()) {
						action.setId(selectInt(logger, "SELECT rdir_action_tbl_seq.NEXTVAL FROM DUAL"));
						
						String sql = "INSERT INTO rdir_action_tbl (action_id, company_id, description, shortname, creation_date, change_date, action_type, active)"
							+ " VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?)";
								
						update(logger,
							sql,
							action.getId(),
							action.getCompanyID(),
							action.getDescription(),
							action.getShortname(),
							action.getType(),
							action.getIsActive() ? 1 : 0);
					} else {
						String sql = "INSERT INTO rdir_action_tbl (company_id, description, shortname, creation_date, change_date, action_type, active)"
							+ " VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?)";
						int newID = insertIntoAutoincrementMysqlTable(logger, "action_id", sql, 
							action.getCompanyID(),
							action.getDescription(),
							action.getShortname(),
							action.getType(),
							action.getIsActive() ? 1 : 0);

						// set the new id to refresh the item
						action.setId(newID);
					}
				} else {
					String updateSql = "UPDATE rdir_action_tbl SET description = ?, shortname = ?, change_date = CURRENT_TIMESTAMP, action_type = ?, active = ? WHERE action_id = ? AND company_id = ?";
					update(logger, updateSql, action.getDescription(), action.getShortname(), action.getType(), action.getIsActive() ? 1 : 0, action.getId(), action.getCompanyID());
				}

				return action.getId();
			} catch (Exception e) {
				logger.error("Couldn't save EMM action: " + e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}
	}
}
