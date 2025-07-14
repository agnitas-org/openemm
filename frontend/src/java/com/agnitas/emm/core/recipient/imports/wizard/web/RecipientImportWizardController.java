/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.imports.wizard.web;

import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PollingUid;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.dao.RecipientDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.recipient.imports.wizard.exception.NotAllowedImportWizardStepException;
import com.agnitas.emm.core.recipient.imports.wizard.form.ImportWizardSteps;
import com.agnitas.emm.core.recipient.imports.wizard.form.ImportWizardSteps.Step;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.emm.core.recipientsreport.service.RecipientsReportService;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.messages.I18nString;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import jakarta.servlet.http.HttpSession;
import com.agnitas.beans.ColumnMapping;
import com.agnitas.beans.DatasourceDescription;
import com.agnitas.beans.ImportProfile;
import com.agnitas.beans.ImportStatus;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.impl.ColumnMappingImpl;
import com.agnitas.beans.impl.DatasourceDescriptionImpl;
import com.agnitas.beans.impl.ImportProfileImpl;
import com.agnitas.emm.core.datasource.enums.SourceGroupType;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.service.ImportException;
import com.agnitas.service.ImportWizardHelper;
import com.agnitas.service.ImportWizardService;
import com.agnitas.service.ProfileImportWorker;
import com.agnitas.service.ProfileImportWorkerFactory;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.impl.ImportWizardContentParseException;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.CsvColInfo;
import com.agnitas.util.CsvDataBreakInsideCellException;
import com.agnitas.util.CsvDataInvalidItemCountException;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.EmmCalendar;
import com.agnitas.util.HttpUtils;
import com.agnitas.util.ImportUtils;
import com.agnitas.util.SafeString;
import com.agnitas.util.importvalues.CheckForDuplicates;
import com.agnitas.util.importvalues.DateFormat;
import com.agnitas.util.importvalues.ImportMode;
import com.agnitas.util.importvalues.MailType;
import com.agnitas.util.importvalues.Separator;
import com.agnitas.util.importvalues.TextRecognitionChar;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/recipient/import/wizard")
@PermissionMapping("recipient.import.wizard")
@SessionAttributes(types = ImportWizardSteps.class)
public class RecipientImportWizardController implements XssCheckAware {

    public static final String RECIPIENTS_IMPORT_WIZARD_KEY = "RECIPIENTS_IMPORT_WIZARD";
    private final Map<PollingUid, Date> importStartDates = new ConcurrentHashMap<>();
    private static final Logger logger = LogManager.getLogger(RecipientImportWizardController.class);
    private static final String IMPORT_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "RecipientImport";
    private static final String EMAIL_STR = "email";
    
    private final RecipientDao recipientDao;
    private final DatasourceDescriptionDao datasourceDescriptionDao;
    private final ProfileFieldDao profileFieldDao;
    private final DataSource dataSource;
    private final ConfigService configService;
    private final MailinglistService mailinglistService;
    private final RecipientsReportService reportService;
    private final ImportWizardService importWizardService;
    private final ProfileImportWorkerFactory profileImportWorkerFactory;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final UserActivityLogService userActivityLogService;

    public RecipientImportWizardController(RecipientDao recipientDao,
                                           DatasourceDescriptionDao datasourceDescriptionDao,
                                           ProfileFieldDao profileFieldDao,
                                           DataSource dataSource, ConfigService configService,
                                           MailinglistService mailinglistService, RecipientsReportService reportService,
                                           ImportWizardService importWizardService,
                                           ProfileImportWorkerFactory profileImportWorkerFactory,
                                           MailinglistApprovalService mailinglistApprovalService,
                                           UserActivityLogService userActivityLogService) {
        this.recipientDao = recipientDao;
        this.datasourceDescriptionDao = datasourceDescriptionDao;
        this.profileFieldDao = profileFieldDao;
        this.dataSource = dataSource;
        this.configService = configService;
        this.mailinglistService = mailinglistService;
        this.reportService = reportService;
        this.importWizardService = importWizardService;
        this.profileImportWorkerFactory = profileImportWorkerFactory;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.userActivityLogService = userActivityLogService;
    }

