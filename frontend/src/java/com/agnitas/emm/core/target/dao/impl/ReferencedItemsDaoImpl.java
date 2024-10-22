/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.agnitas.beans.TargetLight;
import com.agnitas.dao.impl.mapper.TargetLightRowMapper;
import com.agnitas.emm.core.target.dao.ReferencedItemsDao;

/**
 * Implementation of {@link ReferencedItemsDao} interface.
 */
public class ReferencedItemsDaoImpl extends BaseDaoImpl implements ReferencedItemsDao {

	private static final Logger LOGGER = LogManager.getLogger(ReferencedItemsDaoImpl.class);
	
	@Override
	public void removeAllReferencedObjects(final int companyID, final int targetID) {
		update(LOGGER, "DELETE FROM target_ref_mailing_tbl WHERE company_ref=? AND target_ref=?", companyID, targetID);
		update(LOGGER, "DELETE FROM target_ref_link_tbl WHERE company_ref=? AND target_ref=?", companyID, targetID);
		update(LOGGER, "DELETE FROM target_ref_autoimport_tbl WHERE company_ref=? AND target_ref=?", companyID, targetID);
		update(LOGGER, "DELETE FROM target_ref_profilefield_tbl WHERE company_ref=? AND target_ref=?", companyID, targetID);
	}

	@Override
	public final void saveReferencedMailings(final int companyID, final int targetID, final List<Integer> ids) {
		final JdbcTemplate template = getJdbcTemplate();
		
		final BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
			@Override
			public final int getBatchSize() {
				return ids.size();
			}

			@Override
			public final void setValues(final PreparedStatement ps, final int index) throws SQLException {
				ps.setInt(1, companyID);
				ps.setInt(2, targetID);
				ps.setInt(3, ids.get(index));
			}
		};
		
		template.batchUpdate("INSERT INTO target_ref_mailing_tbl (company_ref, target_ref, mailing_ref) VALUES (?,?,?)", bpss);
	}

	@Override
	public final void saveReferencedLinks(final int companyID, final int targetID, final List<Integer> ids) {
		final JdbcTemplate template = getJdbcTemplate();
		
		final BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
			@Override
			public final int getBatchSize() {
				return ids.size();
			}

			@Override
			public final void setValues(final PreparedStatement ps, final int index) throws SQLException {
				ps.setInt(1, companyID);
				ps.setInt(2, targetID);
				ps.setInt(3, ids.get(index));
			}
		};
		
		template.batchUpdate("INSERT INTO target_ref_link_tbl (company_ref, target_ref, link_ref) VALUES (?,?,?)", bpss);
	}

	@Override
	public final void saveReferencedAutoImports(final int companyID, final int targetID, final List<Integer> ids) {
		final JdbcTemplate template = getJdbcTemplate();
		
		final BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
			@Override
			public final int getBatchSize() {
				return ids.size();
			}

			@Override
			public final void setValues(final PreparedStatement ps, final int index) throws SQLException {
				ps.setInt(1, companyID);
				ps.setInt(2, targetID);
				ps.setInt(3, ids.get(index));
			}
		};
		
		template.batchUpdate("INSERT INTO target_ref_autoimport_tbl (company_ref, target_ref, autoimport_ref) VALUES (?,?,?)", bpss);
	}

	@Override
	public final void saveReferencedProfileFields(final int companyID, final int targetID, final List<String> profileFieldNames) {
		final JdbcTemplate template = getJdbcTemplate();
		
		final BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
			@Override
			public final int getBatchSize() {
				return profileFieldNames.size();
			}

			@Override
			public final void setValues(final PreparedStatement ps, final int index) throws SQLException {
				ps.setInt(1, companyID);
				ps.setInt(2, targetID);
				ps.setNString(3, profileFieldNames.get(index));
			}
		};
		
		template.batchUpdate("INSERT INTO target_ref_profilefield_tbl (company_ref, target_ref, name) VALUES (?,?,?)", bpss);
	}

	@Override
	public final List<TargetLight> listTargetGroupsReferencingProfileField(final int companyID, final String visibleShortname) {
		final String sql = "SELECT DISTINCT * FROM dyn_target_tbl WHERE target_id IN (SELECT target_ref FROM target_ref_profilefield_tbl WHERE company_ref=? AND LOWER(name) = LOWER(?))";
		
		return select(LOGGER, sql, TargetLightRowMapper.INSTANCE, companyID, visibleShortname);
	}

	@Override
	public final List<TargetLight> listTargetGroupsReferencingMailing(final int companyID, final int mailingID) {
		final String sql = "SELECT DISTINCT * FROM dyn_target_tbl WHERE target_id IN (SELECT target_ref FROM target_ref_mailing_tbl WHERE company_ref=? AND mailing_ref=?)";
		
		return select(LOGGER, sql, TargetLightRowMapper.INSTANCE, companyID, mailingID);
	}

	@Override
	public final List<TargetLight> listTargetGroupsReferencingLink(final int companyID, final int linkID) {
		final String sql = "SELECT DISTINCT * FROM dyn_target_tbl WHERE target_id IN (SELECT target_ref FROM target_ref_link_tbl WHERE company_ref=? AND link_ref=?)";
		
		return select(LOGGER, sql, TargetLightRowMapper.INSTANCE, companyID, linkID);
	}

	@Override
	public final List<TargetLight> listTargetGroupsReferencingAutoImport(final int companyID, final int autoImportID) {
		final String sql = "SELECT DISTINCT * FROM dyn_target_tbl WHERE target_id IN (SELECT target_ref FROM target_ref_autoimport_tbl WHERE company_ref=? AND autoimport_ref=?)";
		
		return select(LOGGER, sql, TargetLightRowMapper.INSTANCE, companyID, autoImportID);
	}
}
