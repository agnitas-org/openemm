/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.dao.impl;

import com.agnitas.dao.SeasonKeyDao;
import com.agnitas.dao.impl.mapper.BlobRowMapper;


public class SeasonKeyDaoImpl extends BaseDaoImpl implements SeasonKeyDao {
    @Override
    public byte[] getSeasonKey(String keyId) {
        String query = "SELECT season_key FROM season_key_tbl WHERE key_id = ?";
        return select(query, BlobRowMapper.INSTANCE, keyId).getFirst();
    }

    @Override
    public byte[] getSeasonSignature(String keyId) {
        String query = "SELECT signature FROM season_key_tbl WHERE key_id = ?";
        return select(query, BlobRowMapper.INSTANCE, keyId).getFirst();
    }
}
