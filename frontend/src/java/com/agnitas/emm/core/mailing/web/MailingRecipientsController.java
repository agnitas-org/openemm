/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.web;

import static com.agnitas.web.mvc.Pollable.LONG_TIMEOUT;
import static org.agnitas.emm.core.recipient.RecipientUtils.formatRecipientDateTimeValue;
import static org.agnitas.emm.core.recipient.RecipientUtils.formatRecipientDateValue;
import static org.agnitas.emm.core.recipient.RecipientUtils.formatRecipientDoubleValue;
import static com.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.service.GenericExportWorker;
import com.agnitas.service.MailingRecipientExportWorker;
import com.agnitas.service.MailingRecipientExportWorkerFactory;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.FileUtils;
import com.agnitas.emm.core.mailing.enums.MailingRecipientsAdditionalColumn;
import com.agnitas.web.forms.FormUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PollingUid;
import com.agnitas.beans.ProfileField;
import com.agnitas.emm.core.components.service.MailingRecipientsService;
import com.agnitas.emm.core.mailing.bean.MailingRecipientStatRow;
import com.agnitas.emm.core.mailing.forms.MailingRecipientsFormSearchParams;
import com.agnitas.emm.core.mailing.forms.MailingRecipientsOverviewFilter;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.messages.I18nString;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.WebStorage;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;

import jakarta.servlet.http.HttpSession;

