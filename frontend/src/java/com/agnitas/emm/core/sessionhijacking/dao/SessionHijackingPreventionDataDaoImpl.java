/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.sessionhijacking.dao;

import java.util.List;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.core.sessionhijacking.beans.IpSettings;
import org.springframework.jdbc.core.RowMapper;

/**
 * DAO accessing configuration of {@link com.agnitas.emm.core.sessionhijacking.web.GroupingSessionHijackingPreventionFilter}.
 */
public class SessionHijackingPreventionDataDaoImpl extends BaseDaoImpl implements SessionHijackingPreventionDataDao {

    private static final RowMapper<IpSettings> ROW_MAPPER = (rs, rowNum) -> {
        String ip = rs.getString("ip");
        int group = rs.getInt("ip_group");
        boolean groupIsNull = rs.wasNull();

        return new IpSettings(ip, groupIsNull ? null : group);
    };


    @Override
    public List<IpSettings> listIpSettings() {
        return select("SELECT ip, ip_group FROM sessionhijackingprevention_tbl", ROW_MAPPER);
    }

}
