/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.DatasourceDescription;
import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.ImportStatus;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.ColumnMappingImpl;
import org.agnitas.beans.impl.DatasourceDescriptionImpl;
import org.agnitas.beans.impl.ImportProfileImpl;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.ImportException;
import org.agnitas.service.ImportWizardService;
import org.agnitas.service.ProfileImportWorker;
import org.agnitas.service.ProfileImportWorkerFactory;
import org.agnitas.service.impl.ImportWizardContentParseException;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.CsvColInfo;
import org.agnitas.util.CsvDataInvalidItemCountException;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.EmmCalendar;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.ImportUtils;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.agnitas.util.SafeString;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.CheckForDuplicates;
import org.agnitas.util.importvalues.DateFormat;
import org.agnitas.util.importvalues.ImportMode;
import org.agnitas.util.importvalues.MailType;
import org.agnitas.util.importvalues.Separator;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.agnitas.web.StrutsActionBase;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComDatasourceDescriptionDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mailinglist.service.ComMailinglistService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.recipientsreport.service.RecipientsReportService;
import com.agnitas.emm.core.upload.bean.UploadData;
import com.agnitas.emm.core.upload.dao.ComUploadDao;
import com.agnitas.messages.I18nString;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Classic Import
 */
public final class ComImportWizardAction extends StrutsActionBase {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComImportWizardAction.class);
	
	/**
	 * Constant for Action List
	 */
	public static final int ACTION_START = 1;

	public static final int ACTION_CSV = 2;

	public static final int ACTION_PARSE = 3;

	public static final int ACTION_MODE = 4;

	public static final int ACTION_PRESCAN = 5;

	public static final int ACTION_MLISTS = 6;

	public static final int ACTION_WRITE = 7;

	public static final int ACTION_PREVIEW_SCROLL = 8;

	public static final int ACTION_VIEW_STATUS = 9;

	public static final int ACTION_VIEW_STATUS_WINDOW = 10;

	public static final int ACTION_GET_ERROR_DATE = 11;

	public static final int ACTION_GET_ERROR_EMAIL = 12;

	public static final int ACTION_GET_ERROR_EMAILDOUBLE = 13;

	public static final int ACTION_GET_ERROR_GENDER = 14;

	public static final int ACTION_GET_ERROR_MAILTYPE = 15;

	public static final int ACTION_GET_ERROR_NUMERIC = 16;

	public static final int ACTION_GET_ERROR_STRUCTURE = 17;

	public static final int ACTION_GET_DATA_PARSED = 18;

	public static final int ACTION_GET_ERROR_BLACKLIST = 19;

	public static final int ACTION_CHECK_FIELDS = 20;

    public static final int ACTION_VERIFY_MISSING_FIELDS = 21;
	
	public static final String CLASSIC_IMPORT_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "RecipientImport_Classic";

    private static final String FUTURE_TASK = "IMPORT_WRITE_CONTENT";
    
    private static final String IMPORT_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "RecipientImport";
        
	protected DataSource dataSource;
	protected ConfigService configService;
	protected ComRecipientDao recipientDao;
	protected ComDatasourceDescriptionDao datasourceDescriptionDao;
	protected ImportWizardService importWizardService;
	protected RecipientsReportService reportService;
    private MailinglistApprovalService mailinglistApprovalService;
    
    private Map<String, Future<? extends Object>> futureHolder;
    private ExecutorService workerExecutorService;
	private ComMailinglistService mailinglistService;
    private ComUploadDao uploadDao;
	private ProfileImportWorkerFactory profileImportWorkerFactory;

	@Required
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	   
    @Required
    public final void setMailinglistApprovalService(final MailinglistApprovalService service) {
    	this.mailinglistApprovalService = Objects.requireNonNull(service, "Mailinglist approval service is null");
    }

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setRecipientDao(ComRecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}

	@Required
	public void setDatasourceDescriptionDao(ComDatasourceDescriptionDao datasourceDescriptionDao) {
		this.datasourceDescriptionDao = datasourceDescriptionDao;
	}

	@Required
	public void setImportWizardService(ImportWizardService importWizardService) {
		this.importWizardService = importWizardService;
	}

	@Required
	public void setReportService(RecipientsReportService reportService) {
		this.reportService = reportService;
	}

	@Required
	public void setMailinglistService(ComMailinglistService mailinglistService) {
		this.mailinglistService = mailinglistService;
	}

	@Required
	public void setUploadDao(ComUploadDao uploadDao) {
		this.uploadDao = uploadDao;
	}

	@Required
    public void setFutureHolder(Map<String, Future<? extends Object>> futureHolder) {
        this.futureHolder = futureHolder;
    }

	@Required
    public void setWorkerExecutorService(ExecutorService workerExecutorService) {
        this.workerExecutorService = workerExecutorService;
    }

	@Required
	public void setProfileImportWorkerFactory(ProfileImportWorkerFactory profileImportWorkerFactory) {
		this.profileImportWorkerFactory = profileImportWorkerFactory;
	}

    @Override
    public String subActionMethodName(int subAction) {
        switch (subAction) {
        case ACTION_START:
            return "start";
        case ACTION_MODE:
            return "mode";
        case ACTION_CSV:
            return "csv";
        case ACTION_CHECK_FIELDS:
            return "check_fields";
        case ACTION_VERIFY_MISSING_FIELDS:
            return "verify_missing_fields";
            
        case ACTION_PARSE:
            return "parse";
        case ACTION_PREVIEW_SCROLL:
            return "preview_scroll";
        case ACTION_PRESCAN:
            return "prescan";
        case ACTION_MLISTS:
            return "mlists";
        case ACTION_WRITE:
            return "write";
            
        case ACTION_VIEW_STATUS:
            return "view_status";
        case ACTION_VIEW_STATUS_WINDOW:
            return "view_status_window";
            
        case ACTION_GET_ERROR_DATE:
            return "get_error_date";
        case ACTION_GET_ERROR_EMAIL:
            return "get_error_email";
        case ACTION_GET_ERROR_EMAILDOUBLE:
            return "get_error_emaildouble";
        case ACTION_GET_ERROR_GENDER:
            return "get_error_gender";
        case ACTION_GET_ERROR_MAILTYPE:
            return "get_error_mailtype";
        case ACTION_GET_ERROR_NUMERIC:
            return "get_error_numeric";
        case ACTION_GET_ERROR_STRUCTURE:
            return "get_error_structure";
        case ACTION_GET_ERROR_BLACKLIST:
            return "get_error_blacklist";
        case ACTION_GET_DATA_PARSED:
            return "get_data_parsed";
        
        default:
            return super.subActionMethodName(subAction);
        }
    }
    
    @Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse response) throws IOException, ServletException {
		ComImportWizardForm aForm = (ComImportWizardForm) form;
		ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
		ActionForward destination = null;

		if (!AgnUtils.isUserLoggedIn(req)) {
			return mapping.findForward("logon");
		} else if (!AgnUtils.allowed(req, Permission.WIZARD_IMPORTCLASSIC)) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.notAllowed"));
		} else {
	        //validation form
	        errors = formValidate(aForm, req);
	        if (!errors.isEmpty()) {
	            saveErrors(req, errors);
	            if (aForm.getPreviousForward().isEmpty()) {
	                return mapping.getInputForward();
	            } else {
	                return mapping.findForward(aForm.getPreviousForward());
	            }
	        }
	
			if (logger.isInfoEnabled()) {
				logger.info("ImportWizard action: " + aForm.getAction());
			}
	
			try {
				switch(aForm.getAction()) {
	
				case ACTION_START:
					// check if writeContent is running
					if (aForm.isWriteContentRunning()) {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.classicimport.alreadyrunning"));
						destination = mapping.findForward("error");
					} else {
	                    aForm.setAttachmentCsvFileID(0);
						// reset running import
						aForm.setImportIsRunning(false);
	                    loadCsvUploads(req);
	                    aForm.setUseCsvUpload(false);
	                    aForm.setAction(ACTION_MODE);
						destination = mapping.findForward("start");
					}
					break;
	
				case ACTION_MODE:
	                if (aForm.isUseCsvUpload()) {
	                    aForm.setCsvFile(aForm.getFormFileByUploadId(aForm.getAttachmentCsvFileID(), "text/csv"));
	                }
	                
					if (aForm.getCsvFile().getFileName().toLowerCase().endsWith(".zip") || aForm.getCsvFile().getFileName().toLowerCase().endsWith(".gz")) {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("import.error.zipFile_not_allowed"));
						aForm.setAction(ACTION_MODE);
						destination = mapping.findForward("start");
					} else if (aForm.getCsvFile().getFileData().length <= 0) {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("autoimport.error.emptyFile", aForm.getCsvFile().getFileName()));
						aForm.setAction(ACTION_MODE);
						destination = mapping.findForward("start");
					} else {
						aForm.setAction(ACTION_CSV);
						destination = mapping.findForward("mode");
					}
					break;
	
				case ACTION_CSV:
	                aForm.setAction(ACTION_CHECK_FIELDS);
					destination=mapping.findForward("mapping");
					break;
	
				case ACTION_CHECK_FIELDS:
	                Map<String, String[]> parameterMap = req.getParameterMap();
	                Map<String, String> mapParametersOnlyMap = new HashMap<>();
	                for (Entry<String, String[]> entry : parameterMap.entrySet()) {
	                    if (entry.getKey().startsWith("map_")) {
	                    	// This works on a bug happening, when the parameter in testmode is only a single String
	                        Object valueObject = entry.getValue();
	                        if (valueObject instanceof String[]) {
	                        	String[] value = (String[]) valueObject;
	                        	mapParametersOnlyMap.put(entry.getKey(), value[0]);
	                        } else {
	                        	String value = (String) valueObject;
	                        	mapParametersOnlyMap.put(entry.getKey(), value);
	                        }
	                    }
	                }
	
	                aForm.getImportWizardHelper().mapColumns(mapParametersOnlyMap);
	
	                if (aForm.isColumnDuplicate(mapParametersOnlyMap)) {
	                    errors.add("global", new ActionMessage("error.import.column.dbduplicate"));
	                    aForm.setAction(ACTION_CHECK_FIELDS);
	                    destination = mapping.findForward("mapping");
	                    break;
	                }
	
	                try {
	                    aForm.getStatus().getErrors().clear();
						importWizardService.doParse(aForm.getImportWizardHelper());
	                    aForm.getImportWizardHelper().setLinesOK(aForm.getImportWizardHelper().getLinesOKFromFile());
	                    int maxRowsAllowedForClassicImport = configService.getIntegerValue(ConfigValue.ClassicImportMaxRows, AgnUtils.getCompanyID(req));
	                    if (maxRowsAllowedForClassicImport >= 0 && aForm.getImportWizardHelper().getLinesOK() > maxRowsAllowedForClassicImport) {
	                        errors.add("global", new ActionMessage("error.import.maxlinesexceeded", aForm.getImportWizardHelper().getLinesOK(), maxRowsAllowedForClassicImport));
	                    }
	                } catch (ImportWizardContentParseException e) {
	                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(e.getErrorMessageKey()));
	                    aForm.setAction(ACTION_CHECK_FIELDS);
	                    destination = mapping.findForward("mapping");
	                    break;
	                } catch (CsvDataInvalidItemCountException e) {
	                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.import.data.itemcount", e.getExpected(), e.getActual(), e.getErrorLineNumber()));
	                } catch (Exception e) {
	                    logger.error("Exception caught: " + e.getMessage(), e);
	                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.import.exception", e.getMessage()));
	                }
	
	                checkProfileKeyColumnIndexed(aForm, req, messages);
	
					if (aForm.verifyMissingFieldsNeeded()) {
	                    aForm.setAction(ACTION_PARSE);
	                    destination = mapping.findForward("verifymissingfields");
	                }
	                else {
	                    aForm.setAction(ACTION_PRESCAN);
					    destination = mapping.findForward("verify");
	                }
	
					break;
	
	            case ACTION_VERIFY_MISSING_FIELDS:
	                aForm.setAction(ACTION_PARSE);
	                destination = mapping.findForward("verifymissingfields");
					break;
	
				case ACTION_PARSE:
					aForm.setAction(ACTION_PRESCAN);
					destination=mapping.findForward("verify");
					break;
	
				case ACTION_PREVIEW_SCROLL:
					aForm.setAction(ACTION_PRESCAN);
					destination=mapping.findForward("verify");
					break;
	
					// insert here csv "pre-scan" - results:
				case ACTION_PRESCAN:
					aForm.setAction(ACTION_MLISTS);
					destination=mapping.findForward("prescan");
					break;
	
				case ACTION_MLISTS:
					if (aForm.getMode() != ImportMode.TO_BLACKLIST.getIntValue()) {
						aForm.setAction(ACTION_WRITE);
						loadMailinglistsPageFormData(req);
						destination = mapping.findForward("mlists");
						ComAdmin admin = AgnUtils.getAdmin(req);
						if (aForm.getMailingLists() != null && aForm.getMailingLists().size() <= 0 && !admin.permissionAllowed(Permission.IMPORT_WITHOUT_MAILINGLIST) && aForm.getMode() != ImportMode.UPDATE.getIntValue()) {
							errors.add("global", new ActionMessage("error.import.no_mailinglist"));
							aForm.setMailingLists(null);
						}
						break;
					}
					//$FALL-THROUGH$ - MODE_BLACKLIST => fall through to ACTION_WRITE
				case ACTION_WRITE:
					if (aForm.isImportIsRunning()) {
						destination=mapping.findForward("error");
						break;
					}
					aForm.setImportIsRunning(true);
					aForm.setAction(ACTION_VIEW_STATUS);
					aForm.getStatus().getErrors().clear();
	                startImportWorker(mapping, req, errors, aForm);
	                destination = checkImportWorker(mapping, req, errors, aForm);
					break;
	
				case ACTION_VIEW_STATUS:
					destination=mapping.findForward("view_status");
					break;
	
				case ACTION_VIEW_STATUS_WINDOW:
					if (aForm.getErrorId() != null) {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(aForm.getErrorId()));
						aForm.setErrorId(null);
					}
	
					checkImportWorker(mapping, req, errors, aForm);
					destination = mapping.findForward("view_status_window");
					break;
	
				case ACTION_GET_ERROR_DATE:
				case ACTION_GET_ERROR_EMAIL:
				case ACTION_GET_ERROR_EMAILDOUBLE:
				case ACTION_GET_ERROR_GENDER:
				case ACTION_GET_ERROR_MAILTYPE:
				case ACTION_GET_ERROR_NUMERIC:
				case ACTION_GET_ERROR_STRUCTURE:
				case ACTION_GET_ERROR_BLACKLIST:
				case ACTION_GET_DATA_PARSED:
					String outfile = getDataFile(aForm);
					response.setContentType("text/plain");
	                HttpUtils.setDownloadFilenameHeader(response, aForm.getDownloadName() + ".csv");
					response.setCharacterEncoding(aForm.getStatus().getCharset());
					ServletOutputStream ostream = response.getOutputStream();
					ostream.write(outfile.getBytes(aForm.getStatus().getCharset()));
					ostream.close();
					destination = null;
					break;
	
				default:
					aForm.setAction(ACTION_PARSE);
					destination=mapping.findForward("list");
				}
	
			} catch (Exception e) {
				logger.error("execute: "+e, e);
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
			}
		}

		// Report any errors we have discovered back to the original form
		if (!errors.isEmpty() && destination != null) {
			StringBuilder errorMessage = new StringBuilder();
			@SuppressWarnings("unchecked")
			Iterator<ActionMessage> messageIterator = errors.get();
			while (messageIterator.hasNext()) {
				if (errorMessage.length() > 0) {
					errorMessage.append("\n");
				}
				ActionMessage message = messageIterator.next();
				errorMessage.append(message.getKey() + ": " + message.getValues());
			}
			logger.error("Import had errors: " + errorMessage.toString());
			saveErrors(req, errors);
			// return new ActionForward(mapping.getForward());
		}

        if (!messages.isEmpty()) {
        	saveMessages(req, messages);
        }

        if (destination != null) {
        	aForm.setPreviousForward(destination.getName());
        }
        
		return destination;
	}

    private void loadResultPageFormData(HttpServletRequest req, ComImportWizardForm comImportWizardForm) {
        Map<String, Mailinglist> mailinglists = new HashMap<>();
        if (comImportWizardForm.getResultMailingListAdded() != null) {
	        for (String mailinglistID : comImportWizardForm.getResultMailingListAdded().keySet()) {
	            Mailinglist mailinglist = mailinglistService.getMailinglist(Integer.parseInt(mailinglistID), AgnUtils.getCompanyID(req));
	            mailinglists.put(mailinglistID, mailinglist);
	        }
        }
        req.setAttribute("mailinglists", mailinglists);
        req.setAttribute("resultMLAdded", comImportWizardForm.getResultMailingListAdded());
    }

    private void loadMailinglistsPageFormData(HttpServletRequest req) {
        List<Mailinglist> mailinglists = mailinglistApprovalService.getEnabledMailinglistsForAdmin(AgnUtils.getAdmin(req));
        req.setAttribute("mailinglists", mailinglists);
    }

    private void startImportWorker(ActionMapping mapping, HttpServletRequest req, ActionMessages errors, ComImportWizardForm comImportWizardForm) {
		try {
			String key = FUTURE_TASK + "@" + req.getSession(false).getId();
			
			if (!futureHolder.containsKey(key)) {
				// Create new import worker
				Future<? extends Object> writeContentFuture = getWriteContentFuture(req, comImportWizardForm);
				futureHolder.put(key, writeContentFuture);
				comImportWizardForm.setFutureIsRuning(true);
				comImportWizardForm.setImportIsRunning(true);
			}
		} catch (Exception e) {
			logger.error("company: " + e, e);
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
			comImportWizardForm.setError(true); // do not refresh when an error has been occurred
			comImportWizardForm.setImportIsRunning(false);
		}
	}
    
	private ActionForward checkImportWorker(ActionMapping mapping, HttpServletRequest req, ActionMessages errors, ComImportWizardForm comImportWizardForm) {
		try {
			String key = FUTURE_TASK + "@" + req.getSession(false).getId();

			// Check for import end
			if (futureHolder.containsKey(key) && futureHolder.get(key) != null && futureHolder.get(key).isDone()) {
				Future<? extends Object> future = futureHolder.remove(key);
				comImportWizardForm.setFutureIsRuning(false);
				comImportWizardForm.setImportIsRunning(false);
				comImportWizardForm.setCsvFile(null);
				comImportWizardForm.setParsedContent(null);
				comImportWizardForm.setParsedData(null);
				comImportWizardForm.setCsvAllColumns(null);
			
				comImportWizardForm.getErrorMap().remove(ImportErrorType.BLACKLIST_ERROR);
				comImportWizardForm.getErrorMap().remove(ImportErrorType.DATE_ERROR);
				comImportWizardForm.getErrorMap().remove(ImportErrorType.EMAIL_ERROR);
				comImportWizardForm.getErrorMap().remove(ImportErrorType.MAILTYPE_ERROR);
				comImportWizardForm.getErrorMap().remove(ImportErrorType.NUMERIC_ERROR);
				comImportWizardForm.getErrorMap().remove(ImportErrorType.STRUCTURE_ERROR);

				comImportWizardForm.addDbInsertStatusMessage("import.csv_completed");
				comImportWizardForm.setDbInsertStatus(1000);
				
				req.setAttribute("importIsDone", true);
				
				// Import is done, so clean up temp items, show status and write log
				if (future != null && future.get() instanceof ProfileImportWorker) {
					ProfileImportWorker profileImportWorker = (ProfileImportWorker) future.get();
					if (profileImportWorker.getError() != null) {
						createReportResult(AgnUtils.getAdmin(req), profileImportWorker, comImportWizardForm, true);
						throw profileImportWorker.getError();
					} else {
						createReportResult(AgnUtils.getAdmin(req), profileImportWorker, comImportWizardForm, false);
					}
					profileImportWorker.cleanUp();
				}

				writeClassicImportLog(req, comImportWizardForm);

				loadResultPageFormData(req, comImportWizardForm);
			} else {
				// Import still continues
				comImportWizardForm.setError(false);
			}
		} catch (ImportException e) {
			logger.error("classicimport: " + e, e);
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(e.getErrorMessageKey(), e.getAdditionalErrorData()));
			// do not refresh when an error has occurred
			comImportWizardForm.setError(true);
			comImportWizardForm.getDbInsertStatusMessages().clear();
			comImportWizardForm.addDbInsertStatusMessage(e.getErrorMessageKey());
			req.setAttribute("importError", e.getMessage(AgnUtils.getAdmin(req).getLocale()));
		} catch (Exception e) {
			logger.error("classicimport: " + e, e);
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
			// do not refresh when an error has occurred
			comImportWizardForm.setError(true);
			comImportWizardForm.getDbInsertStatusMessages().clear();
			comImportWizardForm.addDbInsertStatusMessage("error.exception");
		}
		
		return mapping.findForward("view_status");
	}

    private void createReportResult(ComAdmin admin, ProfileImportWorker profileImportWorker, ComImportWizardForm comImportWizardForm, boolean isError) throws Exception {
		// Create report, statistics data for GUI
    	Map<String, String> resultMailingListAdded = new HashMap<>();
    	if (profileImportWorker.getMailinglistAssignStatistics() != null) {
	    	for (Entry<Integer, Integer> entry : profileImportWorker.getMailinglistAssignStatistics().entrySet()) {
	    		resultMailingListAdded.put(entry.getKey().toString(), entry.getValue().toString());
	    	}
    	}
    	comImportWizardForm.setResultMailingListAdded(resultMailingListAdded);
		
		EmmCalendar my_calendar = new EmmCalendar(TimeZone.getDefault());
		TimeZone zone = TimeZone.getTimeZone(admin.getAdminTimezone());
		my_calendar.changeTimeWithZone(zone);
		Date my_time = my_calendar.getTime();
		
		String filename = Long.toString(my_time.getTime()) + ".csv";
		String csvfile = generateLocalizedImportCSVReport(admin.getLocale(), my_time, profileImportWorker.getImportProfile(), profileImportWorker.getStatus(), comImportWizardForm.getMode());

		reportService.createAndSaveImportReport(admin, filename, comImportWizardForm.getDatasourceID(), new Date(), csvfile, -1, isError);
	}
	
	private String generateLocalizedImportCSVReport(Locale locale, Date my_time, ImportProfile importProfile, ImportStatus status, int mode) {
		String csvfile = "";
		csvfile += SafeString.getLocaleString("import.SubscriberImport", locale);
		csvfile += "\n" + SafeString.getLocaleString("settings.fieldType.DATE", locale) + ": ; \"" + my_time + "\"\n";
		csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_email", locale) + ":;" + status.getError("email");
		csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_blacklist", locale) + ":;" + status.getError("blacklist");
		csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_double", locale) + ":;" + status.getError("keyDouble");
		csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_numeric", locale) + ":;" + status.getError("numeric");
		csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_mailtype", locale) + ":;" + status.getError("mailtype");
		csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_gender", locale) + ":;" + status.getError("gender");
		csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_date", locale) + ":;" + status.getError("date");
		csvfile += "\n" + SafeString.getLocaleString("csv_errors_linestructure", locale) + ":;" + status.getError("structure");
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
		
		String modeString = "";
		try {
			modeString = SafeString.getLocaleString(ImportMode.getFromInt(mode).getMessageKey(), locale);
		} catch (Exception e) {
			logger.error("Invalid import mode in " + ComImportWizardAction.class.getSimpleName() + ", mode : " + mode, e);
		}
		csvfile += "\n" + "mode:;" + modeString;

		return csvfile;
	}
	
	private Future<? extends Object> getWriteContentFuture(final HttpServletRequest request, final ComImportWizardForm aForm) throws Exception {
        final int companyID = AgnUtils.getCompanyID(request);
        final ComAdmin admin = AgnUtils.getAdmin(request);
        final int adminID = admin.getAdminID();
        
    	// Using ProfileImportWorker behind ClassicImport-GUI
    	
    	// Store uploaded import file
    	File importFile = new File(File.createTempFile("upload_csv_file_" + companyID + "_" + adminID + "_", ".csv", AgnUtils.createDirectory(IMPORT_FILE_DIRECTORY + "/" + admin.getCompanyID())).getAbsolutePath());
        try (InputStream inputStream = aForm.getCsvFile().getInputStream()) {
            try (FileOutputStream outputStream = new FileOutputStream(importFile, false)) {
            	IOUtils.copy(inputStream, outputStream);
            }
        }
    	
		// set datasource id
		DatasourceDescription dsDescription = new DatasourceDescriptionImpl();
		dsDescription.setId(0);
		dsDescription.setCompanyID(admin.getCompanyID());
		dsDescription.setSourcegroupID(2);
		dsDescription.setCreationDate(new Date());
		dsDescription.setDescription(aForm.getCsvFile().getFileName());
		dsDescription.setDescription2("EMM-Import (Classic/ProfileImport)");
		datasourceDescriptionDao.save(dsDescription);

		aForm.getStatus().setDatasourceID(dsDescription.getId());
		
		ImportProfile importProfile = new ImportProfileImpl();
		importProfile.setName(I18nString.getLocaleString("import.Wizard", admin.getLocale()));
		importProfile.setCompanyId(admin.getCompanyID());
		importProfile.setAdminId(admin.getAdminID());
		
		if (admin.permissionAllowed(Permission.IMPORT_MODE_DUPLICATES)) {
			// Only use this value, if the user has the right to change it
			importProfile.setUpdateAllDuplicates(aForm.isUpdateAllDuplicates());
		} else {
			// If the user doesn't have the right to change the value, the default for "UpdateAllDuplicates" in classic import is always "false" (historical)
			importProfile.setUpdateAllDuplicates(false);
		}
		
		// Data from first classic import page
		importProfile.setSeparator(Separator.getSeparatorByChar(aForm.getStatus().getSeparator()).getIntValue());
		importProfile.setTextRecognitionChar(TextRecognitionChar.getTextRecognitionCharByString(aForm.getStatus().getDelimiter()).getIntValue());
		importProfile.setCharset(Charset.getCharsetByName(aForm.getStatus().getCharset()).getIntValue());
		importProfile.setDateFormat(DateFormat.getDateFormatByValue(aForm.getDateFormat()).getIntValue());
		
		// Data from second classic import page
		
		// Translate ComImportWizardForm.ModeInt into ImportMode.ModeInt
		importProfile.setImportMode(ImportMode.getFromInt(aForm.getMode()).getIntValue());
		
		importProfile.setNullValuesAction(aForm.getStatus().getIgnoreNull());
		importProfile.setKeyColumn(aForm.getStatus().getKeycolumn());
		
		// Translate CustomerImportStatus.DoubleCheckInt into CheckForDuplicates.DoubleCheckInt
		if (aForm.getStatus().getDoubleCheck() == ImportStatus.DOUBLECHECK_NONE) {
			importProfile.setCheckForDuplicates(CheckForDuplicates.NO_CHECK.getIntValue());
		} else if (aForm.getStatus().getDoubleCheck() == ImportStatus.DOUBLECHECK_FULL) {
			importProfile.setCheckForDuplicates(CheckForDuplicates.COMPLETE.getIntValue());
		} else {
			throw new Exception("Invalid duplicate check index int: " + aForm.getStatus().getDoubleCheck());
		}
		
		// Data from third classic import page
		aForm.getColumnMapping().remove("gender_dummy");
		aForm.getColumnMapping().remove("mailtype_dummy");
		List<ColumnMapping> columnMapping = new ArrayList<>();
		for (Entry<String, CsvColInfo> entry : aForm.getColumnMapping().entrySet()) {
			ColumnMapping columnMappingEntry = new ColumnMappingImpl();
			columnMappingEntry.setDatabaseColumn(entry.getValue().getName());
			columnMappingEntry.setFileColumn(entry.getKey());
			columnMapping.add(columnMappingEntry);
		}
		importProfile.setColumnMapping(columnMapping);

		// Data from fourth classic import page (missng mailtype, gender)
		importProfile.setDefaultMailType(MailType.getFromInt(Integer.parseInt(aForm.getImportWizardHelper().getManualAssignedMailingType())).getIntValue());
		
		// Data from seventh classic import page
		List<Integer> mailingListIdsToAssign = new ArrayList<>();
		if (aForm.getImportWizardHelper().getMailingLists() != null) {
    		for (String mailinglistIdString : aForm.getImportWizardHelper().getMailingLists()) {
    			mailingListIdsToAssign.add(Integer.parseInt(mailinglistIdString));
    		}
		}

		ProfileImportWorker profileImportWorker = profileImportWorkerFactory.getProfileImportWorker(
			false, // Not interactive mode, because there is no error edit GUI
			mailingListIdsToAssign,
			request.getSession(false).getId(),
			admin,
			dsDescription.getId(),
			importProfile,
			new RemoteFile(aForm.getCsvFile().getFileName(), importFile, -1),
			aForm.getStatus());
		
		if (admin.permissionAllowed(Permission.RECIPIENT_GENDER_EXTENDED)) {
			profileImportWorker.setMaxGenderValue(ConfigService.MAX_GENDER_VALUE_EXTENDED);
		} else {
			profileImportWorker.setMaxGenderValue(ConfigService.MAX_GENDER_VALUE_BASIC);
		}
		
		aForm.setDbInsertStatusMessages(new LinkedList<>());

        Future<? extends Object> future = workerExecutorService.submit(profileImportWorker);
        return future;
    }

    private void loadCsvUploads(HttpServletRequest req) {
        List<String> extentions = new ArrayList<>();
        extentions.add("csv");
		req.setAttribute("csvFiles", getOverviewListByExtention(req, extentions));
    }

	private List<UploadData> getOverviewListByExtention(HttpServletRequest req, List<String> extentions) {
		return uploadDao.getOverviewListByExtention(AgnUtils.getAdmin(req), extentions);
	}

    private void checkProfileKeyColumnIndexed(ComImportWizardForm aForm, HttpServletRequest req, ActionMessages messages) {
    	String keyColumn = aForm.getImportWizardHelper().getKeyColumn().toLowerCase();
		if (!DbUtilities.checkForIndex(dataSource, "customer_" + AgnUtils.getCompanyID(req) + "_tbl", Arrays.asList(new String[]{keyColumn}))) {
			messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.import.keyColumn.index"));
		}
    }

    private void writeClassicImportLog(HttpServletRequest request, ComImportWizardForm form) {
        try {
            Vector<String> allMailingListsIds = form.getImportWizardHelper().getMailingLists();

            String fileName = form.getCsvFile() != null ? form.getCsvFile().getFileName() : "not set";

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

            writeUserActivityLog(AgnUtils.getAdmin(request), "import classic from file", description.toString());
        } catch (Exception e) {
            logger.error("classic import recipients" + e, e);
        }
    }

	/**
	 * Loads CVS data file from Database
	 * 
	 * @param aForm
	 * @return data
	 */
    private String getDataFile(ComImportWizardForm aForm) {
		switch (aForm.getAction()) {
			case ACTION_GET_ERROR_DATE:
				return aForm.getError(ImportErrorType.DATE_ERROR).toString();
			case ACTION_GET_ERROR_EMAIL:
				return aForm.getError(ImportErrorType.EMAIL_ERROR).toString();
			case ACTION_GET_ERROR_EMAILDOUBLE:
				return aForm.getError(ImportErrorType.KEYDOUBLE_ERROR).toString();
			case ACTION_GET_ERROR_GENDER:
				return aForm.getError(ImportErrorType.GENDER_ERROR).toString();
			case ACTION_GET_ERROR_MAILTYPE:
				return aForm.getError(ImportErrorType.MAILTYPE_ERROR).toString();
			case ACTION_GET_ERROR_NUMERIC:
				return aForm.getError(ImportErrorType.NUMERIC_ERROR).toString();
			case ACTION_GET_ERROR_STRUCTURE:
				return aForm.getError(ImportErrorType.STRUCTURE_ERROR).toString();
			case ACTION_GET_ERROR_BLACKLIST:
				return aForm.getError(ImportErrorType.BLACKLIST_ERROR).toString();
			case ACTION_GET_DATA_PARSED:
				return aForm.getParsedData().toString();
			default:
				return "";
		}
	}

	public ActionErrors formValidate(ComImportWizardForm form, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();

		if (logger.isInfoEnabled()) {
			logger.info("validate: " + form.getAction());
		}

		switch (form.getAction()) {
			case ComImportWizardAction.ACTION_START:
				if (form.getImportWizardHelper() == null ) {
					form.setImportWizardHelper(importWizardService.createHelper());
					form.getImportWizardHelper().setCompanyID(AgnUtils.getCompanyID(request));
					final Locale locale = AgnUtils.getLocale(request);
					form.getImportWizardHelper().setLocale(locale);
				}
				break;

			case ComImportWizardAction.ACTION_CSV:
				if (AgnUtils.parameterNotEmpty(request, "mode_back")) {
					form.setAction(ComImportWizardAction.ACTION_START);
				} else {
					try {
				        CaseInsensitiveMap<String, CsvColInfo> dbColumnsAvailable = recipientDao.readDBColumns(AgnUtils.getCompanyID(request));
				        for (String hiddenColumn : ImportUtils.getHiddenColumns(AgnUtils.getAdmin(request))) {
				        	dbColumnsAvailable.remove(hiddenColumn);
				        }
				        form.getImportWizardHelper().setDbAllColumns(dbColumnsAvailable);
				        
						importWizardService.parseFirstLine(form.getImportWizardHelper());
					} catch (ImportWizardContentParseException e) {
						if (e.getAdditionalErrorData() != null && e.getAdditionalErrorData().length > 0) {
							errors.add("global", new ActionMessage(e.getErrorMessageKey(), e.getAdditionalErrorData()));
						} else {
							errors.add("global", new ActionMessage(e.getErrorMessageKey()));
						}
					}
				}
				break;

			case ComImportWizardAction.ACTION_CHECK_FIELDS:
				if (AgnUtils.parameterNotEmpty(request, "mapping_back")) {
					form.setAction(ComImportWizardAction.ACTION_MODE);
				}
				break;

			case ComImportWizardAction.ACTION_PARSE:
				if (AgnUtils.parameterNotEmpty(request, "verifymissingfields_back")) {
					form.setAction(ComImportWizardAction.ACTION_CSV);
					form.getImportWizardHelper().clearDummyColumnsMappings();
				}
				break;

			case ComImportWizardAction.ACTION_MODE:
				form.getImportWizardHelper().getStatus().setErrors(new HashMap<ImportErrorType, Integer>());

				if (form.isUseCsvUpload()) {
					if (form.getAttachmentCsvFileID() == 0) {
						form.setUseCsvUpload(false);
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.classicimport.no_csv_file"));
					}
				} else if ((form.getCsvFile() == null) || (form.getCsvFile().getFileName().isEmpty())) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.classicimport.no_csv_file"));
				}

				form.checkAndReadCsvFile(request, errors);
				
				if (StringUtils.isBlank(form.getImportWizardHelper().getStatus().getKeycolumn())) {
					form.getImportWizardHelper().getStatus().setKeycolumn("email");
				}

				break;

			case ComImportWizardAction.ACTION_PRESCAN:
				if (AgnUtils.parameterNotEmpty(request, "verify_back")) {
					if (form.verifyMissingFieldsNeeded()) {
						form.setAction(ComImportWizardAction.ACTION_VERIFY_MISSING_FIELDS);
					} else {
						form.setAction(ComImportWizardAction.ACTION_CSV);
						form.getImportWizardHelper().clearDummyColumnsMappings();
					}
				} else {
					if (StringUtils.isBlank(form.getImportWizardHelper().getStatus().getKeycolumn())) {
						form.getImportWizardHelper().getStatus().setKeycolumn("email");
					}
				}
				break;

			case ComImportWizardAction.ACTION_MLISTS:
				if (AgnUtils.parameterNotEmpty(request, "prescan_back")) {
					form.setAction(ComImportWizardAction.ACTION_PREVIEW_SCROLL);
				}
				break;

			case ComImportWizardAction.ACTION_WRITE:
				form.handleWrite(request, errors);
				break;

			case ComImportWizardAction.ACTION_PREVIEW_SCROLL:
				if (form.getImportWizardHelper().getParsedContent() != null) {
					if (form.getImportWizardHelper().getPreviewOffset() >= form.getImportWizardHelper().getParsedContent().size()) {
						form.getImportWizardHelper().setPreviewOffset(form.getImportWizardHelper().getParsedContent().size() - 6);
					}
				}
				if (form.getImportWizardHelper().getPreviewOffset() < 0) {
					form.getImportWizardHelper().setPreviewOffset(0);
				}
				break;
			default:
				break;
		}
		return errors;
	}
}
