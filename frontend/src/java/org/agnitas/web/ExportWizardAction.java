/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import static org.agnitas.util.UserActivityUtil.addChangedFieldLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.agnitas.beans.ExportPredef;
import org.agnitas.beans.impl.ExportPredefImpl;
import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.emm.core.autoexport.dao.AutoExportDao;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.GenericExportWorker;
import org.agnitas.service.RecipientExportWorker;
import org.agnitas.service.RecipientExportWorkerFactory;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.HttpUtils;
import org.agnitas.web.forms.ExportWizardForm;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.mailinglist.service.ComMailinglistService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.service.ExportPredefService;
import com.agnitas.util.FutureHolderMap;

/**
 * Implementation of <strong>Action</strong> that handles customer exports
 */
public class ExportWizardAction extends StrutsActionBase {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ExportWizardAction.class);
	
    public static final String FUTURE_TASK = "EXPORT_RECIPIENTS";

	public static final int ACTION_COLLECT_DATA = ACTION_LAST + 2;

	public static final int ACTION_DOWNLOAD = ACTION_LAST + 4;

	public static final int ACTION_VIEW_STATUS_WINDOW = ACTION_LAST + 5;
	
	public static final int ACTION_PROCEED = ACTION_LAST + 8;

    private ExportPredefService exportPredefService;
    protected ComTargetDao targetDao;
    private ComMailinglistService mailinglistService;
    protected DataSource dataSource;
	protected ConfigService configService;
    private FutureHolderMap futureHolder;
    private ExecutorService workerExecutorService;
	protected JavaMailService javaMailService;
	protected RecipientExportReporter recipientExportReporter;
	protected AutoExportDao autoExportDao;
    private MailinglistApprovalService mailinglistApprovalService;
    private RecipientExportWorkerFactory recipientExportWorkerFactory;
    
    @Required
    public final void setRecipientExportWorkerFactory(final RecipientExportWorkerFactory factory) {
    	this.recipientExportWorkerFactory = Objects.requireNonNull(factory, "RecipientExportWorkerFactory is null");
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
	public void setFutureHolder(FutureHolderMap futureHolder) {
		this.futureHolder = futureHolder;
	}

	@Required
	public void setWorkerExecutorService(ExecutorService workerExecutorService) {
		this.workerExecutorService = workerExecutorService;
	}

	@Required
	public void setJavaMailService(JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}

	@Required
	public void setRecipientExportReporter(RecipientExportReporter recipientExportReporter) {
		this.recipientExportReporter = recipientExportReporter;
	}
	
	public void setAutoExportDao(AutoExportDao autoExportDao) {
		this.autoExportDao = autoExportDao;
	}

	// --------------------------------------------------------- Public Methods

    @Override
    public String subActionMethodName(int subAction) {
        switch (subAction) {
        case ACTION_COLLECT_DATA:
            return "collect_data";
        case ACTION_PROCEED:
            return "collect_data";
        case ACTION_VIEW_STATUS_WINDOW:
            return "view_status_window";
        case ACTION_DOWNLOAD:
            return "download";
        case ACTION_CONFIRM_DELETE:
            return "confirm_delete";
        default:
            return super.subActionMethodName(subAction);
        }
    }


    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     * <br>
	 * ACTION_LIST: forwards to predefined export definition list page.
	 * <br><br>
	 * ACTION_QUERY: loads chosen predefined export definition data into form or, if there was "Back" button pressed,<br>
     *     clears form data; loads lists of target group and mailing lists into form; <br>
     *     forwards to predefined export definition query page.
	 * <br><br>
     * ACTION_COLLECT_DATA: proceeds exporting recipients from database according to the export definition;<br>
     *     provides storing the export result in temporary zip file, stores name of the temporary file in form;
     *     forwards to export view page.
     * <br><br>
     * ACTION_VIEW_STATUS_WINDOW: forwards to export view page.
     * <br><br>
     * ACTION_DOWNLOAD: provides downloading prepared zip file with list of recipients for export; sends notification <br>
     *     email with export report for admin if the current admin have this option.
	 * <br><br>
     * ACTION_SAVE_QUESTION: forwards to page for edit export definition name and description.
	 * <br><br>
     * ACTION_SAVE: checks the name of export definition:<br>
     *     if it is not filled or its length is less than 3 chars - forwards to page for editing export definition
     *     name and description and shows validation error message<br>
     *     if the name is valid, checks the export definition id value. If id of export definition is 0, inserts new
     *     export definition db entry, otherwise updates db entry with given id.<br>
     *     Forwards to export definition list page.
     * <br><br>
	 * ACTION_CONFIRM_DELETE: checks if an ID of export definition is given and loads the export definition data;<br>
     *     forwards to jsp with question to confirm deletion.
	 * <br><br>
	 * ACTION_DELETE: marks the chosen predefined export definition as deleted and saves the changes in database;<br>
     *     forwards to predefined export definition list page.
	 * <br><br>
	 * Any other ACTION_* would cause a forward to "query"
     * <br><br>
     * @param form ActionForm object, data for the action filled by the jsp
     * @param req HTTP request
     * @param response HTTP response
     * @param mapping The ActionMapping used to select this instance
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     * @return destination specified in struts-config.xml to forward to next jsp
     */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse response) throws IOException, ServletException {
        // Validate the request parameters specified by the user
        ExportWizardForm aForm = null;
		ActionMessages messages = new ActionMessages();
        ActionMessages errors = new ActionMessages();
        ActionForward destination = null;

        ComAdmin admin = AgnUtils.getAdmin(req);
        int companyID = admin.getCompanyID();

        if (form != null) {
            aForm = (ExportWizardForm) form;
        } else {
            aForm = new ExportWizardForm();
        }

        logger.info("Action: "+aForm.getAction());
        
        String futureKey = FUTURE_TASK + "@" + req.getSession(false).getId();

        try {
            switch(aForm.getAction()) {

                case ACTION_LIST:
                	// List all available export profiles
                    destination = mapping.findForward("list");
                    break;

                case ACTION_VIEW:
                	// Show the selected export profile or start a new export profile
                    if (aForm.getExportPredefID() != 0 && !aForm.isBackButtonPressed()) {
                        loadPredefExportFromDB(aForm, req);
                    }

                    aForm.setTargetGroups(targetDao.getTargetLights(companyID, false));
                    aForm.setMailinglistObjects(mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));

                    aForm.setAction(ACTION_SAVE);
					aForm.setLocaleDatePattern(AgnUtils.getDatePickerFormatPattern(admin));
                    destination = mapping.findForward("view");
                    break;

                case ACTION_VIEW_STATUS_WINDOW:
                	// Show export result with download link
                    destination = mapping.findForward("download");
                    break;

                case ACTION_DOWNLOAD:
                	// Download export data file
                    File exportedFile = checkTempRecipientExportFile(companyID, aForm.getExportedFile(), errors);
                    if (exportedFile != null && exportedFile.exists()) {
                        String filename = getExportFileBasename(req) + ".zip";
                        aForm.setDownloadName(filename);
                        
                        try (FileInputStream inputStream = new FileInputStream(exportedFile)) {
							response.setContentType("application/zip");
				            HttpUtils.setDownloadFilenameHeader(response, filename);
							response.setContentLength((int)exportedFile.length());
							
							try (ServletOutputStream outputStream = response.getOutputStream()) {
								IOUtils.copy(inputStream, outputStream);
							}
						}

                        writeUserActivityLog(admin, "export recipients", aForm.getExportedFile());

                        destination = null;
                    } else {
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.export.file_not_ready"));
                    }
                    break;

                case ACTION_SAVE:
                	// Save current export profile for later usage under the given name
					if (StringUtils.length(aForm.getShortname()) < 3) {
						errors.add("shortname", new ActionMessage("error.name.too.short"));
	                    destination = mapping.findForward("view");
					} else {
						if (aForm.getExportPredefID() != 0) {
							saveExport(aForm, req);
						} else {
							insertExport(aForm, req);
						}
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
						destination = mapping.findForward("view");
					}
                    aForm.setTargetGroups(targetDao.getTargetLights(companyID, false));
                    aForm.setMailinglistObjects(mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));

                    aForm.setAction(ACTION_SAVE);
                    aForm.setLocaleDatePattern(AgnUtils.getDatePickerFormatPattern(admin));
					aForm.setAction(ExportWizardAction.ACTION_VIEW);
                    break;

                case ACTION_CONFIRM_DELETE:
                	// Show confirmation form to delete selected export profile
                	if (!"0".equals(req.getParameter("exportPredefID"))) {
                        loadPredefExportFromDB(aForm, req);
                    }
                	aForm.setAction(ACTION_DELETE);
                    destination = mapping.findForward("delete_question");
                    break;

                case ACTION_DELETE:
                	// Delete selected export profile
                	if (AgnUtils.isNumber(req.getParameter("exportPredefID"))) {
	                	int exportPredefID = Integer.parseInt(req.getParameter("exportPredefID"));
	                	if (exportPredefID > 0) {
	                		// Mark export profile as deleted
	                		ExportPredef exportPredef = exportPredefService.get(aForm.getExportPredefID(), companyID);
							if (exportPredef != null) {
								List<AutoExport> autoExportsList = autoExportDao == null ? null : autoExportDao.listAutoExportsUsingProfile(exportPredefID);
			                	if (autoExportsList != null && autoExportsList.size() > 0) {
			                		AutoExport autoExport = autoExportsList.get(0);
			                		if (autoExportsList.size() > 1) {
			                			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.export.profile.delete.used.more", autoExport.getShortname() + " (ID: " + autoExport.getAutoExportId() + ")", autoExportsList.size() - 1));
			                		} else {
			                			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.export.profile.delete.used", autoExport.getShortname() + " (ID: " + autoExport.getAutoExportId() + ")"));
			                		}
			                	} else {
								    exportPredef.setDeleted(1);
									exportPredefService.save(exportPredef);
								    writeUserActivityLog(admin, "delete export definition", getExportDescription(exportPredef));
								}
		                	}
	                	}
                	}
                    destination = mapping.findForward("list");
                    break;
                
                case ACTION_COLLECT_DATA:
                	// Start new export or check for progress of running export
                    aForm.setAction(ACTION_PROCEED);
                    if (!futureHolder.containsKey(futureKey) || futureHolder.get(futureKey) == null) {
                    	// Start a new potentially long running process
                    	File exportFile = getTempRecipientExportFile(companyID);
                    	RecipientExportWorker exportWorker = this.recipientExportWorkerFactory.newWorker(getExportProfileFromForm(aForm, req, companyID), admin);
                    	exportWorker.setDataSource(dataSource);
						exportWorker.setMailinglistService(mailinglistService);
                    	exportWorker.setExportFile(exportFile.getAbsolutePath());
                    	exportWorker.setZipped(true);
                    	exportWorker.setZippedFileName(getExportFileBasename(req) + ".zip");
                    	exportWorker.setUsername(admin.getUsername() + " (ID: " + admin.getAdminID() + ")");
                    	exportWorker.setRemoteFile(new RemoteFile("", exportFile, -1));
            			exportWorker.setDateFormat(admin.getDateFormat());
            			exportWorker.setDateTimeFormat(admin.getDateTimeFormatWithSeconds());
            			exportWorker.setMaximumExportLineLimit(configService.getIntegerValue(ConfigValue.ProfileRecipientExportMaxRows, companyID));
                    	Future<GenericExportWorker> future = workerExecutorService.submit(exportWorker);
                    	futureHolder.put(futureKey, future);
                    }

                    if (aForm.getRefreshMillis() < ExportWizardForm.REFRESH_MILLIS_MAXIMUM) {
                    	// raise the refresh time
                        aForm.setRefreshMillis(aForm.getRefreshMillis() + ExportWizardForm.REFRESH_MILLIS_STEP);
                    }
                    
            		aForm.setDbExportStatusMessages(new LinkedList<>());
            		aForm.setDbExportStatus(0);
            		aForm.setLinesOK(0);
            		aForm.setExportedFile(null);
            		aForm.setExportStartDate(new Date());
            		
                    destination = mapping.findForward("progress");
                    break;
                    
                case ACTION_PROCEED:
                	// Check for progress of running export
                    aForm.setAction(ACTION_PROCEED);
                    destination = mapping.findForward("progress");
                    
                    if (futureHolder.containsKey(futureKey) && futureHolder.get(futureKey) != null) {
                        // Check for end of export process
                    	if (futureHolder.get(futureKey).isDone()) {
	                    	// Collecting data for export is done
	    					@SuppressWarnings("unchecked")
							Future<GenericExportWorker> future = (Future<GenericExportWorker>) futureHolder.remove(futureKey);
	                        aForm.setRefreshMillis(ExportWizardForm.DEFAULT_REFRESH_MILLIS);
	                        RecipientExportWorker worker = (RecipientExportWorker) future.get();
	                        if (worker.getError() != null) {
	                        	recipientExportReporter.sendExportErrorMail(worker, admin);
	                        	recipientExportReporter.createAndSaveExportReport(worker, admin, true);
	                        	errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("export.result.error", worker.getError().getMessage()));
	                        } else {
	                        	recipientExportReporter.createAndSaveExportReport(worker, admin, false);
	                        	
	                			recipientExportReporter.sendExportReportMail(worker, admin);
	                			
	                			// Export via GUI is always zipped, but we need the date in the name of the zipped CSV file to be used in the name of the download zip file
	        					if (worker.getZippedFileName().toLowerCase().endsWith(".csv")) {
			                        aForm.setDownloadName(worker.getZippedFileName().substring(0, worker.getZippedFileName().length() - 4) + ".zip");
	        					} else if (worker.getZippedFileName().toLowerCase().endsWith(".zip")) {
			                        aForm.setDownloadName(worker.getZippedFileName());
	        					} else {
			                        aForm.setDownloadName(worker.getZippedFileName() + ".zip");
	        					}
	        					
		                		aForm.setDbExportStatusMessages(new LinkedList<>());
		                		aForm.setDbExportStatus(100);
		                		aForm.setExportStartDate(null);
		        				aForm.setLinesOK((int) worker.getExportedLines());
		        				aForm.setExportedFile(new File(worker.getExportFile()).getName());
								writeCollectContentLog(aForm, req, worker.getStartTime(), worker.getEndTime());
								aForm.setAction(ACTION_VIEW_STATUS_WINDOW);
	                        }
							destination = mapping.findForward("finish");
	                    } else {
	                    	// Collecting data for export is not done yet, so show wait screen
	                        if (aForm.getRefreshMillis() < ExportWizardForm.REFRESH_MILLIS_MAXIMUM) {
	                        	// raise the refresh time
	                            aForm.setRefreshMillis(aForm.getRefreshMillis() + ExportWizardForm.REFRESH_MILLIS_STEP);
	                        }
	                        
	                		aForm.setDbExportStatusMessages(new LinkedList<>());
							Date startTime = aForm.getExportStartDate();
							if (startTime == null) {
								startTime = new Date();
								aForm.setExportStartDate(startTime);
							}

	                		// Because we don't calculate the number of exported lines to do in forehand, which costs db performance,
	                		// we calculate a virtual moving progress (66% after 3 Minutes, progress becoming slower and slower
	                		float exportTimeSeconds = (new Date().getTime() - startTime.getTime()) / 1000;
	                		if (exportTimeSeconds > 0.0f) {
		                		int virtualPercentageDone = (int) ((1 - (1 / ((exportTimeSeconds + 60) / 60))) * 100);
		                		aForm.setDbExportStatus(virtualPercentageDone);
	                    	} else {
	                    		aForm.setDbExportStatus(0);
	                    	}
	                		
	                		aForm.setLinesOK(0);
	                		aForm.setExportedFile(null);
	                		
	                        destination = mapping.findForward("progress");
	                    }
                    } else {
                    	logger.error("Invalid state of export");
                    	throw new Exception("Invalid state of export");
                    }
                    break;

                default:
                    aForm.setAction(ACTION_VIEW);
					aForm.setLocaleDatePattern(AgnUtils.getDatePickerFormatPattern(admin));
                    destination = mapping.findForward("view");
            }

            List<ExportPredef> exports = exportPredefService.getExportProfiles(admin);
            aForm.setExportPredefList(exports);
            aForm.setExportPredefCount(exports.size());
        } catch (Exception e) {
        	logger.error("execute: " + e, e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
        }

		if (!messages.isEmpty()) {
			saveMessages(req, messages);
		}

        // Report any errors we have discovered back to the original form
        if (!errors.isEmpty()) {
            saveErrors(req, errors);
            if(destination==null) {
                return new ActionForward(mapping.getInput());
            }
        }

        return destination;
    }

    /**
     * Loads chosen predefined export data from database into form.
     *
     * @param aForm ExportWizardForm object
     * @param req HTTP request
     * @return true==success
     *         false==error
     */
	protected boolean loadPredefExportFromDB(ExportWizardForm aForm, HttpServletRequest req) {
		ExportPredef exportPredef = exportPredefService.get(aForm.getExportPredefID(), AgnUtils.getCompanyID(req));

		if (exportPredef != null) {
			aForm.setShortname(exportPredef.getShortname());
			aForm.setDescription(exportPredef.getDescription());
			aForm.setCharset(exportPredef.getCharset());
			aForm.setDelimiter(exportPredef.getDelimiter());
			aForm.setAlwaysQuote(exportPredef.isAlwaysQuote() ? 1 : 0);
			aForm.setSeparatorInternal(exportPredef.getSeparator());
			aForm.setTargetID(exportPredef.getTargetID());
			aForm.setMailinglistID(exportPredef.getMailinglistID());
			aForm.setUserStatus(exportPredef.getUserStatus());
			aForm.setUserType(exportPredef.getUserType());

			SimpleDateFormat inputDateFormat = AgnUtils.getDatePickerFormat(AgnUtils.getAdmin(req), true);
			Date timestampStart = exportPredef.getTimestampStart();
			if (timestampStart != null) {
				aForm.setTimestampStart(inputDateFormat.format(timestampStart));
			}
			Date timestampEnd = exportPredef.getTimestampEnd();
			if (timestampEnd != null) {
				aForm.setTimestampEnd(inputDateFormat.format(timestampEnd));
			}
			int timestampLastDays = exportPredef.getTimestampLastDays();
			if (timestampLastDays > 0) {
				aForm.setTimestampLastDays(Integer.toString(timestampLastDays));
			} else {
				aForm.setTimestampLastDays(null);
			}
			Date creationDateStart = exportPredef.getCreationDateStart();
			if (creationDateStart != null) {
				aForm.setCreationDateStart(inputDateFormat.format(creationDateStart));
			}
			Date creationDateEnd = exportPredef.getCreationDateEnd();
			if (creationDateEnd != null) {
				aForm.setCreationDateEnd(inputDateFormat.format(creationDateEnd));
			}
			int creationDateLastDays = exportPredef.getCreationDateLastDays();
			if (creationDateLastDays > 0) {
				aForm.setCreationDateLastDays(Integer.toString(creationDateLastDays));
			} else {
				aForm.setCreationDateLastDays(null);
			}
			Date mailinglistBindStart = exportPredef.getMailinglistBindStart();
			if (mailinglistBindStart != null) {
				aForm.setMailinglistBindStart(inputDateFormat.format(mailinglistBindStart));
			}
			Date mailinglistBindEnd = exportPredef.getMailinglistBindEnd();
			if (mailinglistBindEnd != null) {
				aForm.setMailinglistBindEnd(inputDateFormat.format(mailinglistBindEnd));
			}
			int mailinglistBindLastDays = exportPredef.getMailinglistBindLastDays();
			if (mailinglistBindLastDays > 0) {
				aForm.setMailinglistBindLastDays(Integer.toString(mailinglistBindLastDays));
			} else {
				aForm.setMailinglistBindLastDays(null);
			}

			// process columns:
			try {
				aForm.setColumns(exportPredef.getColumns().split(";"));

				if (StringUtils.isNotBlank(exportPredef.getMailinglists())) {
					aForm.setMailinglists(exportPredef.getMailinglists().split(";"));
				}
			} catch (Exception e) {
				logger.error("loadPredefExportFromDB: " + e, e);
				return false;
			}

			writeUserActivityLog(AgnUtils.getAdmin(req), "view export", exportPredef.getShortname() + " (" + exportPredef.getId() + ")");
		} else {
			logger.error("loadPredefExportFromDB - no ID given?: " + aForm.getExportPredefID());
			return false;
		}

		return true;
	}

    /**
     * Creates new predefined export definition database entry
     *
     * @param aForm ExportWizardForm object
     * @param req HTTP request
     * @return true==success
     *         false==error
     */
    protected boolean insertExport(ExportWizardForm aForm, HttpServletRequest req) {
        ExportPredef exportPredef = exportPredefService.create(AgnUtils.getCompanyID(req));

        // perform insert:
        exportPredef.setShortname(aForm.getShortname());
        exportPredef.setDescription(aForm.getDescription());
        exportPredef.setCharset(aForm.getCharset());
        exportPredef.setColumns(StringUtils.join(aForm.getColumns(), ";"));
        exportPredef.setMailinglists(StringUtils.join(aForm.getMailinglists(), ";"));
        exportPredef.setMailinglistID(aForm.getMailinglistID());
        exportPredef.setDelimiter(aForm.getDelimiter());
        exportPredef.setAlwaysQuote(aForm.getAlwaysQuote() > 0);
        String separator = aForm.getSeparator();
        separator = "\t".equals( separator ) ? "t" : separator;
		exportPredef.setSeparator(separator);
        exportPredef.setTargetID(aForm.getTargetID());
        exportPredef.setUserStatus(aForm.getUserStatus());
        exportPredef.setUserType(aForm.getUserType());
        try {
            loadDateParametersFromFormToBean(aForm, req, exportPredef);
        } catch (ParseException e) {
        	logger.error(e.getMessage(), e);
        }
		exportPredefService.save(exportPredef);

        writeSaveExportDefinitionLog(exportPredef, true, req);

        return true;
    }

    /**
     * Updates predefined export definition database entry
     *
     * @param aForm ExportWizardForm object
     * @param req HTTP request
     * @return true==success
     *         false==error
     */
    protected boolean saveExport(ExportWizardForm aForm, HttpServletRequest req) {
		int companyId = AgnUtils.getCompanyID(req);

		ExportPredef oldExport = exportPredefService.get(aForm.getExportPredefID(), companyId);
        ExportPredef exportPredef = exportPredefService.get(aForm.getExportPredefID(), companyId);

        // perform update in db:
        exportPredef.setShortname(aForm.getShortname());
        exportPredef.setDescription(aForm.getDescription());
        exportPredef.setCharset(aForm.getCharset());
        exportPredef.setColumns(StringUtils.join(aForm.getColumns(), ";"));
        exportPredef.setMailinglists(StringUtils.join(aForm.getMailinglists(), ";"));
        exportPredef.setMailinglistID(aForm.getMailinglistID());
        exportPredef.setDelimiter(aForm.getDelimiter());
        exportPredef.setAlwaysQuote(aForm.getAlwaysQuote() > 0);
        String separator = aForm.getSeparator();
        separator = "\t".equals( separator ) ? "t" : separator;
		exportPredef.setSeparator(separator);
        exportPredef.setTargetID(aForm.getTargetID());
        exportPredef.setUserStatus(aForm.getUserStatus());
        exportPredef.setUserType(aForm.getUserType());
        try {
            loadDateParametersFromFormToBean(aForm, req, exportPredef);
        } catch (ParseException e) {
        	logger.error(e.getMessage(), e);
        }
		exportPredefService.save(exportPredef);

		writeExportChangeLog(oldExport, exportPredef, AgnUtils.getAdmin(req));

        return true;
    }

	private void writeExportChangeLog(ExportPredef oldExport, ExportPredef newExport, ComAdmin admin) {
		DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, admin.getLocale());

		StringBuilder descriptionSb = new StringBuilder();
		descriptionSb.append(addChangedFieldLog("shortname", newExport.getShortname(), oldExport.getShortname()))
			.append(addChangedFieldLog("description", newExport.getDescription(), oldExport.getDescription()))
			.append(addChangedFieldLog("mailing list", newExport.getMailinglistID(), oldExport.getMailinglistID()))
			.append(addChangedFieldLog("target group", newExport.getTargetID(), oldExport.getTargetID()))
			.append(addChangedFieldLog("recipient type", newExport.getUserType(), oldExport.getUserType()))
			.append(addChangedFieldLog("recipient status", newExport.getUserStatus(),oldExport.getUserStatus()))
			.append(addChangedFieldLog("columns", newExport.getColumns(), oldExport.getColumns()))
			.append(addChangedFieldLog("mailing lists", newExport.getMailinglists(), oldExport.getMailinglists()))
			.append(addChangedFieldLog("separator", newExport.getSeparator(), oldExport.getSeparator()))
			.append(addChangedFieldLog("delimiter", newExport.getDelimiter(), oldExport.getDelimiter()))
			.append(addChangedFieldLog("charset", newExport.getCharset(), oldExport.getCharset()))
			.append(addChangedFieldLog("change period start", newExport.getTimestampStart(), oldExport.getTimestampStart(), dateFormat))
			.append(addChangedFieldLog("change period end", newExport.getTimestampEnd(), oldExport.getTimestampEnd(), dateFormat))
			.append(addChangedFieldLog("change period last days", newExport.getTimestampLastDays(), oldExport.getTimestampLastDays()))
			.append(addChangedFieldLog("creation period start", newExport.getCreationDateStart(), oldExport.getCreationDateStart(), dateFormat))
			.append(addChangedFieldLog("creation period end", newExport.getCreationDateEnd(), oldExport.getCreationDateEnd(), dateFormat))
			.append(addChangedFieldLog("creation period last days", newExport.getCreationDateLastDays(), oldExport.getCreationDateLastDays()))
			.append(addChangedFieldLog("ML binding period start", newExport.getMailinglistBindStart(), oldExport.getMailinglistBindStart(), dateFormat))
			.append(addChangedFieldLog("ML binding period end", newExport.getMailinglistBindEnd(), oldExport.getMailinglistBindEnd(), dateFormat))
			.append(addChangedFieldLog("ML binding period last days", newExport.getMailinglistBindLastDays(), oldExport.getMailinglistBindLastDays()));

		if (StringUtils.isNotBlank(descriptionSb.toString())) {
			descriptionSb.insert(0, ". ");
			descriptionSb.insert(0, getExportDescription(oldExport));

			writeUserActivityLog(admin, "edit export", descriptionSb.toString());
		}
	}

    /**
     * Loads date values into given bean from the form; parsed dates by certain format before loading.
     *
     * @param aForm  ExportWizardForm object
     * @param exportPredef  ExportPredef bean object (is filling with data from the form inside the method)
     * @throws ParseException
     */
	protected void loadDateParametersFromFormToBean(ExportWizardForm aForm, HttpServletRequest req, ExportPredef exportPredef) throws ParseException {
		SimpleDateFormat inputDateFormat = AgnUtils.getDatePickerFormat(AgnUtils.getAdmin(req), true);

		String timestampStart = aForm.getTimestampStart();
		if (StringUtils.isNotEmpty(timestampStart)) {
			exportPredef.setTimestampStart(inputDateFormat.parse(timestampStart));
		} else {
			exportPredef.setTimestampStart(null);
		}
		String timestampEnd = aForm.getTimestampEnd();
		if (StringUtils.isNotEmpty(timestampEnd)) {
			exportPredef.setTimestampEnd(inputDateFormat.parse(timestampEnd));
		} else {
			exportPredef.setTimestampEnd(null);
		}
		String timestampLastDays = aForm.getTimestampLastDays();
		if (AgnUtils.isNumber(timestampLastDays)) {
			exportPredef.setTimestampLastDays(Integer.parseInt(timestampLastDays));
		} else {
			exportPredef.setTimestampLastDays(0);
		}
		String creationDateStart = aForm.getCreationDateStart();
		if (StringUtils.isNotEmpty(creationDateStart)) {
			exportPredef.setCreationDateStart(inputDateFormat.parse(creationDateStart));
		} else {
			exportPredef.setCreationDateStart(null);
		}
		String creationDateEnd = aForm.getCreationDateEnd();
		if (StringUtils.isNotEmpty(creationDateEnd)) {
			exportPredef.setCreationDateEnd(inputDateFormat.parse(creationDateEnd));
		} else {
			exportPredef.setCreationDateEnd(null);
		}
		String creationDateLastDays = aForm.getCreationDateLastDays();
		if (AgnUtils.isNumber(creationDateLastDays)) {
			exportPredef.setCreationDateLastDays(Integer.parseInt(creationDateLastDays));
		} else {
			exportPredef.setCreationDateLastDays(0);
		}
		String mailinglistBindStart = aForm.getMailinglistBindStart();
		if (StringUtils.isNotEmpty(mailinglistBindStart)) {
			exportPredef.setMailinglistBindStart(inputDateFormat.parse(mailinglistBindStart));
		} else {
			exportPredef.setMailinglistBindStart(null);
		}
		String mailinglistBindEnd = aForm.getMailinglistBindEnd();
		if (StringUtils.isNotEmpty(mailinglistBindEnd)) {
			exportPredef.setMailinglistBindEnd(inputDateFormat.parse(mailinglistBindEnd));
		} else {
			exportPredef.setMailinglistBindEnd(null);
		}
		String mailinglistBindLastDays = aForm.getMailinglistBindLastDays();
		if (AgnUtils.isNumber(mailinglistBindLastDays)) {
			exportPredef.setMailinglistBindLastDays(Integer.parseInt(mailinglistBindLastDays));
		} else {
			exportPredef.setMailinglistBindLastDays(0);
		}
	}

    private static ExportPredef getExportProfileFromForm(ExportWizardForm form, HttpServletRequest req, int companyID) {
		SimpleDateFormat format = AgnUtils.getDatePickerFormat(AgnUtils.getAdmin(req), true);
		
		ExportPredef exportProfile = new ExportPredefImpl();
		
		exportProfile.setCharset(form.getCharset());
		exportProfile.setColumns(StringUtils.join(form.getColumns(), ";"));
		exportProfile.setCompanyID(companyID);
		exportProfile.setCreationDateLastDays(AgnUtils.isNumber(form.getCreationDateLastDays()) ? Integer.parseInt(form.getCreationDateLastDays()) : 0);
		exportProfile.setCreationDateEnd(parseDate(form.getCreationDateEnd(), format));
		exportProfile.setCreationDateStart(parseDate(form.getCreationDateStart(),format));
		exportProfile.setDeleted(0);
		exportProfile.setDelimiter(form.getDelimiter());
		exportProfile.setAlwaysQuote(form.getAlwaysQuote() > 0);
		exportProfile.setDescription(form.getDescription());
		exportProfile.setId(form.getExportPredefID());
		exportProfile.setMailinglistBindLastDays(AgnUtils.isNumber(form.getMailinglistBindLastDays()) ? Integer.parseInt(form.getMailinglistBindLastDays()) : 0);
		exportProfile.setMailinglistBindEnd(parseDate(form.getMailinglistBindEnd(), format));
		exportProfile.setMailinglistBindStart(parseDate(form.getMailinglistBindStart(), format));
		exportProfile.setMailinglistID(form.getMailinglistID());
		exportProfile.setMailinglists(StringUtils.join(form.getMailinglists(), ";"));
		exportProfile.setSeparator(form.getSeparator());
		exportProfile.setShortname(form.getShortname());
		exportProfile.setTargetID(form.getTargetID());
		exportProfile.setTimestampLastDays(AgnUtils.isNumber(form.getTimestampLastDays()) ? Integer.parseInt(form.getTimestampLastDays()) : 0);
		exportProfile.setTimestampEnd(parseDate(form.getTimestampEnd(), format));
		exportProfile.setTimestampStart(parseDate(form.getTimestampStart(), format));
		exportProfile.setUserStatus(form.getUserStatus());
		exportProfile.setUserType(form.getUserType());
		
		return exportProfile;
	}
	
	private static Date parseDate(String str, DateFormat format) {
		try {
			if (StringUtils.isBlank(str)) {
				return null;
			} else {
				return format.parse(str);
			}
		} catch(ParseException e) {
			logger.warn("Unable to parse date '" + str + "' using format " + format, e);
			
			return null;
		}
	}

    /**
     * Write log collect recipients data log
     * @param aForm : ExportWizardForm object
     * @param request : request
     * @param startDate : export start time
	 * @param endDate : export end time
     */
    private void writeCollectContentLog(ExportWizardForm aForm, HttpServletRequest request, Date startDate, Date endDate) {
    	ComAdmin admin = AgnUtils.getAdmin(request);
        int companyId = AgnUtils.getCompanyID(request);

        String mailingList = getMailingListById(aForm.getMailinglistID(), companyId);
        String targetGroup = getTargetGroupById(aForm.getTargetID(), companyId);
        String recipientType = getRecipientTypeByLetter(aForm.getUserType());
        String recipientStatus = getRecipientStatusById(aForm.getUserStatus());
        int numberOfColumns = aForm.getColumns().length;

        writeUserActivityLog(admin, "export",
                "Export started at: " + new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS).format(startDate) + ". " +
                		"ended at: " + new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS).format(endDate) + ". " +
                        "Number of profiles: " + aForm.getLinesOK() + ". " +
                        "Export parameters:" +
                        " mailing list: " + mailingList +
                        ", target group: " + targetGroup +
                        ", recipient type: " + recipientType +
                        ", recipient status: " + recipientStatus +
                        ", number of selected columns: " + numberOfColumns);
    }

    /**
     * Write to log an entry about creation/alteration of an export definition.
     * @param exportPredef export definition representation object
     * @param isNewEntry creation (true) or alteration (false)
     * @param req http request object
     */
    private void writeSaveExportDefinitionLog(ExportPredef exportPredef, boolean isNewEntry, HttpServletRequest req) {
        writeUserActivityLog(AgnUtils.getAdmin(req), (isNewEntry ? "create" : "edit") + " export definition", getExportDescription(exportPredef));
    }

    /**
     *  Get a text representation of export parameter "Recipient type"
     *
     * @param letter recipient type letter
     * @return text representation of recipient type
     */
    private String getRecipientTypeByLetter(String letter){
        switch (letter){
            case "E":
                return "All";
            case "A":
                return "Administrator";
            case "T":
                return "Test recipient";
            case "W":
                return "Normal recipient";
            default:
                return "not set";
        }
    }

    /**
     *  Get a text representation of export parameter "Recipient status"
     *
     * @param statusId recipient status id
     * @return text representation of recipient status
     */
    private String getRecipientStatusById(int statusId){
        switch (statusId){
            case 0:
                return "All";
            case 1:
                return "Active";
            case 2:
                return "Bounced";
            case 3:
                return "Opt-Out by admin";
            case 4:
                return "Opt-Out by recipient";
            case 5:
                return "Waiting for user confirmation";
            case 6:
                return "blacklisted";
            case 7:
                return "suspended";
            default:
                return "not set";
        }
    }

    /**
     *  Get a text representation of export parameter "target group"
     *
     * @param targetId target group id
     * @param companyId company id
     * @return a text representation of export parameter "target group"
     */
    private String getTargetGroupById(int targetId, int companyId){
       if (targetId == 0) {
           return "All";
       } else {
           return targetDao.getTarget(targetId, companyId).getTargetName();
       }
    }

    /**
     *  Get a text representation of export parameter "Mailing list"
     *
     * @param listId mailing list id
     * @param companyId company id
     * @return a text representation of export parameter "Mailing list"
     */
    private String getMailingListById(int listId, int companyId){
       if (listId == 0) {
           return "All";
       } else if (listId == -1) {
           return "No mailing list";
       } else {
           return mailinglistService.getMailinglistName(listId, companyId);
       }
    }
	
	private File checkTempRecipientExportFile(int companyID, String fileName, ActionMessages errors) {
		File companyCsvExportDirectory = new File(RecipientExportWorker.EXPORT_FILE_DIRECTORY + File.separator + companyID);
		if (!companyCsvExportDirectory.exists()) {
			companyCsvExportDirectory.mkdirs();
		}
		
		if (StringUtils.isNotBlank(fileName)) {
			String mandatoryExportTempFilePrefix = "RecipientExport_" + companyID + "_";
			
			if (!fileName.startsWith(mandatoryExportTempFilePrefix) || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
				logger.error("Illegal temp file for export: " + fileName);
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.permissionDenied"));
				return null;
			} else {
				return new File(companyCsvExportDirectory, fileName);
			}
		} else {
			return null;
		}
	}

	/**
	 * Get a description for export definition entity to be passed to {@link #writeUserActivityLog(com.agnitas.beans.ComAdmin, String, int)}.
	 * @param definition an export definition entity.
	 * @return a description of the {@code ep}.
	 */
	private String getExportDescription(ExportPredef definition) {
		return definition.getShortname() + " (" + definition.getId() + ")";
	}

    /**
     * Returns DAO accessing target groups.
     * 
     * @return DAO accessing target groups
     */
    public ComTargetDao getTargetDao() {
        return targetDao;
    }

    /**
     * Set DAO accessing target groups.
     * 
     * @param targetDao DAO accessing target groups
     */
    @Required
    public void setTargetDao(ComTargetDao targetDao) {
        this.targetDao = targetDao;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

	public void setMailinglistService(ComMailinglistService mailinglistService) {
		this.mailinglistService = mailinglistService;
	}

	@Required
	public void setExportPredefService(ExportPredefService exportPredefService) {
		this.exportPredefService = exportPredefService;
	}

	/**
	 * Generates a filename to be used for the Download.
	 * This name appear in the download window of the clients browser and is NOT the the real name within the webserver's filesystem.
	 * This name has intentionally no fileextension, because this can be .csv or .zip.
	 * 
	 * @param req
	 * @return
	 */
	private String getExportFileBasename(HttpServletRequest req) {
		return AgnUtils.getCompany(req).getShortname() + "_" + new SimpleDateFormat(DateUtilities.YYYYMD).format(new Date());
	}

	private File getTempRecipientExportFile(int companyID) {
		File companyCsvExportDirectory = new File(RecipientExportWorker.EXPORT_FILE_DIRECTORY + File.separator + companyID);
		if (!companyCsvExportDirectory.exists()) {
			companyCsvExportDirectory.mkdirs();
		}
		
		String dateString = new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS_FORFILENAMES).format(new Date());
		File importTempFile = new File(companyCsvExportDirectory, "RecipientExport_" + companyID + "_" + dateString + ".zip");
		int duplicateCount = 1;
		while (importTempFile.exists()) {
			importTempFile = new File(companyCsvExportDirectory, "RecipientExport_" + companyID + "_" + dateString + "_" + (duplicateCount++) + ".zip");
		}
		
		return importTempFile;
	}
}
