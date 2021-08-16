/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.web.util;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderFilterListBuilder;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderFilterListBuilderException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConverter;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;
import com.agnitas.emm.core.target.web.QueryBuilderTargetGroupForm;
import com.agnitas.emm.core.target.web.TargetgroupViewFormat;

/**
 * Presentation layer utility class to synchronize the data of all target group editors.
 */
public final class EditorContentSynchronizer {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(EditorContentSynchronizer.class);
	
	/** EQL-to-Querybuilder converter. */
	private EqlToQueryBuilderConverter eqlToQueryBuilderConverter;
	
	private QueryBuilderToEqlConverter queryBuilderToEqlConverter;
	
	/** Builder for QueryBuilder filters. */
	private QueryBuilderFilterListBuilder filterListBuilder;
	
	/**
	 * Synchronizes all target group editors depending on requested editor view and submitted data from JSP.
	 * 
	 * @param admin company admin
	 * @param form form bean
	 * @param newFormat view format the user requested to be shown
	 *
	 * 
	 * @throws EditorContentSynchronizationException on errors synchronizing the editors data.
	 */
	public final TargetgroupViewFormat synchronizeEditors(final ComAdmin admin, final QueryBuilderTargetGroupForm form, final TargetgroupViewFormat newFormat) throws EditorContentSynchronizationException {
		if(newFormat == null) {
			throw new IllegalArgumentException("New format cannot be null");
		}
		
		final TargetgroupViewFormat currentFormat = TargetgroupViewFormat.fromCode(form.getFormat());
		
		if(currentFormat == null) {
			if(logger.isDebugEnabled()) {
				logger.debug(String.format("Current format ('%s') is unknown", form.getFormat()));
			}

			throw new EditorContentSynchronizationException(String.format("Current format ('%s') is unknown", form.getFormat()));
		}

		// Call synchronization method
		return callSynchronizationMethod(admin, currentFormat, newFormat, form);
	}
	
	private final TargetgroupViewFormat callSynchronizationMethod(final ComAdmin admin, final TargetgroupViewFormat currentFormat, final TargetgroupViewFormat newFormat, final QueryBuilderTargetGroupForm form) throws EditorContentSynchronizationException {
		if(currentFormat != newFormat) {
			switch(currentFormat) {
			case EQL:
				synchronizeEqlToQuerybuilder(admin, form);
				return newFormat;
				
			case QUERY_BUILDER:
				synchronizeQuerybuilderToEql(admin.getCompanyID(), form);
				return newFormat;
				
			default:
				throw new EditorContentSynchronizationException(String.format("Synchronizing %s to %s not supported", currentFormat, newFormat));
			}
		} else {
			return currentFormat;
		}
	}
	
	public final void synchronizeEqlToQuerybuilder(final ComAdmin admin, final QueryBuilderTargetGroupForm form) throws EditorContentSynchronizationException {
		try {
			form.setQueryBuilderRules(eqlToQueryBuilderConverter.convertEqlToQueryBuilderJson(form.getEql(), admin.getCompanyID()));
			form.setQueryBuilderFilters(filterListBuilder.buildFilterListJson(admin));
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
	
	public final void synchronizeQuerybuilderToEql(final int companyID, final QueryBuilderTargetGroupForm form) throws EditorContentSynchronizationException {
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
	
}