    @ExceptionHandler(NotAllowedImportWizardStepException.class)
    public String onNotAllowedImportWizardStepException(HttpSession session) {
        ImportWizardSteps steps = (ImportWizardSteps) session.getAttribute("importWizardSteps");
        if (steps.isImportRunning()) {
            return showProgressPage(steps);
        }
        return "redirect:/recipient/import/wizard/step/" + steps.getCurrentStep().getControllerEndpointName();
    }

    private String showProgressPage(ImportWizardSteps steps) {
        if (!steps.isImportRunning()) {
            steps.setImportUID(UUID.randomUUID().toString());
        }

        return "recipient_import_wizard_progress";
    }

    @ModelAttribute("importWizardSteps")
    public ImportWizardSteps getImportWizardSteps(Admin admin) {
        ImportWizardSteps steps = new ImportWizardSteps();
        ImportWizardHelper helper = importWizardService.createHelper();
        helper.setCompanyID(admin.getCompanyID());
        helper.setLocale(admin.getLocale());
        steps.setHelper(helper);
        steps.setCurrentStep(Step.FILE);
        return steps;
    }

    @GetMapping("/step/file.action")
    public String fileStepView(Model model, Admin admin) {
        model.addAttribute("importWizardSteps", getImportWizardSteps(admin));
        model.addAttribute("csvFiles", importWizardService.getCsvUploads(admin));
        return "recipient_import_wizard_file_step";
    }

    @PostMapping("/step/file.action")
    public String fileStepSave(ImportWizardSteps steps, Admin admin, Popups popups) throws IOException {
        SimpleServiceResult result = importWizardService.checkAndReadCsvFile(steps, admin);
        if (!result.isSuccess()) {
            popups.addPopups(result);
            return MESSAGES_VIEW;
        }
        steps.nextStep(Step.MODE);
        return "redirect:/recipient/import/wizard/step/mode.action";
    }

    @GetMapping("/step/mode.action")
    public String modeStepView(ImportWizardSteps steps) {
        if (StringUtils.isBlank(steps.getHelper().getStatus().getKeycolumn())) {
            steps.getHelper().getStatus().setKeycolumn(EMAIL_STR);
        }
        return "recipient_import_wizard_mode_step";
    }

    @PostMapping("/step/mode.action")
    public String modeStepSave(ImportWizardSteps steps) {
        steps.nextStep(Step.MAPPING);
        return "redirect:/recipient/import/wizard/step/mapping.action";
    }

    @GetMapping("/step/mapping.action")
    public String mappingStepView(@RequestParam(defaultValue = "false") boolean back, ImportWizardSteps steps, Admin admin, Popups popups) throws Exception {
        steps.setCurrentStep(Step.MAPPING);
        ImportWizardHelper helper = steps.getHelper();
        if (back) {
            helper.clearDummyColumnsMappings();
        }

        ServiceResult<List<CsvColInfo>> csvColumns = importWizardService.parseFirstLine(helper);
        if (!csvColumns.isSuccess()) {
            popups.addPopups(csvColumns);
            return MESSAGES_VIEW;
        }
        helper.setDbAllColumns(new TreeMap<>(getAvailableDbColumns(helper, admin)));
        helper.setCsvAllColumns(new ArrayList<>(csvColumns.getResult()));
        helper.setCsvMaxUsedColumn(csvColumns.getResult().size());
        return "recipient_import_wizard_mapping_step";
    }

    private CaseInsensitiveMap<String, CsvColInfo> getAvailableDbColumns(ImportWizardHelper helper, Admin admin) {
        CaseInsensitiveMap<String, CsvColInfo> dbColumnsAvailable = recipientDao.readDBColumns(admin.getCompanyID(), admin.getAdminID(), Collections.singletonList(helper.getKeyColumn()));
        RecipientStandardField.getImportChangeNotAllowedColumns(admin.permissionAllowed(Permission.IMPORT_CUSTOMERID)).forEach(dbColumnsAvailable::remove);
        profileFieldDao.getProfileFieldsMap(admin.getCompanyID(), admin.getAdminID()).entrySet().stream()
                .filter(profileFieldEntry -> profileFieldEntry.getValue().getModeEdit() == ProfileFieldMode.NotVisible)
                .forEach(profileFieldEntry -> dbColumnsAvailable.remove(profileFieldEntry.getKey()));
        return dbColumnsAvailable;
    }

