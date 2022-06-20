/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import org.agnitas.dao.DateFormatDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DateFormatDaoImpl extends BaseDaoImpl implements DateFormatDao {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(DateFormatDaoImpl.class);
	
	@Override
	public String getFormat(int typeID) throws Exception {
		return select(logger, "SELECT format FROM date_tbl WHERE type = ?", String.class, typeID);
	}
}
