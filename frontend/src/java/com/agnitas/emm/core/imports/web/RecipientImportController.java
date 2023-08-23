/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.imports.web;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PollingUid;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.commons.dto.FileDto;
import com.agnitas.emm.core.import_profile.component.parser.RecipientImportFileContentParser;
import com.agnitas.emm.core.import_profile.component.parser.RecipientImportFileContentParserFactory;
import com.agnitas.emm.core.import_profile.component.validator.RecipientImportFileValidator;
import com.agnitas.emm.core.import_profile.component.validator.RecipientImportFileValidatorFactory;
import com.agnitas.emm.core.imports.beans.ImportErrorCorrection;
import com.agnitas.emm.core.imports.beans.ImportProgressSteps;
import com.agnitas.emm.core.imports.beans.ImportResultFileType;
import com.agnitas.emm.core.imports.form.ImportErrorsCorrectionsForm;
import com.agnitas.emm.core.imports.form.RecipientImportForm;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.recipient.imports.wizard.dto.LocalFileDto;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.mvc.AgnRedirectView;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.impl.StrutsPopups;
import com.agnitas.web.perm.annotations.PermissionMapping;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.DatasourceDescription;
import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.DatasourceDescriptionImpl;
import org.agnitas.beans.impl.ImportStatusImpl;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.dao.SourceGroupType;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.service.ImportException;
import org.agnitas.service.ImportProfileService;
import org.agnitas.service.ProfileImportWorker;
import org.agnitas.service.ProfileImportWorkerFactory;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.service.impl.CSVColumnState;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.ImportUtils;
import org.agnitas.util.UserActivityUtil;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.ImportMode;
import org.agnitas.util.importvalues.Separator;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.agnitas.web.ProfileImportReporter;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.agnitas.web.mvc.Pollable.SHORT_TIMEOUT;
import static java.text.MessageFormat.format;
import static org.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static org.agnitas.util.ImportUtils.RECIPIENT_IMPORT_FILE_ATTRIBUTE_NAME;

public class RecipientImportController {

    private static final Logger LOGGER = LogManager.getLogger(RecipientImportController.class);
    private static final String IMPORT_DATA_KEY = "import-recipients";
    private static final String ERRORS_EDIT_KEY = "import-recipients-errors";

    // TODO: think about keeping the worker in the session
    private final Map<String, ProfileImportWorker> importWorkersMap = new ConcurrentHashMap<>();

    private final ImportProfileService importProfileService;
    private final MailinglistService mailinglistService;
    private final EmmActionService emmActionService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final RecipientImportFileValidatorFactory importFileValidatorFactory;
    private final RecipientImportFileContentParserFactory contentParserFactory;
    private final RecipientService recipientService;
    private final DatasourceDescriptionDao datasourceDescriptionDao;
    private final ProfileImportWorkerFactory profileImportWorkerFactory;
    private final ConfigService configService;
    private final WebStorage webStorage;
    private final ImportRecipientsDao importRecipientsDao;
    private final ProfileImportReporter importReporter;
    private final UserActivityLogService userActivityLogService;

    public RecipientImportController(ImportProfileService importProfileService, MailinglistService mailinglistService, EmmActionService emmActionService,
                                     MailinglistApprovalService mailinglistApprovalService, RecipientImportFileValidatorFactory importFileValidatorFactory,
                                     RecipientImportFileContentParserFactory contentParserFactory, RecipientService recipientService, DatasourceDescriptionDao datasourceDescriptionDao,
                                     ProfileImportWorkerFactory profileImportWorkerFactory, ConfigService configService, WebStorage webStorage, ImportRecipientsDao importRecipientsDao,
                                     ProfileImportReporter importReporter, UserActivityLogService userActivityLogService) {

        this.importProfileService = importProfileService;
        this.mailinglistService = mailinglistService;
        this.emmActionService = emmActionService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.importFileValidatorFactory = importFileValidatorFactory;
        this.contentParserFactory = contentParserFactory;
        this.recipientService = recipientService;
        this.datasourceDescriptionDao = datasourceDescriptionDao;
        this.profileImportWorkerFactory = profileImportWorkerFactory;
        this.configService = configService;
        this.webStorage = webStorage;
        this.importRecipientsDao = importRecipientsDao;
        this.importReporter = importReporter;
        this.userActivityLogService = userActivityLogService;
    }