    @PostMapping("/step/mapping.action")
    public String mappingStepSave(Admin admin, ImportWizardSteps steps, Popups popups) {
        if (isColumnDuplicate(steps.getMappingStep().getColumnMapping())) {
            popups.alert("error.import.column.dbduplicate");
            return MESSAGES_VIEW;
        }
        steps.getHelper().mapColumns(steps.getMappingStep().getColumnMapping());

        if (!tryParseContent(admin, steps, popups)) {
            return MESSAGES_VIEW;
        }
        if (isProfileKeyColumnIndexed(steps.getHelper(), admin.getCompanyID())) {
            popups.warning("warning.import.keyColumn.index");
        }
        if (steps.isMissingFieldsStepNeeded()) {
            steps.nextStep(Step.VERIFY_MISSING_FIELDS);
            return "redirect:/recipient/import/wizard/step/verifyMissingFields.action";
        }
        steps.nextStep(Step.VERIFY);
        return "redirect:/recipient/import/wizard/step/verify.action";
    }

    private boolean tryParseContent(Admin admin, ImportWizardSteps steps, Popups popups) {
        try {
            importWizardService.parseContent(steps.getHelper());
            steps.getHelper().setLinesOK(importWizardService.getLinesOKFromFile(steps.getHelper()));
            int maxRowsAllowedForClassicImport = configService.getIntegerValue(ConfigValue.ClassicImportMaxRows, admin.getCompanyID());
            if (maxRowsAllowedForClassicImport >= 0 && steps.getHelper().getLinesOK() > maxRowsAllowedForClassicImport) {
                popups.alert("error.import.maxlinesexceeded", steps.getHelper().getLinesOK(), maxRowsAllowedForClassicImport);
            } else if (steps.getHelper().getLinesOK() == 0) {
                popups.alert("error.import.invalid.settings");
            }
        } catch (ImportWizardContentParseException e) {
            popups.alert(e.getErrorMessageKey());
        } catch (CsvDataInvalidItemCountException e) {
            popups.alert("error.import.data.itemcount", e.getExpected(), e.getActual(), e.getErrorLineNumber());
        } catch (CsvDataBreakInsideCellException e) {
            popups.alert("error.import.file.linebreak", e.getCellIndex(), e.getErrorLineNumber());
        } catch (Exception e) {
            logger.error("Exception caught: {}", e.getMessage(), e);
            popups.alert("error.import.exception", e.getMessage());
        }
        return !popups.hasAlertPopups();
    }

    private boolean isProfileKeyColumnIndexed(ImportWizardHelper helper, int companyId) {
        String keyColumn = helper.getKeyColumn().toLowerCase();
        return !DbUtilities.checkForIndex(dataSource, "customer_" + companyId + "_tbl", Collections.singletonList(keyColumn));
    }

    private boolean isColumnDuplicate(Map<String, String> columnMapping) {
        Set<String> dbColumnSet = new HashSet<>();
        for (Map.Entry<String, String> entry : columnMapping.entrySet()) {
            if (entry.getKey().startsWith("map_")) {
                String value = entry.getValue();
                if (!"NOOP".equals(value) && dbColumnSet.contains(value)) {
                    return true;
                }
                dbColumnSet.add(value);
            }
        }
        return false;
    }

    @GetMapping("/step/verifyMissingFields.action")
    public String verifyMissingFieldsStepView() {
        return "recipient_import_wizard_verify_missing_fields_step";
    }

    @PostMapping("/step/verifyMissingFields.action")
    public String verifyMissingFieldsStepSave(ImportWizardSteps steps) {
        steps.nextStep(Step.PRESCAN);
        return "redirect:/recipient/import/wizard/step/verify.action";
    }

