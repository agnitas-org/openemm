/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.beans.UndoMailing;
import com.agnitas.beans.impl.UndoMailingImpl;
import com.agnitas.dao.UndoDynContentDao;
import com.agnitas.dao.UndoMailingComponentDao;
import com.agnitas.dao.UndoMailingDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class UndoMailingDaoImpl extends BaseDaoImpl implements UndoMailingDao {
	
	private static final String SELECT_MAILING_UNDO_STATEMENT = "SELECT * FROM undo_mailing_tbl WHERE mailing_id = ? AND undo_id = ?";

	private static final String SELECT_LAST_MAILING_UNDO_ID_STATEMENT = "SELECT max(undo_id) AS last_undo_id FROM undo_mailing_tbl WHERE mailing_id = ?";
		
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

	private UndoMailingComponentDao undoComponentDao;
	private UndoDynContentDao undoDynContentDao;
	
	public void setUndoDynContentDao(UndoDynContentDao undoDynContentDao) {
		this.undoDynContentDao = undoDynContentDao;
	}

	public void setUndoMailingComponentDao(UndoMailingComponentDao undoMailingComponentDao) {
		this.undoComponentDao = undoMailingComponentDao;
	}

	// --------------------------------------------------------------------------------------------------------------------------------------- JDBC helper
    protected class UndoMailingRowMapper implements RowMapper<UndoMailing> {
		@Override
		public UndoMailing mapRow(ResultSet resultSet, int row) throws SQLException {
			UndoMailingImpl undo = new UndoMailingImpl();
			
			undo.setId(resultSet.getInt("mailing_id"));
			undo.setUndoId(resultSet.getInt("undo_id"));
			undo.setUndoCreationDate(resultSet.getTimestamp("undo_creation_date"));
			
			return undo;
		}
	}
	
	// --------------------------------------------------------------------------------------------------------------------------------------- business logic

	@Override
	public UndoMailing getUndoData(int mailingId, int undoId) {
		return selectObjectDefaultNull(SELECT_MAILING_UNDO_STATEMENT, new UndoMailingRowMapper(), mailingId, undoId);
	}

	@Override
	public UndoMailing getLastUndoData(int mailingId) {
		int undoId = selectInt(SELECT_LAST_MAILING_UNDO_ID_STATEMENT, mailingId);
		
		if( undoId == 0) {
			return null;
		} else {
			return getUndoData(mailingId, undoId);
		}
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoData(int undoId) {
		update(DELETE_MAILING_STATEMENT, new Object[] { undoId });
	}
	
	@Override
	public final List<Integer> findUndoIdsToCleanup(final int retentionTime) {
		final String sql = isOracleDB()
				? "select distinct(undo_id) from (SELECT undo_id FROM undo_mailing_tbl WHERE undo_creation_date <= SYSDATE - ? union all SELECT DISTINCT um.undo_id FROM maildrop_status_tbl mds, mailing_account_tbl ma, undo_mailing_tbl um  WHERE um.mailing_id = mds.mailing_id AND um.mailing_id = ma.mailing_id AND mds.status_field = 'W' AND mds.genstatus = 3) subsel"
				: "select distinct(undo_id) from (SELECT undo_id FROM undo_mailing_tbl WHERE undo_creation_date <= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL ? DAY) union all SELECT DISTINCT um.undo_id FROM maildrop_status_tbl mds, mailing_account_tbl ma, undo_mailing_tbl um WHERE um.mailing_id = mds.mailing_id AND um.mailing_id = ma.mailing_id AND mds.status_field = 'W' AND mds.genstatus = 3) subsel";
		
		List<Integer> undoIds = select(sql, IntegerRowMapper.INSTANCE, retentionTime);
		undoIds.sort(Integer::compare);
		return undoIds;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoDataForMailing(int mailingID) {
		update(DELETE_UNDODATA_FOR_MAILING_STATEMENT, new Object[] { mailingID });
	}

	@Override
	public List<Integer> findAllUndoIdsForMailings(List<Integer> mailings) {
		if (CollectionUtils.isEmpty(mailings)) {
			return Collections.emptyList();
		}

		String query = "SELECT undo_id FROM undo_mailing_tbl WHERE "
				+ makeBulkInClauseForInteger("mailing_id", mailings);

		return select(query, IntegerRowMapper.INSTANCE);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoDataOverLimit(int mailingId, int undoId) {
		if( undoId == 0) {
			return;
		}
		
		update(DELETE_UNDODATA_OVER_LIMIT_FOR_MAILING_STATEMENT, new Object[] { mailingId , undoId });
	}

	@Override
    public int getUndoIdOverLimit(int mailingId, int undoLimit) {
        if (isOracleDB()) {
            return selectInt(SELECT_FIRST_UNDO_ID_OVER_LIMIT, mailingId, undoLimit);
        } else {
            return selectInt(SELECT_FIRST_UNDO_ID_OVER_LIMIT_SQL, mailingId, undoLimit);
        }
    }

	@Override
	public void deleteUndoData(List<Integer> undoIds) {
		if (CollectionUtils.isEmpty(undoIds)) {
			return;
		}

		String query = "DELETE FROM undo_mailing_tbl WHERE " + makeBulkInClauseForInteger("undo_id", undoIds);
		update(query);
	}

	@Override
	public int saveUndoData(int mailingID, Date undoCreationDate, int undoAdminID) {
        if (mailingID > 0 && undoAdminID > 0) {
            int undoId;
            
            if (isOracleDB()) {
                undoId = selectInt("SELECT undo_id_seq.NEXTVAL FROM DUAL");
        		update("INSERT INTO undo_mailing_tbl (mailing_id, undo_id, undo_creation_date, undo_admin_id) VALUES (?, ?, ?, ?)", mailingID, undoId, undoCreationDate, undoAdminID);
            } else {
            	undoId = insertIntoAutoincrementMysqlTable("undo_id", "INSERT INTO undo_mailing_tbl (mailing_id, undo_creation_date, undo_admin_id) VALUES (?, ?, ?)", mailingID, undoCreationDate, undoAdminID);
            }
            
    		this.undoComponentDao.saveUndoData(mailingID, undoId);
    		this.undoDynContentDao.saveUndoData(mailingID, undoId);
    		
            return undoId;
        } else {
            return 0;
        }
	}
}