    @GetMapping("/chooseMethod.action")
    @PermissionMapping("choose.method")
    public String chooseImportMethod(Admin admin) {
        boolean hasAccessToStandardImport = (admin.permissionAllowed(Permission.WIZARD_IMPORT));
        boolean hasAccessToWizardImport = admin.permissionAllowed(Permission.WIZARD_IMPORTCLASSIC);

        if (hasAccessToStandardImport && !hasAccessToWizardImport) {
            return "redirect:/recipient/import/view.action";
        }

        if (hasAccessToWizardImport && !hasAccessToStandardImport) {
            if (!admin.permissionAllowed(Permission.IMPORT_WIZARD_ROLLBACK)) {
                return "redirect:/recipient/import/wizard/step/file.action";
            } else {
                return "redirect:/importwizard.do?action=1";
            }
        }

        return "recipient_import_method_choose";
    }

    @GetMapping("/view.action")
    public String view(@ModelAttribute("form") RecipientImportForm form, Admin admin, Model model, HttpSession session) {
        clearFinishedWorker(session);

        if (isWorkerExists(session)) {
            return continueImportExecution();
        }

        if (form.getProfileId() == 0) {
            form.setProfileId(admin.getDefaultImportProfileID());
        }

        form.setFileName(getImportFileNameFromSession(session));

        prepareModelAttributesForViewStartPage(admin, model);
        return "recipient_import_start";
    }

