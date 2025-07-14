/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.web.util;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderFilterListBuilder;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConverter;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;
import com.agnitas.emm.core.target.form.TargetEditForm;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.target.web.TargetgroupViewFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Presentation layer utility class to synchronize the data of all target group editors.
 */
public class EditorContentSynchronizer {

	private static final Logger logger = LogManager.getLogger(EditorContentSynchronizer.class);
	
	private EqlToQueryBuilderConverter eqlToQueryBuilderConverter;
	private QueryBuilderToEqlConverter queryBuilderToEqlConverter;
	private TargetService targetService;

	/** Builder for QueryBuilder filters. */
	private QueryBuilderFilterListBuilder filterListBuilder;

	public void synchronizeEditors(Admin admin, TargetgroupViewFormat synchronizeFrom, TargetEditForm form) throws EditorContentSynchronizationException {
		switch (synchronizeFrom) {
			case EQL:
				synchronizeEqlToQueryBuilder(admin, form);
				break;

			case QUERY_BUILDER:
				synchronizeQueryBuilderToEql(admin.getCompanyID(), form);
				break;

			default:
				throw new EditorContentSynchronizationException(String.format("Synchronizing from %s format not supported", synchronizeFrom));
		}
	}

	public final void synchronizeEqlToQueryBuilder(Admin admin, TargetEditForm form) throws EditorContentSynchronizationException {
		try {
			boolean excludeHiddenFields = !targetService.isEqlContainsInvisibleFields(form.getEql(), admin.getCompanyID(), admin.getAdminID());

			form.setQueryBuilderRules(eqlToQueryBuilderConverter.convertEqlToQueryBuilderJson(form.getEql(), admin.getCompanyID()));
			form.setQueryBuilderFilters(filterListBuilder.buildFilterListJson(admin, excludeHiddenFields));
		} catch(EqlParserException e) {
			logger.info("Syntax error in EQL code", e);
			throw new EditorContentSynchronizationException("Syntax error in EQL code", e);
		} catch(EqlToQueryBuilderConversionException e) {
			logger.info("Error converting EQL to QueryBuilder", e);
			throw new EditorContentSynchronizationException("Error converting EQL to QueryBuilder", e);
		} catch(Exception e) {
			logger.info("General error converting EQL to QueryBuilder", e);
			throw new EditorContentSynchronizationException("General error converting EQL to QueryBuilder", e);
		}
	}

	public final void synchronizeQueryBuilderToEql(int companyID, TargetEditForm form) throws EditorContentSynchronizationException {
		try {
            form.setEql(queryBuilderToEqlConverter.convertQueryBuilderJsonToEql(form.getQueryBuilderRules(), companyID));
		} catch(QueryBuilderToEqlConversionException e) {
			logger.info("Error converting QueryBuilder to EQL", e);
			throw new EditorContentSynchronizationException("Error converting QueryBuilder to EQL", e);
		} catch(Exception e) {
			logger.info("General error converting EQL to QueryBuilder", e);
			throw new EditorContentSynchronizationException("General error converting EQL to QueryBuilder", e);
		}
	}

	// -------------------------------------------------------------------------------------------------------------------------------------------- Dependency Injection
	public void setEqlToQueryBuilderConverter(EqlToQueryBuilderConverter converter) {
		this.eqlToQueryBuilderConverter = converter;
	}
	
	public void setQueryBuilderToEqlConverter(QueryBuilderToEqlConverter converter) {
		this.queryBuilderToEqlConverter = converter;
	}

	public void setQueryBuilderFilterListBuilder(QueryBuilderFilterListBuilder builder) {
		this.filterListBuilder = builder;
	}

	public void setTargetService(TargetService targetService) {
		this.targetService = targetService;
	}
}
