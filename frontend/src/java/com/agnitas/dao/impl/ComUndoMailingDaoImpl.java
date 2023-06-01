/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.ComUndoMailing;
import com.agnitas.beans.impl.ComUndoMailingImpl;
import com.agnitas.dao.ComUndoDynContentDao;
import com.agnitas.dao.ComUndoMailingComponentDao;
import com.agnitas.dao.ComUndoMailingDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;

public class ComUndoMailingDaoImpl extends BaseDaoImpl implements ComUndoMailingDao {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ComUndoMailingDaoImpl.class);
	
	private static final String SELECT_MAILING_UNDO_STATEMENT = "SELECT * FROM undo_mailing_tbl WHERE mailing_id = ? AND undo_id = ?";

	private static final String SELECT_LAST_MAILING_UNDO_ID_STATEMENT = "SELECT max(undo_id) AS last_undo_id FROM undo_mailing_tbl WHERE mailing_id = ?";
	
	private static final String INSERT_MAILING_UNDO_STATEMENT =
		"INSERT INTO undo_mailing_tbl " +
		"(mailing_id, undo_id, undo_creation_date, undo_admin_id) " +
		"VALUES (?, ?, ?, ?)";
	
	private static final String DELETE_MAILING_STATEMENT =
		"DELETE FROM undo_mailing_tbl " +
		"WHERE undo_id = ?";
	
	private static final String DELETE_UNDODATA_FOR_MAILING_STATEMENT =
		"DELETE FROM undo_mailing_tbl " +
		"WHERE mailing_id = ?";
	
	private static final String SELECT_FIRST_UNDO_ID_OVER_LIMIT =
		"SELECT max(undo_id) " +
		"FROM (" +
		"SELECT undo_id, rownum AS x "+
		"FROM (SELECT undo_id FROM undo_mailing_tbl WHERE mailing_id = ? ORDER BY undo_id DESC)) WHERE x > ?";
	
	private static final String SELECT_FIRST_UNDO_ID_OVER_LIMIT_SQL =
		"SELECT max(undo_id) " +
		"FROM (" +
		"SELECT undo_id "+
		"FROM (SELECT undo_id FROM undo_mailing_tbl WHERE mailing_id = ?) sel2 ORDER BY undo_id DESC LIMIT ?,1000) sel1 ";

	private static final String DELETE_UNDODATA_OVER_LIMIT_FOR_MAILING_STATEMENT =
		"DELETE FROM undo_mailing_tbl " +
		"WHERE mailing_id = ? AND undo_id <= ?";

	
	// --------------------------------------------------------------------------------------------------------------------------------------- DI code

	private ComUndoMailingComponentDao undoComponentDao;
	private ComUndoDynContentDao undoDynContentDao;
	
	public void setUndoDynContentDao(ComUndoDynContentDao undoDynContentDao) {
		this.undoDynContentDao = undoDynContentDao;
	}

	public void setUndoMailingComponentDao(ComUndoMailingComponentDao undoMailingComponentDao) {
		this.undoComponentDao = undoMailingComponentDao;
	}

	// --------------------------------------------------------------------------------------------------------------------------------------- JDBC helper
    protected class ComUndoMailing_RowMapper implements RowMapper<ComUndoMailing> {
		@Override
		public ComUndoMailing mapRow(ResultSet resultSet, int row) throws SQLException {
			ComUndoMailingImpl undo = new ComUndoMailingImpl();
			
			undo.setId(resultSet.getInt("mailing_id"));
			undo.setUndoId(resultSet.getInt("undo_id"));
			undo.setUndoCreationDate(resultSet.getTimestamp("undo_creation_date"));
			
			return undo;
		}
	}
	
	// --------------------------------------------------------------------------------------------------------------------------------------- business logic

	@Override
	public ComUndoMailing getUndoData(int mailingId, int undoId) {
		return selectObjectDefaultNull(logger, SELECT_MAILING_UNDO_STATEMENT, new ComUndoMailing_RowMapper(), mailingId, undoId);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void saveUndoData(int mailingId, int undoId, Date undoCreationDate, int undoAdminId) {
		if(mailingId == 0) {
			return;
		}

		update(logger, INSERT_MAILING_UNDO_STATEMENT, mailingId, undoId, undoCreationDate, undoAdminId);
		
		this.undoComponentDao.saveUndoData(mailingId, undoId);
		this.undoDynContentDao.saveUndoData(mailingId, undoId);
	}
	@Override
	public ComUndoMailing getLastUndoData(int mailingId) {
		int undoId = selectInt(logger, SELECT_LAST_MAILING_UNDO_ID_STATEMENT, mailingId);
		
		if( undoId == 0) {
			return null;
		} else {
			return getUndoData(mailingId, undoId);
		}
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoData(int undoId) {
		update(logger, DELETE_MAILING_STATEMENT, new Object[] { undoId });
	}
	
	@Override
	public final List<Integer> findUndoIdsToCleanup(final int retentionTime) {
		final String sql = isOracleDB()
				? "select distinct(undo_id) from (SELECT undo_id FROM undo_mailing_tbl WHERE undo_creation_date <= SYSDATE - ? union all SELECT DISTINCT um.undo_id FROM maildrop_status_tbl mds, mailing_account_tbl ma, undo_mailing_tbl um  WHERE um.mailing_id = mds.mailing_id AND um.mailing_id = ma.mailing_id AND mds.status_field = 'W' AND mds.genstatus = 3) subsel"
				: "select distinct(undo_id) from (SELECT undo_id FROM undo_mailing_tbl WHERE undo_creation_date <= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL ? DAY) union all SELECT DISTINCT um.undo_id FROM maildrop_status_tbl mds, mailing_account_tbl ma, undo_mailing_tbl um WHERE um.mailing_id = mds.mailing_id AND um.mailing_id = ma.mailing_id AND mds.status_field = 'W' AND mds.genstatus = 3) subsel";
		
		return select(logger, sql, IntegerRowMapper.INSTANCE, retentionTime);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoDataForMailing(int mailingID) {
		update(logger, DELETE_UNDODATA_FOR_MAILING_STATEMENT, new Object[] { mailingID });
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoDataOverLimit(int mailingId, int undoId) {
		if( undoId == 0) {
			return;
		}
		
		update(logger, DELETE_UNDODATA_OVER_LIMIT_FOR_MAILING_STATEMENT, new Object[] { mailingId , undoId });
	}

	@Override
    public int getUndoIdOverLimit(int mailingId, int undoLimit) {
        if (isOracleDB()) {
            return selectInt(logger, SELECT_FIRST_UNDO_ID_OVER_LIMIT, mailingId, undoLimit);
        } else {
            return selectInt(logger, SELECT_FIRST_UNDO_ID_OVER_LIMIT_SQL, mailingId, undoLimit);
        }
    }

}
