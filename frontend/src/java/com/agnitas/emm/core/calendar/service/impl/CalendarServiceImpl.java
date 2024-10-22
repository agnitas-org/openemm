/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.calendar.beans.CalendarUnsentMailing;
import com.agnitas.emm.core.calendar.beans.MailingPopoverInfo;
import com.agnitas.emm.core.calendar.service.CalendarService;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.messages.I18nString;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Required;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

public class CalendarServiceImpl implements CalendarService {
    private MailingDao mailingDao;

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String TIME_FORMAT = "HH:mm";
    private static final String LINE_SEPARATOR = "\u2028";

    @Required
    public void setMailingDao(MailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    protected MailingDao getMailingDao() {
        return mailingDao;
    }

    @Override
    public List<CalendarUnsentMailing> getUnplannedMailings(Admin admin) {
        return mailingDao.getNotSentMailings(admin, false);
    }

    @Override
    public List<CalendarUnsentMailing> getPlannedUnsentMailings(Admin admin) {
        return mailingDao.getNotSentMailings(admin, true);
    }

    @Override
    public PaginatedListImpl<Map<String, Object>> getUnsentMailings(Admin admin, int listSize) {
        return mailingDao.getUnsentMailings(admin, listSize);
    }

    @Override
    public PaginatedListImpl<Map<String, Object>> getPlannedMailings(Admin admin, int listSize) {
        return mailingDao.getPlannedMailings(admin, listSize);
    }
    
    @Override
    public List<MailingPopoverInfo> mailingsPopoverInfo(Set<Integer> mailingIds, Admin admin) {
        return mailingDao.getMailingsCalendarInfo(mailingIds, admin);
    }

    @Override
    public JSONArray getMailings(Admin admin, LocalDate startDate, LocalDate endDate, int limit) {
        List<Map<String, Object>> mailings = new ArrayList<>();
        int companyId = admin.getCompanyID();
        ZoneId zoneId = AgnUtils.getZoneId(admin);

        Date start = DateUtilities.toDate(startDate.atStartOfDay(), zoneId);
        Date end = DateUtilities.toDate(endDate.plusDays(1).atStartOfDay(), zoneId);

        mailings.addAll(getSentAndScheduledMailings(admin, start, end, limit));
        mailings.addAll(getPlannedMailings(admin, start, end, limit));

        List<Integer> sentMailings = getSentMailings(mailings);

        Map<Integer, Integer> openers = Collections.emptyMap();
        Map<Integer, Integer> clickers = Collections.emptyMap();
        if (!sentMailings.isEmpty()) {
            openers = mailingDao.getOpeners(companyId, sentMailings);
            clickers = mailingDao.getClickers(companyId, sentMailings);
        }

        setMailingData(mailings);

        return mailingsAsJson(mailings, openers, clickers, admin);
    }

    @Override
    public JSONArray getMailingsLight(Admin admin, LocalDate startDate, LocalDate endDate) {
        ZoneId zoneId = AgnUtils.getZoneId(admin);
        Date start = DateUtilities.toDate(startDate.atStartOfDay(), zoneId);
        Date end = DateUtilities.toDate(endDate.plusDays(1), zoneId);

        List<Map<String, Object>> mailings = ListUtils.union(
                mailingDao.getSentAndScheduledLight(admin, start, end),
                mailingDao.getPlannedMailingsLight(admin, start, end));
        return mailingsAsJsonRedesigned(mailings, admin);
    }

    private JSONArray mailingsAsJsonRedesigned(List<Map<String, Object>> mailings, Admin admin) {
        JSONArray json = new JSONArray();
        TimeZone timeZone = AgnUtils.getTimeZone(admin);
        DateFormat dateFormat = DateUtilities.getFormat(DATE_FORMAT, timeZone);
        DateFormat timeFormat = DateUtilities.getFormat(TIME_FORMAT, timeZone);
        mailings.forEach(mailing -> json.add(mailingToJson(mailing, dateFormat, timeFormat)));
        return json;
    }

    private JSONObject mailingToJson(Map<String, Object> mailing, DateFormat dateFormat, DateFormat timeFormat) {
        JSONObject object = new JSONObject();
        Date sendDate = (Date) mailing.get("senddate");
        object.element("shortname", getShortname(mailing));
        object.element("mailingId", mailing.get("mailingid"));
        object.element("workstatus", mailing.get("workstatus"));
        object.element("sendDate", dateFormat.format(sendDate));
        object.element("sendTime", timeFormat.format(sendDate));
        object.element("mailinglistName", mailing.get("mailinglist_name"));
        return object;
    }

    protected List<Map<String, Object>> getPlannedMailings(Admin admin, Date startDate, Date endDate, int limit) {
        List<Map<String, Object>> plannedMailings = mailingDao.getPlannedMailings(admin, startDate, endDate, limit);
        return addSomeFieldsToPlannedMailings(plannedMailings, AgnUtils.getZoneId(admin));
    }

    protected List<Map<String, Object>> addSomeFieldsToPlannedMailings(final List<Map<String, Object>> mailings, final ZoneId zoneId) {
        final Date midnight = DateUtilities.midnight(zoneId);
        for (Map<String, Object> plannedMailing : mailings) {
            Date sendDate = (Date) plannedMailing.get("senddate");

            plannedMailing.put("planned", true);
            plannedMailing.put("plannedInPast", sendDate.before(midnight));
        }
        return mailings;
    }

    protected List<Map<String, Object>> getSentAndScheduledMailings(final Admin admin, Date startDate, Date endDate, int limit) {
        List<Map<String, Object>> mailings = mailingDao.getSentAndScheduled(admin, startDate, endDate, limit);
        return addSomeFieldsToSentAndScheduledMailings(mailings);
    }

    protected List<Map<String, Object>> addSomeFieldsToSentAndScheduledMailings(final List<Map<String, Object>> mailings) {
        for (Map<String, Object> mailing : mailings) {
            mailing.put("planned", false);
            mailing.put("plannedInPast", false);
        }

        return mailings;
    }

    private List<Integer> getSentMailings(List<Map<String, Object>> mailings) {
        List<Integer> sentMailings = new ArrayList<>();

        for (Map<String, Object> mailing : mailings) {
            if (getIntValue(mailing, "genstatus") > MaildropGenerationStatus.SCHEDULED.getCode()) {
                sentMailings.add(getIntValue(mailing, "mailingId"));
            }
        }

        return sentMailings;
    }

    private void setMailingData(List<Map<String, Object>> mailings) {
        for (Map<String, Object> mailing : mailings) {

            String subject = (String) mailing.get("subject");
            if (Objects.nonNull(subject)) {
                subject = subject
                        .replace("'", "&#39;")
                        .replace("\n", "<br />")
                        .replace(LINE_SEPARATOR, "");
                mailing.put("subject", subject);
            }
        }
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
        ZoneId zoneId = AgnUtils.getZoneId(admin);
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
        } else if (Objects.nonNull(mailing.getPlanDate()) || admin.isRedesignedUiUsed()) {
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

    private int getIntValue(Map<String, Object> map, String key) {
        Number num = (Number) map.get(key);

        return num.intValue();
    }

    private JSONArray mailingsAsJson(List<Map<String, Object>> mailings, Map<Integer, Integer> openers, Map<Integer, Integer> clickers, Admin admin) {
        TimeZone timezone = AgnUtils.getTimeZone(admin);
        Locale locale = admin.getLocale();

        DateFormat dateFormat = DateUtilities.getFormat(DATE_FORMAT, timezone);
        DateFormat timeFormat = DateUtilities.getFormat(TIME_FORMAT, timezone);

        JSONArray result = new JSONArray();

        for (Map<String, Object> mailing : mailings) {
            JSONObject object = new JSONObject();

            Date sendDate = (Date) mailing.get("senddate");
            int mailingId = getIntValue(mailing, "mailingId");
            boolean isSent = getIntValue(mailing, "genstatus") > MaildropGenerationStatus.SCHEDULED.getCode();
            boolean isOnlyPostType = getIntValue(mailing, "isOnlyPostType") > 0;

            //hardcode because dao returns keys in different case (depends on db)
            object.element("shortname", getShortname(mailing));
            object.element("mailingId", mailing.get("mailingid"));
            object.element("workstatus", mailing.get("workstatus"));
            object.element("workstatusIn", I18nString.getLocaleString((String) mailing.get("workstatus"), locale));
            object.element("preview_component", mailing.get("preview_component"));
            object.element("mailsSent", mailing.get("mailssent"));
            if (admin.isRedesignedUiUsed()) {
                Object mediatype = mailing.get("mediatype");
                if (mediatype != null) {
                    object.element("mediatype", MediaTypes.getMediaTypeForCode(((Number)mediatype).intValue()));
                }
            }
            object.element("subject", mailing.get("subject"));
            object.element("planned", mailing.get("planned"));
            object.element("plannedInPast", mailing.get("plannedInPast"));
            object.element("sendDate", DateUtilities.format(sendDate, dateFormat));
            object.element("sendTime", DateUtilities.format(sendDate, timeFormat));
            object.element("sent", isSent);
            object.element("isOnlyPostType", isOnlyPostType);
            object.element("openers", openers.getOrDefault(mailingId, 0));
            object.element("clickers", clickers.getOrDefault(mailingId, 0));

            result.add(object);
        }

        return result;
    }

    private String getShortname(Map<String, Object> mailing) {
        String shortname = (String) mailing.get("shortname");

        if (StringUtils.isNotEmpty(shortname)) {
            return shortname.replace("'", "&#39;")
                    .replace(LINE_SEPARATOR, "");
        }

        return StringUtils.EMPTY;
    }
}
