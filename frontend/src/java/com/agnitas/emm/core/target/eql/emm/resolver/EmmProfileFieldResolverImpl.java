/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.resolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.ProfileField;
import com.agnitas.dao.ComProfileFieldDao;
import com.agnitas.emm.core.target.eql.codegen.DataType;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldNameResolver;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldResolveException;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldTypeResolver;
import com.agnitas.emm.core.target.eql.codegen.resolver.UnknownProfileFieldException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderConfiguration;

/**
 * EMM-specific implementation of the interfaces
 * {@link ProfileFieldNameResolver} and {@link ProfileFieldTypeResolver}.
 * 
 * Because this instance depends on the company ID, one instance per company ID
 * is needed.
 */
public class EmmProfileFieldResolverImpl implements EmmProfileFieldResolver {

	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(EmmProfileFieldResolverImpl.class);

	/** Mapping from short name of profile field to essential profile field data. */
	private final Map<String, ColumnNameAndType> shortNameToInfoMap;
	
	/** Mapping from name of profile field to shortname of profile field.
	 * A map containing previously resolved profile field names is used to to speed up conversion
	 * DB names of profile fields are keys, profile field shortnames are the values. */
	private Map<String, String> resolvedNames;

	/**
	 * Internally used data structure to combine the name and type of a profile
	 * field.
	 */
	private static class ColumnNameAndType {
		/** Name of a database column. */
		public final String dbName;

		/** Normalized type of the column. */
		public final DataType type;

		/**
		 * Creates a new instance.
		 * 
		 * @param dbName name of database column
		 * @param type   normalized data type
		 */
		public ColumnNameAndType(String dbName, DataType type) {
			this.dbName = dbName;
			this.type = type;
		}
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param companyId       company ID to use
	 * @param profileFieldDao DAO for accessing profile field data
	 * 
	 * @throws ProfileFieldResolveException on errors reading profile field data
	 */
	public EmmProfileFieldResolverImpl(@VelocityCheck int companyId, ComProfileFieldDao profileFieldDao)
			throws ProfileFieldResolveException {
		try {
			this.shortNameToInfoMap = readProfileFields(companyId, profileFieldDao);
			this.resolvedNames = new HashMap<>();
		} catch (Exception e) {
			throw new ProfileFieldResolveException("Error reading profile field data", e);
		}
	}

	public EmmProfileFieldResolverImpl(@VelocityCheck int companyId, ComProfileFieldDao profileFieldDao,
			QueryBuilderConfiguration configuration) throws ProfileFieldResolveException {
		try {
			this.shortNameToInfoMap = readProfileFields(companyId, profileFieldDao);
			configuration.getIndependentFields().forEach(field -> shortNameToInfoMap.put(field.getShortname(),
					new ColumnNameAndType(StringUtils.EMPTY, DataType.TEXT)));
			
			registerInternalColumnNames(this.shortNameToInfoMap);

			this.resolvedNames = new HashMap<>();

		} catch (Exception e) {
			throw new ProfileFieldResolveException("Error reading profile field data", e);
		}
	}
	
	private static final void registerInternalColumnNames(final Map<String, ColumnNameAndType> map) {
		map.put("$tracking_veto", new ColumnNameAndType("sys_tracking_veto", DataType.NUMERIC));
	}

	/**
	 * Reads data of all profile fields of given company and extracts essential
	 * data.
	 * 
	 * @param companyId company ID
	 * @param dao       DAO accessing profile field data
	 * 
	 * @return map of profile field short names to essential data
	 * 
	 * @throws Exception on errors reading or extracting profile field data
	 */
	private static Map<String, ColumnNameAndType> readProfileFields(int companyId, ComProfileFieldDao dao)
			throws Exception {
		Map<String, ColumnNameAndType> map = new HashMap<>();

		CaseInsensitiveMap<String, ProfileField> rawMap = dao.getComProfileFieldsMap(companyId, false);
		SimpleDataType simpleType;
		ColumnNameAndType cnat;
		for (ProfileField field : rawMap.values()) {
			simpleType = DbColumnType.getSimpleDataType(field.getDataType(), field.getNumericScale());
			cnat = new ColumnNameAndType(field.getColumn(), DbTypeMapper.mapDbType(simpleType));

			map.put(field.getShortname().toLowerCase(), cnat);
		}

		return map;
	}

	@Override
	public DataType resolveProfileFieldType(String profileFieldName) throws ProfileFieldResolveException {
		if (logger.isInfoEnabled()) {
			logger.info("Resolving type of profile field '" + profileFieldName + "'");
		}

		try {
			ColumnNameAndType column = getProfileFieldData(profileFieldName);

			if (column != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Profile field '" + profileFieldName + "' is of type " + column.type);
				}

				return column.type;
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Unknown profile field: " + profileFieldName);
				}

				throw new UnknownProfileFieldException(profileFieldName);
			}
		} catch (Exception e) {
			logger.error("Cannot resolve type of profile field '" + profileFieldName + "'", e);

			throw new ProfileFieldResolveException("Cannot resolve type of profile field '" + profileFieldName + "'",
					e);
		}
	}

	@Override
	public String resolveProfileFieldName(String profileFieldName) throws ProfileFieldResolveException {
		if (logger.isInfoEnabled()) {
			logger.info("Resolving column name of profile field '" + profileFieldName + "'");
		}

		try {
			ColumnNameAndType column = getProfileFieldData(profileFieldName);

			if (column != null) {
				String columnName = column.dbName;

				if (logger.isDebugEnabled()) {
					logger.debug("Column name of profile field '" + profileFieldName + "' is '" + columnName + "'");
				}

				return columnName;
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Unknown profile field: " + profileFieldName);
				}

				throw new UnknownProfileFieldException(profileFieldName);
			}
		} catch (Exception e) {
			logger.error("Cannot resolve type of profile field '" + profileFieldName + "'", e);

			throw new ProfileFieldResolveException("Cannot resolve name of profile field '" + profileFieldName + "'", e);
		}
	}
	
	@Override
	public String resolveProfileFieldColumnName(String dbName) throws ProfileFieldResolveException {
		try {
			String shortname = resolvedNames.get(dbName);
			if (shortname == null) {
				shortname = getProfileFieldName(dbName);
				
				resolvedNames.put(dbName, shortname);
			}
			return "`" + StringUtils.defaultIfEmpty(shortname, dbName) + "`";
		} catch(Exception e) {
			throw new ProfileFieldResolveException("Cannot resolve dbName of profile field'" + dbName + "'", e);
		}
	}

	/**
	 * Returns the normalized type and DB column name of a profile field.
	 * 
	 * @param name shortname of the profile field
	 * 
	 * @return name and type
	 * 
	 * @throws Exception on errors reading profile field data
	 */
	private ColumnNameAndType getProfileFieldData(String name) throws Exception {
		return shortNameToInfoMap.get(name.toLowerCase());
	}
	
	/**
	 * Returns the normalized type and DB column name of a profile field.
	 *
	 * @param dbName name of the profile field
	 *
	 * @return shortname
	 *
	 * @throws Exception on errors reading profile field data
	 */
	private String getProfileFieldName(String dbName) {
		Optional<Map.Entry<String, ColumnNameAndType>> optional = shortNameToInfoMap.entrySet().stream()
				.filter(pair -> StringUtils.equalsIgnoreCase(pair.getValue().dbName, dbName))
				.findAny();
		
		return optional.isPresent() ? optional.get().getKey() : "";
	}
}
