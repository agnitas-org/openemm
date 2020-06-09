/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.web.listener;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.target.TargetRepresentation;
import org.agnitas.target.impl.TargetRepresentationImpl;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.eql.emm.legacy.TargetRepresentationToEqlConversionException;

public class TargetGroupMigrationListener implements ServletContextListener {

	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(TargetGroupMigrationListener.class);
	
	private static final class TargetGroupData {
		public final int targetID;
		public final TargetRepresentation targetRepresentation;
		
		public TargetGroupData(final int targetID, final TargetRepresentation targetRepresentation) {
			this.targetID = targetID;
			this.targetRepresentation = targetRepresentation;
		}
	}
	
	private static final class TargetGroupDataRowMapper implements RowMapper<TargetGroupData> {
		@Override
		public final TargetGroupData mapRow(final ResultSet resultSet, final int row) throws SQLException {
			final int targetId = resultSet.getInt("target_id");
			final TargetRepresentation targetRepresentation = makeTargetRepresentationFromSerializedData(resultSet);
			
			return new TargetGroupData(targetId, targetRepresentation);
		}
		
		private TargetRepresentation makeTargetRepresentationFromSerializedData(final ResultSet resultSet) throws SQLException {
			Blob targetRepresentationBlob = resultSet.getBlob("target_representation");
			
			if (resultSet.wasNull() || targetRepresentationBlob.length() == 0) {
				return new TargetRepresentationImpl();
			} else {
				try {
					try(final InputStream lobStream = targetRepresentationBlob.getBinaryStream()) {
						try(final ObjectInputStream stream = new ObjectInputStream(lobStream)) {
							return (TargetRepresentation) stream.readObject();
						}
					}
				} catch(final IOException e) {
					throw new SQLException("Error reading serialized form", e);
				} catch (ClassNotFoundException e) {
					throw new SQLException("Error de-serialized target group", e);
				} finally {
					targetRepresentationBlob.free();
				}
			}
		}
	}
	
	@Override
	public final void contextDestroyed(final ServletContextEvent servletContextEvent) {
		// Nothing to do here
	}

	@Override
	public final void contextInitialized(final ServletContextEvent servletContextEvent) {
		final ServletContext servletContext = servletContextEvent.getServletContext();
		final WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		final DataSource dataSource = webApplicationContext.getBean("dataSource", DataSource.class);
		final EqlFacade eqlFacade = webApplicationContext.getBean("EqlFacade", EqlFacade.class);
		
		migrateTargetGroupsOfMarkedCompanies(dataSource, eqlFacade);
	}
	
	private static final void migrateTargetGroupsOfMarkedCompanies(final DataSource dataSource, final EqlFacade eqlFacade) {
		 final List<Integer> companyIds = listMarkedCompanies(dataSource);
		 
		 for (final int companyId : companyIds) {
			 try {
				 migrateTargetGroupsOfCompany(companyId, dataSource, eqlFacade);
				 removeCompanyMarker(companyId, dataSource);
			 } catch(final Exception e) {
				 LOGGER.error(String.format("Error migrating target groups of company %d", companyId), e);
			 }
		 }
	}
	
	private static final void migrateTargetGroupsOfCompany(final int companyId, final DataSource dataSource, final EqlFacade eqlFacade) {
		final List<TargetGroupData> targetGroups = listTargetGroups(companyId, dataSource);
		
		for (final TargetGroupData targetGroup : targetGroups) {
			try {
				migrateTargetGroup(companyId, targetGroup, dataSource, eqlFacade);
			} catch(final Exception e) {
				 LOGGER.error(String.format("Error migrating target group %d of company %d", targetGroup.targetID, companyId), e);
			}
		}
	}
	
	private static final void migrateTargetGroup(int companyId, final TargetGroupData targetGroup, final DataSource dataSource, final EqlFacade eqlFacade) throws TargetRepresentationToEqlConversionException {
		if (companyId == 0) {
			// Migrate listsplit targetgroups available for all clients
			companyId = 1;
		}
		final String eql = eqlFacade.convertTargetRepresentationToEql(targetGroup.targetRepresentation, companyId);
		
		final String sql = "UPDATE dyn_target_tbl SET eql = ?, target_representation = NULL WHERE target_id = ?";
		final JdbcTemplate template = new JdbcTemplate(dataSource);
		template.update(sql,eql, targetGroup.targetID);
		
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info(String.format("Migrated target group %d", targetGroup.targetID));
		}
	}
	
	private static final List<Integer> listMarkedCompanies(final DataSource dataSource) {
		final JdbcTemplate template = new JdbcTemplate(dataSource);

		return template.query("SELECT company_id FROM company_info_tbl WHERE cname = ? and cvalue = 'true'", new IntegerRowMapper(), ConfigValue.MigrateTargetGroupsOnStartup.toString());
	}
	
	private static final List<TargetGroupData> listTargetGroups(final int companyId, final DataSource dataSource) {
		final String sql = "SELECT target_id, target_representation FROM dyn_target_tbl WHERE company_id = ? AND (locked IS NULL OR locked = 0) AND (eql IS NULL OR eql = '') AND target_representation IS NOT NULL";
		
		final JdbcTemplate template = new JdbcTemplate(dataSource);

		return template.query(sql, new TargetGroupDataRowMapper(), companyId);
	}
	
	private static final void removeCompanyMarker(final int companyId, final DataSource dataSource) {
		final String sql = "UPDATE company_info_tbl SET cvalue = 'false', timestamp = CURRENT_TIMESTAMP, description = 'Migration done' WHERE cname = ? and company_id = ?";
		final JdbcTemplate template = new JdbcTemplate(dataSource);
		template.update(sql, ConfigValue.MigrateTargetGroupsOnStartup.toString(), companyId);
	}
}
