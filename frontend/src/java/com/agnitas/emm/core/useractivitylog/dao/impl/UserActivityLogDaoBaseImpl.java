/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.dao.impl;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.useractivitylog.dao.UserActivityLogDaoBase;
import com.agnitas.beans.AdminEntry;
import com.agnitas.dao.impl.PaginatedBaseDaoImpl;

public abstract class UserActivityLogDaoBaseImpl extends PaginatedBaseDaoImpl implements UserActivityLogDaoBase {

    @Override
    public void addAdminUseOfFeature(Admin admin, String feature, Date date) {
        if (admin != null && admin.getAdminID() != 0) {
            if (isOracleDB()) {
                String updateSql = "UPDATE admin_use_tbl SET use_count = use_count + 1, last_use = ? WHERE admin_id = ? AND feature = ?";
                int updatedLines = update(updateSql, date, admin.getAdminID(), feature);
                if (updatedLines == 0) {
                    String insertSql = "INSERT INTO admin_use_tbl (admin_id, feature, use_count, last_use) VALUES (?, ?, 1, ?)";
                    try {
                        update(insertSql, admin.getAdminID(), feature, date);
                    } catch (Exception e) {
                        // if another request already created the entry meanwhile
                        update(updateSql, date, admin.getAdminID(), feature);
                    }
                }
            } else {
                String sql = "INSERT INTO admin_use_tbl " +
                        "(admin_id, feature, use_count, last_use) " +
                        "VALUES(?, ?, 1, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "use_count = use_count + 1, last_use = ?";
                update(sql, admin.getAdminID(), feature, date, date);
            }
        }
    }

    protected String buildVisibleAdminsCondition(List<AdminEntry> visibleAdmins) {
        List<String> usernames = visibleAdmins.stream()
                .filter(Objects::nonNull)
                .map(AdminEntry::getUsername)
                .toList();

        if (!usernames.isEmpty()) {
            return makeBulkInClauseForString("username", usernames);
        }

        return "";
    }
}
