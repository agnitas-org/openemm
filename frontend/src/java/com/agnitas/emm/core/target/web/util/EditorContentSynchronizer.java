/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.web.util;

import com.agnitas.emm.core.target.service.ComTargetService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderFilterListBuilder;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderFilterListBuilderException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConverter;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;
import com.agnitas.emm.core.target.form.TargetEditForm;
import com.agnitas.emm.core.target.web.TargetgroupViewFormat;

/**
 * Presentation layer utility class to synchronize the data of all target group editors.
 */
public class EditorContentSynchronizer {

	private static final Logger logger = LogManager.getLogger(EditorContentSynchronizer.class);
	
	private EqlToQueryBuilderConverter eqlToQueryBuilderConverter;
	private QueryBuilderToEqlConverter queryBuilderToEqlConverter;
	private ComTargetService targetService;

	/** Builder for QueryBuilder filters. */
	private QueryBuilderFilterListBuilder filterListBuilder;

	public void synchronizeEditors(final Admin admin, final TargetgroupViewFormat synchronizeFrom, final TargetEditForm form) throws EditorContentSynchronizationException {
		switch (synchronizeFrom) {
			case EQL:
				synchronizeEqlToQuerybuilder(admin, form);
				break;

			case QUERY_BUILDER:
				synchronizeQuerybuilderToEql(admin.getCompanyID(), form);
				break;

			default:
				throw new EditorContentSynchronizationException(String.format("Synchronizing from %s format not supported", synchronizeFrom));
		}
	}

	public final void synchronizeEqlToQuerybuilder(final Admin admin, final TargetEditForm form) throws EditorContentSynchronizationException {
		try {
			boolean excludeHiddenFields = !targetService.isEqlContainsInvisibleFields(form.getEql(), admin.getCompanyID(), admin.getAdminID());

			form.setQueryBuilderRules(eqlToQueryBuilderConverter.convertEqlToQueryBuilderJson(form.getEql(), admin.getCompanyID()));
			form.setQueryBuilderFilters(filterListBuilder.buildFilterListJson(admin, excludeHiddenFields));
		} catch(final EqlParserException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Syntax error in EQL code", e);
			}

			throw new EditorContentSynchronizationException("Syntax error in EQL code", e);
		} catch(final EqlToQueryBuilderConversionException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Error converting EQL to QueryBuilder", e);
			}

			throw new EditorContentSynchronizationException("Error converting EQL to QueryBuilder", e);
		} catch(final QueryBuilderFilterListBuilderException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Error building QB filters", e);
			}

			throw new EditorContentSynchronizationException("Error building QB filters", e);
		} catch(final Exception e) {
			if(logger.isInfoEnabled()) {
				logger.info("General error converting EQL to QueryBuilder", e);
			}

			throw new EditorContentSynchronizationException("General error converting EQL to QueryBuilder", e);
		}
	}

	public final void synchronizeQuerybuilderToEql(final int companyID, final TargetEditForm form) throws EditorContentSynchronizationException {
		try {
			final String eql = this.queryBuilderToEqlConverter.convertQueryBuilderJsonToEql(form.getQueryBuilderRules(), companyID);

			form.setEql(eql);
		} catch(final QueryBuilderToEqlConversionException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Error converting QueryBuilder to EQL", e);
			}

			throw new EditorContentSynchronizationException("Error converting QueryBuilder to EQL", e);
		} catch(final Exception e) {
			if(logger.isInfoEnabled()) {
				logger.info("General error converting EQL to QueryBuilder", e);
			}

			throw new EditorContentSynchronizationException("General error converting EQL to QueryBuilder", e);
		}
	}
	
	
	// -------------------------------------------------------------------------------------------------------------------------------------------- Dependency Injection
	@Required
	public void setEqlToQueryBuilderConverter(final EqlToQueryBuilderConverter converter) {
		this.eqlToQueryBuilderConverter = converter;
	}
	
	@Required
	public final void setQueryBuilderToEqlConverter(final QueryBuilderToEqlConverter converter) {
		this.queryBuilderToEqlConverter = converter;
	}

	@Required
	public final void setQueryBuilderFilterListBuilder(final QueryBuilderFilterListBuilder builder) {
		this.filterListBuilder = builder;
	}

	@Required
	public void setTargetService(ComTargetService targetService) {
		this.targetService = targetService;
	}
}
