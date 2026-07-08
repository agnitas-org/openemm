/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.MailingBase;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.trackablelinks.dao.ClickRankingDao;
import com.agnitas.emm.core.trackablelinks.dto.ClickRanking;
import com.agnitas.emm.core.trackablelinks.service.ClickRankingService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClickRankingServiceImpl implements ClickRankingService {

    private final ClickRankingDao clickRankingDao;
    private final MailingService mailingService;

    public ClickRankingServiceImpl(ClickRankingDao clickRankingDao, MailingService mailingService) {
        this.clickRankingDao = clickRankingDao;
        this.mailingService = mailingService;
    }

    @Transactional
    @Override
    public void refreshRankings(int mailinglistId, int companyId, int lookbackDays) {
        if (lookbackDays <= 0) {
            return;
        }
        Set<Integer> mailingIds = mailingService.getMailingsForMailinglist(mailinglistId, companyId)
                .stream()
                .map(MailingBase::getId)
                .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(mailingIds)) {
            return;
        }

        LocalDateTime fromIncl = LocalDate.now().minusDays(lookbackDays).atStartOfDay();
        LocalDateTime toExcl = LocalDate.now().plusDays(1).atStartOfDay();
        List<ClickRanking> newRankings = clickRankingDao.calculateRankings(mailingIds, companyId, fromIncl, toExcl);

        clickRankingDao.deleteAll(companyId);

        if (!newRankings.isEmpty()) {
            clickRankingDao.batchInsert(newRankings);
        }
    }

    @Override
    public List<ClickRanking> findByTopic(String topic, int companyId) {
        return clickRankingDao.findByTopic(topic, companyId);
    }
}
