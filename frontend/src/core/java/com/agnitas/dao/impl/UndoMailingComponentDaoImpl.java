/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.agnitas.beans.MailingComponentType;
import com.agnitas.beans.UndoMailingComponent;
import com.agnitas.beans.impl.UndoMailingComponentImpl;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.UndoMailingComponentDao;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

public class UndoMailingComponentDaoImpl extends BaseDaoImpl implements UndoMailingComponentDao {

	private final RowMapper<UndoMailingComponent> undoMailingComponentRowMapper = new RowMapper<>() {
		@Override
		public UndoMailingComponent mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			UndoMailingComponentImpl undo = new UndoMailingComponentImpl();

			byte[] bytes = resultSet.getBytes("binblock");
			if (bytes != null) {
				undo.setBinaryBlock(bytes, resultSet.getString("mtype"));
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
			undo.setType(MailingComponentType.getMailingComponentTypeByCode(resultSet.getInt("comptype")));
			undo.setUndoId(resultSet.getInt("undo_id"));
			undo.setUrlID(resultSet.getInt("url_id"));

			return undo;
		}
	};
	
	@Override
	@DaoUpdateReturnValueCheck
	@Transactional
	public void saveUndoData(int mailingId, int undoId) {
		if (mailingId <= 0) {
			return;
		}

		try {
			String query = """
					INSERT INTO undo_component_tbl(company_id,
					                               mailtemplate_id,
					                               mailing_id,
					                               component_id,
					                               mtype,
					                               required,
					                               comptype,
					                               comppresent,
					                               compname,
					                               emmblock,
					                               binblock,
					                               target_id,
					                               timestamp,
					                               url_id,
					                               undo_id)
					    (SELECT component_tbl.company_id,
					            component_tbl.mailtemplate_id,
					            component_tbl.mailing_id,
					            component_tbl.component_id,
					            component_tbl.mtype,
					            component_tbl.required,
					            component_tbl.comptype,
					            component_tbl.comppresent,
					            component_tbl.compname,
					            component_tbl.emmblock,
					            component_tbl.binblock,
					            component_tbl.target_id,
					            component_tbl.timestamp,
					            url_id,
					            ?
					     FROM component_tbl
					     WHERE mailing_id = ?
					       AND component_tbl.comptype = ?)
					""";

			update(query, undoId, mailingId, MailingComponentType.Template.getCode());
		} catch (Exception e) {
			logger.error("Error while writing undo data for components of mailing {}", mailingId, e);
		}
	}

	@Override
	public List<UndoMailingComponent> getAllUndoDataForMailing(int mailingId, int undoId) {
		return select("SELECT * FROM undo_component_tbl WHERE mailing_id = ? AND undo_id = ?",
				undoMailingComponentRowMapper, mailingId, undoId);
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoData(int undoId) {
		update("DELETE FROM undo_component_tbl WHERE undo_id = ?", undoId);
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoDataForMailing(int mailingID) {
		update("DELETE FROM undo_component_tbl WHERE mailing_id = ?", mailingID);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUndoDataOverLimit(int mailingId, int undoId) {
		if (undoId == 0) {
			return;
		}
		
		update("DELETE FROM undo_component_tbl WHERE mailing_id = ? AND undo_id <= ?", mailingId , undoId);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteByCompany(int companyId) {
		return deleteByCompany("undo_component_tbl", companyId);
	}

	@Override
	public void deleteUndoData(List<Integer> undoIds) {
		if (CollectionUtils.isEmpty(undoIds)) {
			return;
		}

		String query = "DELETE FROM undo_component_tbl WHERE " + makeBulkInClauseForInteger("undo_id", undoIds);
		update(query);
	}

}
