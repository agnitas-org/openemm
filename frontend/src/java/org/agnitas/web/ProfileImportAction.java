/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import static com.agnitas.emm.core.workflow.service.util.WorkflowUtils.updateForwardParameters;
import static org.agnitas.web.forms.StrutsFormBase.DEFAULT_REFRESH_MILLIS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.DatasourceDescription;
import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.ImportStatus;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.ProfileRecipientFields;
import org.agnitas.beans.impl.DatasourceDescriptionImpl;
import org.agnitas.beans.impl.ImportStatusImpl;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.dao.SourceGroupType;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.ImportException;
import org.agnitas.service.ImportProfileService;
import org.agnitas.service.ProfileImportCsvPreviewLoader;
import org.agnitas.service.ProfileImportErrorRecipientQueryWorker;
import org.agnitas.service.ProfileImportWorker;
import org.agnitas.service.ProfileImportWorkerFactory;
import org.agnitas.service.WebStorage;
import org.agnitas.service.impl.CSVColumnState;
import org.agnitas.service.impl.ImportWizardContentParseException;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.ImportUtils;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.DateFormat;
import org.agnitas.util.importvalues.ImportMode;
import org.agnitas.util.importvalues.Separator;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.MediaType;

import com.agnitas.beans.Admin;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.recipientsreport.service.impl.RecipientReportUtils;
import com.agnitas.messages.I18nString;
import com.agnitas.util.FutureHolderMap;
import com.agnitas.web.forms.ComNewImportWizardForm;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Profileimport Action
 * 
 * The profileImportWorker is stored in session data because it must be restarted after interactive error corrections made by user.
 * Storage in futureholder would clean up all data kept by the profileImportWorker after some done-timeout.
 */
