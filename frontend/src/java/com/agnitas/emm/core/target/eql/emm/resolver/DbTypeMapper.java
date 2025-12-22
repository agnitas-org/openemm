/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.resolver;

import com.agnitas.emm.core.target.eql.codegen.DataType;
import com.agnitas.util.DbColumnType;
import com.agnitas.util.DbColumnType.SimpleDataType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DbTypeMapper {
	
	private static final Logger logger = LogManager.getLogger(DbTypeMapper.class);
	
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
			case Numeric, Float:
				return DataType.NUMERIC;
				
			case Characters:
				return DataType.TEXT;
	
			case Date, DateTime:
				return DataType.DATE;
				
			default:
				logger.error("Unsupported column type {}", columnType);
				throw new IllegalArgumentException("Unsupported column type " + columnType);
		}
	}

}
