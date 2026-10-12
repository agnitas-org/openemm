/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.workflow.service;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.birtstatistics.dto.StatisticMetric;
import com.agnitas.emm.core.birtstatistics.enums.StatisticType;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingStatisticDto;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatisticForm;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.birtstatistics.service.MailingRawMetrics;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService.ByDeviceClass;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.commons.dto.DateTimeRange;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowStatisticDto;
import com.agnitas.emm.core.workflow.dto.WorkflowStatisticsResponse;
import com.agnitas.emm.core.workflow.dto.WorkflowStatisticsResponse.DeviceType;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.mailing.autooptimization.beans.Optimization;
import com.agnitas.mailing.autooptimization.service.OptimizationService;
import com.agnitas.messages.I18nString;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
public class WorkflowStatisticsService {

    private final WorkflowService workflowService;
    private final OptimizationService optimizationService;
    private final BirtStatisticsService birtStatisticsService;
    private final MailingStatisticsService mailingStatisticsService;
    private final MailingService mailingService;
    private final TargetService targetService;

    public WorkflowStatisticsService(
            WorkflowService workflowService,
            OptimizationService optimizationService,
            BirtStatisticsService birtStatisticsService,
            MailingStatisticsService mailingStatisticsService,
            MailingService mailingService,
            TargetService targetService
    ) {
        this.workflowService = workflowService;
        this.optimizationService = optimizationService;
        this.birtStatisticsService = birtStatisticsService;
        this.mailingStatisticsService = mailingStatisticsService;
        this.mailingService = mailingService;
        this.targetService = targetService;
    }

    public WorkflowStatisticsResponse getStatistics(int workflowId, DateRange dateRange, Set<Integer> targetGroups, Admin admin) {
        int companyId = admin.getCompanyID();
        Workflow workflow = workflowService.getWorkflow(workflowId, companyId);
        boolean isTotalOptimization = isTotalStatisticAvailable(workflow, companyId);
        String targetSql = targetService.getTargetSqlCombinedWithOr(targetGroups, companyId);

        List<OptimizationEntity> entities = isTotalOptimization
                ? getOptimizationEntities(workflowId, companyId, admin.getLocale())
                : getMailings(workflowId, admin).stream()
                .map(m -> new OptimizationEntity(m, m.getShortname(), false))
                .toList();

        List<MailingRawMetrics> rawMetrics = entities.stream()
                .map(e -> computeRawMetrics(e.mailing().getId(), companyId, dateRange, targetSql))
                .toList();

        List<Long> avgMailingSizes = entities.stream()
                .map(e -> mailingStatisticsService.getAvgMailingSizeKb(e.mailing().getId(), companyId))
                .toList();

        List<ExtraStats> extraStats = IntStream.range(0, entities.size())
                .mapToObj(i -> isTotalOptimization
                        ? getExtraStats(entities.get(i).mailing().getId(), companyId, dateRange, rawMetrics.get(i), targetSql)
                        : null)
                .toList();

        List<WorkflowStatisticsResponse.MailingData> mailings = IntStream.range(0, entities.size())
                .mapToObj(i -> new WorkflowStatisticsResponse.MailingData(
                        entities.get(i).mailing().getId(),
                        entities.get(i).label(),
                        toMetrics(rawMetrics.get(i), avgMailingSizes.get(i), extraStats.get(i))
                ))
                .toList();

        WorkflowStatisticsResponse.Metrics total = rawMetrics.isEmpty()
                ? null
                : toMetrics(sum(rawMetrics), average(avgMailingSizes), sumExtraStats(extraStats));

        Map<DeviceType, StatisticMetric> openersByDevice = null;
        Map<DeviceType, StatisticMetric> clickersByDevice = null;
        WorkflowStatisticsResponse.OptimizationInfo optimizationInfo = null;

        if (isTotalOptimization) {
            int finalMailingId = optimizationService.getFinalMailingId(companyId, workflowId);
            int finalIdx = indexOfMailing(entities, finalMailingId);
            if (finalIdx >= 0) {
                ExtraStats finalExtra = extraStats.get(finalIdx);
                openersByDevice = mapDeviceStatsForUi(finalExtra.openersByDevice(), rawMetrics.get(finalIdx).measuredOpeners());
                clickersByDevice = mapDeviceStatsForUi(finalExtra.clickersByDevice(), rawMetrics.get(finalIdx).clickers());
            }
            optimizationInfo = buildOptimizationInfo(entities, finalMailingId, admin);
        }

        return new WorkflowStatisticsResponse(total, mailings, openersByDevice, clickersByDevice, optimizationInfo);
    }