    @RequestMapping("/preview.action")
    public String preview(@ModelAttribute("form") RecipientImportForm form, Admin admin, Popups popups, Model model, HttpSession session) throws Exception {
        if (!isImportFileExists(form, session)) {
            popups.alert("error.import.no_file");
            return MESSAGES_VIEW;
        }

        if (findImportProfiles(admin).isEmpty()) {
            popups.alert("error.import.no_profile");
            return MESSAGES_VIEW;
        }

        ImportProfile profile = importProfileService.getImportProfileById(form.getProfileId());
        FileDto fileDto = getImportFile(form, admin, session);

        if (!ImportUtils.checkIfImportFileHasData(fileDto.toFile(), profile.getZipPassword())) {
            popups.alert("autoimport.error.emptyFile", fileDto.getName());
            return MESSAGES_VIEW;
        }

        if (profile.getImportMode() == ImportMode.REACTIVATE_BOUNCED.getIntValue()) {
            popups.warning("warning.import.mode.bounceractivation");
        }

        boolean isProfileValid = validateImportProfile(profile, popups);

        if (!isProfileValid || !isFileValid(fileDto.toFile(), profile, popups)) {
            return String.format("redirect:/import-profile/%d/view.action", profile.getId());
        }

        ServiceResult<List<List<String>>> parsingResult = parseImportFileContent(fileDto.toFile(), profile);
        if (!parsingResult.isSuccess()) {
            popups.addPopups(parsingResult);
            return MESSAGES_VIEW;
        }

        checkIfProfileKeyColumnIndexed(profile, popups);

        if (profile.getActionForNewRecipients() > 0) {
            loadEnforcedMailinglist(profile, admin, popups, model);
        }

        if (isPossibleToSelectMailinglist(profile)) {
            form.setSelectedMailinglists(prepareSelectedMailinglistsMap(profile));
            model.addAttribute("mailinglists", mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
            model.addAttribute("possibleToSelectMailinglist", true);
        }

        model.addAttribute("parsedContent", parsingResult.getResult());

        return "recipient_import_preview";
    }

    @RequestMapping("/execute.action")
    public Object execute(@ModelAttribute("form") RecipientImportForm form, Admin admin, Popups popups, HttpSession session) throws Exception {
        ProfileImportWorker importWorker;

        if (importWorkersMap.containsKey(session.getId())) {
            importWorker = extractImportWorker(session);
        } else {
            ImportProfile profile = importProfileService.getImportProfileById(form.getProfileId());
            if (!validateDataBeforeExecution(profile, form, admin, popups)) {
                return MESSAGES_VIEW;
            }

            importWorker = createNewProfileImportWorker(form, profile, admin, session);
            writeImportStartLog(admin, form, importWorker.getImportFile().getLocalFile());
            importWorkersMap.put(session.getId(), importWorker);
        }

        Callable<Object> worker = () -> {
            importWorker.call();

            Map<String, Object> attributesMap = new HashMap<>();

            if (importWorker.getError() != null) {
                StrutsPopups.put(attributesMap, ((StrutsPopups) popups));
                addImportErrorsToPopups(importWorker.getError(), popups);

                clearWorkerInSession(session);

                return new ModelAndView(new AgnRedirectView(String.format("/recipient/import/preview.action?profileId=%d", importWorker.getImportProfileId()), true), attributesMap);
            }

            if (importWorker.isWaitingForInteraction()) {
                return "redirect:/recipient/import/errors/edit.action";
            }

            prepareModelAttributesForResultPage(attributesMap, importWorker, admin);
            importWorker.cleanUp();

            return new ModelAndView("recipient_import_result", attributesMap);
        };

        Map<String, Object> attributesMap = new HashMap<>();

        PollingUid pollingUid = new PollingUid(session.getId(), IMPORT_DATA_KEY);
        return new Pollable<>(pollingUid, SHORT_TIMEOUT, new ModelAndView(viewProgress(importWorker, attributesMap, false), attributesMap), worker);
    }

    private void prepareModelAttributesForResultPage(Map<String, Object> attributesMap, ProfileImportWorker worker, Admin admin) {
        attributesMap.put("reportEntries", importReporter.generateImportStatusEntries(worker, worker.getImportProfile().isNoHeaders()));
        attributesMap.put("assignedMailinglists", extractAssignedMailinglists(worker, admin));
        attributesMap.put("mailinglistMessage", detectRecipientsOperationMessageCode(worker.getImportProfile()));
        attributesMap.put("mailinglistAssignStats", worker.getMailinglistAssignStatistics());

        File fileWithValidRecipients = worker.getStatus().getImportedRecipientsCsv();
        if (fileWithValidRecipients != null) {
            attributesMap.put("validRecipientsFile", fileWithValidRecipients.getName());
        }

        File resultFile = worker.getResultFile();
        if (resultFile != null) {
            attributesMap.put("resultFile", resultFile.getName());
        }

        File fileWithInvalidRecipients = worker.getStatus().getInvalidRecipientsCsv();
        if (fileWithInvalidRecipients != null) {
            attributesMap.put("invalidRecipientsFile", fileWithInvalidRecipients.getName());
        }

        File fileWithDuplicatedRecipients = worker.getStatus().getDuplicateInCsvOrDbRecipientsCsv();
        if (fileWithDuplicatedRecipients != null) {
            attributesMap.put("duplicateRecipientsFile", fileWithDuplicatedRecipients.getName());
        }

        File fileWithFixedRecipients = worker.getStatus().getFixedByUserRecipientsCsv();
        if (fileWithFixedRecipients != null) {
            attributesMap.put("fixedRecipientsFile", fileWithFixedRecipients.getName());
        }
    }

    private Set<Mailinglist> extractAssignedMailinglists(ProfileImportWorker worker, Admin admin) {
        Set<Mailinglist> assignedMailingLists = new HashSet<>();
        Map<MediaTypes, Map<Integer, Integer>> mailinglistAssignStatistics = worker.getMailinglistAssignStatistics();

        if (mailinglistAssignStatistics == null) {
            return assignedMailingLists;
        }

        List<Mailinglist> availableMailinglists = mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin);

        for (MediaTypes mediaType : mailinglistAssignStatistics.keySet()) {
            for (Mailinglist mailinglist : availableMailinglists) {
                if (mailinglistAssignStatistics.get(mediaType).containsKey(mailinglist.getId())) {
                    assignedMailingLists.add(mailinglist);
                }
            }
        }

        return assignedMailingLists;
    }

