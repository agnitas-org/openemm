/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.stats.recipient;

import java.time.LocalDate;
import java.util.Map;

import com.agnitas.emm.core.birtstatistics.dto.RecipientProgress;
import com.agnitas.emm.core.stats.recipient.RecipientProgressDao.Filter;
import com.agnitas.emm.core.target.service.TargetService;
import org.springframework.stereotype.Service;

@Service
class RecipientStatsServiceImpl implements RecipientStatsService {

    private final RecipientProgressDao recipientProgressDao;
    private final RecipientStatsMapper recipientStatsMapper;
    private final TargetService targetService;

    public RecipientStatsServiceImpl(
            RecipientProgressDao recipientProgressDao,
            RecipientStatsMapper recipientStatsMapper,
            TargetService targetService
    ) {
        this.recipientProgressDao = recipientProgressDao;
        this.recipientStatsMapper = recipientStatsMapper;
        this.targetService = targetService;
    }

    @Override
    public Map<LocalDate, RecipientProgress> getDailyProgress(int companyId, RecipientStatsQuery query) {
        String targetSql = targetService.getTargetSQL(query.targetId(), companyId);
        Filter filter = recipientStatsMapper.toFilter(query, targetSql);
        Map<LocalDate, RecipientProgress> progress = recipientProgressDao.getProgress(companyId, filter);
        addMissingDays(progress, query);
        return progress;
    }

    private static void addMissingDays(Map<LocalDate, RecipientProgress> progress, RecipientStatsQuery query) {
        LocalDate current = query.dateRange().getFrom();
        while (current.isBefore(query.dateRange().getTo())) {
            progress.putIfAbsent(current, new RecipientProgress());
            current = current.plusDays(1);
        }
    }
}
