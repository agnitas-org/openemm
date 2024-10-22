/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.agnitas.beans.MailingComponentType;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.beans.ComUndoMailingComponent;
import com.agnitas.beans.impl.ComUndoMailingComponentImpl;
import com.agnitas.dao.ComUndoMailingComponentDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;

public class ComUndoMailingComponentDaoImpl extends BaseDaoImpl implements ComUndoMailingComponentDao {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ComUndoMailingComponentDaoImpl.class);

	// Statement to do a table-to-table data transfer
	private static final String INSERT_COMPONENT_STATEMENT =
		"INSERT INTO undo_component_tbl " +
		"(company_id, mailtemplate_id, mailing_id, " +
		" component_id, mtype, required, comptype, comppresent, compname, " +
		" emmblock, binblock, target_id, timestamp, url_id, " +
		" undo_id) " +
		"(SELECT component_tbl.company_id, component_tbl.mailtemplate_id, component_tbl.mailing_id, " +
		" component_tbl.component_id, component_tbl.mtype, component_tbl.required, component_tbl.comptype, component_tbl.comppresent, component_tbl.compname, " +
		" component_tbl.emmblock, component_tbl.binblock, component_tbl.target_id, component_tbl.timestamp, url_id, ? " +
        " FROM component_tbl WHERE mailing_id = ? AND component_tbl.comptype = ?) ";
	
	private static final String SELECT_COMPONENT_STATEMENT =
		"SELECT * FROM undo_component_tbl WHERE mailing_id = ? AND component_id = ? AND undo_id = ?";

	private static final String SELECT_ALL_COMPONENTSLIST_STATEMENT =
		"SELECT * FROM undo_component_tbl WHERE mailing_id = ? AND undo_id = ?";
	
	private static final String DELETE_COMPONENT_STATEMENT =
		"DELETE FROM undo_component_tbl WHERE undo_id = ?";
	
	private static final String DELETE_UNDODATA_FOR_MAILING_STATEMENT =
		"DELETE FROM undo_component_tbl WHERE mailing_id = ?";
	
	private static final String DELETE_UNDODATA_OVER_LIMIT_FOR_MAILING_STATEMENT =
		"DELETE FROM undo_component_tbl WHERE mailing_id = ? AND undo_id <= ?";
	
	// --------------------------------------------------------------------------------------------------------------------------------------- JDBC helper

	private final RowMapper<ComUndoMailingComponent> undoMailingComponentRowMapper = new RowMapper<>() {
		@Override
		public ComUndoMailingComponent mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			ComUndoMailingComponentImpl undo = new ComUndoMailingComponentImpl();

            Blob blob = resultSet.getBlob("binblock");
            if (blob != null) {
                try (InputStream is = blob.getBinaryStream()) {
                    undo.setBinaryBlock(IOUtils.toByteArray(is), resultSet.getString("mtype"));
                } catch (Exception ex) {
                	logger.error("Error:" + ex, ex);
                }
            } else {
    			undo.setEmmBlock(resultSet.getString("emmblock"), resultSet.getString("mtype"));
            }

			undo.setCompanyID(resultSet.getInt("company_id"));
			undo.setComponentName(resultSet.getString("compname"));
			undo.setId(resultSet.getInt("component_id"));
			// TODO undo.setLink(link)
			undo.setMailingID(resultSet.getInt("mailing_id"));
			undo.setTargetID(resultSet.getInt("target_id"));
			undo.setTimestamp(resultSet.getTimestamp("timestamp"));
			try {
				undo.setType(MailingComponentType.getMailingComponentTypeByCode(resultSet.getInt("comptype")));
			} catch (Exception e) {
				throw new SQLException("Invalid component type found: " + resultSet.getInt("comptype"), e);
			}
			undo.setUndoId(resultSet.getInt("undo_id"));
			undo.setUrlID(resultSet.getInt("url_id"));

			return undo;
		}
	};
	
	@Override
	@DaoUpdateReturnValueCheck
	@Transactional
	public final void saveUndoData(final int mailingId, final int undoId) {
		if(mailingId > 0) {
			try {
				// Perform a direct table-to-table copy
				update(logger, ComUndoMailingComponentDaoImpl.INSERT_COMPONENT_STATEMENT, undoId, mailingId, MailingComponentType.Template.getCode());
			} catch(final Exception e) {
            	final String msg = String.format("Error while writing undo data for components of mailing %d", mailingId);
            	
            	logger.error(msg, e);
			}
		}
	}

	/*
	@Override
	@DaoUpdateReturnValueCheck
    public void saveUndoData(int mailingId, int undoId) {
        if (mailingId == 0) {
			return;
		}
        if (isOracleDB()) {
            update(logger, ComUndoMailingComponentDaoImpl.INSERT_COMPONENT_STATEMENT, undoId, mailingId, MailingComponentType.Template.getCode());
        } else {
            try(final Connection connection = getDataSource().getConnection()) {
            	final boolean previousAutoCommit = connection.getAutoCommit();
            	connection.setAutoCommit(false);
            	
            	try(final Statement statement = connection.createStatement()) {
            		final String query = "SELECT component_tbl.company_id, component_tbl.mailtemplate_id, component_tbl.mailing_id, " +
                            " component_tbl.component_id, component_tbl.mtype, component_tbl.required, component_tbl.comptype, component_tbl.comppresent, component_tbl.compname, " +
                            " component_tbl.emmblock, component_tbl.binblock, component_tbl.target_id, component_tbl.timestamp, component_tbl.url_id FROM component_tbl WHERE mailing_id = " + mailingId + " AND component_tbl.comptype = " + MailingComponentType.Template.getCode();
            		
            		
                    try (final ResultSet resultSet = statement.executeQuery(query)) {
                        final String insertStatement = "INSERT INTO undo_component_tbl"
                    		+ " (company_id, mailtemplate_id, mailing_id, component_id, mtype, required, comptype, comppresent, compname, emmblock, binblock, target_id, timestamp, url_id, undo_id)"
                    		+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        
                        try (PreparedStatement preparedStatement = connection.prepareStatement(insertStatement)) {
                    		while (resultSet.next()) {
                    			int i = 1;
                    			preparedStatement.setInt(i++, resultSet.getInt("company_id"));
                    			preparedStatement.setInt(i++, resultSet.getInt("mailtemplate_id"));
                    			preparedStatement.setInt(i++, resultSet.getInt("mailing_id"));
                    			preparedStatement.setInt(i++, resultSet.getInt("component_id"));
                    			preparedStatement.setNString(i++, resultSet.getString("mtype"));
                    			preparedStatement.setInt(i++, resultSet.getInt("required"));
                    			preparedStatement.setInt(i++, resultSet.getInt("comptype"));
                    			preparedStatement.setInt(i++, resultSet.getInt("comppresent"));
                    			preparedStatement.setNString(i++, resultSet.getString("compname"));
                    			preparedStatement.setClob(i++, resultSet.getClob("emmblock"));
                    			preparedStatement.setBlob(i++, resultSet.getBlob("binblock"));
                    			preparedStatement.setInt(i++, resultSet.getInt("target_id"));
                    			preparedStatement.setTimestamp(i++, resultSet.getTimestamp("timestamp"));
                    			preparedStatement.setInt(i++, resultSet.getInt("URL_ID"));
                    			preparedStatement.setInt(i++, undoId);
    	                        preparedStatement.execute();
                        	}
                        }
                        
                        connection.commit();
                    } catch(final SQLException e) {
                    	connection.rollback();
                    	
                    	throw e;
                    }
            	} finally {
            		connection.setAutoCommit(previousAutoCommit);
            	}
            } catch(final SQLException e) {
            	final String msg = String.format("Error while writing undo data for components of mailing %d", mailingId);
            	
            	logger.error(msg, e);
            }
         
        }
    }
    */
	
	@Override
	public ComUndoMailingComponent getUndoData(int mailingId, int componentId, int undoId) {
		return selectObjectDefaultNull(logger, ComUndoMailingComponentDaoImpl.SELECT_COMPONENT_STATEMENT, undoMailingComponentRowMapper, mailingId, componentId, undoId);
	}

	@Override
	public List<ComUndoMailingComponent> getAllUndoDataForMailing(int mailingId, int undoId) {
		return select(logger, SELECT_ALL_COMPONENTSLIST_STATEMENT, this.undoMailingComponentRowMapper, mailingId, undoId);
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoData(int undoId) {
		update(logger, DELETE_COMPONENT_STATEMENT, undoId);
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoDataForMailing(int mailingID) {
		update(logger, DELETE_UNDODATA_FOR_MAILING_STATEMENT, mailingID);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoDataOverLimit(int mailingId, int undoId) {
		if( undoId == 0) {
			return;
		}
		
		update(logger, DELETE_UNDODATA_OVER_LIMIT_FOR_MAILING_STATEMENT, mailingId , undoId);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteByCompany(int companyId) {
		return deleteByCompany(logger, "undo_component_tbl", companyId);
	}

	@Override
	public void deleteUndoData(List<Integer> undoIds) {
		if (CollectionUtils.isEmpty(undoIds)) {
			return;
		}

		String query = "DELETE FROM undo_component_tbl WHERE " + makeBulkInClauseForInteger("undo_id", undoIds);
		update(logger, query);
	}
}
