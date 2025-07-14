/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.birtstatistics.enums.StatisticType;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingStatisticDto;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatisticForm;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.commons.dto.DateTimeRange;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowStatisticDto;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.mailing.autooptimization.service.OptimizationService;
import com.agnitas.messages.I18nString;
import com.agnitas.util.DateUtilities;
import org.springframework.web.context.request.RequestContextHolder;

public class WorkflowStatisticsService {

	private WorkflowService workflowService;
    private OptimizationService optimizationService;
	private BirtStatisticsService birtStatisticsService;
	private MailingService mailingService;

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
		return getMailingsMap(workflow, admin).keySet()
				.stream()
				.map(mId -> mailingService.getMailing(admin.getCompanyID(), mId))
				.toList();
	}

	private Map<Integer, String> getMailingsMap(Workflow workflow, Admin admin) {
        return workflow.getWorkflowIcons().stream()
				.filter(WorkflowUtils::isMailingIcon)
				.filter(icon -> mailingService.exists(WorkflowUtils.getMailingId(icon), admin.getCompanyID()))
				.collect(Collectors.toMap(WorkflowUtils::getMailingId, WorkflowIcon::getIconTitle));
	}

	// TODO: EMMGUI-714: Remove when removing old design
	public Map<String, String> getStatUrlsMap(int workflowId, Admin admin) {
		final int companyId = admin.getCompanyID();
		String sessionId = RequestContextHolder.getRequestAttributes().getSessionId();
		Map<String, String> urlsMap = new LinkedHashMap<>();

		Workflow workflow = workflowService.getWorkflow(workflowId, companyId);
		urlsMap.put(getWorkflowStatUrl(workflow, admin, sessionId, null), I18nString.getLocaleString("statistic.total", admin.getLocale()));

		if (isTotalStatisticAvailable(workflow)) {
			int mailingI = getFinalMailingID(workflow, companyId);
			urlsMap.put(getMailingStatUrl(mailingI, admin, sessionId), I18nString.getLocaleString("resultMailing", admin.getLocale()));
		}

		urlsMap.putAll(workflow.getWorkflowIcons().stream()
			.filter(WorkflowUtils::isMailingIcon)
			.filter(icon -> mailingService.exists(WorkflowUtils.getMailingId(icon), companyId))
			.collect(Collectors.toMap(
				icon -> getMailingStatUrl(WorkflowUtils.getMailingId(icon), admin, sessionId),
				WorkflowIcon::getIconTitle)));
		return urlsMap;
	}

	private String getMailingStatUrl(int mailingId, Admin admin, String sessionId) {
		MailingStatisticDto statDto = getMailingStatisticDto(mailingId, admin);
		return birtStatisticsService.getMailingStatisticUrl(admin, sessionId, statDto);
	}

	public String getWorkflowStatUrl(Workflow workflow, Admin admin, String sessionId, MailingStatisticForm form) {
		int companyId = admin.getCompanyID();
		if (!isTotalStatisticAvailable(workflow, companyId)) {
			if (!admin.isRedesignedUiUsed()) {
				WorkflowStatisticDto statistic = new WorkflowStatisticDto();

				statistic.setCompanyId(admin.getCompanyID());
				statistic.setFormat("html");
				statistic.setWorkflowId(workflow.getWorkflowId());
				statistic.setDateMode(DateMode.NONE);

				return birtStatisticsService.getWorkflowStatisticUrl(admin, statistic);
			}

			return getWorkflowStatUrl(workflow.getWorkflowId(), admin, form);
		}

		int finalMailingId = optimizationService.getFinalMailingId(companyId, workflow.getWorkflowId());

		MailingStatisticDto statDto = getMailingStatisticDto(finalMailingId, admin);
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

	private int getFinalMailingID(Workflow workflow, int companyId){
		if (isTotalStatisticAvailable(workflow)) {
			return optimizationService.getFinalMailingId(companyId, workflow.getWorkflowId());
		}

        return 0;
    }
    
	private boolean isTotalStatisticAvailable(Workflow workflow) {
		return workflow != null && isTotalStatisticAvailable(workflow.getStatus(), workflow.getWorkflowIcons());
    }

    private boolean isTotalStatisticAvailable(Workflow.WorkflowStatus status, List<WorkflowIcon> icons) {
        return status == Workflow.WorkflowStatus.STATUS_COMPLETE &&
                WorkflowUtils.isAutoOptWorkflow(icons);
    }

	private boolean isTotalStatisticAvailable(Workflow workflow, int companyId) {
		return workflow != null
			   && workflow.getStatus() == Workflow.WorkflowStatus.STATUS_COMPLETE
			   && WorkflowUtils.isAutoOptWorkflow(workflow.getWorkflowIcons())
			   && mailingService.exists(
			optimizationService.getFinalMailingId(companyId, workflow.getWorkflowId()),
			companyId);
	}

	public void setMailingService(MailingService mailingService) {
		this.mailingService = mailingService;
	}

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

    public void setOptimizationService(OptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

	public void setBirtStatisticsService(BirtStatisticsService birtStatisticsService) {
		this.birtStatisticsService = birtStatisticsService;
	}
}
