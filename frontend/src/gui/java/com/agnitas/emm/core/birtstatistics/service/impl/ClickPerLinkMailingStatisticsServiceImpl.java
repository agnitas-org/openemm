/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.service.impl;

import static com.agnitas.messages.I18nString.getLocaleString;
import static com.agnitas.util.AgnUtils.calculateRate;
import static java.util.Objects.requireNonNullElse;
import static org.apache.tomcat.jakartaee.commons.lang3.StringUtils.defaultIfBlank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.beans.Admin;
import com.agnitas.beans.TrackableLink;
import com.agnitas.emm.core.birtstatistics.dao.TrackableLinkStatisticsDao;
import com.agnitas.emm.core.birtstatistics.dto.ClickStatisticsPerLinkResponse;
import com.agnitas.emm.core.birtstatistics.dto.ClickStatisticsPerLinkResponse.LinkCategoryBlock;
import com.agnitas.emm.core.birtstatistics.dto.ClickStatisticsPerLinkResponse.LinkStats;
import com.agnitas.emm.core.birtstatistics.dto.ClickStatisticsPerLinkResponse.ShareStats;
import com.agnitas.emm.core.birtstatistics.dto.ClickStatisticsPerLinkResponse.SummaryBlock;
import com.agnitas.emm.core.birtstatistics.dto.ClickStatisticsPerLinkResponse.SummaryCategoryBlock;
import com.agnitas.emm.core.birtstatistics.dto.ClickStatisticsPerLinkResponse.TargetGroupStats;
import com.agnitas.emm.core.birtstatistics.enums.StatisticLinkCategory;
import com.agnitas.emm.core.birtstatistics.enums.StatisticLinkCategory.CategoryType;
import com.agnitas.emm.core.birtstatistics.model.LinkClickStatisticData;
import com.agnitas.emm.core.birtstatistics.service.ClickPerLinkMailingStatisticsService;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkService;
import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.StatisticMetric;
import com.agnitas.util.CsvWriter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class ClickPerLinkMailingStatisticsServiceImpl implements ClickPerLinkMailingStatisticsService {

    private static final int ALL_RECIPIENTS_TARGET_ID = -1;

    private static final Comparator<StatRow> CSV_ROW_COMPARATOR =
            Comparator.comparing(StatRow::categoryType)
                    .thenComparing(StatRow::targetGroupId)
                    .thenComparing(StatRow::type, Comparator.reverseOrder())
                    .thenComparing(r -> r.clicks().value(), Comparator.reverseOrder())
                    .thenComparing(r -> r.url() == null ? "" : r.url());

    private final TrackableLinkService trackableLinkService;
    private final TrackableLinkStatisticsDao linkStatisticsDao;
    private final TargetService targetService;

    public ClickPerLinkMailingStatisticsServiceImpl(
            TrackableLinkService trackableLinkService,
            TrackableLinkStatisticsDao linkStatisticsDao,
            TargetService targetService
    ) {
        this.trackableLinkService = trackableLinkService;
        this.linkStatisticsDao = linkStatisticsDao;
        this.targetService = targetService;
    }

    private enum ShareType {

        TOTAL("report.total"),
        MOBILE("report.mobile", DeviceClass.MOBILE, null),
        ANONYMOUS("statistics.share.anonymous", null, 0);

        final DeviceClass deviceClass;
        final Integer customerId;
        final String messageKey;

        ShareType(String messageKey) {
            this(messageKey, null, null);
        }

        ShareType(String messageKey, DeviceClass deviceClass, Integer customerId) {
            this.messageKey = messageKey;
            this.deviceClass = deviceClass;
            this.customerId = customerId;
        }

    }

    private record Counts(long clicks, long clickers) {
        Counts add(Counts other) {
            return new Counts(
                    this.clicks + other.clicks,
                    this.clickers + other.clickers
            );
        }

        static Counts empty() {
            return new Counts(0, 0);
        }
    }

    private static class StatisticsDataHolder {

        record DataKey(ShareType shareType, Integer targetGroupId) { }

        final Map<DataKey, Map<Integer, LinkClickStatisticData>> data = new HashMap<>();

        void put(ShareType shareType, Integer targetGroupId, Map<Integer, LinkClickStatisticData> data) {
            this.data.put(new DataKey(shareType, targetGroupId), data);
        }

        Map<Integer, LinkClickStatisticData> get(ShareType shareType, Integer targetGroupId) {
            return this.data.get(new DataKey(shareType, targetGroupId));
        }

    }

    private enum StatRowType {
        SUMMARY,
        URL
    }

    private record StatRow(
            CategoryType categoryType,
            Integer targetGroupId,
            ShareType shareType,
            StatRowType type,
            Integer linkId,
            String url,
            StatisticMetric clicks,
            StatisticMetric clickers
    ) {
    }

    private record CsvRow(
            String url,
            String categoryName,
            String shareName,
            String targetGroupName,
            StatisticMetric clicks,
            StatisticMetric clickers
    ) {

        static CsvRow empty() {
            return new CsvRow(null, null, null, null, null, null);
        }

    }

    @Override
    public ClickStatisticsPerLinkResponse getStatistics(int mailingId, Set<Integer> targetGroups, Admin admin) {
        targetGroups = prepareTargetIds(targetGroups);
        List<StatRow> rows = collectStatistics(mailingId, targetGroups, admin);

        return UiResponseBuilder.build(rows, targetGroups, admin);
    }

    private static class UiResponseBuilder {
        static ClickStatisticsPerLinkResponse build(List<StatRow> rows, Set<Integer> targetGroups, Admin admin) {
            Map<StatRowType, List<StatRow>> rowsByType = rows.stream()
                    .collect(Collectors.groupingBy(StatRow::type));

            Map<CategoryType, StatisticLinkCategory> categoriesMap = buildCategories(admin);

            return new ClickStatisticsPerLinkResponse(
                    buildSummary(rowsByType.get(StatRowType.SUMMARY), categoriesMap, targetGroups),
                    buildLinkBlocks(rowsByType.get(StatRowType.URL), categoriesMap, targetGroups)
            );
        }

        private static SummaryBlock buildSummary(
                List<StatRow> rows, Map<CategoryType, StatisticLinkCategory> categoriesMap, Set<Integer> targetGroupIds
        ) {
            Map<CategoryType, List<StatRow>> rowsByCategory = rows.stream()
                    .collect(Collectors.groupingBy(StatRow::categoryType));

            List<SummaryCategoryBlock> blocks = Stream.of(CategoryType.values())
                    .map(t ->
                            new SummaryCategoryBlock(
                                    categoriesMap.get(t),
                                    buildTargetStats(rowsByCategory.get(t), targetGroupIds)
                            )
                    )
                    .toList();

            return new SummaryBlock(blocks);
        }

        private static List<LinkCategoryBlock> buildLinkBlocks(
                List<StatRow> rows, Map<CategoryType, StatisticLinkCategory> categoriesMap, Set<Integer> targetGroupIds
        ) {
            if (CollectionUtils.isEmpty(rows)) {
                return Collections.emptyList();
            }

            Map<CategoryType, List<StatRow>> rowsByCategory = rows.stream()
                    .collect(Collectors.groupingBy(StatRow::categoryType));

            return Stream.of(CategoryType.values())
                    .map(t ->
                            new LinkCategoryBlock(
                                    categoriesMap.get(t),
                                    getLinkStats(rowsByCategory.get(t), targetGroupIds)
                            )
                    ).toList();
        }

        private static List<LinkStats> getLinkStats(List<StatRow> rows, Set<Integer> targetGroups) {
            Map<String, List<StatRow>> rowsByUrl = rows.stream()
                    .collect(Collectors.groupingBy(StatRow::url));

            Map<String, Integer> idsByUrl = rows.stream()
                    .collect(Collectors.toMap(StatRow::url, StatRow::linkId, (a, b) -> a));

            return rowsByUrl.entrySet()
                    .stream()
                    .map(e ->
                            new LinkStats(
                                    idsByUrl.get(e.getKey()),
                                    e.getKey(),
                                    buildTargetStats(e.getValue(), targetGroups)
                            )
                    )
                    .sorted(Comparator.<LinkStats>comparingLong(
                            stat -> stat.targetGroups().getFirst().total().clicks().value()).reversed()
                    )
                    .toList();
        }

        private static List<TargetGroupStats> buildTargetStats(List<StatRow> rows, Set<Integer> targetGroups) {
            return targetGroups.stream()
                    .map(t ->
                            new TargetGroupStats(
                                    t == ALL_RECIPIENTS_TARGET_ID ? null : t,
                                    toShareStats(rows, ShareType.TOTAL, t),
                                    toShareStats(rows, ShareType.MOBILE, t),
                                    toShareStats(rows, ShareType.ANONYMOUS, t)
                            ))
                    .toList();
        }

        private static ShareStats toShareStats(List<StatRow> rows, ShareType shareType, Integer targetId) {
            return rows.stream()
                    .filter(r ->
                            Objects.equals(r.targetGroupId(), targetId) && Objects.equals(shareType, r.shareType)
                    )
                    .findAny()
                    .map(statRow -> new ShareStats(statRow.clicks(), statRow.clickers()))
                    .orElseGet(() ->
                            new ShareStats(
                                    new StatisticMetric(0, 0),
                                    new StatisticMetric(0, 0)
                            )
                    );
        }
    }

    @Override
    public byte[] getCsvData(int mailingId, Set<Integer> targetGroups, Admin admin) throws Exception {
        targetGroups = prepareTargetIds(targetGroups);
        List<StatRow> rows = collectStatistics(mailingId, targetGroups, admin);

        Map<CategoryType, StatisticLinkCategory> categoriesMap = buildCategories(admin);
        Map<Integer, String> targetNames = getTargetGroupNames(targetGroups, admin);

        List<CsvRow> csvRows = rows.stream()
                .sorted(CSV_ROW_COMPARATOR)
                .map(r -> toCsvRow(r, r.categoryType(), admin, categoriesMap, targetNames))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        this::addEmptyRowsAfterCategory
                ));

        return CsvWriter.csv(ListUtils.union(
                List.of(getHeaderForCsv(admin.getLocale())),
                getRowsForCsv(csvRows)
        ));
    }

    private List<CsvRow> addEmptyRowsAfterCategory(List<CsvRow> rows) {
        List<CsvRow> result = new ArrayList<>();
        String currentCategory = null;
        for (CsvRow row : rows) {
            if (!Objects.equals(currentCategory, row.categoryName())) {
                if (currentCategory != null) {
                    result.add(CsvRow.empty());
                }
                currentCategory = row.categoryName();
            }
            result.add(row);
        }
        return result;
    }

    private CsvRow toCsvRow(
            StatRow row,
            CategoryType categoryType,
            Admin admin,
            Map<CategoryType, StatisticLinkCategory> categoriesMap,
            Map<Integer, String> targetNames
    ) {
        return new CsvRow(
                row.url(),
                categoriesMap.get(categoryType).name(),
                getLocaleString(row.shareType().messageKey, admin.getLocale()),
                targetNames.get(row.targetGroupId()),
                row.clicks(),
                row.clickers()
        );
    }

    private List<String> getHeaderForCsv(Locale locale) {
        return List.of(
                getLocaleString("URL", locale),
                "",
                getLocaleString("MobileOrTotal", locale),
                getLocaleString("birt.Target", locale),
                getLocaleString("statistic.Clicks", locale),
                getLocaleString("report.clicksGrossInPercent", locale),
                getLocaleString("statistic.clicker", locale),
                getLocaleString("report.clicksNetInPercent", locale)
        );
    }

    private List<List<String>> getRowsForCsv(List<CsvRow> rows) {
        return rows.stream()
                .map(data -> Arrays.asList(
                        defaultIfBlank(data.url(), ""),
                        defaultIfBlank(data.categoryName(), ""),
                        defaultIfBlank(data.shareName(), ""),
                        defaultIfBlank(data.targetGroupName(), ""),
                        data.clicks() == null ? "" : String.valueOf(data.clicks().value()),
                        data.clicks() == null ? "" : data.clicks().formatRate(),
                        data.clickers() == null ? "" : String.valueOf(data.clickers().value()),
                        data.clickers() == null ? "" : data.clickers().formatRate()
                ))
                .toList();
    }

    private List<StatRow> collectStatistics(int mailingId, Set<Integer> targetIds, Admin admin) {
        List<TrackableLink> links = trackableLinkService.getTrackableLinks(admin.getCompanyID(), mailingId, false);
        Map<CategoryType, List<TrackableLink>> linksByCategory = groupLinksByCategory(links);

        StatisticsDataHolder dataHolder = loadStatistics(mailingId, admin.getCompanyID(), targetIds);
        Map<Integer, Counts> totals = calculateTotals(links, dataHolder, targetIds);

        List<StatRow> rows = new ArrayList<>();

        for (CategoryType categoryType : CategoryType.values()) {
            for (Integer targetId : targetIds) {
                rows.addAll(collectUrlRows(linksByCategory.get(categoryType), dataHolder, categoryType, targetId, totals));
                rows.addAll(collectSummaryRows(dataHolder, linksByCategory, categoryType, targetId, totals));
            }
        }

        return rows;
    }

    private List<StatRow> collectUrlRows(
            List<TrackableLink> links,
            StatisticsDataHolder dataHolder,
            CategoryType categoryType,
            Integer targetId,
            Map<Integer, Counts> totals
    ) {
        List<StatRow> rows = new ArrayList<>();

        for (TrackableLink link : links) {
            for (ShareType share : ShareType.values()) {
                LinkClickStatisticData data = dataHolder.get(share, targetId).get(link.getId());
                Counts total = totals.get(targetId);

                Counts counts = data == null
                        ? Counts.empty()
                        : new Counts(data.clicksCount(), data.clickersCount());

                rows.add(new StatRow(
                        categoryType,
                        targetId,
                        share,
                        StatRowType.URL,
                        link.getId(),
                        link.getFullUrl(),
                        new StatisticMetric(counts.clicks(), calculateRate(counts.clicks(), total.clicks())),
                        new StatisticMetric(counts.clickers(), calculateRate(counts.clickers(), total.clickers()))
                ));
            }
        }

        return rows;
    }

    private List<StatRow> collectSummaryRows(
            StatisticsDataHolder dataHolder,
            Map<CategoryType, List<TrackableLink>> linksByCategory,
            CategoryType categoryType,
            Integer targetId,
            Map<Integer, Counts> totals
    ) {
        List<StatRow> rows = new ArrayList<>();

        for (ShareType share : ShareType.values()) {
            Counts counts = aggregate(
                    linksByCategory.get(categoryType),
                    dataHolder.get(share, targetId)
            );
            Counts total = totals.get(targetId);

            rows.add(new StatRow(
                    categoryType,
                    targetId,
                    share,
                    StatRowType.SUMMARY,
                    null,
                    null,
                    new StatisticMetric(counts.clicks(), calculateRate(counts.clicks(), total.clicks())),
                    new StatisticMetric(counts.clickers(), calculateRate(counts.clickers(), total.clickers()))
            ));
        }

        return rows;
    }

    private StatisticsDataHolder loadStatistics(int mailingId, int companyId, Set<Integer> targetIds) {
        StatisticsDataHolder dataHolder = new StatisticsDataHolder();

        for (Integer targetId : targetIds) {
            String targetSql = getTargetSql(companyId, targetId);

            for (ShareType share : ShareType.values()) {
                Map<Integer, LinkClickStatisticData> data = linkStatisticsDao.getClickStatisticsPerLink(
                        mailingId,
                        companyId,
                        targetSql,
                        Collections.emptySet(),
                        new DateRange(),
                        share.deviceClass,
                        share.customerId
                );

                dataHolder.put(share, targetId, data);
            }
        }

        return dataHolder;
    }

    private Map<Integer, Counts> calculateTotals(
            List<TrackableLink> links,
            StatisticsDataHolder dataHolder,
            Set<Integer> targetIds
    ) {
        Map<Integer, Counts> result = new HashMap<>();

        targetIds.forEach(targetId -> {
            Counts totalCounts = Stream.of(ShareType.values())
                    .map(shareType -> aggregate(links, dataHolder.get(shareType, targetId)))
                    .reduce(Counts.empty(), Counts::add);

            result.put(targetId, totalCounts);
        });

        return result;
    }

    private Counts aggregate(
            Collection<TrackableLink> links,
            Map<Integer, LinkClickStatisticData> data
    ) {
        Counts total = Counts.empty();

        for (TrackableLink link : links) {
            LinkClickStatisticData d = data.get(link.getId());
            if (d != null) {
                total = total.add(new Counts(d.clicksCount(), d.clickersCount()));
            }
        }
        return total;
    }

    private String getTargetSql(int companyId, Integer targetId) {
        return targetId != ALL_RECIPIENTS_TARGET_ID
                ? targetService.getTargetGroup(targetId, companyId).getTargetSQL()
                : "";
    }

    private Map<CategoryType, List<TrackableLink>> groupLinksByCategory(List<TrackableLink> links) {
        Map<CategoryType, List<TrackableLink>> result = links.stream()
                .collect(Collectors.groupingBy(t ->
                        t.isAdminLink() ? CategoryType.ADMINISTRATIVE : CategoryType.LEADING
                ));

        for (CategoryType category : CategoryType.values()) {
            result.putIfAbsent(category, Collections.emptyList());
        }

        return result;
    }

    private static Map<CategoryType, StatisticLinkCategory> buildCategories(Admin admin) {
        return Stream.of(CategoryType.values())
                .collect(Collectors.toMap(
                        Function.identity(),
                        ct -> new StatisticLinkCategory(getCategoryName(ct, admin), ct)
                ));
    }

    private static String getCategoryName(CategoryType type, Admin admin) {
        return I18nString.getLocaleString(type.getMessageKey(), admin.getLocale());
    }

    private Map<Integer, String> getTargetGroupNames(Set<Integer> targetGroups, Admin admin) {
        return targetGroups.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        id ->
                                id == ALL_RECIPIENTS_TARGET_ID
                                        ? getLocaleString("statistic.all_subscribers", admin.getLocale())
                                        : targetService.getTargetGroup(id, admin.getCompanyID()).getTargetName()
                ));
    }

    private SequencedSet<Integer> prepareTargetIds(Set<Integer> targetIds) {
        SequencedSet<Integer> ids = new LinkedHashSet<>();
        ids.add(ALL_RECIPIENTS_TARGET_ID);
        ids.addAll(requireNonNullElse(targetIds, Collections.emptySet()));

        return ids;
    }

}
