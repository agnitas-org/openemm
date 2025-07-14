/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.beans.UndoDynContent;
import com.agnitas.beans.impl.UndoDynContentImpl;
import com.agnitas.dao.UndoDynContentDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UndoDynContentDaoImpl extends BaseDaoImpl implements UndoDynContentDao {
	
	private static final String INSERT_CONTENT_STATEMENT =
		"INSERT INTO undo_dyn_content_tbl " +
		"(dyn_content_id, dyn_name_id, target_id, dyn_order, dyn_content, mailing_id, company_id, undo_id) " +
		"(SELECT dyn_content_tbl.dyn_content_id, dyn_content_tbl.dyn_name_id, dyn_content_tbl.target_id, dyn_content_tbl.dyn_order, dyn_content_tbl.dyn_content, dyn_content_tbl.mailing_id, dyn_content_tbl.company_id,? FROM dyn_content_tbl WHERE mailing_id = ?)";
	
	private static final String SELECT_CONTENTLIST_STATMENT =
		"SELECT undo_dyn_content_tbl.*, dyn_name_tbl.dyn_name " +
		"FROM undo_dyn_content_tbl, dyn_name_tbl " +
		"WHERE undo_dyn_content_tbl.mailing_id = ? " +
		"AND undo_id = ? " +
		"AND dyn_name_tbl.dyn_name_id = undo_dyn_content_tbl.dyn_name_id";

	private static final String DELETE_CONTENT_STATEMENT =
		"DELETE FROM undo_dyn_content_tbl " +
		"WHERE undo_id = ?";
	
	private static final String DELETE_UNDODATA_FOR_MAILING_STATEMENT =
		"DELETE FROM undo_dyn_content_tbl " +
		"WHERE mailing_id = ?";

	private static final String DELETE_UNDODATA_OVER_LIMIT_FOR_MAILING_STATEMENT =
		"DELETE FROM undo_dyn_content_tbl " +
		"WHERE mailing_id = ? AND undo_id <= ?";
	
	private static final String DELETE_ADDED_CONTENT =
		"DELETE FROM dyn_content_tbl " +
		"WHERE mailing_id = ? " +
		"AND dyn_content_id NOT IN (" +
		"  SELECT dyn_content_id " +
		"  FROM undo_dyn_content_tbl " +
		"  WHERE mailing_id = ? " +
		"  AND undo_id = ?)";

	// --------------------------------------------------------------------------------------------------------------------------------------- JDBC helper
	
	private final RowMapper<UndoDynContent> undoDynContentRowMapper = new RowMapper<>() {
		@Override
		public UndoDynContent mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			UndoDynContentImpl undoContent = new UndoDynContentImpl();
			
			undoContent.setCompanyID(resultSet.getInt("COMPANY_ID"));
			undoContent.setDynContent(resultSet.getString("DYN_CONTENT"));
			undoContent.setId(resultSet.getInt("DYN_CONTENT_ID"));
			undoContent.setDynNameID(resultSet.getInt("DYN_NAME_ID"));
			undoContent.setDynName(resultSet.getString("DYN_NAME"));
			undoContent.setDynOrder(resultSet.getInt("DYN_ORDER"));
			undoContent.setMailingID(resultSet.getInt("MAILING_ID"));
			undoContent.setTargetID(resultSet.getInt("TARGET_ID"));
			undoContent.setUndoId(resultSet.getInt("UNDO_ID"));
			
			return undoContent;
		}
	};

	// --------------------------------------------------------------------------------------------------------------------------------------- business logic
	
	@Override
	@DaoUpdateReturnValueCheck
	public void saveUndoData(int mailingId, int undoId) {
		if (mailingId != 0) {
			update(UndoDynContentDaoImpl.INSERT_CONTENT_STATEMENT, undoId, mailingId);
		}
	}
	
	@Override
	public List<UndoDynContent> getAllUndoDataForMailing(int mailingId, int undoId) {
		return select(UndoDynContentDaoImpl.SELECT_CONTENTLIST_STATMENT, undoDynContentRowMapper, mailingId, undoId);
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoData(int undoId) {
		update(DELETE_CONTENT_STATEMENT, undoId);
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoDataForMailing(int mailingID) {
		update(DELETE_UNDODATA_FOR_MAILING_STATEMENT, mailingID);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoDataOverLimit(int mailingId, int undoId) {
		if (undoId != 0) {
			update(DELETE_UNDODATA_OVER_LIMIT_FOR_MAILING_STATEMENT, mailingId , undoId);
		}
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public void deleteAddedDynContent(int mailingId, int undoId) {
		update(DELETE_ADDED_CONTENT, mailingId, mailingId, undoId);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteByCompany(int companyId) {
		return deleteByCompany("undo_dyn_content_tbl", companyId);
	}

	@Override
	public void deleteUndoData(List<Integer> undoIds) {
		if (CollectionUtils.isEmpty(undoIds)) {
			return;
		}

		String query = "DELETE FROM undo_dyn_content_tbl WHERE " + makeBulkInClauseForInteger("undo_id", undoIds);
		update(query);
	}
}