    @GetMapping("/step/verify.action")
    public String verifyStepView(Model model, ImportWizardSteps steps, Admin admin) {
        ImportWizardHelper helper = steps.getHelper();
        model.addAttribute("parsedContentJson", importWizardService.getParsedContentJson(helper, admin));
        if (!admin.isRedesignedUiUsed()) {
            model.addAttribute("previewHeaders", helper.getCsvAllColumns().stream()
                    .filter(CsvColInfo::isActive)
                    .map(CsvColInfo::getName).collect(Collectors.toList()));
        }

        return "recipient_import_wizard_verify_step";
    }

    @PostMapping("/step/verify.action")
    public String verifyStepSave(ImportWizardSteps steps) {
        steps.nextStep(Step.PRESCAN);
        return "redirect:/recipient/import/wizard/step/preScan.action";
    }

    @GetMapping("/step/preScan.action")
    public String preScanStepView(@RequestParam(defaultValue = "false") boolean back, ImportWizardSteps steps) {
        if (StringUtils.isBlank(steps.getHelper().getStatus().getKeycolumn())) {
            steps.getHelper().getStatus().setKeycolumn(EMAIL_STR);
        }
        return "recipient_import_wizard_preScan_step";
    }

    @PostMapping("/step/preScan.action")
    public String preScanStepSave(ImportWizardSteps steps) {
        steps.nextStep(Step.MAILING_LISTS);
        return "redirect:/recipient/import/wizard/step/mailinglists.action";
    }

