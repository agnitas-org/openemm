/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.stats.recipient;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.agnitas.dao.BindingEntryDao;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.birtstatistics.dto.RecipientProgressDetails;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.util.importvalues.MailType;
import org.springframework.stereotype.Service;

@Service("recipientStatsService")
class RecipientStatsServiceImpl implements RecipientStatsService {

    protected final TargetService targetService;
    protected final RecipientStatsDao recipientStatsDao;
    private final RecipientStatsMapper recipientStatsMapper;
    private final BindingEntryDao bindingEntryDao;

    public RecipientStatsServiceImpl(
            RecipientStatsDao recipientStatsDao,
            RecipientStatsMapper recipientStatsMapper,
            TargetService targetService,
            BindingEntryDao bindingEntryDao
    ) {
        this.recipientStatsDao = recipientStatsDao;
        this.recipientStatsMapper = recipientStatsMapper;
        this.bindingEntryDao = bindingEntryDao;
        this.targetService = targetService;
    }

    @Override
    public Map<LocalDate, RecipientProgressDetails> getDailyProgress(RecipientStatsQuery query) {
        List<BindStatusDetails> rows = recipientStatsDao.getRecipientStatusCounts(toFilter(query));

        Map<LocalDate, RecipientProgressDetails> statsMap = new HashMap<>();
        for (BindStatusDetails row : rows) {
            LocalDate day = row.timestamp().atZone(query.dateRange().getZoneId()).toLocalDate();
            RecipientProgressDetails progress = statsMap.computeIfAbsent(day, d -> new RecipientProgressDetails());
            updateByStatus(progress, row.userStatus(), row.amount());
        }
        addMissingDays(statsMap, query.dateRange().iterator(), RecipientProgressDetails::new);
        return new TreeMap<>(statsMap);
    }

    protected RecipientStatsFilter toFilter(RecipientStatsQuery query) {
        String targetSql = getTargetSQL(query);
        DateRange dateRange = query.dateRange().toDateRange();
        return recipientStatsMapper.toFilter(query, targetSql, dateRange);
    }

    protected String getTargetSQL(RecipientStatsQuery query) {
        return targetService.getTargetSqlOrEmpty(query.targetId(), query.companyId());
    }

    private void updateByStatus(RecipientProgressDetails item, UserStatus status, long amount) {
        switch (status) {
            case Active -> item.setOptIns(item.getOptIns() + amount);
            case Bounce -> item.setBounced(item.getBounced() + amount);
            case WaitForConfirm -> item.setOptInWithoutConfirm(item.getOptInWithoutConfirm() + amount);
            case Blacklisted -> item.setBlocklisted(item.getBlocklisted() + amount);
            case null, default -> item.setOptOuts(item.getOptOuts() + amount);
        }
    }

    protected static <T> void addMissingDays(
            Map<LocalDate, T> map,
            Iterable<LocalDate> dateRange,
            Supplier<T> factory
    ) {
        for (LocalDate day : dateRange) {
            map.putIfAbsent(day, factory.get());
        }
    }

    @Override
    public Map<String, Integer> getStatusInfo(int mailinglistId, int targetId, int companyId) {
        return bindingEntryDao.getRecipientStatusStat(mailinglistId, targetId, companyId);
    }

    @Override
    public Map<MailType, Integer> getMailFormat(RecipientStatsQuery query) {
        Map<MailType, Long> mailtypeCounts = recipientStatsDao.getMailtypeCounts(toFilter(query));
        long total = mailtypeCounts.values().stream().mapToLong(Long::longValue).sum();
        return mailtypeCounts.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> (int) ((100 * e.getValue()) / total),
                (a, b) -> a,
                LinkedHashMap::new
        ));
    }

    @Override
    public Map<UserStatus, Long> getRecipientStatus(RecipientStatsQuery query) {
        return recipientStatsDao.getUserStatusCounts(toFilter(query));
    }
}
