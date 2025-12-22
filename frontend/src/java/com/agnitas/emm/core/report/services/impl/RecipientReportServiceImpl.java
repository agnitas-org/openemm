/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.services.impl;

import static com.agnitas.messages.I18nString.getLocaleString;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.RecipientHistory;
import com.agnitas.beans.RecipientMailing;
import com.agnitas.beans.RecipientReaction;
import com.agnitas.beans.WebtrackingHistoryEntry;
import com.agnitas.dao.BindingEntryDao;
import com.agnitas.dao.RecipientDao;
import com.agnitas.emm.core.recipient.dao.RecipientProfileHistoryDao;
import com.agnitas.emm.core.recipientsreport.bean.SummedRecipientStatus;
import com.agnitas.emm.core.report.bean.CompositeBindingEntry;
import com.agnitas.emm.core.report.bean.CompositeBindingEntryHistory;
import com.agnitas.emm.core.report.bean.PlainBindingEntry;
import com.agnitas.emm.core.report.bean.PlainBindingEntryHistory;
import com.agnitas.emm.core.report.bean.RecipientBindingHistory;
import com.agnitas.emm.core.report.bean.RecipientEntity;
import com.agnitas.emm.core.report.builder.RecipientBindingHistoryBuilder;
import com.agnitas.emm.core.report.builder.impl.RecipientBindingHistoryBuilderImpl;
import com.agnitas.emm.core.report.converter.CollectionConverter;
import com.agnitas.emm.core.report.converter.impl.RecipientDeviceHistoryDtoConverter;
import com.agnitas.emm.core.report.converter.impl.RecipientEntityDtoConverter;
import com.agnitas.emm.core.report.converter.impl.RecipientMailingHistoryDtoConverter;
import com.agnitas.emm.core.report.converter.impl.RecipientRetargetingHistoryDtoConverter;
import com.agnitas.emm.core.report.converter.impl.RecipientStatusHistoryDtoConverter;
import com.agnitas.emm.core.report.dao.BindingEntryHistoryDao;
import com.agnitas.emm.core.report.dao.RecipientHistoryReportDao;
import com.agnitas.emm.core.report.director.RecipientBindingHistoryDirector;
import com.agnitas.emm.core.report.director.impl.RecipientBindingHistoryDirectorImpl;
import com.agnitas.emm.core.report.dto.RecipientEntityDto;
import com.agnitas.emm.core.report.dto.RecipientMailingLinkClicksHistoryDto;
import com.agnitas.emm.core.report.dto.RecipientMailingOpeningsHistoryDto;
import com.agnitas.emm.core.report.dto.RecipientSoftBounceHistoryDto;
import com.agnitas.emm.core.report.generator.TableGenerator;
import com.agnitas.emm.core.report.mapper.Mapper;
import com.agnitas.emm.core.report.printer.RecipientEntityDtoPrinter;
import com.agnitas.emm.core.report.services.RecipientReportService;
import com.agnitas.beans.Mailinglist;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.util.CsvWriter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class RecipientReportServiceImpl implements RecipientReportService {

    public static final String TXT_REPORT_FOOTER = "The data is stored on servers in Germany";

    protected final RecipientHistoryReportDao historyDao;
    protected final TableGenerator txtTableGenerator;
    private final BindingEntryDao bindingEntryDao;
    private final BindingEntryHistoryDao bindingEntryHistoryDao;
    private final RecipientProfileHistoryDao recipientProfileHistoryDao;
    private final RecipientDao recipientDao;
    private final ConfigService configService;
    private final CollectionConverter<RecipientBindingHistory, RecipientHistory> recipientHistoryConverter;
    private final Mapper<RecipientEntity> recipientEntityMapper;
    private final RecipientStatusHistoryDtoConverter recipientStatusHistoryConverter;
    private final RecipientMailingHistoryDtoConverter recipientMailingHistoryDtoConverter;
    private final RecipientRetargetingHistoryDtoConverter recipientRetargetingHistoryDtoConverter;
    private final RecipientDeviceHistoryDtoConverter recipientDeviceHistoryDtoConverter;
    private final RecipientEntityDtoConverter recipientEntityDtoConverter;
    private final RecipientEntityDtoPrinter recipientEntityDtoPrinter;

    public RecipientReportServiceImpl(BindingEntryDao bindingEntryDao, BindingEntryHistoryDao bindingEntryHistoryDao,
                                      RecipientProfileHistoryDao recipientProfileHistoryDao, RecipientDao recipientDao,
                                      ConfigService configService, CollectionConverter<RecipientBindingHistory, RecipientHistory> recipientHistoryConverter,
                                      Mapper<RecipientEntity> recipientEntityMapper, RecipientHistoryReportDao historyDao,
                                      @Qualifier("txtTableGenerator") TableGenerator txtTableGenerator, RecipientStatusHistoryDtoConverter recipientStatusHistoryConverter,
                                      RecipientMailingHistoryDtoConverter recipientMailingHistoryDtoConverter,
                                      RecipientRetargetingHistoryDtoConverter recipientRetargetingHistoryDtoConverter, RecipientDeviceHistoryDtoConverter recipientDeviceHistoryDtoConverter,
                                      RecipientEntityDtoConverter recipientEntityDtoConverter, RecipientEntityDtoPrinter recipientEntityDtoPrinter) {
        this.bindingEntryDao = bindingEntryDao;
        this.bindingEntryHistoryDao = bindingEntryHistoryDao;
        this.recipientProfileHistoryDao = recipientProfileHistoryDao;
        this.recipientDao = recipientDao;
        this.configService = configService;
        this.recipientHistoryConverter = recipientHistoryConverter;
        this.recipientEntityMapper = recipientEntityMapper;
        this.historyDao = historyDao;
        this.txtTableGenerator = txtTableGenerator;
        this.recipientStatusHistoryConverter = recipientStatusHistoryConverter;
        this.recipientMailingHistoryDtoConverter = recipientMailingHistoryDtoConverter;
        this.recipientRetargetingHistoryDtoConverter = recipientRetargetingHistoryDtoConverter;
        this.recipientDeviceHistoryDtoConverter = recipientDeviceHistoryDtoConverter;
        this.recipientEntityDtoConverter = recipientEntityDtoConverter;
        this.recipientEntityDtoPrinter = recipientEntityDtoPrinter;
    }

    @Override
    public String getRecipientTxtReport(int recipientId, int companyId, Locale locale) {
        return getRecipientInfoTxt(recipientId, companyId, locale) +
                getRecipientReportTxtTables(recipientId, companyId, locale) +
                "\n" + TXT_REPORT_FOOTER;
    }

    protected String getRecipientReportTxtTables(int recipientId, int companyId, Locale locale) {
        return getStatusHistoryTxtTable(recipientId, companyId, locale) +
                getMailingHistoryTxtTable(recipientId, companyId, locale) +
                getTrackingHistoryTxtTable(recipientId, companyId, locale) +
                getDeviceHistoryTxtTable(recipientId, companyId, locale) +
                getSoftBouncesHistoryTxtTable(recipientId, companyId, locale) +
                getIpAddressesOfClicksHistoryTxtTable(recipientId, companyId, locale) +
                getMailingOpeningsTxtTable(recipientId, companyId, locale) +
                getMailingClicksTxtTable(recipientId, companyId, locale) +
                getUserFormClicksTxtTable(recipientId, companyId, locale) +
                getWorkflowReactionsHistoryTxtTable(recipientId, companyId, locale);
    }

    private String getIpAddressesOfClicksHistoryTxtTable(int recipientId, int companyId, Locale locale) {
        return txtTableGenerator.generate(historyDao.getIpAddressesOfClicksHistory(recipientId, companyId), locale);
    }

    private String getUserFormClicksTxtTable(int recipientId, int companyId, Locale locale) {
        return txtTableGenerator.generate(historyDao.getUserFormClicksHistory(recipientId, companyId), locale);
    }

    private String getMailingClicksTxtTable(int recipientId, int companyId, Locale locale) {
        List<RecipientMailingLinkClicksHistoryDto> clicks = historyDao.getMailingClicksHistory(recipientId, companyId);
        return txtTableGenerator.generate(clicks, locale);
    }

    private String getMailingOpeningsTxtTable(int recipientId, int companyId, Locale locale) {
        List<RecipientMailingOpeningsHistoryDto> openings = historyDao.getOpeningsHistory(recipientId, companyId);
        return txtTableGenerator.generate(openings, locale);
    }

    private String getSoftBouncesHistoryTxtTable(int recipientId, int companyId, Locale locale) {
        List<RecipientSoftBounceHistoryDto> softBouncesDto = historyDao.getSoftBouncesHistory(recipientId, companyId);
        return txtTableGenerator.generate(softBouncesDto, locale);
    }

    private String getDeviceHistoryTxtTable(int recipientId, int companyId, Locale locale) {
        List<RecipientReaction> deviceHistory = getDeviceHistory(recipientId, companyId);
        return txtTableGenerator.generate(recipientDeviceHistoryDtoConverter.convert(deviceHistory, locale), locale);
    }

    private String getTrackingHistoryTxtTable(int recipientId, int companyId, Locale locale) {
        List<WebtrackingHistoryEntry> trackingHistory = getRetargetingHistory(recipientId, companyId);
        return txtTableGenerator.generate(recipientRetargetingHistoryDtoConverter.convert(trackingHistory), locale);
    }

    private String getMailingHistoryTxtTable(int recipientId, int companyId, Locale locale) {
        List<RecipientMailing> mailingHistory = getMailingHistory(recipientId, companyId);
        return txtTableGenerator.generate(recipientMailingHistoryDtoConverter.convert(mailingHistory, locale), locale);
    }

    private String getStatusHistoryTxtTable(int recipientId, int companyId, Locale locale) {
        List<RecipientHistory> statusHistory = getStatusHistory(recipientId, companyId);
        return txtTableGenerator.generate(recipientStatusHistoryConverter.convert(statusHistory, locale), locale);
    }

    private String getRecipientInfoTxt(int recipientId, int companyId, Locale locale) {
        RecipientEntity recipientEntity = getRecipientInfo(recipientId, companyId);
        RecipientEntityDto recipientEntityDto = recipientEntityDtoConverter.convert(recipientEntity, locale);
        return recipientEntityDtoPrinter.print(recipientEntityDto, locale);
    }

    private String getWorkflowReactionsHistoryTxtTable(int recipientId, int companyId, Locale locale) {
        return txtTableGenerator
                .generate(historyDao.getWorkflowReactionsHistory(recipientId, companyId), locale);
    }

    @Override
    public List<RecipientBindingHistory> getBindingHistory(int recipientId, int companyId) {
        List<RecipientBindingHistory> recipientHistories = new LinkedList<>();

        // contains ids of removed mailing lists.
        // need for prevent re-adding information about removed mailing list.
        Set<Integer> removedMailings = new HashSet<>();

        // list contains all Bindings between recipient and mailing list including mailing list info
        List<CompositeBindingEntry> compositeBindings = bindingEntryDao.getCompositeBindings(companyId, recipientId);

        // adding to general list
        recipientHistories.addAll(getRecipientHistoryFromEntries(compositeBindings, companyId, recipientId, removedMailings));

        // list contains last history entry for each already removed Binding
        List<CompositeBindingEntryHistory> nonexistentBindings =
                bindingEntryHistoryDao.getHistoryOfNonexistentBindings(companyId, recipientId);

        // caste type of container
        List<CompositeBindingEntry> nonexistentBindingsConverted = nonexistentBindings.stream()
                .map(binding -> (CompositeBindingEntry) binding)
                .collect(Collectors.toList());

        // adding to general list
        recipientHistories.addAll(getRecipientHistoryFromEntries(nonexistentBindingsConverted, companyId, recipientId, removedMailings));

        return recipientHistories;
    }

    @Override
    public List<RecipientHistory> getProfileHistory(int recipientId, int companyId) {
        if (configService.isRecipientProfileHistoryEnabled(companyId)) {
            return recipientProfileHistoryDao.listProfileFieldHistory(recipientId, companyId);
        }
        return Collections.emptyList();
    }

    @Override
    public List<RecipientHistory> getStatusHistory(int recipientId, int companyId) {
        List<RecipientBindingHistory> recipientBindingHistory = getBindingHistory(recipientId, companyId);
        List<RecipientHistory> convertedBindingHistory = recipientHistoryConverter.convert(recipientBindingHistory);
        List<RecipientHistory> recipientProfileHistory = getProfileHistory(recipientId, companyId);
        List<RecipientHistory> unitedList = ListUtils.union(convertedBindingHistory, recipientProfileHistory);
        unitedList.sort(Comparator.comparing(RecipientHistory::getChangeDate,
                Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
        return unitedList;
    }

    @Override
    public List<RecipientMailing> getMailingHistory(int recipientId, int companyId) {
        return recipientDao.getMailingsDeliveredToRecipient(recipientId, companyId);
    }

    @Override
    public List<WebtrackingHistoryEntry> getRetargetingHistory(int recipientId, int companyId) {
        List<WebtrackingHistoryEntry> recipientWebtrackingHistory = recipientDao.getRecipientWebtrackingHistory(companyId, recipientId);
        recipientWebtrackingHistory.sort(Comparator.comparing(WebtrackingHistoryEntry::getDate,
                Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
        return recipientWebtrackingHistory;
    }

    @Override
    public List<RecipientReaction> getDeviceHistory(int recipientId, int companyId) {
        return recipientDao.getRecipientReactionsHistory(recipientId, companyId);
    }

    @Override
    public RecipientEntity getRecipientInfo(int recipientId, int companyId){
        return recipientEntityMapper.map(recipientDao.getCustomerDataFromDb(companyId, recipientId));
    }

    private List<RecipientBindingHistory> getRecipientHistoryFromEntries(List<CompositeBindingEntry> compositeBindings,
                                                                         int companyId, int recipientId,
                                                                         Set<Integer> removedMailings) {

        List<RecipientBindingHistory> recipientHistories = new LinkedList<>();

        for (CompositeBindingEntry bindingEntry : compositeBindings) {
            // getting history for current binding
            List<PlainBindingEntryHistory> bindingEntryHistory = bindingEntryHistoryDao.getHistory(companyId,
                    recipientId, bindingEntry.getMailingListId(), bindingEntry.getMediaType());

            if (CollectionUtils.isNotEmpty(bindingEntryHistory)) {
                recipientHistories.addAll(getRecipientBindingHistory(bindingEntry, bindingEntryHistory));
            } else {
                recipientHistories.addAll(getRecipientBindingHistory(bindingEntry));
            }

            // checking if mailinglist removed
            Mailinglist mailinglist = bindingEntry.getMailingList();
            RecipientBindingHistoryBuilder historyBuilder = new RecipientBindingHistoryBuilderImpl(bindingEntry, mailinglist);
            RecipientBindingHistoryDirector historyDirector = new RecipientBindingHistoryDirectorImpl(historyBuilder);
            if ((Objects.isNull(mailinglist) || mailinglist.isRemoved())
                    && !removedMailings.contains(bindingEntry.getMailingListId())) {

                recipientHistories.add(historyDirector.constructDeletedMailinglist(bindingEntry.getCreationDate()).build());

                removedMailings.add(bindingEntry.getMailingListId());
            }
        }

        return recipientHistories;
    }

    /**
     * Creates recipient binding history based just on {@code BindingEntry}.
     * Means that this recipient first time in current mailing list.
     *
     * @param compositeBindingEntry contains recipient's entry into mailing list.
     * @return history of current recipient's changes in mailing list.
     */
    private List<RecipientBindingHistory> getRecipientBindingHistory(CompositeBindingEntry compositeBindingEntry) {
        List<RecipientBindingHistory> recipientHistories = new LinkedList<>();

        Date changeDate = compositeBindingEntry.getCreationDate();
        Mailinglist mailingList = compositeBindingEntry.getMailingList();
        recipientHistories.addAll(createHistoryEntries(compositeBindingEntry, mailingList, changeDate));

        return recipientHistories;
    }

    /**
     * Creates recipient binding history based on {@code BindingEntry} and history of its changes.
     * Means that this recipient was modified in current mailing list.
     *
     * @param compositeBindingEntry contains recipient's entry({@code BindingEntry}) into mailing list.
     * @param bindingEntryHistory   contains history of changes for current {@code BindingEntry}.
     * @return history of current recipient's changes in mailing list.
     */
    private List<RecipientBindingHistory> getRecipientBindingHistory(CompositeBindingEntry compositeBindingEntry,
                                                                     List<PlainBindingEntryHistory> bindingEntryHistory) {

        Mailinglist mailingList = compositeBindingEntry.getMailingList();
        List<RecipientBindingHistory> recipientBindingHistories = new LinkedList<>();

        Iterator<PlainBindingEntryHistory> iterator = bindingEntryHistory.iterator();

        PlainBindingEntryHistory previousState = iterator.next();
        while (iterator.hasNext()) {
            PlainBindingEntryHistory currentState = iterator.next();
            recipientBindingHistories.addAll(createHistoryEntries(previousState, currentState, mailingList,
                    previousState.getTimestampChange()));
            previousState = currentState;
        }

        recipientBindingHistories.addAll(createHistoryEntries(previousState, compositeBindingEntry, mailingList,
                previousState.getTimestampChange()));

        return recipientBindingHistories;
    }

    /**
     * Creates history entries based just on {@code BindingEntry}.
     * Uses along with {@link #getRecipientBindingHistory(CompositeBindingEntry)}
     *
     * @param currentState contains recipient's entry({@code BindingEntry}) into mailing list.
     * @param mailingList  contains mailing list information.
     * @param changeTime   date of creating {@code BindingEntry}.
     * @return history of recipient's changed fields in mailing list.
     */
    private List<RecipientBindingHistory> createHistoryEntries(PlainBindingEntry currentState, Mailinglist mailingList,
                                                               Date changeTime) {

        List<RecipientBindingHistory> bindingHistory = new LinkedList<>();

        RecipientBindingHistoryBuilder historyBuilder = new RecipientBindingHistoryBuilderImpl(currentState, mailingList);
        RecipientBindingHistoryDirector historyDirector = new RecipientBindingHistoryDirectorImpl(historyBuilder);

        // getting type changes
        bindingHistory.add(historyDirector.constructChangedType(changeTime, null, currentState.getUserType()).build());

        // getting status changes
        bindingHistory.add(historyDirector.constructChangedStatus(changeTime, null, currentState.getUserStatus()).build());

        // getting remark changes
        bindingHistory.add(historyDirector.constructChangedRemark(changeTime, null, currentState.getUserRemark()).build());

        return bindingHistory;

    }

    /**
     * Creates history entries based {@code BindingEntry} and its change history.
     * Uses along with {@link #getRecipientBindingHistory(CompositeBindingEntry, List)}
     *
     * @param previousState previous record from {@code BindingEntry} history.
     * @param currentState  current record from {@code BindingEntryHistory}.
     * @param mailingList   contains mailing list information.
     * @param changeTime    date of change {@code BindingEntry}.
     * @return history of recipient's changed fields in mailing list.
     */
    private List<RecipientBindingHistory> createHistoryEntries(PlainBindingEntry previousState, PlainBindingEntry currentState,
                                                               Mailinglist mailingList, Date changeTime) {

        List<RecipientBindingHistory> recipientBindingHistories = new LinkedList<>();
        RecipientBindingHistoryBuilder historyBuilder = new RecipientBindingHistoryBuilderImpl(currentState, mailingList);
        RecipientBindingHistoryDirector historyDirector = new RecipientBindingHistoryDirectorImpl(historyBuilder);

        // checking type for changes
        if (!Objects.equals(previousState.getUserType(), currentState.getUserType())) {
            recipientBindingHistories.add(historyDirector.constructChangedType(changeTime,
                    previousState.getUserType(), currentState.getUserType()).build());
        }

        // checking status for changes
        if (!Objects.equals(previousState.getUserStatus(), currentState.getUserStatus())) {
            recipientBindingHistories.add(historyDirector.constructChangedStatus(changeTime,
                    previousState.getUserStatus(), currentState.getUserStatus()).build());
        }

        // check remark for changes
        if (!Objects.equals(previousState.getUserRemark(), currentState.getUserRemark())) {
            recipientBindingHistories.add(historyDirector.constructChangedRemark(changeTime,
                    previousState.getUserRemark(), currentState.getUserRemark()).build());
        }

        // check exit mailing for changes
        if (!Objects.equals(previousState.getExitMailingId(), currentState.getExitMailingId())) {
            recipientBindingHistories.add(historyDirector.constructExitMailingId(changeTime,
                    previousState.getMailingListId(), currentState.getMailingListId()).build());
        }

        // checking if binding removed
        if (currentState instanceof CompositeBindingEntryHistory) {
            recipientBindingHistories.add(historyDirector.constructBindingDeleted(changeTime).build());
        }

        return recipientBindingHistories;
    }

    @Override
    public Map<String, Integer> getRecipientStatusStat(int mailinglistId, int targetId, int companyId) {
        return bindingEntryDao.getRecipientStatusStat(mailinglistId, targetId, companyId);
    }

    @Override
    public JSONArray getFilteredRecipientStatusesJson(Map<String, Integer> statuses, boolean summed) {
        JSONArray jsonArray = new JSONArray();
        statuses.entrySet().stream()
                .filter(statusEntry -> summed == SummedRecipientStatus.getNames().contains(statusEntry.getKey()))
                .map(statusEntry -> getRecipientStatusJson(statusEntry.getKey(), statusEntry.getValue()))
                .forEach(jsonArray::put);
        return jsonArray;
    }

    private JSONObject getRecipientStatusJson(String status, int value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", status);
        jsonObject.put("value", value);
        return jsonObject;
    }

    @Override
    public byte[] getRecipientStatusesCSV(Admin admin, int mailingListId, int targetId) throws Exception {
        return CsvWriter.csv(ListUtils.union(
            List.of(getRecipientStatusesHeaderForCsv(admin.getLocale())),
            getRecipientStatusesRowsForCsv(mailingListId, targetId, admin.getCompanyID())));
    }

    private List<String> getRecipientStatusesHeaderForCsv(Locale locale) {
        return List.of(getLocaleString("recipient.Remark", locale), getLocaleString("Value", locale));
    }

    private List<List<String>> getRecipientStatusesRowsForCsv(int mailingListId, int targetId, int companyId) {
        return getRecipientStatusStat(mailingListId, targetId, companyId).entrySet().stream()
            .map(status -> List.of(status.getKey(), String.valueOf(status.getValue()))).toList();
    }
}