public class MailingRecipientsController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(MailingRecipientsController.class);

    private static final String MAILING_RECIPIENT_TMP_DIRECTORY = AgnUtils.getTempDir() + File.separator + "MailingRecipientsExport";

    private final UserActivityLogService userActivityLogService;
    protected final MailingBaseService mailingBaseService;
    private final ColumnInfoService columnInfoService;
    private final GridServiceWrapper gridService;
    private final WebStorage webStorage;
    private final MailingRecipientsService mailingRecipientsService;
    private final MailingRecipientExportWorkerFactory exportWorkerFactory;

    public MailingRecipientsController(UserActivityLogService userActivityLogService, WebStorage webStorage,
                                       ColumnInfoService columnInfoService, MailingBaseService mailingBaseService,
                                       GridServiceWrapper gridService, MailingRecipientsService mailingRecipientsService,
                                       MailingRecipientExportWorkerFactory exportWorkerFactory) {
        this.webStorage = webStorage;
        this.gridService = gridService;
        this.columnInfoService = columnInfoService;
        this.mailingBaseService = mailingBaseService;
        this.userActivityLogService = userActivityLogService;
        this.mailingRecipientsService = mailingRecipientsService;
        this.exportWorkerFactory = exportWorkerFactory;
    }

    @ModelAttribute
    public MailingRecipientsFormSearchParams getSearchParams() {
        return new MailingRecipientsFormSearchParams();
    }

    @RequestMapping("/{mailingId:\\d+}/recipients/list.action")
    public Pollable<ModelAndView> list(@PathVariable int mailingId, @ModelAttribute("form") MailingRecipientsOverviewFilter filter,
                                       @RequestParam(required = false) boolean restoreSort,
                                       @ModelAttribute MailingRecipientsFormSearchParams searchParams,
                                       Admin admin, Model model, HttpSession session) {
        FormUtils.updateSortingState(webStorage, WebStorage.MAILING_RECIPIENT_OVERVIEW, filter, restoreSort);
        if (isRedesignedUiUsed(admin)) {
            FormUtils.syncSearchParams(searchParams, filter, true);
        }
        prepareListParameters(filter, admin);
        Map<String, ProfileField> profileFields = columnInfoService.getColumnInfoMap(admin.getCompanyID());
        AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
        setupListPageAttributes(model, mailingId, admin, profileFields);

        return new Pollable<>(getPollingUid(filter, session), LONG_TIMEOUT,
                new ModelAndView(redirectToOverview(mailingId), model.asMap()),
                getListWorker(mailingId, filter, profileFields, admin, model));
    }

    private static boolean isRedesignedUiUsed(Admin admin) {
        return admin.isRedesignedUiUsed();
    }

    private String redirectToOverview(int mailingId) {
        return String.format("redirect:/mailing/%d/recipients/list.action", mailingId);
    }

    private Callable<ModelAndView> getListWorker(int mailingId, MailingRecipientsOverviewFilter filter, Map<String, ProfileField> profileFields, Admin admin, Model model) {
        return () -> {
            if (filter.isLoadRecipients()) {
                PaginatedListImpl<MailingRecipientStatRow> recipients = getRecipientsList(mailingId, filter, profileFields, admin);
                recipients.setSortCriterion(filter.getSort());

                int maxRecipients = AgnUtils.getCompanyMaxRecipients(admin);
                if (recipients.getFullListSize() > maxRecipients) {
                    model.addAttribute("deactivatePagination", true);
                    model.addAttribute("countOfRecipients", maxRecipients);
                } else {
                    model.addAttribute("deactivatePagination", false);
                }

                model.addAttribute("recipients", recipients);
                userActivityLogService.writeUserActivityLog(admin, "list recipients", "active tab - recipients", logger);
            }
            return new ModelAndView("mailing_recipients_list", model.asMap());
        };
    }

    private PaginatedListImpl<MailingRecipientStatRow> getRecipientsList(int mailingId, MailingRecipientsOverviewFilter filter,
                                                                         Map<String, ProfileField> profileFields, Admin admin) {
        PaginatedListImpl<MailingRecipientStatRow> recipients;

        if (isRedesignedUiUsed(admin)) {
            recipients = mailingRecipientsService.getMailingRecipients(filter, mailingId, profileFields, admin);
        } else {
            final Set<String> additionalFields = new HashSet<>(filter.getSelectedFields());
            additionalFields.removeAll(MailingRecipientsAdditionalColumn.getColumns());

            recipients = mailingBaseService.getMailingRecipients(mailingId,
                    admin.getCompanyID(), filter.getRecipientsFilter(), filter.getPage(), filter.getNumberOfRows(), filter.getSort(),
                    AgnUtils.sortingDirectionToBoolean(filter.getOrder()), new ArrayList<>(additionalFields));

            for (MailingRecipientStatRow recipient : recipients.getList()) {
                for (String col : additionalFields) {
                    recipient.setVal(col, getFieldFormattedValue(recipient.getVal(col), profileFields.get(col), admin));
                }
            }
        }
        return recipients;
    }

    private PollingUid getPollingUid(MailingRecipientsOverviewFilter filter, HttpSession session) {
        return PollingUid.builder(session.getId(), "recipients")
                .arguments(filter.toArray())
                .build();
    }

    private void prepareListParameters(MailingRecipientsOverviewFilter filter, Admin admin) {
        webStorage.access(WebStorage.MAILING_RECIPIENT_OVERVIEW, storage -> {
            if (filter.getNumberOfRows() > 0) {
                storage.setRowsCount(filter.getNumberOfRows());
                if (isRedesignedUiUsed(admin)) {
                    if (!filter.isInEditColumnsMode()) {
                        filter.setSelectedFields(storage.getSelectedFields());
                    }
                } else {
                    storage.setSelectedFields(filter.getSelectedFields());
                }
            } else {
                filter.setNumberOfRows(storage.getRowsCount());
                filter.setSelectedFields(storage.getSelectedFields());
            }
        });
    }

    private Map<String, String> getFieldsMap(Map<String, ProfileField> profileFields, Admin admin) {
        Map<String, String> fields = profileFields.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getShortname()));

        if (isRedesignedUiUsed(admin)) {
            fields.remove("customer_id");
            fields.remove("email");
            Stream.of(MailingRecipientsAdditionalColumn.values())
                    .forEach(c -> fields.put(c.name(), I18nString.getLocaleString(c.getMessageKey(), admin.getLocale())));
        }

        return fields;
    }

    // TODO: EMMGUI-714: remove when old design will be removed (the same method in com/agnitas/emm/core/components/service/impl/MailingRecipientsServiceImpl.java)
    @Deprecated
    private String getFieldFormattedValue(Object val, ProfileField profileField, Admin admin) {
        if (profileField == null) {
            return "";
        }

        switch (profileField.getSimpleDataType()) {
            case Float:
                return formatRecipientDoubleValue(admin, ((Number) val).doubleValue());
            case Date:
                return formatRecipientDateValue(admin, (Date) val);
            case DateTime:
                return formatRecipientDateTimeValue(admin, (Date) val);
            default:
                return Objects.isNull(val) ? "" : val.toString();
        }
    }

    private void setupListPageAttributes(Model model, int mailingId, Admin admin, Map<String, ProfileField> profileFields) {
        int companyId = admin.getCompanyID();

        model.addAttribute("fieldsMap", getFieldsMap(profileFields, admin));
        model.addAttribute("shortname", mailingBaseService.getMailingName(mailingId, companyId));
        model.addAttribute("workflowId", mailingBaseService.getWorkflowId(mailingId, companyId));
        model.addAttribute("isPostMailing", isPostMailing(mailingId, companyId));
        model.addAttribute("gridTemplateId", gridService.getGridTemplateIdByMailingId(mailingId));
        model.addAttribute("isMailingUndoAvailable", mailingBaseService.checkUndoAvailable(mailingId));

        if (isRedesignedUiUsed(admin)) {
            model.addAttribute("additionalColumns", MailingRecipientsAdditionalColumn.getColumns());
        }
    }

    protected boolean isPostMailing(int mailingId, int companyId) {
        return false;
    }

    @PostMapping("/recipients/setSelectedFields.action")
    public @ResponseBody BooleanResponseDto updateSelectedFields(@RequestParam(required = false) List<String> selectedFields, Popups popups) {
        webStorage.access(WebStorage.MAILING_RECIPIENT_OVERVIEW, storage -> storage.setSelectedFields(selectedFields));
        popups.success(CHANGES_SAVED_MSG);

        return new BooleanResponseDto(popups, !popups.hasAlertPopups());
    }

    @GetMapping("/{id:\\d+}/recipients/search.action")
    public String search(@PathVariable int id, @ModelAttribute MailingRecipientsOverviewFilter filter,
                         @ModelAttribute MailingRecipientsFormSearchParams searchParams, RedirectAttributes ra) {
        FormUtils.syncSearchParams(searchParams, filter, false);
        filter.setLoadRecipients(true);
        ra.addFlashAttribute("form", filter);
        return redirectToOverview(id) + "?restoreSort=true";
    }

    @GetMapping("/{mailingId:\\d+}/recipients/export.action")
    public ResponseEntity<Resource> export(@PathVariable int mailingId, MailingRecipientsOverviewFilter filter,
                                           @ModelAttribute MailingRecipientsFormSearchParams searchParams, Admin admin) throws Exception {
        if (isRedesignedUiUsed(admin)) {
            FormUtils.syncSearchParams(searchParams, filter, true);
            webStorage.access(WebStorage.MAILING_RECIPIENT_OVERVIEW,
                    storage -> filter.setSelectedFields(storage.getSelectedFields()));
        }
        final int companyId = admin.getCompanyID();

        if (mailingBaseService.isMailingExists(mailingId, companyId, false)) {
            File exportTmpFile = new File(MAILING_RECIPIENT_TMP_DIRECTORY + File.separator + getExportTmpFileName(mailingId, companyId));
            writeCsvToTmpFile(exportTmpFile, mailingId, filter, admin);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + getResultFileName(mailingId, admin))
                    .contentLength((int) exportTmpFile.length())
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(new DeleteFileAfterSuccessReadResource(exportTmpFile));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private void writeCsvToTmpFile(File exportTmpFile, int mailingId, MailingRecipientsOverviewFilter filter, Admin admin) throws Exception {
        GenericExportWorker exportWorker;
        if (isRedesignedUiUsed(admin)) {
            exportWorker = exportWorkerFactory.newWorker(filter, mailingId, admin.getCompanyID(), admin.getLocale());
        } else {
            exportWorker = new MailingRecipientExportWorker(admin.getCompanyID(), mailingId, filter.getRecipientsFilter(),
                    new ArrayList<>(filter.getSelectedFields()), filter.getSort(),
                    AgnUtils.sortingDirectionToBoolean(filter.getOrder()), admin.getLocale());
        }

        exportWorker.setExportFile(exportTmpFile.getAbsolutePath());
        exportWorker.setDataSource(mailingBaseService.getDataSource());
        exportWorker.setDateFormat(admin.getDateFormat());
        exportWorker.setDateTimeFormat(admin.getDateTimeFormatWithSeconds());
        exportWorker.setExportTimezone(TimeZone.getTimeZone(admin.getAdminTimezone()).toZoneId());
        exportWorker.call();
    }

    private String getResultFileName(int mailingId, Admin admin) {
        return mailingBaseService.getMailingName(mailingId, admin.getCompanyID()) + "_"
                + I18nString.getLocaleString("Recipients", admin.getLocale()) + "_"
                + new SimpleDateFormat(DateUtilities.YYYYMD).format(new Date()) + ".csv";
    }

    private String getExportTmpFileName(int mailingId, int companyId) {
        return FileUtils.getUniqueFileName(
                String.format("MailingRecipients_%d_%d_%s.csv", companyId, mailingId,
                        new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS_FORFILENAMES).format(new Date())),
                name -> new File(AgnUtils.createDirectory(MAILING_RECIPIENT_TMP_DIRECTORY), name).exists());
    }
}
