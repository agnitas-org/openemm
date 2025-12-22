/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.service.impl;

import static java.util.Collections.emptyList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.calendar.beans.CalendarAutoOptLabel;
import com.agnitas.emm.core.calendar.beans.CalendarComment;
import com.agnitas.emm.core.calendar.beans.CalendarCommentLabel;
import com.agnitas.emm.core.calendar.beans.CalendarMailingLabel;
import com.agnitas.emm.core.calendar.beans.MailingPopoverInfo;
import com.agnitas.emm.core.calendar.form.DashboardCalendarForm;
import com.agnitas.emm.core.calendar.service.CalendarCommentService;
import com.agnitas.emm.core.calendar.service.CalendarService;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailing.bean.MailingDto;
import com.agnitas.emm.core.mailing.dao.MailingDaoOptions;
import com.agnitas.mailing.autooptimization.beans.Optimization;
import com.agnitas.mailing.autooptimization.service.OptimizationService;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.util.DateUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

@Service("calendarService")
public class CalendarServiceImpl implements CalendarService {

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    protected final ExtendedConversionService conversionService;
    private final MailingDao mailingDao;
    private final OptimizationService optimizationService;
    private final CalendarCommentService calendarCommentService;

    public CalendarServiceImpl(ExtendedConversionService conversionService, MailingDao mailingDao,
                               OptimizationService optimizationService,
                               CalendarCommentService calendarCommentService) {
        this.conversionService = conversionService;
        this.mailingDao = mailingDao;
        this.optimizationService = optimizationService;
        this.calendarCommentService = calendarCommentService;
    }

    @Override
    public List<MailingDto> getUnsentUnplannedMailings(Admin admin) {
        return mailingDao.getUnsentMailings(admin, false);
    }

    @Override
    public List<MailingDto> getUnsentPlannedMailings(Admin admin) {
        return mailingDao.getUnsentMailings(admin, true);
    }

    @Override
    public List<MailingPopoverInfo> mailingsPopoverInfo(Set<Integer> mailingIds, Admin admin) {
        return mailingDao.getMailingsCalendarInfo(mailingIds, admin);
    }

    @Override
    public List<MailingDto> getMailings(MailingDaoOptions opts, Admin admin) {
        return ListUtils.union(
            mailingDao.getSentAndScheduled(opts, admin),
            mailingDao.getPlannedMailings(opts, admin));
    }

    @Override
    public boolean moveMailing(Admin admin, int mailingId, LocalDate date) {
        Mailing mailing = mailingDao.getMailing(mailingId, admin.getCompanyID());
        // Avoid schedule in the past.
        if (mailing.getId() == mailingId && !date.isBefore(LocalDate.now())) {
            MaildropEntry drop = getMaildropForUpdate(mailing.getMaildropStatus());

            if (setMailingDate(admin, mailing, drop, date)) {
                mailingDao.saveMailing(mailing, false);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean clearMailingPlannedDate(int mailingId, int companyId) {
        return canClearPlannedDate(mailingId, companyId) && mailingDao.clearPlanDate(mailingId, companyId);
    }

    @Override
    public Map<String, List<?>> getLabels(DashboardCalendarForm form, Admin admin) {
        Date start = form.getStartDate(admin.getZoneId(), DATE_FORMATTER);
        Date end = form.getEndDate(admin.getZoneId(), DATE_FORMATTER);
        return Map.of(
            "mailings", getMailingLabels(start, end, form.getDayMailingsLimit(), admin),
            "comments", form.isLoadComments() ? getCommentLabels(admin, start, end) : emptyList(),
            "optimizations", getAutoOptimizationLabels(admin, start, end));
    }

    @Override
    public List<CalendarMailingLabel> getMailingLabels(Date start, Date end, int limit, Admin admin) {
        MailingDaoOptions opts = MailingDaoOptions
            .builder()
            .setStartIncl(start)
            .setEndExcl(end)
            .limit(limit).build();
        return conversionService.convert(getMailings(opts, admin), MailingDto.class, CalendarMailingLabel.class);
    }

    @Override
    public List<CalendarCommentLabel> getCommentLabels(Admin admin, Date start, Date end) {
        return conversionService.convert(
            calendarCommentService.getComments(start, end, admin),
            CalendarComment.class, CalendarCommentLabel.class);
    }

    private List<CalendarAutoOptLabel> getAutoOptimizationLabels(Admin admin, Date start, Date end) {
        return conversionService.convert(
            optimizationService.getAutoOptimizations(admin, start, end),
            Optimization.class, CalendarAutoOptLabel.class);
    }

    private boolean canClearPlannedDate(int mailingId, int companyId) {
        if (mailingId <= 0) {
            return false;
        }

        Set<MaildropEntry> maildropStatuses = mailingDao.getMailing(mailingId, companyId).getMaildropStatus();
        if (CollectionUtils.isEmpty(maildropStatuses)) {
            return true;
        }

        if (maildropStatuses.size() > 1) {
            return false;
        }

        return maildropStatuses.stream()
                .map(MaildropEntry::getStatus)
                .allMatch(sc -> MaildropStatus.TEST.getCode() == sc || MaildropStatus.ADMIN.getCode() == sc);
    }

    private boolean setMailingDate(Admin admin, Mailing mailing, MaildropEntry drop, LocalDate date) {
        ZoneId zoneId = admin.getZoneId();
        boolean success = false;

        if (Objects.nonNull(drop)) {
            if (drop.getGenStatus() == MaildropGenerationStatus.SCHEDULED.getCode()) {
                // Merge new date with old time (simply change the date without changing time).
                LocalDateTime sendDate = DateUtilities.merge(date, DateUtilities.toLocalTime(drop.getSendDate(), zoneId));
                success = setSendDate(drop, DateUtilities.toDate(sendDate, zoneId));
            } else if (drop.getStatus() == MaildropStatus.ADMIN.getCode() || drop.getStatus() == MaildropStatus.TEST.getCode()) {
                mailing.setPlanDate(DateUtilities.toDate(date, zoneId));
                success = true;
            }
        } else {
            // Mailing has only plan date without any drop statuses.
            mailing.setPlanDate(DateUtilities.toDate(date, zoneId));
            success = true;
        }

        return success;
    }

    private boolean setSendDate(MaildropEntry drop, Date sendDate) {
        boolean result = false;

        if (DateUtilities.isFuture(sendDate)) {
            Date genDate = (drop.getStatus() == MaildropStatus.TEST.getCode()) ? sendDate : DateUtils.addHours(sendDate, -3);

            if (DateUtilities.isPast(genDate)) {
                genDate = new Date();
                drop.setGenStatus(MaildropGenerationStatus.NOW.getCode());
            }

            drop.setSendDate(sendDate);
            drop.setGenDate(genDate);

            result = true;
        }

        return result;
    }

    private MaildropEntry getMaildropForUpdate(Set<MaildropEntry> entries) {
        MaildropEntry drop = null;
        Date maxDate = null;

        for (MaildropEntry entry : entries) {
            if (maxDate == null || maxDate.before(entry.getGenChangeDate())) {
                maxDate = entry.getGenChangeDate();
                drop = entry;
            }
        }

        return drop;
    }
}
