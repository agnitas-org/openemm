/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.resolver;

import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.emm.core.target.eql.codegen.DataType;

public class DbTypeMapper {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(DbTypeMapper.class);
	
	public static DataType mapDbType(DbColumnType columnType) {
		SimpleDataType simpleDataType = DbColumnType.getSimpleDataType(columnType.getTypeName(), columnType.getNumericScale());
		
		return mapDbType(simpleDataType);
	}
	/**
	 * Maps {@link SimpleDataType} to {@link DataType}.
	 * 
	 * @param columnType value of {@link SimpleDataType}
	 * 
	 * @return corresponding {@link DataType} value
	 */
	public static DataType mapDbType(SimpleDataType columnType) {
		switch(columnType) {
			case Numeric:
			case Float:
				return DataType.NUMERIC;
				
			case Characters:
				return DataType.TEXT;
	
			case Date:
			case DateTime:
				return DataType.DATE;
				
			default:
				logger.error("Unsupported column type " + columnType);
				throw new IllegalStateException("Unsupported column type " + columnType);
		}
	}

}