public class ProfileImportAction extends ImportBaseFileAction {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ProfileImportAction.class);
	
	public static final String PROFILEIMPORTWORKER_SESSIONKEY = "PROFILEIMPORT_WORKER";
	
	private static final String FORWARDKEY_PROGRESS = "progress";
	private static final String FORWARDKEY_ERROREDIT = "error_edit";

	public static final String FUTURE_TASK1 = "IMPORT_RECIPIENT_LIST";
	public static final String FUTURE_TASK2 = "IMPORT_RECIPIENT_PROCESS";

	public static final int ACTION_START = 1;
	public static final int ACTION_PREVIEW = 2;
	public static final int ACTION_PROCEED = 3;
	public static final int ACTION_ERROR_EDIT = 4;

	public static final int ACTION_IGNORE_ERRORS = 5;
	public static final int ACTION_RESULT_PAGE = 6;
	public static final int ACTION_DOWNLOAD_CSV_FILE = 7;
	public static final int ACTION_MLISTS_SAVE = 8;
    public static final int ACTION_CANCEL = 9;
    public static final int ACTION_INIT = 10;

	public static final int RECIPIENT_TYPE_VALID = 1;
	public static final int RECIPIENT_TYPE_FIELD_INVALID = 2;
	public static final int RECIPIENT_TYPE_INVALID = 3;
	public static final int RECIPIENT_TYPE_FIXED_BY_HAND = 4;
	public static final int RECIPIENT_TYPE_DUPLICATE_RECIPIENT = 5;
	public static final int RECIPIENT_TYPE_DUPLICATE_IN_NEW_DATA_RECIPIENT = 6;
	public static final int RESULT_TYPE = 7;

	private MailinglistService mailinglistService;
	
	private ComRecipientDao recipientDao;

	private ImportRecipientsDao importRecipientsDao;

	private EmmActionService emmActionService;

	private ImportProfileService importProfileService;

	private DatasourceDescriptionDao datasourceDescriptionDao;

	private ExecutorService workerExecutorService;

	private FutureHolderMap futureHolder;

	private ProfileImportReporter profileImportReporter;

	private WebStorage webStorage;
	
    private MailinglistApprovalService mailinglistApprovalService;
	
	private ProfileImportWorkerFactory profileImportWorkerFactory;
	  
    @Required
    public final void setMailinglistApprovalService(final MailinglistApprovalService service) {
    	this.mailinglistApprovalService = Objects.requireNonNull(service, "Mailinglist approval service is null");
    }

	@Required
	public void setMailinglistService(MailinglistService mailinglistService) {
		this.mailinglistService = mailinglistService;
	}

	@Required
	public void setRecipientDao(ComRecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}

	@Required
	public void setImportRecipientsDao(ImportRecipientsDao importRecipientsDao) {
		this.importRecipientsDao = importRecipientsDao;
	}

	@Required
	public void setEmmActionService(EmmActionService emmActionService) {
		this.emmActionService = emmActionService;
	}

	@Required
	public void setImportProfileService(ImportProfileService importProfileService) {
		this.importProfileService = importProfileService;
	}

	@Required
	public void setDatasourceDescriptionDao(DatasourceDescriptionDao datasourceDescriptionDao) {
		this.datasourceDescriptionDao = datasourceDescriptionDao;
	}

	@Required
	public void setWorkerExecutorService(ExecutorService workerExecutorService) {
		this.workerExecutorService = workerExecutorService;
	}

	@Required
	public void setFutureHolder(FutureHolderMap futureHolder) {
		this.futureHolder = futureHolder;
	}

	@Required
	public void setProfileImportReporter(ProfileImportReporter profileImportReporter) {
		this.profileImportReporter = profileImportReporter;
	}

	@Required
	public void setWebStorage(WebStorage webStorage) {
		this.webStorage = webStorage;
	}

	@Required
	public void setProfileImportWorkerFactory(ProfileImportWorkerFactory profileImportWorkerFactory) {
		this.profileImportWorkerFactory = profileImportWorkerFactory;
	}

	@Override
	public String subActionMethodName(int subAction) {
		switch (subAction) {
		case ProfileImportAction.ACTION_START:
			return "start";
		case ProfileImportAction.ACTION_PREVIEW:
			return "preview";
		case ProfileImportAction.ACTION_PROCEED:
			return "proceed";
		case ProfileImportAction.ACTION_ERROR_EDIT:
			return "error_edit";
		case ProfileImportAction.ACTION_IGNORE_ERRORS:
			return "error_edit";
		case ProfileImportAction.ACTION_DOWNLOAD_CSV_FILE:
			return "download_csv";
		case ProfileImportAction.ACTION_CANCEL:
			return "error_edit";
        case ProfileImportAction.ACTION_INIT:
            return "init";
		default:
			return super.subActionMethodName(subAction);
		}
	}
	
	protected void initUI(final ComNewImportWizardForm aForm, final Admin admin, final ActionMessages errors) {
		// Nothing to do here
	}

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		ActionMessages messages = new ActionMessages();
		ActionMessages errors = new ActionMessages();
		ActionForward destination = null;
		Admin admin = AgnUtils.getAdmin(request);

		ComNewImportWizardForm aForm;
		if (form != null) {
			aForm = (ComNewImportWizardForm) form;
			if (aForm.isError()) {
				aForm.setError(false);
				aForm.setErrorsDuringImport(null);
			}
		} else {
			aForm = new ComNewImportWizardForm();
		}

		if (AgnUtils.parameterNotEmpty(request, "start_proceed")) {
			aForm.setAction(ProfileImportAction.ACTION_PREVIEW);
		}
		if (AgnUtils.parameterNotEmpty(request, "preview_back")) {
			aForm.setAction(ProfileImportAction.ACTION_START);
		}
		if (AgnUtils.parameterNotEmpty(request, "remove_file")) {
			aForm.setCsvFile(null);
		}
		if (AgnUtils.parameterNotEmpty(request, "preview_proceed")) {
			aForm.setAction(ProfileImportAction.ACTION_PROCEED);
		}
		if (request.getParameter("edit_page_save") != null) {
			aForm.setAction(ProfileImportAction.ACTION_ERROR_EDIT);
		}

		initUI(aForm, admin, errors);

		saveErrors(request, errors);

		super.execute(mapping, form, request, response);

		String futureKeyList = FUTURE_TASK1 + "@" + request.getSession(false).getId();

		try {
			ProfileImportWorker profileImportWorker = getSessionsProfileImportWorker(request);
			
			switch (aForm.getAction()) {

			case ACTION_START:
				if (profileImportWorker != null && !profileImportWorker.isWaitingForInteraction() && profileImportWorker.isDone()) {
					// Cleanup leftover ProfileImportWorker, if it is already done
					profileImportWorker.cleanUp();
					clearSessionsProfileImportWorker(request);
					profileImportWorker = null;
				}

				if (profileImportWorker != null) {
					// Leftover ProfileImportWorker is not done, so show its progress
					aForm.setAction(ProfileImportAction.ACTION_PROCEED);
					destination = mapping.findForward(FORWARDKEY_PROGRESS);
					aForm.setCompletedPercent(profileImportWorker.getCompletedPercent());
					aForm.setCurrentProgressStatus(profileImportWorker.getCurrentProgressStatus());
				} else {
					// Prepare view for new Import process
					if (aForm.getListsToAssign() != null) {
						aForm.getListsToAssign().clear();
					}
					
					if (aForm.getDefaultProfileId() == 0) {
						if (request.getAttribute("defaultProfileId") != null) {
							aForm.setDefaultProfileId((int) request.getAttribute("defaultProfileId"));
						} else {
							aForm.setDefaultProfileId(admin.getDefaultImportProfileID());
						}
					}
					aForm.setImportProfiles(getProfileList(admin));
					aForm.setStatus(new ImportStatusImpl());
					aForm.setResultPagePrepared(false);
					destination = mapping.findForward("start");
				}
				break;

            case ACTION_INIT:
                updateForwardParameters(request, true);
                boolean hasAccessToStandardImport = (admin.permissionAllowed(Permission.WIZARD_IMPORT));
                boolean hasAccessToWizardImport = admin.permissionAllowed(Permission.WIZARD_IMPORTCLASSIC);

                if (hasAccessToStandardImport && hasAccessToWizardImport) {
                    destination = mapping.findForward("init");
                } else if (hasAccessToStandardImport && !hasAccessToWizardImport) {
                	destination = new ActionRedirect("/recipient/import/view.action");
                } else if (hasAccessToWizardImport && !hasAccessToStandardImport) {
                	if (!admin.permissionAllowed(Permission.IMPORT_WIZARD_ROLLBACK)) {
						destination = new ActionRedirect("/recipient/import/wizard/step/file.action");
					} else {
						destination = mapping.findForward("wizard");
					}
                }

                break;

			case ACTION_PREVIEW:
				aForm.clearLists();

				if (!aForm.getHasFile() && (aForm.getCsvFile() == null || StringUtils.isEmpty(aForm.getCsvFile().getFileName()))) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.import.no_file"));
					destination = mapping.findForward("start");
					break;
				}
		
				if (aForm.getImportProfiles() == null || aForm.getImportProfiles().isEmpty()) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.import.no_profile"));
					destination = mapping.findForward("start");
					break;
				}
				
				ImportProfile importPreviewProfile = importProfileService.getImportProfileById(aForm.getDefaultProfileId());
				
				if (!ImportUtils.checkIfImportFileHasData(getCurrentFile(request), importPreviewProfile.getZipPassword())) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("autoimport.error.emptyFile", aForm.getCsvFile().getFileName()));
					destination = mapping.findForward("start");
					break;
				}

				request.setAttribute("showImportMailinglistSelection",
						importPreviewProfile.getImportMode() != ImportMode.TO_BLACKLIST.getIntValue()
						&& importPreviewProfile.getImportMode() != ImportMode.BLACKLIST_EXCLUSIVE.getIntValue()
						&& !importPreviewProfile.isMailinglistsAll());
				
				if (importPreviewProfile.getImportMode() == ImportMode.REACTIVATE_BOUNCED.getIntValue()) {
                    messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING,
		                    new ActionMessage(I18nString.getLocaleString("warning.import.mode.bounceractivation", AgnUtils.getLocale(request)), false));
                }
				
				// Override keycolumns for special import plugin
				String importPreviewKeyColumns = (String) request.getSession().getAttribute("IMPORT_KEY_COLUMNS");
				if (StringUtils.isNotBlank(importPreviewKeyColumns)) {
					List<String> keyColumnList = AgnUtils.splitAndTrimStringlist(importPreviewKeyColumns);
					importPreviewProfile.setKeyColumns(keyColumnList);
				}
				
				ProfileImportCsvPreviewLoader profileImportCsvPreviewLoader =
						new ProfileImportCsvPreviewLoader(recipientDao, importRecipientsDao, importPreviewProfile, getCurrentFile(request));
				boolean profileFits;
				try {
					profileImportCsvPreviewLoader.validateImportProfileMatchGivenCSVFile();
					profileFits = true;
				} catch (ImportWizardContentParseException e) {
					aForm.setAction(ProfileImportAction.ACTION_START);
					if (e.getAdditionalErrorData() != null && e.getAdditionalErrorData().length > 0) {
						errors.add("profile", new ActionMessage(e.getErrorMessageKey(), e.getAdditionalErrorData()));
					} else {
						errors.add("profile", new ActionMessage(e.getErrorMessageKey()));
					}
					profileFits = false;
				} catch (ImportException e) {
					errors.add("csvFile", new ActionMessage(e.getErrorMessageKey(), e.getAdditionalErrorData()));
					profileFits = false;
				} catch (Exception e) {
					errors.add("csvFile", new ActionMessage("error.import.exception", e.getMessage()));
					profileFits = false;
				}
				
				boolean keyColumnValid = isProfileKeyColumnValid(importPreviewProfile, errors);

				// log ImportStart
				int importId = new Random().nextInt();
				importPreviewProfile.setImportId(importId);
				if (logger.isInfoEnabled()) {
					logger.info("Import ID: " + importId + " Import Profile ID: "
						+ importPreviewProfile.getId() + " Import Profile Name: "
						+ importPreviewProfile.getName() + " File Name: "
						+ aForm.getCurrentFileName());
				}

				if ((!profileFits || !keyColumnValid) && !errors.isEmpty()) {
					HttpSession session = request.getSession();
					session.setAttribute(ImportProfileAction.IMPORT_PROFILE_ERRORS_KEY, errors);
					session.setAttribute(ImportProfileAction.IMPORT_PROFILE_ID_KEY, aForm.getDefaultProfileId());
					destination = mapping.findForward("profile_edit");
				} else if (!errors.isEmpty()) {
					destination = mapping.findForward("start");
				} else {
					aForm.setPreviewParsedContent(profileImportCsvPreviewLoader.getPreviewParsedContent(errors));
					aForm.setColumns(profileImportCsvPreviewLoader.getColumns());
					if (!errors.isEmpty()) {
						destination = mapping.findForward("start");
						break;
					}

					aForm.setAllMailingLists(getAllMailingLists(admin));
					aForm.setMailinglistAddMessage(profileImportCsvPreviewLoader.createMailinglistAddMessage());
					aForm.getSelectedMailinglists().addAll(importPreviewProfile.getMailinglistIds());
					checkProfileKeyColumnIndexed(messages, errors, importPreviewProfile);

					destination = mapping.findForward("preview");
				}
				
				if (importPreviewProfile.getActionForNewRecipients() > 0) {
					List<Integer> mailinglistIDs = emmActionService.getReferencedMailinglistsFromAction(importPreviewProfile.getCompanyId(), importPreviewProfile.getActionForNewRecipients());
					if (mailinglistIDs.size() == 1) {
						int enforceMailinglistId = mailinglistIDs.get(0);
						Mailinglist enforceMailinglist = mailinglistService.getMailinglist(enforceMailinglistId, admin.getCompanyID());
						messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("import.boundToMailinglist", "\"" + enforceMailinglist.getShortname() + "\" (ID: " + enforceMailinglist.getId() + ")"));
						aForm.setEnforceMailinglist(enforceMailinglist);
					} else if (mailinglistIDs.size() > 1) {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.import.mailinglists.one"));
					}
				}
				break;

			case ACTION_PROCEED:
			case ACTION_IGNORE_ERRORS:
				if (profileImportWorker == null) {
					ImportProfile importProceedProfile = importProfileService.getImportProfileById(aForm.getDefaultProfileId());

					if (importProceedProfile == null) {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.import.no_profile_exists"));
						destination = mapping.findForward("preview");
						break;
					}

					// Override keycolumns for special import plugin
					String importKeyColumns = (String) request.getSession().getAttribute("IMPORT_KEY_COLUMNS");
					if (StringUtils.isNotBlank(importKeyColumns)) {
						List<String> keyColumnList = AgnUtils.splitAndTrimStringlist(importKeyColumns);
						importProceedProfile.setKeyColumns(keyColumnList);
					}

					// Check for right to import without assigning to mailinglists
					List<Integer> assignedLists = getAssignedMailingLists(aForm);
					if (assignedLists.isEmpty() && !admin.permissionAllowed(Permission.IMPORT_WITHOUT_MAILINGLIST) && !importProceedProfile.isMailinglistsAll()) {
						int importMode = importProceedProfile.getImportMode();
						if ((importMode == ImportMode.ADD.getIntValue()
								|| importMode == ImportMode.ADD_AND_UPDATE.getIntValue())) {
							errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.import.no_mailinglist"));
							destination = mapping.findForward("preview");
							break;
						}
					}

					aForm.setCalendarDateFormat(createCalendarDateFormat(importProceedProfile));

					// Create a new ProfileImportWorker
					aForm.setListsToAssign(assignedLists);
					profileImportWorker = createNewProfileImportWorker(aForm, request, importProceedProfile);
					workerExecutorService.submit(profileImportWorker);
					setSessionProfileImportWorker(request, profileImportWorker);
					writeImportLog(admin, aForm);

					String charset = Charset.getCharsetById(importProceedProfile.getCharset()).getCharsetName();
					char separator = Separator.getSeparatorById(importProceedProfile.getSeparator()).getValueChar();
					int mode = importProceedProfile.getImportMode();
					int doublette = importProceedProfile.getCheckForDuplicates();
					int nullValues = importProceedProfile.getNullValuesAction();
					String recognitionChar = TextRecognitionChar.getTextRecognitionCharById(importProceedProfile.getTextRecognitionChar()).getValueString();

					aForm.getStatus().setCharset(charset);
					aForm.getStatus().setSeparator(separator);
					aForm.getStatus().setMode(mode);
					aForm.getStatus().setDoubleCheck(doublette);
					aForm.getStatus().setIgnoreNull(nullValues);
					aForm.getStatus().setDelimiter(recognitionChar);
					aForm.getStatus().setKeycolumn(StringUtils.join(importProceedProfile.getKeyColumns(), ", "));
				}

				if (profileImportWorker != null && profileImportWorker.isDone()) {
					if (aForm.getErrorsDuringImport() != null) {
						errors.add(aForm.getErrorsDuringImport());
						clearSessionsProfileImportWorker(request);
						aForm.setDefaultProfileId(profileImportWorker.getImportProfileId());
						aForm.setRefreshMillis(DEFAULT_REFRESH_MILLIS);
						aForm.setErrorsDuringImport(null);
						destination = mapping.findForward("preview");
					} else if (profileImportWorker.getError() != null) {
						// Check for error messages
						ActionMessage message;
						if (profileImportWorker.getError() instanceof ImportException) {
							message = new ActionMessage(((ImportException) profileImportWorker.getError()).getErrorMessageKey(), ((ImportException) profileImportWorker.getError()).getAdditionalErrorData());
						} else if (profileImportWorker.getError().getCause() instanceof ImportException) {
							ImportException exception = (ImportException) profileImportWorker.getError().getCause();
							message = new ActionMessage(exception.getErrorMessageKey(), exception.getAdditionalErrorData());
						} else {
							message = new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
						}

						errors.add(ActionMessages.GLOBAL_MESSAGE, message);
						clearSessionsProfileImportWorker(request);
						aForm.setDefaultProfileId(profileImportWorker.getImportProfileId());
						aForm.setRefreshMillis(DEFAULT_REFRESH_MILLIS);
						destination = mapping.findForward("preview");
					} else if (profileImportWorker.isWaitingForInteraction()) {
						if (aForm.getAction() == ACTION_IGNORE_ERRORS) {
							// Set all remaining erroneous data to ignore
							profileImportWorker.ignoreErroneousData();

							// Restart ProfileImportWorker to execute the remaining actions
							workerExecutorService.submit(profileImportWorker);
							setSessionProfileImportWorker(request, profileImportWorker);
							aForm.setAction(ProfileImportAction.ACTION_PROCEED);
							destination = mapping.findForward(FORWARDKEY_PROGRESS);
							aForm.setCompletedPercent(profileImportWorker.getCompletedPercent());
							aForm.setCurrentProgressStatus(profileImportWorker.getCurrentProgressStatus());
						} else {
							// Show the remaining erroneous data
							destination = mapping.findForward(FORWARDKEY_ERROREDIT);
						}
					} else {
						clearSessionsProfileImportWorker(request);

						// TODO: Show near limit warning
						profileImportReporter.fillProfileImportForm(profileImportWorker, aForm);
						profileImportWorker.cleanUp();

						destination = mapping.findForward("result_page");
					}
					aForm.setRefreshMillis(DEFAULT_REFRESH_MILLIS);
				} else if (profileImportWorker == null) {
					throw new Exception("Invalid state");
				} else {
					if (aForm.getRefreshMillis() < 1000) {
						// raise the refresh time
						aForm.setRefreshMillis(aForm.getRefreshMillis() + 50);
					}
					aForm.setAction(ProfileImportAction.ACTION_PROCEED);
					destination = mapping.findForward(FORWARDKEY_PROGRESS);
					aForm.setCompletedPercent(profileImportWorker.getCompletedPercent());
					aForm.setCurrentProgressStatus(profileImportWorker.getCurrentProgressStatus());
				}
				
				if(destination != null && "preview".equals(destination.getName())) {
					aForm.getSelectedMailinglists().clear();
					aForm.getSelectedMailinglists().addAll(aForm.getListsToAssign());
				}
				
				break;

			case ACTION_ERROR_EDIT:
				if (profileImportWorker == null) {
					throw new Exception("Unexpected state of process: profileImportWorker is missing");
				}
				
				// Save the corrected data inserted by user
				if (!futureHolder.containsKey(futureKeyList) && request.getSession().getAttribute("recipientsInCurrentTable") != null) {
					final HashMap<String, ProfileRecipientFields> mapRecipientsFromTable = new HashMap<>();
					@SuppressWarnings("unchecked")
					final PaginatedListImpl<Map<String, Object>> recipientsFromTable = (PaginatedListImpl<Map<String, Object>>) request.getSession().getAttribute("recipientsInCurrentTable");
					List<Map<String, Object>> list = recipientsFromTable.getList();
					for (Map<String, Object> dynaBean : list) {
						final ProfileRecipientFields recipient = (ProfileRecipientFields) dynaBean.get(ImportRecipientsDao.ERROR_EDIT_RECIPIENT_EDIT_RESERVED);
						mapRecipientsFromTable.put(recipient.getTemporaryId(), recipient);
					}

					// Get changed recipients
					Map<String, String> changedValues = new HashMap<>();
					Enumeration<String> parameterNames = request.getParameterNames();
					while (parameterNames.hasMoreElements()) {
						String pName = parameterNames.nextElement();
						String paramBeginStr = "changed_recipient_";
						if (pName.startsWith(paramBeginStr)) {
							String changeFieldIdentifier = pName.substring(paramBeginStr.length());
							int csvIndex = Integer.parseInt(changeFieldIdentifier.substring(0, changeFieldIdentifier.indexOf("/RESERVED/")));
							String fieldName = changeFieldIdentifier.substring(changeFieldIdentifier.indexOf("/RESERVED/") + 10);
							String value = request.getParameter(pName);
							changedValues.put(csvIndex + "/" + fieldName, value);
						}
					}

					profileImportWorker.setBeansAfterEditOnErrorEditPage(changedValues);
				}
				
				if (profileImportWorker.hasRepairableErrors()) {
					// Show the remaining erroneous data
					destination = mapping.findForward(FORWARDKEY_ERROREDIT);
				} else {
					// Restart ProfileImportWorker to execute the remaining actions
					workerExecutorService.submit(profileImportWorker);
					setSessionProfileImportWorker(request, profileImportWorker);
					aForm.setAction(ProfileImportAction.ACTION_PROCEED);
					destination = mapping.findForward(FORWARDKEY_PROGRESS);
					aForm.setCompletedPercent(profileImportWorker.getCompletedPercent());
					aForm.setCurrentProgressStatus(profileImportWorker.getCurrentProgressStatus());
				}
				break;

			case ACTION_DOWNLOAD_CSV_FILE:
				File outfile = null;
				String action = "";
				if (aForm.getDownloadFileType() == RECIPIENT_TYPE_VALID) {
					outfile = aForm.getValidRecipientsFile();
					action = "import download valid recipients";
				} else if (aForm.getDownloadFileType() == RECIPIENT_TYPE_INVALID) {
					outfile = aForm.getInvalidRecipientsFile();
					action = "import download invalid recipients";
				} else if (aForm.getDownloadFileType() == RECIPIENT_TYPE_FIXED_BY_HAND) {
					outfile = aForm.getFixedRecipientsFile();
					action = "import download fixed by hand recipients";
				} else if (aForm.getDownloadFileType() == RECIPIENT_TYPE_DUPLICATE_RECIPIENT) {
					outfile = aForm.getDuplicateRecipientsFile();
					action = "import download duplicate recipient";
				} else if (aForm.getDownloadFileType() == RESULT_TYPE) {
					outfile = aForm.getResultFile();
					action = "import download result";
				}
				transferFile(response, errors, outfile);
				writeUserActivityLog(admin, action, "ImportProfile ID: " + aForm.getDefaultProfileId() + ", DataSource ID: " + aForm.getDatasourceId(), logger);
				destination = null;
				break;
				
			case ACTION_CANCEL:
				if (profileImportWorker != null && profileImportWorker.isDone()) {
					// Cleanup leftover ProfileImportWorker, if it is already done
					profileImportWorker.cleanUp();
					clearSessionsProfileImportWorker(request);
					profileImportWorker = null;
				}

				// Prepare view for new Import process
				if (aForm.getListsToAssign() != null) {
					aForm.getListsToAssign().clear();
				}

				aForm.setDefaultProfileId(admin.getDefaultImportProfileID());
				aForm.setImportProfiles(getProfileList(admin));
				aForm.setStatus(new ImportStatusImpl());
				destination = mapping.findForward("start");
				aForm.setResultPagePrepared(false);
				break;
			default:
				throw new Exception("Invalid action");
			}

			if (destination != null && FORWARDKEY_ERROREDIT.equals(destination.getName())) {
				if (profileImportWorker == null) {
					throw new Exception("Unexpected state of process: profileImportWorker is missing");
				}
				
				// Prepare data of erroneous recipients from csv file to be edited by user
				try {
					destination = mapping.findForward("loading");
	
					if (!futureHolder.containsKey(futureKeyList)) {
						// Create a new read worker for erroneous data
						futureHolder.put(futureKeyList, createAndStartImportErrorRecipientQueryWorker(profileImportWorker, request, aForm));
					}
	
					if (futureHolder.get(futureKeyList).isDone()) {
						// Prepare erroneous data to display for correction
						@SuppressWarnings("unchecked")
						Future<PaginatedListImpl<Map<String, Object>>> future = (Future<PaginatedListImpl<Map<String, Object>>>) futureHolder.get(futureKeyList);
						request.setAttribute("recipientList", future.get());
						request.getSession().setAttribute("recipientsInCurrentTable", future.get());
	
						// Columns are needed for display table in jsp
						CSVColumnState[] columns = new CSVColumnState[profileImportWorker.getCsvFileHeaders().size()];
						for (int i = 0; i < profileImportWorker.getImportedDataFileColumns().size(); i++) {
							columns[i] = new CSVColumnState(profileImportWorker.getImportedDataFileColumns().get(i), true, -1);
						}
						aForm.setColumns(columns);
	
						destination = mapping.findForward(FORWARDKEY_ERROREDIT);
						aForm.setAll(future.get().getFullListSize());
						futureHolder.remove(futureKeyList);
						aForm.setRefreshMillis(DEFAULT_REFRESH_MILLIS);
					} else {
						if (aForm.getRefreshMillis() < 1000) {
							// raise the refresh time
							aForm.setRefreshMillis(aForm.getRefreshMillis() + 50);
						}
						aForm.setError(false);
					}
				} catch (Exception e) {
					logger.error("recipientList: " + e.getMessage(), e);
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
					// do not refresh when an error has been occurred
					aForm.setError(true);
					destination = mapping.findForward(FORWARDKEY_PROGRESS);
	
					ImportStatus status = aForm.getStatus();
					status.setFields(0);
					status.setUpdated(0);
					status.setInserted(0);
					status.setAlreadyInDb(0);
				}
			}
		} catch (Exception e) {
			logger.error("execute: " + e.getMessage(), e);
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
		}

		// Report any errors we have discovered back to the original form
		if (!errors.isEmpty() && destination != null) {
			saveErrors(request, errors);
			if (destination.getName().equals(FORWARDKEY_PROGRESS)) {
				aForm.setErrorsDuringImport(errors);
			}
		}

		if (!messages.isEmpty()) {
			saveMessages(request, messages);
		}
		
		return destination;
	}

	/**
	 * Method checks if profile has key column in its column mappings
	 *
	 * @param importProfile selected import profile
	 *
	 * @param errors
	 *            errors to add error to if key column is not imported
	 * @return true if key column is contained in one of column mappings
	 */
	private boolean isProfileKeyColumnValid(ImportProfile importProfile, ActionMessages errors) {
		if (importProfile.isAutoMapping())  {
			return true;
		} else {
			List<ColumnMapping> mapping = importProfile.getColumnMapping();
			List<String> mappedDbColumns = mapping.stream()
					.map(ColumnMapping::getDatabaseColumn)
					.filter(Objects::nonNull)
					.map(String::toLowerCase)
					.collect(Collectors.toList());
			
			for (String keyColumn : importProfile.getKeyColumns()) {
				if (!mappedDbColumns.contains(keyColumn.toLowerCase())) {
					errors.add("profile", new ActionMessage("error.import.keycolumn_not_imported"));
					return false;
				}
			}
		
			return true;
		}
	}

	/**
	 * Method checks if all the key columns are indexed in database
	 *
	 * @param messages
	 *            messages to add warning to if key column is not indexed
	 *
	 * @param importProfile
	 *             selected import profile
	 */
	private void checkProfileKeyColumnIndexed(ActionMessages messages, ActionMessages errors, ImportProfile importProfile) {
		List<String> columnsToCheck = importProfile.getKeyColumns();
		if (CollectionUtils.isNotEmpty(columnsToCheck)) {
			if (!importRecipientsDao.isKeyColumnIndexed(importProfile.getCompanyId(), columnsToCheck)) {
				int unindexedLimit = configService.getIntegerValue(ConfigValue.MaximumContentLinesForUnindexedImport, importProfile.getCompanyId());
				if (unindexedLimit >= 0 && importRecipientsDao.getResultEntriesCount("SELECT COUNT(*) FROM customer_" + importProfile.getCompanyId() + "_tbl") > unindexedLimit) {
					errors.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("error.import.keyColumn.index"));
				} else {
					messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.import.keyColumn.index"));
				}
			}
		}
	}

	/**
	 * Method creates date format for error-edit-page calendar using import profile date format
	 * @throws Exception
	 */
	private String createCalendarDateFormat(ImportProfile importProfile) throws Exception {
		int dateFormat = importProfile.getDateFormat();
		String csvFormat = DateFormat.getDateFormatById(dateFormat).getValue();
		csvFormat = csvFormat.replace("yyyy", "%Y");
		csvFormat = csvFormat.replace("MM", "%m");
		csvFormat = csvFormat.replace("dd", "%d");
		csvFormat = csvFormat.replace("HH", "%H");
		csvFormat = csvFormat.replace("mm", "%M");
		csvFormat = csvFormat.replace("ss", "%S");
		return csvFormat;
	}

	/**
	 * Gets all mailing lists for current company id
	 *
	 * @param admin current user
	 * @return all mailing lists for current company id
	 */
	private List<Mailinglist> getAllMailingLists(Admin admin) {
		return mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin);
	}

	/**
	 * Gets list of mailing lists ids that were assigned on
	 * assign-mailinglist-page; takes data from request
	 *
	 * @param aForm
	 *            form
	 * @return ids of assigned mailing lists
	 */
	private List<Integer> getAssignedMailingLists(ComNewImportWizardForm aForm) {
		List<Integer> mailingLists = new ArrayList<>(aForm.getSelectedMailinglists());
		
		if (mailingLists.isEmpty()) {
			List<Integer> listsToAssign = aForm.getListsToAssign();
			if (listsToAssign != null) {
				return listsToAssign;
			}
		}
		return mailingLists;
	}

	/**
	 * Method transfers given file to action response (for user to download)
	 *
	 * @param response
	 *            action response
	 * @param errors
	 *            errors
	 * @param outfile
	 *            file to transfer
	 * @throws IOException
	 *             exceptions that can occur while working with IO
	 */
	private void transferFile(HttpServletResponse response, ActionMessages errors, File outfile) throws IOException {
		if (outfile != null) {
			byte bytes[] = new byte[16384];
			int len;

			try (FileInputStream instream = new FileInputStream(outfile)) {
				if (outfile.getName().endsWith(".zip")) {
					response.setContentType("application/zip");
				} else if (outfile.getName().endsWith(RecipientReportUtils.TXT_EXTENSION)) {
					response.setContentType(MediaType.TEXT_PLAIN_VALUE);
				}
	            HttpUtils.setDownloadFilenameHeader(response, outfile.getName());
				response.setContentLength((int) outfile.length());

				// Do not close this stream, it's managed by the servlet
				// container
				try (ServletOutputStream ostream = response.getOutputStream()) {
					while ((len = instream.read(bytes)) != -1) {
						ostream.write(bytes, 0, len);
					}
				}
			}
		} else {
			errors.add("global", new ActionMessage("error.export.file_not_ready"));
		}
	}

	/**
	 * @param admin current user
	 * @return list of import profiles for overview page with current company id
	 */
	private List<ImportProfile> getProfileList(Admin admin) {
		return importProfileService.getImportProfilesByCompanyId(admin.getCompanyID());
	}

	/**
	 * Get a list of recipients according to your validation
	 */
	private Future<PaginatedListImpl<Map<String, Object>>> createAndStartImportErrorRecipientQueryWorker(ProfileImportWorker profileImportWorker, HttpServletRequest request, ComNewImportWizardForm aForm) throws Exception {
		String temporaryErrorTableName = profileImportWorker.getTemporaryErrorTableName();
		String sort = getSort(request, aForm);
		String direction = request.getParameter("dir");

		FormUtils.syncNumberOfRows(webStorage, WebStorage.IMPORT_WIZARD_ERRORS_OVERVIEW, aForm);

		int rownums = aForm.getNumberOfRows();
		if (direction == null) {
			direction = aForm.getOrder();
		} else {
			aForm.setOrder(direction);
		}

		String pageStr = request.getParameter("page");
		if (pageStr == null || "".equals(pageStr.trim())) {
			if (aForm.getPage() == null || "".equals(aForm.getPage().trim())) {
				aForm.setPage("1");
			}
			pageStr = aForm.getPage();
		} else {
			aForm.setPage(pageStr);
		}

		if (aForm.isNumberOfRowsChanged()) {
			aForm.setPage("1");
			aForm.setNumberOfRowsChanged(false);
			pageStr = "1";
		}
		
		Future<PaginatedListImpl<Map<String, Object>>> future = workerExecutorService.submit(
			new ProfileImportErrorRecipientQueryWorker(importRecipientsDao, temporaryErrorTableName, sort, direction, Integer.parseInt(pageStr), rownums, aForm.getAll(), profileImportWorker.getImportedDataFileColumns()));
		return future;
	}

	private ProfileImportWorker createNewProfileImportWorker(ComNewImportWizardForm aForm, HttpServletRequest request, ImportProfile importProfile) throws Exception {
		Admin admin = AgnUtils.getAdmin(request);

		// set datasource id
		DatasourceDescription dsDescription = new DatasourceDescriptionImpl();
		dsDescription.setId(0);
		dsDescription.setCompanyID(admin.getCompanyID());
		dsDescription.setSourceGroupType(SourceGroupType.File);
		dsDescription.setCreationDate(new Date());
		dsDescription.setDescription((String) request.getSession().getAttribute(CSV_ORIGINAL_FILE_NAME_KEY));
		dsDescription.setDescription2("EMM-Import (ProfileImport)");
		datasourceDescriptionDao.save(dsDescription);

		aForm.getStatus().setDatasourceID(dsDescription.getId());
		aForm.setDatasourceId(dsDescription.getId());

		ProfileImportWorker profileImportWorker = profileImportWorkerFactory.getProfileImportWorker(
			true,
			aForm.getListsToAssign(),
			request.getSession(false).getId(),
			admin,
			dsDescription.getId(),
			importProfile,
			new RemoteFile((String) request.getSession().getAttribute(CSV_ORIGINAL_FILE_NAME_KEY), getCurrentFile(request), -1),
			aForm.getStatus());
		
		return profileImportWorker;
	}

	/**
	 * Initialize the list which keeps the current width of the columns, with a
	 * default value of '-1' A JavaScript in the corresponding jsp will set the
	 * style.width of the column.
	 *
	 * @param size
	 *            number of columns
	 * @return
	 */
	@Override
	protected List<String> getInitializedColumnWidthList(int size) {
		List<String> columnWidthList = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			columnWidthList.add("-1");
		}
		return columnWidthList;
	}

	protected String getSort(HttpServletRequest request, ComNewImportWizardForm aForm) {
		String sort = request.getParameter("sort");
		if (sort == null) {
			sort = aForm.getSort();
		} else {
			aForm.setSort(sort);
		}
		return sort;
	}

	private void writeImportLog(Admin admin, ComNewImportWizardForm aForm) {
		try {
			List<Mailinglist> allMailingLists = aForm.getAllMailingLists();
			List<Integer> listsToAssign = aForm.getListsToAssign();
			StringBuilder assignedMailingLists = new StringBuilder();

			for (Integer assignedList : listsToAssign) {

				for (Mailinglist list : allMailingLists) {

					if (list.getId() == assignedList) {
						assignedMailingLists.append(list.getShortname());
						assignedMailingLists.append(", ");
					}
				}
			}

			int profileId = aForm.getDefaultProfileId();
			ImportProfile importProfileById = importProfileService.getImportProfileById(profileId);

			String fileName = aForm.getCsvFile() != null ? aForm.getCsvFile().getFileName() : "not set";

			StringBuilder description = new StringBuilder();
			description.append("Import started at: ");
			description.append(new SimpleDateFormat("yyyy/MM/dd HH-mm-ss").format(Calendar.getInstance().getTime()));
			description.append(". File name: ");
			description.append(fileName);
			description.append(", mailing list(s): ");
			description.append(assignedMailingLists);
			description.append("used profile: ");
			description.append(importProfileById.getName());
			description.append(".");

			writeUserActivityLog(admin, "import from file", description.toString());
		} catch (Exception e) {
			logger.error("import recipients" + e.getMessage(), e);
		}
	}
	
	private void setSessionProfileImportWorker(HttpServletRequest request, ProfileImportWorker profileImportWorker) {
		request.getSession().setAttribute(PROFILEIMPORTWORKER_SESSIONKEY, profileImportWorker);
	}

	private ProfileImportWorker getSessionsProfileImportWorker(HttpServletRequest request) throws Exception {
		return (ProfileImportWorker) request.getSession().getAttribute(PROFILEIMPORTWORKER_SESSIONKEY);
	}

	private void clearSessionsProfileImportWorker(HttpServletRequest request) throws Exception {
		request.getSession().removeAttribute(PROFILEIMPORTWORKER_SESSIONKEY);
	}
}
