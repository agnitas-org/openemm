/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.dao.impl;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import com.agnitas.beans.BindingEntry;
import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.birtstatistics.dao.MailingBounceStatsDao;
import com.agnitas.emm.core.birtstatistics.dto.EmailBounceDetail;
import com.agnitas.emm.core.bounce.Bounce;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.Tuple;
import com.agnitas.util.importvalues.Gender;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MailingBounceStatsDaoImpl extends BaseDaoImpl implements MailingBounceStatsDao {

    public MailingBounceStatsDaoImpl(
            @Qualifier("dataSource") DataSource dataSource,
            JavaMailService javaMailService
    ) {
        super(dataSource, javaMailService);
    }

    @Override
    public Map<Bounce, Integer> getSoftBounces(int mailingId, int companyId, String targetSql) {
        String joinCustomerTbl = isBlank(targetSql)
                ? "" :
                "JOIN customer_%d_tbl cust ON (bounce.customer_id = cust.customer_id)".formatted(companyId);
        return selectLinkedMap("""
                        SELECT COUNT(DISTINCT bounce.customer_id) amount, bounce.detail AS detail
                        FROM bounce_tbl bounce %s
                        WHERE bounce.company_id = ? AND bounce.mailing_id = ? AND bounce.detail <= 509 %s
                        GROUP BY detail
                        ORDER BY detail""".formatted(joinCustomerTbl, andClause(targetSql)),
                (rs, rowNum) -> new Tuple<>(
                        Bounce.from(rs.getInt("detail")),
                        rs.getInt("amount")
                ),
                companyId,
                mailingId
        );
    }

    @Override
    public Map<Bounce, Integer> getHardBounces(int mailingId, int companyId, String targetSql) {
        String joinCustomerTbl = isBlank(targetSql)
                ? "" :
                "JOIN customer_%d_tbl cust ON bind.customer_id = cust.customer_id".formatted(companyId);
        return selectLinkedMap("""
                        SELECT bind.user_remark AS detail, COUNT(DISTINCT bind.customer_id) AS amount
                        FROM customer_%d_binding_tbl bind %s
                        WHERE bind.exit_mailing_id = ?
                          AND bind.user_status = ?
                          AND bind.user_type IN ('%s', '%s')
                          %s
                        GROUP BY bind.user_remark""".formatted(
                        companyId,
                        joinCustomerTbl,
                        BindingEntry.UserType.World.getTypeCode(),
                        BindingEntry.UserType.WorldVIP.getTypeCode(),
                        andClause(targetSql)
                ),
                (rs, rowNum) -> new Tuple<>(
                        getBounceFromRemark(rs.getString("detail")),
                        rs.getInt("amount")
                ),
                mailingId,
                UserStatus.Bounce.getStatusCode()
        );
    }

    private static Bounce getBounceFromRemark(String userRemark) {
        if (userRemark == null || !userRemark.startsWith(BOUNCE_REMARK_SIGN)) {
            return Bounce.OTHER_HARD_BOUNCE;
        }
        String bounceDetailCodeStr = userRemark.substring(BOUNCE_REMARK_SIGN.length()).trim();
        return AgnUtils.isNumber(bounceDetailCodeStr)
                ? Bounce.from(Integer.parseInt(bounceDetailCodeStr))
                : Bounce.OTHER_HARD_BOUNCE;
    }

    @Override
    public List<EmailBounceDetail> getHardBouncesWithDetailAndEmail(int mailingId, int companyId, String targetSql) {
        // default
        return select("""
                SELECT
                    cust.email email,
                    cust.gender gender,
                    cust.firstname firstname,
                    cust.lastname lastname,
                    cust.customer_id customer_id,
                    MAX(bind.user_remark) AS user_remark
                FROM customer_%d_binding_tbl bind
                  JOIN customer_%d_tbl cust ON bind.customer_id = cust.customer_id
                WHERE bind.user_status = ?
                  AND bind.exit_mailing_id = ?
                  AND bind.user_type IN ('%s', '%s')
                  %s
                GROUP BY cust.email, cust.gender, cust.firstname, cust.lastname, cust.customer_id"""
                .formatted(
                        companyId,
                        companyId,
                        BindingEntry.UserType.World.getTypeCode(),
                        BindingEntry.UserType.WorldVIP.getTypeCode(),
                        andClause(targetSql)
                ), (rs, i) -> {
            String userRemark = rs.getString("user_remark");
            int code = 510; // default
            if (userRemark != null && userRemark.startsWith(BOUNCE_REMARK_SIGN)) {
                String bounceDetailCodeString = userRemark.substring(BOUNCE_REMARK_SIGN.length()).trim();
                if (AgnUtils.isNumber(bounceDetailCodeString)) {
                    code = Integer.parseInt(bounceDetailCodeString);
                }
            }
            return new EmailBounceDetail(
                    code,
                    rs.getInt("customer_id"),
                    Gender.getGenderById(rs.getInt("gender")),
                    rs.getString("email"),
                    rs.getString("firstname"),
                    rs.getString("lastname")

            );
        }, UserStatus.Bounce.getStatusCode(), mailingId);
    }

    @Override
    public List<EmailBounceDetail> getSoftBouncesWithDetailAndEmail(int mailingId, int companyId, String targetSql) {
        return select("""
                        SELECT cust.email email,
                               cust.gender gender,
                               cust.firstname firstname,
                               cust.lastname lastname,
                               cust.customer_id customer_id,
                               bounce.detail AS detail
                        FROM bounce_tbl bounce
                          JOIN customer_%d_tbl cust ON (bounce.customer_id = cust.customer_id)
                        WHERE bounce.company_id = ?
                          AND bounce.mailing_id = ?
                          AND bounce.detail <= 509
                          %s""".formatted(companyId, andClause(targetSql)),
                (rs, i) -> new EmailBounceDetail(
                        rs.getInt("detail"),
                        rs.getInt("customer_id"),
                        Gender.getGenderById(rs.getInt("gender")),
                        rs.getString("email"),
                        rs.getString("firstname"),
                        rs.getString("lastname")
                ),
                companyId, mailingId
        );
    }
}
