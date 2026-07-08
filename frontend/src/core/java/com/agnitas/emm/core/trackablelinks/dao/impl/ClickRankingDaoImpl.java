/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.dao.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.trackablelinks.dao.ClickRankingDao;
import com.agnitas.emm.core.trackablelinks.dto.ClickRanking;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class ClickRankingDaoImpl extends BaseDaoImpl implements ClickRankingDao {

    private static final RowMapper<ClickRanking> CLICK_RANKING_ROW_MAPPER = (rs, i) -> new ClickRanking(
            rs.getInt("url_id"),
            rs.getInt("company_id"),
            rs.getString("url"),
            rs.getString("topic"),
            rs.getLong("count"),
            rs.getInt("rank"),
            rs.getTimestamp("first_clickdate").toInstant()
    );

    public ClickRankingDaoImpl(@Qualifier("dataSource") DataSource dataSource, JavaMailService javaMailService) {
        super(dataSource, javaMailService);
    }

    @Override
    public List<ClickRanking> calculateRankings(Set<Integer> mailingIds, int companyId, LocalDateTime fromIncl, LocalDateTime toExcl) {
        return select("""
                        SELECT
                          t.url_id,
                          t.company_id,
                          t.url,
                          t.topic,
                          t.cnt AS count,
                          ROW_NUMBER() OVER (PARTITION BY t.topic ORDER BY t.cnt DESC, t.first_clickdate ASC) AS rank,
                          t.first_clickdate
                          FROM (
                            SELECT
                              c.url_id,
                              c.company_id,
                              l.full_url as url,
                              a.shortname AS topic,
                              COUNT(*) AS cnt,
                              MIN(c.timestamp) AS first_clickdate
                            FROM rdirlog_%d_tbl c
                              JOIN rdir_url_tbl l ON c.url_id = l.url_id
                              JOIN rdir_action_tbl a ON l.action_id = a.action_id
                            WHERE l.emm_trigger = 1
                              AND c.company_id = ?
                              AND c.timestamp >= ?
                              AND c.timestamp < ?
                              AND c.mailing_id IN (%s)
                            GROUP BY c.url_id, c.company_id, l.full_url, a.shortname
                        ) t
                        ORDER BY t.cnt DESC, t.first_clickdate ASC
                        """.formatted(companyId, StringUtils.join(mailingIds, ",")),
                CLICK_RANKING_ROW_MAPPER, companyId, Timestamp.valueOf(fromIncl), Timestamp.valueOf(toExcl));
    }

    @Override
    public void batchInsert(List<ClickRanking> rankings) {
        batchupdate("""
                        INSERT INTO hp_clickranking_tbl (url_id, company_id, url, topic, count, rank, first_clickdate)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                rankings.stream()
                        .map(row -> new Object[]{
                                row.urlId(),
                                row.companyId(),
                                row.url(),
                                row.topic(),
                                row.count(),
                                row.rank(),
                                Date.from(row.firstClickDate())
                        })
                        .toList()
        );
    }

    @Override
    public List<ClickRanking> findByTopic(String topic, int companyId) {
        return select("""
                        SELECT *
                        FROM hp_clickranking_tbl
                        WHERE company_id = ?
                          AND topic = ?
                        ORDER BY count DESC
                        """,
                CLICK_RANKING_ROW_MAPPER, companyId, topic
        );
    }

    @Override
    public void deleteAll(int companyId) {
        update("DELETE FROM hp_clickranking_tbl WHERE company_id = ?", companyId);
    }
}