    private int indexOfMailing(List<OptimizationEntity> entities, int mailingId) {
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i).mailing().getId() == mailingId) {
                return i;
            }
        }
        return -1;
    }

    private WorkflowStatisticsResponse.OptimizationInfo buildOptimizationInfo(
            List<OptimizationEntity> entities,
            int finalMailingId,
            Admin admin
    ) {
        if (entities.isEmpty()) {
            return null;
        }

        List<WorkflowStatisticsResponse.OptimizationInfo.GroupInfo> groups = entities.stream().map(entity -> {
                    Mailing mailing = entity.mailing();
                    return new WorkflowStatisticsResponse.OptimizationInfo.GroupInfo(
                            mailing.getId(),
                            entity.label(),
                            getSubject(mailing),
                            mailing.getSenddate() == null ? "-" : admin.getDateFormat().format(mailing.getSenddate()),
                            mailing.getId() == finalMailingId,
                            entity.winner()
                    );
                })
                .toList();

        return new WorkflowStatisticsResponse.OptimizationInfo(groups);
    }

    private String getSubject(Mailing mailing) {
        return Optional.ofNullable(mailing.getEmailParam())
                .map(MediatypeEmail::getSubject)
                .orElse("");
    }

    private record ExtraStats(ByDeviceClass openersByDevice, ByDeviceClass clickersByDevice, long openingsGross, long clicksGross) {
    }

    private ExtraStats getExtraStats(int mailingId, int companyId, DateRange dateRange, MailingRawMetrics raw, String targetSql) {
        Map<DeviceClass, Integer> openersByDeviceClass = mailingStatisticsService.getOpenersByDevice(mailingId, companyId, targetSql, dateRange);
        Map<DeviceClass, Integer> clickersByDeviceClass = mailingStatisticsService.getClickersByDevice(mailingId, companyId, targetSql, dateRange);

        return new ExtraStats(
                mailingStatisticsService.toByDeviceClass(openersByDeviceClass, raw.measuredOpeners()),
                mailingStatisticsService.toByDeviceClass(clickersByDeviceClass, raw.clickers()),
                mailingStatisticsService.getOpeningsCount(mailingId, companyId, targetSql, dateRange),
                mailingStatisticsService.getClicksCount(mailingId, companyId, targetSql, dateRange)
        );
    }

    private ExtraStats sumExtraStats(List<ExtraStats> stats) {
        List<ExtraStats> present = stats.stream().filter(Objects::nonNull).toList();
        if (present.isEmpty()) {
            return null;
        }

        return new ExtraStats(
                sumByDevice(present, ExtraStats::openersByDevice),
                sumByDevice(present, ExtraStats::clickersByDevice),
                present.stream().mapToLong(ExtraStats::openingsGross).sum(),
                present.stream().mapToLong(ExtraStats::clicksGross).sum()
        );
    }

    private ByDeviceClass sumByDevice(List<ExtraStats> stats, Function<ExtraStats, ByDeviceClass> extractor) {
        return new ByDeviceClass(
                stats.stream().map(extractor).mapToLong(ByDeviceClass::pc).sum(),
                stats.stream().map(extractor).mapToLong(ByDeviceClass::tablet).sum(),
                stats.stream().map(extractor).mapToLong(ByDeviceClass::mobile).sum(),
                stats.stream().map(extractor).mapToLong(ByDeviceClass::smartTv).sum(),
                stats.stream().map(extractor).mapToLong(ByDeviceClass::multi).sum()
        );
    }

    private Map<DeviceType, StatisticMetric> mapDeviceStatsForUi(ByDeviceClass byDevice, long total) {
        Map<DeviceType, StatisticMetric> values = new LinkedHashMap<>();
        for (DeviceType type : DeviceType.values()) {
            StatisticMetric val = switch (type) {
                case TOTAL -> StatisticMetric.of(total, total);
                case PC -> StatisticMetric.of(byDevice.pc(), total);
                case TABLET -> StatisticMetric.of(byDevice.tablet(), total);
                case MOBILE -> StatisticMetric.of(byDevice.mobile(), total);
                case SMART_TV -> StatisticMetric.of(byDevice.smartTv(), total);
                case MULTIPLE -> StatisticMetric.of(byDevice.multi(), total);
            };
            values.put(type, val);
        }
        return values;
    }

    private record OptimizationEntity(Mailing mailing, String label, boolean winner) {
    }

    private List<OptimizationEntity> getOptimizationEntities(int workflowId, int companyId, Locale locale) {
        int finalMailingId = optimizationService.getFinalMailingId(companyId, workflowId);
        int optimizationId = optimizationService.getOptimizationIdByFinalMailing(finalMailingId, companyId);
        if (optimizationId <= 0) {
            return Collections.emptyList();
        }

        Optimization optimization = optimizationService.getOptimization(optimizationId, companyId, true);

        List<OptimizationEntity> entities = new ArrayList<>();
        for (int mailingId : optimization.getTestmailingIDs()) {
            if (mailingService.exists(mailingId, companyId)) {
                Mailing mailing = mailingService.getMailing(companyId, mailingId);
                entities.add(new OptimizationEntity(mailing, mailing.getShortname(), mailingId == optimization.getResultMailingID()));
            }
        }

        if (mailingService.exists(finalMailingId, companyId)) {
            Mailing mailing = mailingService.getMailing(companyId, finalMailingId);
            entities.add(new OptimizationEntity(mailing, I18nString.getLocaleString("resultMailing", locale), false));
        }

        return entities;
    }

    private long average(List<Long> values) {
		if (CollectionUtils.isEmpty(values)) {
			return 0;
		}

		long sum = values.stream().mapToLong(Long::longValue).sum();
	    return sum / values.size();
    }

    private MailingRawMetrics computeRawMetrics(int mailingId, int companyId, DateRange dateRange, String targetSql) {
        return mailingStatisticsService.getRawMetrics(mailingId, companyId, MaildropStatus.realSendStatuses(), targetSql, dateRange);
    }

    private WorkflowStatisticsResponse.Metrics toMetrics(MailingRawMetrics raw, long avgMailingSize, ExtraStats extra) {
        return new WorkflowStatisticsResponse.Metrics(
                StatisticMetric.of(raw.totalOpeners(), raw.sentEmails()),
                StatisticMetric.of(raw.measuredOpeners(), raw.sentEmails()),
                StatisticMetric.of(raw.invisibleOpeners(), raw.sentEmails()),
                raw.anonymousOpenersCount(),
                new StatisticMetric(raw.sentEmails(), raw.sentEmails() > 0 ? 100 : 0),
                raw.acceptedEmails() == null ? null : StatisticMetric.of(raw.acceptedEmails(), raw.sentEmails()),
                StatisticMetric.of(raw.optOuts(), raw.sentEmails()),
                StatisticMetric.of(raw.hardBounces(), raw.sentEmails()),
                StatisticMetric.of(raw.clickers(), raw.sentEmails()),
                raw.anonymousClickersCount(),
                raw.softBounces() == null ? null : StatisticMetric.of(raw.softBounces(), raw.sentEmails()),
                raw.revenue(),
                avgMailingSize,
                extra == null ? null : toDeviceTypeMetrics(extra.openersByDevice(), raw.measuredOpeners()),
                extra == null ? null : toDeviceTypeMetrics(extra.clickersByDevice(), raw.clickers()),
                extra == null ? null : extra.openingsGross(),
                extra == null ? null : extra.clicksGross(),
                extra == null ? null : AgnUtils.calculateRate(extra.clicksGross(), extra.openingsGross())
        );
    }

    private Map<DeviceType, StatisticMetric> toDeviceTypeMetrics(ByDeviceClass byDevice, long total) {
        return Map.of(
                DeviceType.PC, StatisticMetric.of(byDevice.pc(), total),
                DeviceType.TABLET, StatisticMetric.of(byDevice.tablet(), total),
                DeviceType.MOBILE, StatisticMetric.of(byDevice.mobile(), total),
                DeviceType.SMART_TV, StatisticMetric.of(byDevice.smartTv(), total),
                DeviceType.MULTIPLE, StatisticMetric.of(byDevice.multi(), total)
        );
    }

    private MailingRawMetrics sum(List<MailingRawMetrics> counts) {
        return new MailingRawMetrics(
                counts.stream().mapToLong(MailingRawMetrics::totalOpeners).sum(),
                counts.stream().mapToLong(MailingRawMetrics::measuredOpeners).sum(),
                counts.stream().mapToLong(MailingRawMetrics::invisibleOpeners).sum(),
                counts.stream().mapToLong(MailingRawMetrics::anonymousOpenersCount).sum(),
                counts.stream().mapToLong(MailingRawMetrics::sentEmails).sum(),
                sumNullable(counts, MailingRawMetrics::acceptedEmails, Long::sum),
                counts.stream().mapToLong(MailingRawMetrics::optOuts).sum(),
                counts.stream().mapToLong(MailingRawMetrics::hardBounces).sum(),
                counts.stream().mapToLong(MailingRawMetrics::clickers).sum(),
                counts.stream().mapToLong(MailingRawMetrics::anonymousClickersCount).sum(),
                sumNullable(counts, MailingRawMetrics::softBounces, Long::sum),
                sumNullable(counts, MailingRawMetrics::revenue, Double::sum)
        );
    }

    private <R> R sumNullable(
			List<MailingRawMetrics> counts,
			Function<MailingRawMetrics, R> extractor,
			BinaryOperator<R> reducer
	) {
        List<R> values = counts.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .toList();

		if (values.isEmpty()) {
			return null;
		}

        return values.stream().reduce(reducer).orElse(null);
    }

    public DateRange getSelectedDateRange(Workflow workflow, MailingStatisticForm form, Admin admin) {
        int companyId = admin.getCompanyID();
        form.setStatisticType(null);

        if (isTotalStatisticAvailable(workflow, companyId)) {
            form.setDateMode(DateMode.NONE);
            return new DateRange();
        }

        LocalDateTime minSendDate = Optional.ofNullable(findMinSendDate(workflow.getWorkflowId(), admin))
                .map(d -> DateUtilities.toLocalDateTime(d, admin.getZoneId()))
                .orElse(null);

        DateTimeRange dateRestrictions = getDateTimeRestrictions(form, minSendDate, admin.getDateFormatter());
        form.getStartDate().set(dateRestrictions.getFrom(), admin.getDateFormatter());
        form.getEndDate().set(dateRestrictions.getTo(), admin.getDateFormatter());

        return toDateRange(dateRestrictions, admin.getZoneId());
    }

    private DateRange toDateRange(DateTimeRange dateTimeRange, ZoneId zoneId) {
        ZonedDateTime fromZoned = Optional.ofNullable(dateTimeRange.getFrom())
                .map(ldt -> ldt.atZone(zoneId))
                .orElse(null);
        ZonedDateTime toZoned = Optional.ofNullable(dateTimeRange.getTo())
                .map(ldt -> ldt.atZone(zoneId))
                .orElse(null);

        return new DateRange(fromZoned, toZoned);
    }

	public int getStartYear(int workflowId, Admin admin) {
		Date mailingStartDate = findMinSendDate(workflowId, admin);

		if (mailingStartDate != null) {
			return DateUtilities.getYear(mailingStartDate);
		}
		return Year.now().getValue(); // fallback is current year
	}

	private Date findMinSendDate(int workflowId, Admin admin) {
        List<Date> sendDates = getMailings(workflowId, admin)
				.stream()
				.flatMap(m -> Stream.of(m.getSenddate(), mailingService.getMailingLastSendDate(m.getId())))
				.filter(Objects::nonNull)
				.toList();

		return sendDates.isEmpty() ? null : Collections.min(sendDates);
	}

	public Map<Integer, String> getStatMailings(int workflowId, Admin admin) {
		final int companyId = admin.getCompanyID();
		Map<Integer, String> statMailings = new LinkedHashMap<>();

		Workflow workflow = workflowService.getWorkflow(workflowId, companyId);
		statMailings.put(0, I18nString.getLocaleString("statistic.total", admin.getLocale()));

		if (isTotalStatisticAvailable(workflow, companyId)) {
			int mailingId = optimizationService.getFinalMailingId(companyId, workflow.getWorkflowId());
			statMailings.put(mailingId, I18nString.getLocaleString("resultMailing", admin.getLocale()));
		}
		statMailings.putAll(getMailingsMap(workflow, admin));
		return statMailings;
	}

	public List<Mailing> getMailings(int workflowId, Admin admin) {
		Workflow workflow = workflowService.getWorkflow(workflowId, admin.getCompanyID());
		return getMailingsMap(workflow, admin)
                .keySet()
				.stream()
				.map(mId -> mailingService.getMailing(admin.getCompanyID(), mId))
				.toList();
	}

	private Map<Integer, String> getMailingsMap(Workflow workflow, Admin admin) {
        return workflow.getWorkflowIcons()
				.stream()
				.filter(WorkflowUtils::isMailingIcon)
				.filter(icon -> mailingService.exists(WorkflowUtils.getMailingId(icon), admin.getCompanyID()))
				.collect(Collectors.toMap(WorkflowUtils::getMailingId, WorkflowIcon::getIconTitle, (a, b) -> b));
	}

    // TODO: GWUA-7000: Remove when removing 'workflow.rptdesign'
	public String getWorkflowStatUrl(Workflow workflow, Admin admin, String sessionId, MailingStatisticForm form) {
		int companyId = admin.getCompanyID();
		form.setStatisticType(null);
		if (!isTotalStatisticAvailable(workflow, companyId)) {
			return getWorkflowStatUrl(workflow.getWorkflowId(), admin, form);
		}

		int finalMailingId = optimizationService.getFinalMailingId(companyId, workflow.getWorkflowId());
		MailingStatisticDto statDto = getMailingStatisticDto(finalMailingId, admin);
        statDto.setSelectedTargets(form.getSelectedTargets());
		statDto.setType(StatisticType.SUMMARY_AUTO_OPT);
		statDto.setOptimizationId(optimizationService.getOptimizationIdByFinalMailing(finalMailingId, companyId));
		return birtStatisticsService.getMailingStatisticUrl(admin, sessionId, statDto);
	}

	private MailingStatisticDto getMailingStatisticDto(int mailingId, Admin admin) {
		MailingStatisticDto statDto = new MailingStatisticDto();
		Mailing mailing = mailingService.getMailing(admin.getCompanyID(), mailingId);
		statDto.setMailingId(mailingId);
		statDto.setDateMode(DateMode.NONE);
		statDto.setType(StatisticType.SUMMARY);
		statDto.setShortname(mailing.getShortname());
		statDto.setDescription(mailing.getDescription());
		return statDto;
	}

	private String getWorkflowStatUrl(int workflowId, Admin admin, MailingStatisticForm form) {
		LocalDateTime minSendDate = Optional.ofNullable(findMinSendDate(workflowId, admin))
				.map(d -> DateUtilities.toLocalDateTime(d, admin.getZoneId()))
				.orElse(null);

		DateTimeRange dateRestrictions = getDateTimeRestrictions(form, minSendDate, admin.getDateFormatter());
		form.getStartDate().set(dateRestrictions.getFrom(), admin.getDateFormatter());
		form.getEndDate().set(dateRestrictions.getTo(), admin.getDateFormatter());

		WorkflowStatisticDto statistic = new WorkflowStatisticDto();

		statistic.setCompanyId(admin.getCompanyID());
		statistic.setFormat("html");
		statistic.setWorkflowId(workflowId);
		statistic.setDateMode(form.getDateMode());
		statistic.setDateRange(dateRestrictions);

		return birtStatisticsService.getWorkflowStatisticUrl(admin, statistic);
	}

	private DateTimeRange getDateTimeRestrictions(MailingStatisticForm form, LocalDateTime mailingStart, DateTimeFormatter dateFormatter) {
		LocalDateTime startDate = form.getStartDate().get(dateFormatter);
		LocalDateTime endDate = form.getEndDate().get(dateFormatter);

		return birtStatisticsService.getDateTimeRestrictions(
				new DateTimeRange(startDate, endDate),
				form.getDateMode(),
				mailingStart,
				form.getYear(),
				form.getMonthValue()
		);
	}

	public boolean isTotalStatisticAvailable(Workflow workflow, int companyId) {
		return workflow != null
			   && workflow.getStatus() == Workflow.WorkflowStatus.STATUS_COMPLETE
			   && WorkflowUtils.isAutoOptWorkflow(workflow.getWorkflowIcons())
			   && mailingService.exists(
				   optimizationService.getFinalMailingId(companyId, workflow.getWorkflowId()),
			       companyId);
    }
}