    @GetMapping("/step/mailinglists.action")
    public String mailinglistsStepView(Model model, ImportWizardSteps steps, Admin admin) {
        if (steps.getHelper().getMode() == ImportMode.TO_BLACKLIST.getIntValue()) {
            return showProgressPage(steps);
        }
        model.addAttribute("mailinglists", mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
        return "recipient_import_wizard_mailinglists_step";
    }

    @PostMapping("/step/mailinglists.action")
    public String mailinglistsStepSave(Admin admin, ImportWizardSteps steps, Popups popups) {
        if (mailinglistRequiredForImport(admin, steps.getHelper())) {
            popups.alert("error.import.no_mailinglist");
            return MESSAGES_VIEW;
        }
        steps.getHelper().setDbInsertStatus(0);
        return showProgressPage(steps);
    }

    @RequestMapping("/run.action")
    public Object run(Model model, Admin admin, ImportWizardSteps steps, HttpSession session, Popups popups) {
        ImportWizardHelper helper = steps.getHelper();
        boolean isRedesignedUI = admin.isRedesignedUiUsed();
        PollingUid pollingUid = Pollable.uid(session.getId(), RECIPIENTS_IMPORT_WIZARD_KEY, steps.getImportUID());

        Callable<ModelAndView> importWorker = () -> {
            try {
                importStartDates.put(pollingUid, new Date());

                steps.setImportRunning(true);
                tryRunImport(model, admin, steps, popups, helper, session);

                helper.addDbInsertStatusMessageAndParameters("import.csv_completed");
                helper.setDbInsertStatus(1000);
                if (!isRedesignedUI) {
                    model.addAttribute("importIsDone", true);
                }
                writeClassicImportLog(admin, helper);
                loadResultPageAttrs(model, helper, admin.getCompanyID());
                return new ModelAndView("recipient_import_wizard_result", model.asMap());
            } finally {
                importStartDates.remove(pollingUid);
            }
        };

        return new Pollable<>(
                pollingUid,
                isRedesignedUI ? Pollable.SHORT_TIMEOUT : Pollable.DEFAULT_TIMEOUT,
                new ModelAndView(isRedesignedUI ? "recipient_import_wizard_progress" : "recipient_import_wizard_result", "completedPercent", calculateImportProgressPercentage(pollingUid)),
                importWorker);
    }

    private int calculateImportProgressPercentage(PollingUid pollingUid) {
        Date date = importStartDates.get(pollingUid);
        return AgnUtils.calculateProgressPercentage(date);
    }

    private void tryRunImport(Model model, Admin admin, ImportWizardSteps steps, Popups popups, ImportWizardHelper helper, HttpSession session) {
        try {
            runImport(admin, steps, helper, session);
        } catch (Exception e) {
            logger.error("import wizard: {}", e.getMessage(), e);
            // do not refresh when an error has occurred
            CollectionUtils.emptyIfNull(helper.getDbInsertStatusMessagesAndParameters()).clear();
            if (e instanceof ImportException importException) {
                popups.alert(importException.getErrorMessageKey(), importException.getAdditionalErrorData());
                helper.addDbInsertStatusMessageAndParameters(importException.getErrorMessageKey());
                model.addAttribute("importError", importException.getMessage(admin.getLocale()));
            } else {
                popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
                helper.addDbInsertStatusMessageAndParameters("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
            }
        } finally {
            steps.setImportRunning(false);
        }
    }

    private void runImport(Admin admin, ImportWizardSteps steps, ImportWizardHelper helper, HttpSession session) throws Exception {
    	ProfileImportWorker worker = getProfileImportWorker(admin, session.getId(), steps);
        worker.call();

        if (worker.getError() != null) {
            createReportResult(admin, worker, helper, true);
            throw worker.getError();
        } else {
            createReportResult(admin, worker, helper, false);
        }
        worker.cleanUp();
    }

    private void loadResultPageAttrs(Model model, ImportWizardHelper helper, int companyID) {
        Map<String, Mailinglist> mailinglists = new HashMap<>();
        Map<MediaTypes, Map<String, String>> resultMailingListAdded = helper.getResultMailingListAdded();
        if (resultMailingListAdded != null) {
            for (MediaTypes mediaType : resultMailingListAdded.keySet()) {
                for (String mailinglistID : resultMailingListAdded.get(mediaType).keySet()) {
                    Mailinglist mailinglist = mailinglistService.getMailinglist(Integer.parseInt(mailinglistID), companyID);
                    mailinglists.put(mailinglistID, mailinglist);
                }
            }
            model.addAttribute("resultMLAdded", resultMailingListAdded.get(MediaTypes.EMAIL));
        }
        model.addAttribute("mailinglists", mailinglists);
    }

    private void writeClassicImportLog(Admin admin, ImportWizardHelper helper) {
        Vector<String> allMailingListsIds = helper.getMailingLists();

        String fileName = helper.getFile() != null ? helper.getFile().getName() : "not set";

        StringBuilder description = new StringBuilder();
        String prefix = " ";

        description.append("Classic import started at: ");
        description.append(new SimpleDateFormat("yyyy/MM/dd HH-mm-ss").format(new Date()));
        description.append(". File name: ");
        description.append(fileName);
        description.append(", mailing lists IDs:");

        if (allMailingListsIds != null) {
            for (String mailingListId : allMailingListsIds) {
                description.append(prefix);
                prefix = ", ";
                description.append(mailingListId);
            }
        }
        description.append(".");
        
        userActivityLogService.writeUserActivityLog(admin, "import classic from file", description.toString(), logger);
    }

    private void createReportResult(Admin admin, ProfileImportWorker profileImportWorker, ImportWizardHelper helper, boolean isError) {
        // Create report, statistics data for GUI
        Map<MediaTypes, Map<String, String>> resultMailingListAdded = new HashMap<>();
        if (profileImportWorker.getMailinglistAssignStatistics() != null) {
            for (MediaTypes mediaType : profileImportWorker.getMailinglistAssignStatistics().keySet()) {
                for (Map.Entry<Integer, Integer> entry : profileImportWorker.getMailinglistAssignStatistics().get(mediaType).entrySet()) {
                    resultMailingListAdded.put(mediaType, new HashMap<>());
                    resultMailingListAdded.get(mediaType).put(entry.getKey().toString(), entry.getValue().toString());
                }
            }
        }
        helper.setResultMailingListAdded(resultMailingListAdded);
        createRecipientReport(admin, profileImportWorker, helper, isError);
    }

    private Date getTimeForResultCsv(Admin admin) {
        EmmCalendar emmCalender = new EmmCalendar(TimeZone.getDefault());
        TimeZone zone = TimeZone.getTimeZone(admin.getAdminTimezone());
        emmCalender.changeTimeWithZone(zone);
        return emmCalender.getTime();
    }

    private void createRecipientReport(Admin admin, ProfileImportWorker profileImportWorker, ImportWizardHelper helper, boolean isError) {
        Date time = getTimeForResultCsv(admin);
        String filename = time.getTime() + ".csv";
        String csvFile = generateLocalizedImportCSVReport(admin.getLocale(), time, profileImportWorker.getStatus(), helper.getMode());

        RecipientsReport report = new RecipientsReport();

        report.setDatasourceId(helper.getStatus().getDatasourceID());
        report.setFilename(filename);
        report.setReportDate(new Date());
        report.setIsError(isError);

        report.setEntityId(profileImportWorker.getImportProfileId());
        report.setEntityType(RecipientsReport.EntityType.IMPORT);
        report.setEntityExecution(RecipientsReport.EntityExecution.MANUAL);
        report.setEntityData(RecipientsReport.EntityData.PROFILE);

        reportService.saveNewReport(admin, admin.getCompanyID(), report, csvFile);
    }

    private String generateLocalizedImportCSVReport(Locale locale, Date date, ImportStatus status, int mode) {
        String csvfile = "";
        csvfile += SafeString.getLocaleString("import.SubscriberImport", locale);
        csvfile += "\n" + SafeString.getLocaleString("Date", locale) + ": ; \"" + date + "\"\n";
        csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_email", locale) + ":;" + status.getError(EMAIL_STR);
        csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_blacklist", locale) + ":;" + status.getError("blacklist");
        csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_double", locale) + ":;" + status.getError("keyDouble");
        csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_numeric", locale) + ":;" + status.getError("numeric");
        csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_mailtype", locale) + ":;" + status.getError("mailtype");
        csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_gender", locale) + ":;" + status.getError("gender");
        csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_date", locale) + ":;" + status.getError("date");
        csvfile += "\n" + SafeString.getLocaleString("csv_errors_linestructure", locale) + ":;" + status.getError("structure");
        csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_invalidNullValues", locale) + ":;" + status.getInvalidNullValues();
        csvfile += "\n" + SafeString.getLocaleString("error.import.value.large", locale) + ":;" + status.getError("valueTooLarge");
        csvfile += "\n" + SafeString.getLocaleString("error.import.number.large", locale) + ":;" + status.getError("numberTooLarge");
        csvfile += "\n" + SafeString.getLocaleString("error.import.invalidFormat", locale) + ":;" + status.getError("invalidFormat");
        csvfile += "\n" + SafeString.getLocaleString("error.import.missingMandatory", locale) + ":;" + status.getError("missingMandatory");
        if (!status.getErrorColumns().isEmpty()) {
            csvfile += "\n" + SafeString.getLocaleString("error.import.errorColumns", locale) + ":;" + StringUtils.join(status.getErrorColumns(), ", ");
        }
        csvfile += "\n" + SafeString.getLocaleString("import.result.filedataitems", locale) + ":;" + status.getCsvLines();
        csvfile += "\n" + SafeString.getLocaleString("import.RecipientsAllreadyinDB", locale) + ":;" + status.getAlreadyInDb();
        if (mode == ImportMode.ADD.getIntValue() || mode == ImportMode.ADD_AND_UPDATE.getIntValue()) {
            csvfile += "\n" + SafeString.getLocaleString("import.result.imported", locale) + ":;" + status.getInserted();
        }
        if (mode == ImportMode.UPDATE.getIntValue() || mode == ImportMode.ADD_AND_UPDATE.getIntValue()) {
            csvfile += "\n" + SafeString.getLocaleString("import.result.updated", locale) + ":;" + status.getUpdated();
        }
        if (mode == ImportMode.TO_BLACKLIST.getIntValue()) {
            csvfile += "\n" + SafeString.getLocaleString("import.result.blacklisted", locale) + ":;" + status.getBlacklisted();
        }

        csvfile += "\n" + "mode:;" + SafeString.getLocaleString(ImportMode.getFromInt(mode).getMessageKey(), locale);

        return csvfile;
    }

    private ProfileImportWorker getProfileImportWorker(Admin admin, String sessionId, ImportWizardSteps steps) throws Exception {
        ImportWizardHelper helper = steps.getHelper();
        final int companyID = admin.getCompanyID();
        final int adminID = admin.getAdminID();

        // Using ProfileImportWorker behind ClassicImport-GUI

        // Store uploaded import file
        File importFile = new File(File.createTempFile("upload_csv_file_" + companyID + "_" + adminID + "_", ".csv", AgnUtils.createDirectory(IMPORT_FILE_DIRECTORY + "/" + companyID)).getAbsolutePath());
        try (InputStream inputStream = helper.getFile().toInputStream()) {
            try (FileOutputStream outputStream = new FileOutputStream(importFile, false)) {
                IOUtils.copy(inputStream, outputStream);
            }
        }

        // set datasource id
        DatasourceDescription dsDescription = new DatasourceDescriptionImpl();
        dsDescription.setId(0);
        dsDescription.setCompanyID(companyID);
        dsDescription.setSourceGroupType(SourceGroupType.File);
        dsDescription.setCreationDate(new Date());
        dsDescription.setDescription(helper.getFile().getName());
        dsDescription.setDescription2("EMM-Import (Classic/ProfileImport)");
        datasourceDescriptionDao.save(dsDescription);

        helper.getStatus().setDatasourceID(dsDescription.getId());

        ImportProfile importProfile = new ImportProfileImpl();
        importProfile.setName(I18nString.getLocaleString("import.Wizard", admin.getLocale()));
        importProfile.setCompanyId(companyID);
        importProfile.setAdminId(admin.getAdminID());
        importProfile.setReportLocale(admin.getLocale());
        importProfile.setReportTimezone(admin.getAdminTimezone());


        // Only use this value, if the user has the right to change it
        // If the user doesn't have the right to change the value, the default for "UpdateAllDuplicates" in classic import is always "false" (historical)
        importProfile.setUpdateAllDuplicates(admin.permissionAllowed(Permission.IMPORT_MODE_DUPLICATES)
                && steps.getModeStep().isUpdateAllDuplicates());

        // Data from first classic import page
        importProfile.setSeparator(Separator.getSeparatorByChar(helper.getStatus().getSeparator()).getIntValue());
        importProfile.setTextRecognitionChar(TextRecognitionChar.getTextRecognitionCharByString(helper.getStatus().getDelimiter()).getIntValue());
        importProfile.setCharset(com.agnitas.util.importvalues.Charset.getCharsetByName(helper.getStatus().getCharset()).getIntValue());
        importProfile.setDateFormat(DateFormat.getDateFormatByValue(helper.getDateFormat()).getIntValue());

        // Data from second classic import page

        importProfile.setImportMode(ImportMode.getFromInt(helper.getMode()).getIntValue());

        importProfile.setNullValuesAction(helper.getStatus().getIgnoreNull());
        importProfile.setKeyColumn(helper.getStatus().getKeycolumn());

        // Translate CustomerImportStatus.DoubleCheckInt into CheckForDuplicates.DoubleCheckInt
        if (helper.getStatus().getDoubleCheck() == ImportStatus.DOUBLECHECK_NONE) {
            importProfile.setCheckForDuplicates(CheckForDuplicates.NO_CHECK.getIntValue());
        } else if (helper.getStatus().getDoubleCheck() == ImportStatus.DOUBLECHECK_FULL) {
            importProfile.setCheckForDuplicates(CheckForDuplicates.COMPLETE.getIntValue());
        } else {
            throw new Exception("Invalid duplicate check index int: " + helper.getStatus().getDoubleCheck());
        }

        // Data from third classic import page
        helper.getColumnMapping().remove("gender_dummy");
        helper.getColumnMapping().remove("mailtype_dummy");
        List<ColumnMapping> columnMapping = new ArrayList<>();
        for (Map.Entry<String, CsvColInfo> entry : helper.getColumnMapping().entrySet()) {
            ColumnMapping columnMappingEntry = new ColumnMappingImpl();
            columnMappingEntry.setDatabaseColumn(entry.getValue().getName());
            columnMappingEntry.setFileColumn(entry.getKey());
            columnMapping.add(columnMappingEntry);
        }
        importProfile.setColumnMapping(columnMapping);

        // Data from fourth classic import page (missng mailtype, gender)
        importProfile.setDefaultMailType(MailType.getFromInt(Integer.parseInt(helper.getManualAssignedMailingType())).getIntValue());

        // Data from seventh classic import page
        List<Integer> mailingListIdsToAssign = new ArrayList<>();
        if (helper.getMailingLists() != null) {
            for (String mailinglistIdString : helper.getMailingLists()) {
                mailingListIdsToAssign.add(Integer.parseInt(mailinglistIdString));
            }
        }

        ProfileImportWorker profileImportWorker = profileImportWorkerFactory.getProfileImportWorker(
                false, // Not interactive mode, because there is no error edit GUI
                mailingListIdsToAssign,
                sessionId,
                companyID,
                admin,
                dsDescription.getId(),
                importProfile,
                new RemoteFile(helper.getFile().getName(), importFile, -1),
                helper.getStatus());

        profileImportWorker.setMaxGenderValue(admin.permissionAllowed(Permission.RECIPIENT_GENDER_EXTENDED)
                ? ConfigService.MAX_GENDER_VALUE_EXTENDED
                : ConfigService.MAX_GENDER_VALUE_BASIC);

        helper.clearDbInsertStatusMessagesAndParameters();
        return profileImportWorker;
    }

    private boolean mailinglistRequiredForImport(Admin admin, ImportWizardHelper helper) {
        return CollectionUtils.isEmpty(helper.getMailingLists())
                && helper.getMode() != ImportMode.UPDATE.getIntValue()
                && !(helper.getMode() == ImportMode.ADD.getIntValue() && admin.permissionAllowed(Permission.IMPORT_WITHOUT_MAILINGLIST))
                && !(helper.getMode() == ImportMode.ADD_AND_UPDATE.getIntValue() && admin.permissionAllowed(Permission.IMPORT_WITHOUT_MAILINGLIST));
    }

    @GetMapping("/downloadCsv.action")
    public ResponseEntity<?> downloadCsv(@RequestParam(required = false) String errorType, ImportWizardSteps steps, String downloadName, Admin admin) throws UnsupportedEncodingException {
        ImportWizardHelper helper = steps.getHelper();
        String charset = "result".equals(downloadName) ? "UTF-8" : helper.getStatus().getCharset();
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "plain", Charset.forName(charset)))
                .header(HttpHeaders.CONTENT_DISPOSITION, HttpUtils.getContentDispositionAttachment(downloadName + ".csv", charset))
                .body(getDataToDownload(admin, helper, downloadName).getBytes(charset));
    }

