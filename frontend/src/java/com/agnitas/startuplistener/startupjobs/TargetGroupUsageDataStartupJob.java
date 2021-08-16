/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.startuplistener.startupjobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.eql.codegen.CodeGeneratorException;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldResolveException;
import com.agnitas.emm.core.target.eql.codegen.resolver.ReferenceTableResolveException;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;
import com.agnitas.emm.core.target.eql.referencecollector.SimpleReferenceCollector;
import com.agnitas.emm.core.target.service.ReferencedItemsService;
import com.agnitas.startuplistener.api.JobContext;
import com.agnitas.startuplistener.api.StartupJob;
import com.agnitas.startuplistener.api.StartupJobException;

public final class TargetGroupUsageDataStartupJob implements StartupJob {

	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(TargetGroupUsageDataStartupJob.class);

	private EqlFacade eqlFacade;
	private ReferencedItemsService itemService;
	
	@Override
	public final void runStartupJob(final JobContext context) throws StartupJobException {
		// Check if a company ID is defined
		if (context.getCompanyId() <= 0) {
			final String msg = String.format("Startup job %d requires a company ID", context.getJobId());
			LOGGER.error(msg);
			throw new StartupJobException(msg);
		}

		this.eqlFacade = context.getApplicationContext().getBean("EqlFacade", EqlFacade.class);
		this.itemService = context.getApplicationContext().getBean("TargetGroupsReferencedItemsService", ReferencedItemsService.class);

		try {
			updateTargetGroupUsageData(context);
		} catch(final Exception e) {
			final String msg = String.format("Error updating target group usage data for company %d", context.getCompanyId());
			
			LOGGER.error(msg, e);
			
			throw new StartupJobException(msg, e);
		}
	}

	private final void updateTargetGroupUsageData(final JobContext context) throws SQLException {
		final String sql = "SELECT target_id, eql FROM dyn_target_tbl WHERE company_id=? AND (deleted IS NULL OR deleted=0)";
		
		try(final Connection con = context.getDataSource().getConnection()) {
			try(final PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setInt(1, context.getCompanyId());
				
				try(final ResultSet rs = ps.executeQuery()) {
					while(rs.next()) {
						final int targetID = rs.getInt(1);
						final String eql = rs.getString(2);
						
						updateTargetGroupUsageData(context, targetID, eql);
					}
				}
			}
		}
	}
	
	private final void updateTargetGroupUsageData(final JobContext context, final int targetID, final String eql) {
		final SimpleReferenceCollector collector = new SimpleReferenceCollector();

		try {
			eqlFacade.convertEqlToSql(eql, context.getCompanyId(), collector);

			this.itemService.saveReferencedItems(collector, context.getCompanyId(), targetID);
		} catch(final EqlParserException | CodeGeneratorException | ReferenceTableResolveException | ProfileFieldResolveException e) {
			this.itemService.removeReferencedItems(context.getCompanyId(), targetID);
		}
	}
}
