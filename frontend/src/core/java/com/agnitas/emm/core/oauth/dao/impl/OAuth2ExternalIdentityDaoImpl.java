/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.oauth.dao.impl;

import java.util.Optional;
import javax.sql.DataSource;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.oauth.dao.OAuth2ExternalIdentityDao;
import com.agnitas.emm.core.oauth.model.OAuthExternalIdentityRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class OAuth2ExternalIdentityDaoImpl extends BaseDaoImpl implements OAuth2ExternalIdentityDao {

    private final RowMapper<OAuthExternalIdentityRecord> rowMapper = (rs, rowNum) ->
            new OAuthExternalIdentityRecord(
                    rs.getString("issuer"),
                    rs.getString("subject"),
                    rs.getInt("admin_id"),
                    rs.getInt("company_id")
            );

    protected OAuth2ExternalIdentityDaoImpl(
            @Qualifier("dataSource") DataSource dataSource,
            @Autowired(required = false) JavaMailService javaMailService
    ) {
        super(dataSource, javaMailService);
    }

    @Override
    public int save(String issuer, String subject, int adminId, int companyId) {
        if (isOracleDB()) {
            int newId = selectInt("SELECT oauth2_ext_identity_tbl_seq.nextval FROM DUAL");
            String sql = """
                INSERT INTO oauth2_ext_identity_tbl (id, admin_id, issuer, subject, company_id)
                VALUES (?, ?, ?, ?, ?)
                """;

            update(sql, newId, adminId, issuer, subject, companyId);
            return newId;
        }

        String sql = """
                INSERT INTO oauth2_ext_identity_tbl (admin_id, issuer, subject, company_id)
                VALUES (?, ?, ?, ?)
                """;
        return insert("id", sql, adminId, issuer, subject, companyId);
    }

    @Override
    public Optional<OAuthExternalIdentityRecord> find(String issuer, String subject) {
        String sql = """
                SELECT admin_id, issuer, subject, company_id
                FROM oauth2_ext_identity_tbl
                WHERE issuer = ? AND subject = ?
                """;

        return findObject(sql, rowMapper, issuer, subject);
    }

    @Override
    public void delete(String issuer, int adminId) {
        update("DELETE FROM oauth2_ext_identity_tbl WHERE issuer = ? AND admin_id = ?", issuer, adminId);
    }

    @Override
    public boolean exists(String issuer, int adminId) {
        String sql = """
                SELECT COUNT(*)
                FROM oauth2_ext_identity_tbl
                WHERE issuer = ? AND admin_id = ?
                """;

        return selectInt(sql, issuer, adminId) > 0;
    }

}