    private ImportUtils.ImportErrorType getErrorTypeByDownloadName(String downloadName) {
        switch (downloadName) {
            case "error_date":
                return ImportUtils.ImportErrorType.DATE_ERROR;
            case "error_email":
                return ImportUtils.ImportErrorType.EMAIL_ERROR;
            case "double_email":
                return ImportUtils.ImportErrorType.KEYDOUBLE_ERROR;
            case "error_gender":
                return ImportUtils.ImportErrorType.GENDER_ERROR;
            case "error_mailtype":
                return ImportUtils.ImportErrorType.MAILTYPE_ERROR;
            case "error_numeric":
                return ImportUtils.ImportErrorType.NUMERIC_ERROR;
            case "error_structure":
                return ImportUtils.ImportErrorType.STRUCTURE_ERROR;
            case "error_blacklist":
                return ImportUtils.ImportErrorType.BLACKLIST_ERROR;
            default:
                return null;
        }
    }

    private String getDataToDownload(Admin admin, ImportWizardHelper helper, String downloadName) {
        ImportUtils.ImportErrorType errorType = getErrorTypeByDownloadName(downloadName);
        if (errorType != null) {
            return helper.getErrorData().get(errorType).toString();
        }
        switch (downloadName) {
            case "import_ok":
                return helper.getParsedData().toString();
            case "result":
                Date time = getTimeForResultCsv(admin);
                return generateLocalizedImportCSVReport(admin.getLocale(), time, helper.getStatus(), helper.getMode());
            default:
                return "";
        }
    }
}
