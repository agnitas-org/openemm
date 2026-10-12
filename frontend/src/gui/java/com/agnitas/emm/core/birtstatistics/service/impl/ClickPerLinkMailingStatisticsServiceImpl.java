/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.service.impl;

import static com.agnitas.messages.I18nString.getLocaleString;
import static com.agnitas.util.AgnUtils.calculateRate;
import static java.util.Objects.requireNonNullElse;
import static org.apache.tomcat.jakartaee.commons.lang3.StringUtils.defaultIfBlank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.beans.Admin;
import com.agnitas.beans.TrackableLink;
import com.agnitas.emm.core.birtstatistics.dto.ClickStatisticsPerLinkResponse;
import com.agnitas.emm.core.birtstatistics.dto.ClickStatisticsPerLinkResponse.LinkCategoryBlock;
import com.agnitas.emm.core.birtstatistics.dto.ClickStatisticsPerLinkResponse.LinkStats;
import com.agnitas.emm.core.birtstatistics.dto.ClickStatisticsPerLinkResponse.ShareStats;
import com.agnitas.emm.core.birtstatistics.dto.ClickStatisticsPerLinkResponse.SummaryBlock;
import com.agnitas.emm.core.birtstatistics.dto.ClickStatisticsPerLinkResponse.SummaryCategoryBlock;
import com.agnitas.emm.core.birtstatistics.dto.ClickStatisticsPerLinkResponse.TargetGroupStats;
import com.agnitas.emm.core.birtstatistics.dto.StatisticMetric;
import com.agnitas.emm.core.birtstatistics.enums.StatisticLinkCategory;
import com.agnitas.emm.core.birtstatistics.enums.StatisticLinkCategory.CategoryType;
import com.agnitas.emm.core.birtstatistics.service.ClickPerLinkMailingStatisticsService;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkClickStatsService;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkClickStatsService.ClickCounts;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkClickStatsService.ShareType;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkClickStatsService.TargetClickStats;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkService;
import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.util.CsvWriter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class ClickPerLinkMailingStatisticsServiceImpl implements ClickPerLinkMailingStatisticsService {

    private static final Comparator<StatRow> CSV_ROW_COMPARATOR =
            Comparator.comparing(StatRow::categoryType)
                    .thenComparing(StatRow::targetGroupId)
                    .thenComparing(StatRow::type, Comparator.reverseOrder())
                    .thenComparing(r -> r.clicks().value(), Comparator.reverseOrder())
                    .thenComparing(r -> r.url() == null ? "" : r.url());

    private final TrackableLinkService trackableLinkService;
    private final TrackableLinkClickStatsService trackableLinkClickStatsService;
    private final MailingStatisticsService mailingStatisticsService;

    public ClickPerLinkMailingStatisticsServiceImpl(
            TrackableLinkService trackableLinkService,
            TrackableLinkClickStatsService trackableLinkClickStatsService,
            MailingStatisticsService mailingStatisticsService
    ) {
        this.trackableLinkService = trackableLinkService;
        this.trackableLinkClickStatsService = trackableLinkClickStatsService;
        this.mailingStatisticsService = mailingStatisticsService;
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
        List<LightTarget> targets = getTargets(targetGroups, admin);
        List<StatRow> rows = collectStatistics(mailingId, targets, admin);

        return UiResponseBuilder.build(rows, targets, admin);
    }

    private static class UiResponseBuilder {
        static ClickStatisticsPerLinkResponse build(List<StatRow> rows, List<LightTarget> targets, Admin admin) {
            Map<StatRowType, List<StatRow>> rowsByType = rows.stream()
                    .collect(Collectors.groupingBy(StatRow::type));

            Map<CategoryType, StatisticLinkCategory> categoriesMap = buildCategories(admin);

            return new ClickStatisticsPerLinkResponse(
                    buildSummary(rowsByType.get(StatRowType.SUMMARY), categoriesMap, targets),
                    buildLinkBlocks(rowsByType.get(StatRowType.URL), categoriesMap, targets)
            );
        }

        private static SummaryBlock buildSummary(
                List<StatRow> rows, Map<CategoryType, StatisticLinkCategory> categoriesMap, List<LightTarget> targets
        ) {
            Map<CategoryType, List<StatRow>> rowsByCategory = rows.stream()
                    .collect(Collectors.groupingBy(StatRow::categoryType));

            List<SummaryCategoryBlock> blocks = Stream.of(CategoryType.values())
                    .map(t ->
                            new SummaryCategoryBlock(
                                    categoriesMap.get(t),
                                    buildTargetStats(rowsByCategory.get(t), targets)
                            )
                    )
                    .toList();

            return new SummaryBlock(blocks);
        }

        private static List<LinkCategoryBlock> buildLinkBlocks(
                List<StatRow> rows, Map<CategoryType, StatisticLinkCategory> categoriesMap, List<LightTarget> targets
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
                                    getLinkStats(rowsByCategory.get(t), targets)
                            )
                    ).toList();
        }

        private static List<LinkStats> getLinkStats(List<StatRow> rows, List<LightTarget> targets) {
            if (CollectionUtils.isEmpty(rows)) {
                return Collections.emptyList();
            }

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
                                    buildTargetStats(e.getValue(), targets)
                            )
                    )
                    .sorted(Comparator.<LinkStats>comparingLong(
                            stat -> stat.targetGroups().getFirst().total().clicks().value()).reversed()
                    )
                    .toList();
        }

        private static List<TargetGroupStats> buildTargetStats(List<StatRow> rows, List<LightTarget> targets) {
            return targets.stream()
                    .map(t ->
                            new TargetGroupStats(
                                    t.getId() == MailingStatisticsService.ALL_SUBSCRIBERS_TARGETGROUP_ID ? null : t.getId(),
                                    toShareStats(rows, ShareType.TOTAL, t.getId()),
                                    toShareStats(rows, ShareType.MOBILE, t.getId()),
                                    toShareStats(rows, ShareType.ANONYMOUS, t.getId())
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
        List<LightTarget> targets = getTargets(targetGroups, admin);
        List<StatRow> rows = collectStatistics(mailingId, targets, admin);

        Map<CategoryType, StatisticLinkCategory> categoriesMap = buildCategories(admin);
        Map<Integer, String> targetNames = targets.stream()
                .collect(Collectors.toMap(LightTarget::getId, LightTarget::getName));

        List<CsvRow> csvRows = rows.stream()
                .sorted(CSV_ROW_COMPARATOR)
                .map(r -> toCsvRow(r, r.categoryType(), admin, categoriesMap, targetNames))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        this::addEmptyRowsAfterCategory
                ));

        return CsvWriter.csv(
                getHeaderForCsv(admin.getLocale()),
                getRowsForCsv(csvRows)
        );
    }

    private List<LightTarget> getTargets(Set<Integer> targetGroups, Admin admin) {
        return mailingStatisticsService.getTargetsWithAllSubscribersTarget(
                requireNonNullElse(targetGroups, Collections.emptySet()), admin
        );
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

    private List<StatRow> collectStatistics(int mailingId, List<LightTarget> targets, Admin admin) {
        List<TrackableLink> links = trackableLinkService.getTrackableLinks(admin.getCompanyID(), mailingId, false);
        Map<CategoryType, List<TrackableLink>> linksByCategory = groupLinksByCategory(links);
        List<Integer> allLinkIds = links.stream().map(TrackableLink::getId).toList();

        Map<Integer, TargetClickStats> statsByTarget = trackableLinkClickStatsService.getClickCountsByTarget(
                mailingId, admin.getCompanyID(), targets, new DateRange()
        );

        List<StatRow> rows = new ArrayList<>();

        for (LightTarget target : targets) {
            TargetClickStats stats = statsByTarget.get(target.getId());

            Map<ShareType, ClickCounts> totals = new EnumMap<>(ShareType.class);
            for (ShareType share : ShareType.values()) {
                totals.put(share, stats.aggregate(share, allLinkIds));
            }

            for (CategoryType categoryType : CategoryType.values()) {
                rows.addAll(collectUrlRows(linksByCategory.get(categoryType), stats, categoryType, target.getId(), totals));
                rows.addAll(collectSummaryRows(linksByCategory.get(categoryType), stats, categoryType, target.getId(), totals));
            }
        }

        return rows;
    }

    private List<StatRow> collectUrlRows(
            List<TrackableLink> links,
            TargetClickStats stats,
            CategoryType categoryType,
            int targetId,
            Map<ShareType, ClickCounts> totals
    ) {
        List<StatRow> rows = new ArrayList<>();

        for (TrackableLink link : links) {
            for (ShareType share : ShareType.values()) {
                ClickCounts counts = stats.forLink(share, link.getId());
                ClickCounts total = totals.get(share);

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
            List<TrackableLink> links,
            TargetClickStats stats,
            CategoryType categoryType,
            int targetId,
            Map<ShareType, ClickCounts> totals
    ) {
        List<StatRow> rows = new ArrayList<>();
        List<Integer> linkIds = links.stream().map(TrackableLink::getId).toList();

        for (ShareType share : ShareType.values()) {
            ClickCounts counts = stats.aggregate(share, linkIds);
            ClickCounts total = totals.get(share);

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

}