    @GetMapping("/errors/ignore.action")
    public String ignoreErrors(HttpSession session) {
        ProfileImportWorker worker = extractImportWorker(session);

        if (worker != null && worker.isWaitingForInteraction()) {
            worker.ignoreErroneousData();
        }

        return continueImportExecution();
    }

    @RequestMapping("/errors/edit.action")
    public Object editErrors(@ModelAttribute("form") RecipientImportForm form, HttpSession session, Popups popups) {
        Callable<Object> worker = () -> {
            ProfileImportWorker importWorker = extractImportWorker(session);
            Map<String, Object> attributesMap = new HashMap<>();

            try {
                CSVColumnState[] columns = new CSVColumnState[importWorker.getCsvFileHeaders().size()];
                for (int i = 0; i < importWorker.getImportedDataFileColumns().size(); i++) {
                    columns[i] = new CSVColumnState(importWorker.getImportedDataFileColumns().get(i), true, -1);
                }

                attributesMap.put("recipientList", findInvalidRecipients(importWorker, form));
                attributesMap.put("columns", columns);

                return new ModelAndView("recipient_import_edit_error", attributesMap);
            } catch (Exception e) {
                LOGGER.error("Error occurred during retrieving of invalid recipients!", e);
                popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));

                StrutsPopups.put(attributesMap, ((StrutsPopups) popups));
                return new ModelAndView(viewProgress(importWorker, attributesMap, true), attributesMap);
            }
        };

        FormUtils.syncNumberOfRows(webStorage, WebStorage.IMPORT_WIZARD_ERRORS_OVERVIEW, form);

        PollingUid pollingUid = new PollingUid(session.getId(), ERRORS_EDIT_KEY);
        return new Pollable<>(pollingUid, SHORT_TIMEOUT, "recipient_import_loading", worker);
    }

    @PostMapping("/errors/save.action")
    private String saveErrors(HttpSession session, @ModelAttribute ImportErrorsCorrectionsForm form) throws Exception {
        ProfileImportWorker importWorker = extractImportWorker(session);

        if (importWorker == null) {
            LOGGER.error("Can't save fix of import errors!");
            throw new IllegalStateException("Worker not found!");
        }

        Map<String, String> changedValues = form.getErrorsFixes().stream()
                .collect(Collectors.toMap(
                        c -> c.getIndex() + "/" + c.getFieldName(),
                        ImportErrorCorrection::getValue
                ));

        importWorker.setBeansAfterEditOnErrorEditPage(changedValues);

        if (importWorker.hasRepairableErrors()) {
            return "redirect:/recipient/import/errors/edit.action?invalidRecipientsSize=" + form.getInvalidRecipientsSize();
        }

        return continueImportExecution();
    }

    @GetMapping("/cancel.action")
    public String cancel(HttpSession session) {
        ProfileImportWorker worker = extractImportWorker(session);

        if (worker != null && worker.isDone()) {
            cleanUpImportWorker(worker, session);
        }

        return "redirect:/recipient/import/view.action";
    }

    @GetMapping(value = "/download.action", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    FileSystemResource downloadFile(ImportResultFileType importResultFileType, Admin admin, HttpServletResponse response, HttpSession session) throws Exception {
        ProfileImportWorker importWorker = extractImportWorker(session);

        if (importWorker == null) {
            LOGGER.error("Can't download import file!");
            throw new IllegalStateException("Worker not found!");
        }

        File file;
        String action;

        if (importResultFileType.equals(ImportResultFileType.VALID_RECIPIENTS)) {
            file = importWorker.getStatus().getImportedRecipientsCsv();
            action = "import download valid recipients";
        } else if (importResultFileType.equals(ImportResultFileType.INVALID_RECIPIENTS)) {
            file = importWorker.getStatus().getInvalidRecipientsCsv();
            action = "import download invalid recipients";
        } else if (importResultFileType.equals(ImportResultFileType.FIXED_BY_HAND_RECIPIENTS)) {
            file = importWorker.getStatus().getFixedByUserRecipientsCsv();
            action = "import download fixed by hand recipients";
        } else if (importResultFileType.equals(ImportResultFileType.DUPLICATED_RECIPIENTS)) {
            file = importWorker.getStatus().getDuplicateInCsvOrDbRecipientsCsv();
            action = "import download duplicate recipient";
        } else if (importResultFileType.equals(ImportResultFileType.RESULT)) {
            file = importWorker.getResultFile();
            action = "import download result";
        } else {
            LOGGER.error("Can't detect type of import file for downloading!");
            throw new IllegalStateException("Invalid type of file.");
        }

        String actionDescription = String.format(
                "ImportProfile ID: %d, DataSource ID: %d",
                importWorker.getImportProfileId(),
                importWorker.getDatasourceId()
        );

        writeUserActivityLog(action, actionDescription, admin);

        HttpUtils.setDownloadFilenameHeader(response, file.getName());
        return new FileSystemResource(file);
    }

    @GetMapping("/finish.action")
    public String finish(HttpSession session) {
        ProfileImportWorker importWorker = extractImportWorker(session);

        if (importWorker == null) {
            LOGGER.error("Import can't be finished!");
            throw new IllegalStateException("Worker not found!");
        }

        clearWorkerInSession(session);
        int datasourceId = importWorker.getDatasourceId();

        return String.format("redirect:/recipient/list.action?latestDataSourceId=%d", datasourceId);
    }

    @PostMapping("/file/delete.action")
    public @ResponseBody BooleanResponseDto deleteFile(HttpSession session) throws IOException {
        FileDto fileDto = getUploadedFileFromSession(session);

        if (fileDto != null) {
            session.removeAttribute(RECIPIENT_IMPORT_FILE_ATTRIBUTE_NAME);
            Files.deleteIfExists(fileDto.toFile().toPath());
        }

        return new BooleanResponseDto(true);
    }

    private String continueImportExecution() {
        return "redirect:/recipient/import/execute.action";
    }

    private String viewProgress(ProfileImportWorker worker, Map<String, Object> attributesMap, boolean errorOccurred) {
        if (errorOccurred) {
            attributesMap.put("errorOccurred", true);
            attributesMap.put("profileId", worker.getImportProfileId());
        } else {
            attributesMap.put("stepsLength", ImportProgressSteps.values().length);
            attributesMap.put("completedPercent", worker.getCompletedPercent());
            attributesMap.put("currentProgressStatus", worker.getCurrentProgressStatus());
        }

        return "recipient_import_progress";
    }

    private PaginatedListImpl<Map<String, Object>> findInvalidRecipients(ProfileImportWorker worker, RecipientImportForm form) throws Exception {
        return importRecipientsDao.getInvalidRecipientList(
                worker.getTemporaryErrorTableName(),
                worker.getImportedDataFileColumns(),
                form.getSort(),
                form.getOrder(),
                form.getPage(),
                form.getNumberOfRows(),
                form.getInvalidRecipientsSize()
        );
    }

    private boolean validateDataBeforeExecution(ImportProfile profile, RecipientImportForm form, Admin admin, Popups popups) {
        if (profile == null) {
            popups.alert("error.import.no_profile_exists");
            return false;
        }

        // Check for right to import without assigning to mailinglists
        List<Integer> assignedLists = getSelectedMailinglists(form.getSelectedMailinglist());
        if (assignedLists.isEmpty() && !admin.permissionAllowed(Permission.IMPORT_WITHOUT_MAILINGLIST) && !profile.isMailinglistsAll()) {
            int importMode = profile.getImportMode();

            if ((importMode == ImportMode.ADD.getIntValue() || importMode == ImportMode.ADD_AND_UPDATE.getIntValue())) {
                popups.alert("error.import.no_mailinglist");
                return false;
            }
        }

        return true;
    }

    private String detectRecipientsOperationMessageCode(ImportProfile profile) {
        int importMode = profile.getImportMode();
        if (importMode == ImportMode.ADD.getIntValue() || importMode == ImportMode.ADD_AND_UPDATE.getIntValue()
                || importMode == ImportMode.UPDATE.getIntValue()) {
            return "import.result.subscribersAdded";
        }

        if (importMode == ImportMode.MARK_OPT_OUT.getIntValue() || importMode == ImportMode.TO_BLACKLIST.getIntValue()) {
            return "import.result.subscribersUnsubscribed";
        }

        if (importMode == ImportMode.MARK_BOUNCED.getIntValue()) {
            return "import.result.subscribersBounced";
        }

        if (importMode == ImportMode.MARK_SUSPENDED.getIntValue()) {
            return "import.result.subscribersSuspended";
        }

        if (importMode == ImportMode.REACTIVATE_BOUNCED.getIntValue()) {
            return "import.result.bouncedSubscribersReactivated";
        }

        if (importMode == ImportMode.REACTIVATE_SUSPENDED.getIntValue()) {
            return "import.result.subscribersReactivated";
        }

        return "import.result.subscribersAdded";
    }

    private void addImportErrorsToPopups(Exception importException, Popups popups) {
        if (importException instanceof ImportException) {
            popups.alert(((ImportException) importException).getErrorMessageKey(), ((ImportException) importException).getAdditionalErrorData());
        } else if (importException.getCause() instanceof ImportException) {
            ImportException exception = (ImportException) importException.getCause();
            popups.alert(exception.getErrorMessageKey(), exception.getAdditionalErrorData());
        } else {
            popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
        }
    }

    private boolean isWorkerExists(HttpSession session) {
        return extractImportWorker(session) != null;
    }

    private ProfileImportWorker extractImportWorker(HttpSession session) {
        return importWorkersMap.get(session.getId());
    }

    private void cleanUpImportWorker(ProfileImportWorker worker, HttpSession session) {
        worker.cleanUp();
        clearWorkerInSession(session);
    }

    private void clearFinishedWorker(HttpSession session) {
        ProfileImportWorker worker = extractImportWorker(session);

        if (worker != null && !worker.isWaitingForInteraction() && worker.isDone()) {
            worker.cleanUp();
            clearWorkerInSession(session);
        }
    }

    private void clearWorkerInSession(HttpSession session) {
        importWorkersMap.remove(session.getId());
    }

    private void writeImportStartLog(Admin admin, RecipientImportForm form, File importFile) {
        try {
            ImportProfile profile = importProfileService.getImportProfileById(form.getProfileId());

            String description = String.format(
                    "Import started at: %s. File name: %s, mailing list(s): %sused profile: %s.",
                    new SimpleDateFormat("yyyy/MM/dd HH-mm-ss").format(Calendar.getInstance().getTime()),
                    importFile != null ? importFile.getName() : "not set",
                    getSelectedMailinglistsNamesAsStr(form, admin),
                    profile.getName()
            );

            writeUserActivityLog("import from file", description, admin);
        } catch (Exception e) {
            LOGGER.error(format("import recipients{0}", e.getMessage()), e);
        }
    }

    private String getSelectedMailinglistsNamesAsStr(RecipientImportForm form, Admin admin) {
        StringBuilder builder = new StringBuilder();

        List<Mailinglist> availableMailinglists = mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin);
        List<Integer> selectedMailinglists = getSelectedMailinglists(form.getSelectedMailinglist());

        for (int selectedId : selectedMailinglists) {
            for (Mailinglist list : availableMailinglists) {
                if (list.getId() == selectedId) {
                    builder.append(list.getShortname());
                    builder.append(", ");
                }
            }
        }

        return builder.toString();
    }

    private ProfileImportWorker createNewProfileImportWorker(RecipientImportForm form, ImportProfile profile, Admin admin, HttpSession session) throws Exception {
        FileDto fileDto = getImportFile(form, admin, session);

        DatasourceDescription dsDescription = new DatasourceDescriptionImpl();
        dsDescription.setId(0);
        dsDescription.setCompanyID(admin.getCompanyID());
        dsDescription.setSourceGroupType(SourceGroupType.File);
        dsDescription.setCreationDate(new Date());
        dsDescription.setDescription(fileDto.getName());
        dsDescription.setDescription2("EMM-Import (ProfileImport)");
        datasourceDescriptionDao.save(dsDescription);

        ImportStatusImpl importStatus = new ImportStatusImpl();
        importStatus.setDatasourceID(dsDescription.getId());
        importStatus.setCharset(Charset.getCharsetById(profile.getCharset()).getCharsetName());
        importStatus.setSeparator(Separator.getSeparatorById(profile.getSeparator()).getValueChar());
        importStatus.setMode(profile.getImportMode());
        importStatus.setDoubleCheck(profile.getCheckForDuplicates());
        importStatus.setIgnoreNull(profile.getNullValuesAction());
        importStatus.setDelimiter(TextRecognitionChar.getTextRecognitionCharById(profile.getTextRecognitionChar()).getValueString());
        importStatus.setKeycolumn(StringUtils.join(profile.getKeyColumns(), ", "));

        List<Integer> selectedMailinglists = getSelectedMailinglists(form.getSelectedMailinglist());

        return profileImportWorkerFactory.getProfileImportWorker(
                true,
                selectedMailinglists,
                session.getId(),
                admin,
                dsDescription.getId(),
                profile,
                new RemoteFile(fileDto.getName(), fileDto.toFile(), -1),
                importStatus
        );
    }

    private List<Integer> getSelectedMailinglists(Map<Integer, String> mailinglistsMap) {
        return mailinglistsMap.entrySet().stream()
                .filter(e -> "true".equals(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private ServiceResult<List<List<String>>> parseImportFileContent(File importFile, ImportProfile profile) throws Exception {
        RecipientImportFileContentParser contentParser = contentParserFactory.detectParser(profile);
        return contentParser.parse(importFile, profile);
    }

    private void loadEnforcedMailinglist(ImportProfile profile, Admin admin, Popups popups, Model model) {
        List<Integer> mailinglistIDs = emmActionService.getReferencedMailinglistsFromAction(profile.getCompanyId(), profile.getActionForNewRecipients());
        if (mailinglistIDs.size() == 1) {
            Mailinglist mailinglist = mailinglistService.getMailinglist(mailinglistIDs.get(0), admin.getCompanyID());
            popups.warning(
                    "import.boundToMailinglist",
                    "\"" + mailinglist.getShortname() + "\" (ID: " + mailinglist.getId() + ")"
            );
            model.addAttribute("enforcedMailinglist", mailinglist);
        } else if (mailinglistIDs.size() > 1) {
            popups.alert("error.import.mailinglists.one");
        }
    }

    private boolean isFileValid(File importFile, ImportProfile profile, Popups popups) throws Exception {
        RecipientImportFileValidator fileValidator = importFileValidatorFactory.detectValidator(profile);
        SimpleServiceResult validationResult = fileValidator.validate(profile, importFile);

        popups.addPopups(validationResult);
        return validationResult.isSuccess();
    }

    private boolean isPossibleToSelectMailinglist(ImportProfile profile) {
        return profile.getImportMode() != ImportMode.TO_BLACKLIST.getIntValue()
                && profile.getImportMode() != ImportMode.BLACKLIST_EXCLUSIVE.getIntValue()
                && !profile.isMailinglistsAll();
    }

    private boolean validateImportProfile(ImportProfile profile, Popups popups) {
        if (profile.isAutoMapping() && profile.isNoHeaders()) {
            popups.alert("error.import.automapping.missing.header");
            return false;
        }

        if (profile.getColumnMapping().isEmpty() && !profile.isAutoMapping()) {
            popups.alert("error.import.no_columns_maped");
            return false;
        }

        if (!hasMappingsForKeyColumns(profile)) {
            popups.alert("error.import.keycolumn_not_imported");
            return false;
        }

        return true;
    }

    protected boolean isImportFileExists(RecipientImportForm form, HttpSession session) {
        return isImportFileStored(session) ||
                (form.getUploadFile() != null && !StringUtils.isEmpty(form.getUploadFile().getOriginalFilename()));
    }

    private Map<Integer, String> prepareSelectedMailinglistsMap(ImportProfile profile) {
        return profile.getMailinglistIds().stream()
                .collect(Collectors.toMap(Function.identity(), m -> "true"));
    }

    private boolean hasMappingsForKeyColumns(ImportProfile profile) {
        if (profile.isAutoMapping()) {
            return true;
        }

        List<String> mappedDbColumns = profile.getColumnMapping()
                .stream()
                .map(ColumnMapping::getDatabaseColumn)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        return profile.getKeyColumns().stream()
                .allMatch(kc -> mappedDbColumns.contains(kc.toLowerCase()));
    }

    private void checkIfProfileKeyColumnIndexed(ImportProfile profile, Popups popups) {
        if (importProfileService.isKeyColumnsIndexed(profile)) {
            return;
        }

        if (recipientService.hasBeenReachedLimitOnNonIndexedImport(profile.getCompanyId())) {
            popups.alert("error.import.keyColumn.index");
        } else {
            popups.warning("warning.import.keyColumn.index");
        }
    }

    protected FileDto getImportFile(RecipientImportForm form, Admin admin, HttpSession session) throws IOException {
        FileDto uploadedFile = getUploadedFileFromSession(session);

        if (uploadedFile == null) {
            File importFile = ImportUtils.createTempImportFile(form.getUploadFile(), admin);
            uploadedFile = new LocalFileDto(importFile.getAbsolutePath(), form.getUploadFile().getOriginalFilename());
            session.setAttribute(RECIPIENT_IMPORT_FILE_ATTRIBUTE_NAME, uploadedFile);
        }

        return uploadedFile;
    }

    private String getImportFileNameFromSession(HttpSession session) {
        if (!isImportFileStored(session)) {
            return null;
        }

        FileDto fileDto = getUploadedFileFromSession(session);
        return fileDto.getName();
    }

    private FileDto getUploadedFileFromSession(HttpSession session) {
        return ((FileDto) session.getAttribute(RECIPIENT_IMPORT_FILE_ATTRIBUTE_NAME));
    }

    private boolean isImportFileStored(HttpSession session) {
        return session.getAttribute(RECIPIENT_IMPORT_FILE_ATTRIBUTE_NAME) instanceof FileDto;
    }

    protected void prepareModelAttributesForViewStartPage(Admin admin, Model model) {
        model.addAttribute("importProfiles", findImportProfiles(admin));
    }

    private List<ImportProfile> findImportProfiles(Admin admin) {
        return importProfileService.getImportProfilesByCompanyId(admin.getCompanyID());
    }

    private void writeUserActivityLog(String action, String description, Admin admin) {
        UserActivityUtil.log(userActivityLogService, admin, action, description, LOGGER);
    }
}
