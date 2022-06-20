/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mimetypes.dao;

import java.util.List;

import jakarta.activation.MimeType;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MimeTypeWhitelistPatternDaoImpl extends BaseDaoImpl implements MimeTypeWhitelistPatternDao {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(MimeTypeWhitelistPatternDaoImpl.class);

	@Override
	public final List<MimeType> listWhitelistedMimeType() {
		final String sql = "SELECT mimetype FROM mimetype_whitelist_tbl";

		return select(logger, sql, new MimeTypeRowMapper());
	}

}
