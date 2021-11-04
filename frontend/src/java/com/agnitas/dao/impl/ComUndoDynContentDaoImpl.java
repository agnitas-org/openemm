/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.ComUndoDynContent;
import com.agnitas.beans.impl.ComUndoDynContentImpl;
import com.agnitas.dao.ComUndoDynContentDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;

public class ComUndoDynContentDaoImpl extends BaseDaoImpl implements ComUndoDynContentDao {
	private static final transient Logger logger = Logger.getLogger(ComUndoDynContentDaoImpl.class);

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
	
	private static final String DELETE_OUTDATED_CONTENT_STATEMENT =
		"DELETE FROM undo_dyn_content_tbl " +
		"WHERE undo_id <= ?";
	
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
	
	private final RowMapper<ComUndoDynContent> undoDynContentRowMapper = new RowMapper<>() {
		@Override
		public ComUndoDynContent mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			ComUndoDynContentImpl undoContent = new ComUndoDynContentImpl();
			
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
			update(logger, ComUndoDynContentDaoImpl.INSERT_CONTENT_STATEMENT, undoId, mailingId);
		}
	}
	
	@Override
	public List<ComUndoDynContent> getAllUndoDataForMailing(int mailingId, int undoId) {
		return select(logger, ComUndoDynContentDaoImpl.SELECT_CONTENTLIST_STATMENT, undoDynContentRowMapper, mailingId, undoId);
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoData(int undoId) {
		update(logger, DELETE_CONTENT_STATEMENT, undoId);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteOutdatedUndoData(int lastUndoId) {
		update(logger, DELETE_OUTDATED_CONTENT_STATEMENT, lastUndoId);
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoDataForMailing(int mailingID) {
		update(logger, DELETE_UNDODATA_FOR_MAILING_STATEMENT, mailingID);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoDataOverLimit(int mailingId, int undoId) {
		if (undoId != 0) {
			update(logger, DELETE_UNDODATA_OVER_LIMIT_FOR_MAILING_STATEMENT, mailingId , undoId);
		}
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public void deleteAddedDynContent(int mailingId, int undoId) {
		update(logger, DELETE_ADDED_CONTENT, mailingId, mailingId, undoId);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteByCompany(@VelocityCheck int companyId) {
		return deleteByCompany(logger, "undo_dyn_content_tbl", companyId);
	}
}
