/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.startuplistener.startupjobs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.startuplistener.api.JobContext;
import com.agnitas.startuplistener.api.StartupJob;
import com.agnitas.startuplistener.api.StartupJobException;

/**
 * Startup job to assign company tokens to companies with
 * no company token set.
 */
public final class AssignCompanyTokenStartupJob implements StartupJob {

	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(AssignCompanyTokenStartupJob.class);
	
	@Override
	public final void runStartupJob(final JobContext context) throws StartupJobException {
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info("Assigning company tokens");
		}
		final List<Integer> companiesWithoutToken = listCompaniesWithoutToken(context.getDataSource());

		if(companiesWithoutToken.isEmpty()) {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info("No company without token found.");
			}
			
			return;
		}
		
		assert !companiesWithoutToken.isEmpty();			// Here, there is at least one company without a token
		
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info(String.format("Found %d companies without token", companiesWithoutToken.size()));
		}
		
		final CompanyTokenService tokenService = context.getApplicationContext().getBean("CompanyTokenService", CompanyTokenService.class);
		final boolean allSuccessful = assignCompanyTokens(companiesWithoutToken, tokenService);

		if(!allSuccessful) {
			throw new StartupJobException("Assigning tokens to companies not successful for all companies.");
		}
	}

	private final List<Integer> listCompaniesWithoutToken(final DataSource dataSource) throws StartupJobException {
		try(final Connection connection = dataSource.getConnection()) {
			try(final Statement stmt = connection.createStatement()) {
				try(final ResultSet rs = stmt.executeQuery("SELECT company_id FROM company_tbl WHERE company_token IS NULL")) {
					final List<Integer> ids = new ArrayList<>();
					
					while(rs.next()) {
						ids.add(rs.getInt(1));
					}
					
					return ids;
				}
			}
		} catch(final SQLException e) {
			throw new StartupJobException("Error listing companies without tokens", e);
		}
	}
	
	private final boolean assignCompanyTokens(final List<Integer> companyIds, final CompanyTokenService tokenService) {
		boolean allSuccessful = true;
		
		for(final int companyId : companyIds) {
			try {
				tokenService.assignRandomToken(companyId, false);
			} catch(final Exception e) {
				allSuccessful = false;
				
				LOGGER.error(String.format("Error assigning company token to company ID %d", companyId), e);
			}
		}
		
		return allSuccessful;
	}
}
