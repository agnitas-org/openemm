/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverstatus.web;

import static com.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static com.agnitas.util.Const.Mvc.ERROR_MSG;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.agnitas.beans.Admin;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.LicenseDao;
import com.agnitas.dao.ServerMessageDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.PermissionType;
import com.agnitas.emm.core.db_schema.bean.DbSchemaCheckResult;
import com.agnitas.emm.core.db_schema.bean.DbSchemaSnapshot;
import com.agnitas.emm.core.db_schema.service.DbSchemaSnapshotService;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.emm.core.logon.service.LogonServiceException;
import com.agnitas.emm.core.serverstatus.bean.VersionStatus;
import com.agnitas.emm.core.serverstatus.dto.ConfigValueDto;
import com.agnitas.emm.core.serverstatus.forms.JobQueueFormSearchParams;
import com.agnitas.emm.core.serverstatus.forms.JobQueueOverviewFilter;
import com.agnitas.emm.core.serverstatus.forms.ServerConfigForm;
import com.agnitas.emm.core.serverstatus.forms.ServerStatusForm;
import com.agnitas.emm.core.serverstatus.forms.validation.ServerConfigFormValidator;
import com.agnitas.emm.core.serverstatus.forms.validation.ServerStatusFormValidator;
import com.agnitas.emm.core.serverstatus.service.ServerStatusService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonWriter;
import com.agnitas.messages.Message;
import com.agnitas.service.JobDto;
import com.agnitas.service.JobQueueService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.HttpUtils;
import com.agnitas.util.ServerCommand;
import com.agnitas.util.ServerCommand.Command;
import com.agnitas.util.ServerCommand.Server;
import com.agnitas.util.TarGzUtilities;
import com.agnitas.util.TextTableBuilder;
import com.agnitas.util.Version;
import com.agnitas.util.ZipUtilities;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.Anonymous;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.agnitas.emm.core.autoimport.bean.AutoImport;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

public class ServerStatusController implements XssCheckAware {

	private static final Logger logger = LogManager.getLogger(ServerStatusController.class);
	
	private static final String TEMP_LICENSE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "License";

	private static final String FILE_NAME_DATE_FORMAT = DateUtilities.YYYY_MM_DD_HH_MM_SS_FORFILENAMES;

	protected final LogonService logonService;
	protected final CompanyDao companyDao;
	private final DbSchemaSnapshotService dbSchemaSnapshotService;
	private final ServerStatusService serverStatusService;
	private final UserActivityLogService userActivityLogService;
	private final JobQueueService jobQueueService;
	private final JavaMailService javaMailService;
	private final LicenseDao licenseDao;
	private final ServerMessageDao serverMessageDao;
	private final ConfigService configService;

	private final ServerConfigFormValidator configFormValidator = new ServerConfigFormValidator();
	private final ServerStatusFormValidator statusFormValidator = new ServerStatusFormValidator();

	public ServerStatusController(ServerStatusService serverStatusService, UserActivityLogService userActivityLogService,
								  JobQueueService jobQueueService, JavaMailService javaMailService, LogonService logonService,
								  LicenseDao licenseDao, ServerMessageDao serverMessageDao, ConfigService configService, CompanyDao companyDao,
								  DbSchemaSnapshotService dbSchemaSnapshotService) {
		this.serverStatusService = serverStatusService;
		this.userActivityLogService = userActivityLogService;
		this.jobQueueService = jobQueueService;
		this.javaMailService = javaMailService;
		this.logonService = logonService;
		this.licenseDao = licenseDao;
		this.serverMessageDao = serverMessageDao;
		this.configService = configService;
		this.companyDao = companyDao;
        this.dbSchemaSnapshotService = dbSchemaSnapshotService;
    }

	@InitBinder
	public void initBinder(WebDataBinder binder, Admin admin) {
		if (admin != null) {
			binder.registerCustomEditor(Date.class, new CustomDateEditor(admin.getDateFormat(), true));
		}
	}

	@ModelAttribute
	public JobQueueFormSearchParams getSearchParams() {
		return new JobQueueFormSearchParams();
	}

	@RequestMapping(value = "/view.action", method = { RequestMethod.GET, RequestMethod.POST })
	public String view(HttpServletRequest request, Admin admin, Model model, ServerStatusForm form) {
		model.addAttribute("serverStatus", serverStatusService.getServerStatus(request.getServletContext(), admin));
		if (admin.isRedesignedUiUsed()) {
			model.addAttribute("checkDbSchemaAllowed", dbSchemaSnapshotService.exists());
		}

		return "server_status_view";
	}

	@PostMapping("/config/save.action")
	public String saveConfig(Admin admin, ServerStatusForm form, Popups popups) {
		if (!configFormValidator.validate(form.getConfigForm(), popups)) {
			return MESSAGES_VIEW;
		}

		ServerConfigForm configForm = form.getConfigForm();
		boolean saved = serverStatusService.saveServerConfig(configForm.getCompanyId(), configForm.getName(), configForm.getValue(), configForm.getDescription());

		if (saved) {
			userActivityLogService.writeUserActivityLog(admin, new UserAction("server status", "change server configuration: "));
		} else {
			popups.alert(ERROR_MSG);
		}

		return String.format("redirect:/serverstatus/config/view.action?configForm.companyId=%d&configForm.name=%s", configForm.getCompanyId(),
				UriUtils.encodeQueryParam(configForm.getName(), "UTF-8"));
	}

	@RequestMapping(value = "/config/view.action", method = { RequestMethod.GET, RequestMethod.POST })
	public String viewConfig(Admin admin, RedirectAttributes model, ServerStatusForm form, Popups popups) {
		ServerConfigForm configForm = form.getConfigForm();

		int clientId = configForm.getCompanyId();
		String configName = configForm.getName();

		ConfigValueDto config = serverStatusService.getServerConfigurations(clientId, configName);

		boolean isExisted = config.getCompanyId() > 0 && StringUtils.isNotEmpty(config.getName());
		configForm.setCompanyId(config.getCompanyId());
		configForm.setName(config.getName());
		configForm.setValue(config.getValue());
		configForm.setDescription(config.getDescription());

		if (isExisted) {
			userActivityLogService.writeUserActivityLog(admin,
					new UserAction("server status", String.format("view config %s (%d)", configForm.getName(), configForm.getCompanyId())));
		} else {
			logger.error("Cannot find config value: " + configName + " (" + clientId + ")");
			popups.alert("server.config.invalid", String.format("%s (%d)", configName, clientId));
		}

		model.addFlashAttribute("serverStatusForm", form);

		return "redirect:/serverstatus/view.action";
	}
	
	@RequestMapping(value = "/config/download.action", method = { RequestMethod.GET, RequestMethod.POST}, produces = "application/zip")
	public Object downloadConfig() throws Exception {
		File configFile = null;
		try {
			// get file
			configFile = serverStatusService.downloadConfigFile();
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + configFile.getName() + "\";")
					.contentLength(configFile.length())
					.contentType(MediaType.parseMediaType("application/zip"))
					.body(new DeleteFileAfterSuccessReadResource(configFile));
		} catch (IOException ex) {
			logger.error("Error writing file to output stream. Filename was " + "'" + configFile + "'", ex);
			throw new RuntimeException("IOError writing file to output stream");
		}
	}
	
	@RequestMapping(value = "/job/start.action", method = { RequestMethod.GET, RequestMethod.POST })
	public String startJob(Admin admin, RedirectAttributes model, ServerStatusForm form, Popups popups) {
		if (!statusFormValidator.validateJobDescription(form, popups)) {
			return MESSAGES_VIEW;
		}

		Message message;
		String description = form.getJobStart();
		try {
			jobQueueService.startSpecificJobQueueJob(description);
			message = Message.exact("Job " + description + " started on " + AgnUtils.getHostName() + ". See DB data for results");
			popups.success(message);

			userActivityLogService.writeUserActivityLog(admin, new UserAction("server status", "started job '" + description + "'"), logger);
		} catch (Exception e) {
			logger.error("Error while starting job queue by description " + description, e);
			popups.alert(ERROR_MSG);
		}

		model.addFlashAttribute(form);

		return "redirect:/serverstatus/view.action";
	}

	@RequestMapping(value = "/testemail/send.action", method = { RequestMethod.GET, RequestMethod.POST })
	public String sendTestEmail(Admin admin, ServerStatusForm form, RedirectAttributes model, Popups popups) {
		if(!statusFormValidator.validateTestEmail(form, popups)) {
			return MESSAGES_VIEW;
		}

		String testEmail = form.getSendTestEmail();
		SimpleServiceResult mailSendResult = serverStatusService.sendTestMail(admin, testEmail);

		userActivityLogService.writeUserActivityLog(admin, new UserAction("server status", "send test email to " + testEmail), logger);

		model.addFlashAttribute("serverStatusForm", form);

		popups.addPopups(mailSendResult);

		return "redirect:/serverstatus/view.action";
	}

	@RequestMapping(value = "/diagnosis/show.action", method = { RequestMethod.GET, RequestMethod.POST })
	public String diagnosisView(HttpServletRequest request, Admin admin, ServerStatusForm form, RedirectAttributes model, Popups popups) {
		if (!statusFormValidator.validateDiagnosticEmail(form, popups)) {
			return MESSAGES_VIEW;
		}

		SimpleServiceResult diagnosisSendResult = serverStatusService.sendDiagnosisInfo(request.getServletContext(), admin, form.getSendDiagnosis());

		userActivityLogService.writeUserActivityLog(admin, new UserAction("server status", "send diagnosis to " + form.getSendDiagnosis()), logger);

		model.addFlashAttribute(form);
		popups.addPopups(diagnosisSendResult);

		return "redirect:/serverstatus/view.action";
	}

	@RequestMapping(value = "/logfile/download.action", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public @ResponseBody FileSystemResource logFileDownload(Admin admin, HttpServletResponse response) throws IOException {
		String logFilePath = AgnUtils.getUserHomeDir() + "/logs/webapps/emm.log";
		File zippedLogFile = ZipUtilities.zipFile(new File(logFilePath));
		String downloadFileName = String.format("emm_logfile_%s.zip", new SimpleDateFormat(FILE_NAME_DATE_FORMAT).format(new Date()));

		userActivityLogService.writeUserActivityLog(admin, new UserAction("server status", "download log file"), logger);

		HttpUtils.setDownloadFilenameHeader(response, downloadFileName);
		response.setContentType("application/zip");

		return new DeleteFileAfterSuccessReadResource(zippedLogFile);
	}

	@RequestMapping(value = "/logfile/view.action", method = { RequestMethod.GET, RequestMethod.POST })
	public String logFileView(Admin admin, Model model) {
		String logFilePath = AgnUtils.getUserHomeDir() + "/logs/webapps/emm.log";

		String logFileContent = "";
		try {
			logFileContent = FileUtils.readFileToString(new File(logFilePath), "UTF-8");
		} catch (Exception e) {
			logger.error("Cannot read log file " + logFilePath, e);
		}

		userActivityLogService.writeUserActivityLog(admin, new UserAction("server status", "view log file"), logger);

		model.addAttribute("logFileContent", logFileContent);

		return "server_log_view";
	}

	@RequestMapping(value = "/jobqueue/view.action", method = { RequestMethod.GET, RequestMethod.POST })
	public String jobQueueView(@ModelAttribute("filter") JobQueueOverviewFilter filter, @ModelAttribute JobQueueFormSearchParams searchParams, Admin admin, Model model) {
		if (admin.isRedesignedUiUsed()) {
			FormUtils.syncSearchParams(searchParams, filter, true);
		}
		model.addAttribute("activeJobQueueList", jobQueueService.getOverview(filter));
		if (admin.isRedesignedUiUsed()) {
			model.addAttribute("notFilteredFullListSize", filter.isUiFiltersSet() ? jobQueueService.getCountForOverview() : -1);
		}
		model.addAttribute("dateTimeFormat", admin.getDateTimeFormat());
		userActivityLogService.writeUserActivityLog(admin, new UserAction("server status", "job queue view"), logger);

		return "server_job_queue_view";
	}

	@GetMapping("/jobqueue/search.action")
	public String jobQueueSearch(@ModelAttribute JobQueueOverviewFilter filter, @ModelAttribute JobQueueFormSearchParams searchParams) {
		FormUtils.syncSearchParams(searchParams, filter, false);
		return "redirect:/serverstatus/jobqueue/view.action";
	}

	@Anonymous
	@RequestMapping(value = "/version.action", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody String showVersion() {
		return serverStatusService.getVersion();
	}

	@Anonymous
	@RequestMapping(value = "/release.action", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody Object showReleaseVersions(@RequestParam(value = "hostname", required = false) String hostName, @RequestParam(value = "application", required = false) String application, @RequestParam(value = "format", required = false) String outputFormat) throws Exception {
		List<Map<String, Object>> data = configService.getReleaseData(hostName, application);
		SimpleDateFormat format = new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS);
		
		TextTableBuilder textTable = new TextTableBuilder("startup_timestamp", "hostname", "application", "version_number", "build_time", "build_host", "build_user");
		
		for (Map<String, Object> item : data) {
			textTable.startNewLine();
			textTable.addValueToCurrentLine(format.format((Date) item.get("startup_timestamp")));
			textTable.addValueToCurrentLine((String) item.get("host_name"));
			textTable.addValueToCurrentLine((String) item.get("application_name"));
			textTable.addValueToCurrentLine((String) item.get("version_number"));
			if (item.get("build_time") != null) {
				textTable.addValueToCurrentLine(format.format((Date) item.get("build_time")));
			} else {
				textTable.addValueToCurrentLine("");
			}
			textTable.addValueToCurrentLine((String) item.get("build_host"));
			textTable.addValueToCurrentLine((String) item.get("build_user"));
		}
		
		String headText = "Release Log" + (StringUtils.isNotBlank(hostName) ? " " + hostName : "") + (StringUtils.isNotBlank(application) ? " " + application : "");
		
		if ("html".equalsIgnoreCase(outputFormat)) {
			return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/html"))
                .body(new InputStreamResource(new ByteArrayInputStream(textTable.toHtmlString(headText).getBytes("UTF-8"))));
		} else if ("csv".equalsIgnoreCase(outputFormat)) {
			return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/comma-separated-values"))
                .body(new InputStreamResource(new ByteArrayInputStream(textTable.toCsvString().getBytes("UTF-8"))));
		} else {
			return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/plain"))
                .body(new InputStreamResource(new ByteArrayInputStream(textTable.toString().getBytes("UTF-8"))));
		}
	}

	@Anonymous
	@RequestMapping(value = "/premium.action", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody String showPremiumPermissionsList() {
		List<String> premiumList = new ArrayList<>();
		for (Permission permission : Permission.getAllSystemPermissions()) {
			if (permission.getPermissionType() == PermissionType.Premium) {
				premiumList.add(permission.getTokenString());
			}
		}
		Collections.sort(premiumList);
		return StringUtils.join(premiumList, "\n");
	}

	@Anonymous
	@RequestMapping(value = "/jobqueuestatus.action", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String showJobqueueStatus(HttpServletRequest request, HttpServletResponse response) {
		try {
			JsonObject resultJsonObject = new JsonObject();
			int responseCode;
			
			Admin admin = AgnUtils.getAdmin(request);
			if (admin == null) {
				admin = loginAdminByRequestParameters(request, admin);
			}
	
			if (admin == null) {
				resultJsonObject.add("error", "ACCESS DENIED");
				responseCode = 403;
			} else if (!admin.permissionAllowed(Permission.SERVER_STATUS)) {
				resultJsonObject.add("error", "PERMISSION DENIED");
				responseCode = 403;
			} else {
				try {
					List<AutoImport> stallingAutoImports = serverStatusService.getStallingAutoImports();
					int stallingImports = serverStatusService.getStallingImportsAmount(configService.getIntegerValue(ConfigValue.MaxUserImportDurationMinutes));
					
					if (!serverStatusService.isJobQueueRunning()) {
						resultJsonObject.add("status", "ERROR: Jobqueue is not running");
						responseCode = 500;
					} else if (!serverStatusService.isJobQueueStatusOK()) {
						if (StringUtils.isNotBlank(request.getParameter("acknowledge"))) {
							int idToAcknowledge = Integer.parseInt(request.getParameter("acknowledge"));
							serverStatusService.acknowledgeErroneousJob(idToAcknowledge);
							JobDto job = jobQueueService.getJob(idToAcknowledge);
							javaMailService.sendEmail(admin.getCompanyID(), job.getEmailOnError(), "Erroneous Job \"" + job.getDescription() + "\" (ID: " + idToAcknowledge + ", Criticality: " + job.getCriticality() + ") acknowledged", "No more further emails about this error will be sent: \n" + job.getLastResult(), "No more further emails about this error will be sent: <br />\n" + job.getLastResult());
						}
						
						resultJsonObject.add("message", "WARNING: Some jobqueue jobs have errors");
						JsonArray erroneousJobsArray = new JsonArray();
						resultJsonObject.add("erroneousJobs", erroneousJobsArray);
						List<JobDto> erroneousJobs = serverStatusService.getErroneousJobs();
						for (JobDto jobDto : erroneousJobs) {
							JsonObject erroneousJobObject = new JsonObject();
							erroneousJobObject.add("id", jobDto.getId());
							erroneousJobObject.add("description", jobDto.getDescription());
							erroneousJobObject.add("criticality", jobDto.getCriticality());
							erroneousJobObject.add("lastResult", jobDto.getLastResult());
							erroneousJobObject.add("laststart", jobDto.getLastStart());
							erroneousJobObject.add("acknowledged", jobDto.isAcknowledged());
							erroneousJobsArray.add(erroneousJobObject);
						}
						responseCode = 409;
					} else if (stallingAutoImports != null && stallingAutoImports.size() > 0) {
						List<String> stallingAutoImportTexts = new ArrayList<>();
						for (AutoImport stallingAutoImport : stallingAutoImports) {
							stallingAutoImportTexts.add("\"" + stallingAutoImport.getDescription() + "\" (CompanyID: " + stallingAutoImport.getCompanyId() + ", AutoImportID: " + stallingAutoImport.getAutoImportId() + ")");
						}
						
						resultJsonObject.add("status", "ERROR: Some auto import job is stalling (" + StringUtils.join(stallingAutoImportTexts, ", ") + ")");
						
						JsonArray erroneousImportsArray = new JsonArray();
						List<String> erroneousImports = serverStatusService.getErroneousImports();
						for (String importName : erroneousImports) {
							JsonObject erroneousJobObject = new JsonObject();
							erroneousJobObject.add("description", importName);
							erroneousImportsArray.add(erroneousJobObject);
						}
						resultJsonObject.add("erroneousJobs", erroneousImportsArray);
						
						responseCode = 409;
					} else if (stallingImports > 0) {
						resultJsonObject.add("status", "ERROR: Some import is stalling (Amount: " + stallingImports + ")");
						
						JsonArray erroneousImportsArray = new JsonArray();
						List<String> erroneousImports = serverStatusService.getErroneousImports();
						for (String importName : erroneousImports) {
							JsonObject erroneousJobObject = new JsonObject();
							erroneousJobObject.add("description", importName);
							erroneousImportsArray.add(erroneousJobObject);
						}
						resultJsonObject.add("erroneousJobs", erroneousImportsArray);
						
						responseCode = 409;
					} else if (serverStatusService.isExportStalling()) {
						resultJsonObject.add("status", "ERROR: Some export job is stalling");
						
						JsonArray erroneousExportsArray = new JsonArray();
						List<String> erroneousExports = serverStatusService.getErroneousExports();
						for (String exportName : erroneousExports) {
							JsonObject erroneousJobObject = new JsonObject();
							erroneousJobObject.add("description", exportName);
							erroneousExportsArray.add(erroneousJobObject);
						}
						resultJsonObject.add("erroneousJobs", erroneousExportsArray);
						
						responseCode = 409;
					} else {
						resultJsonObject.add("status", "OK");
						responseCode = 200;
					}
				} catch (Exception e) {
					resultJsonObject.add("error", "Internal server error: Cannot read erroneous jobs: " + e.getMessage());
					responseCode = 500;
				}
			}
			
			response.setStatus(responseCode);
			return JsonWriter.getJsonItemString(resultJsonObject);
		} catch (Exception e) {
			response.setStatus(500);
			return "\"Internal server error: " + e.getMessage() + "\"";
		}
	}

	@Anonymous
	@RequestMapping(value = "/dbstatus.action", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody String showDbStatus(HttpServletRequest request, HttpServletResponse response) {
		Admin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			try {
				// User has no session, but try to logon user by given parameters
				// This is especially for nagios/icinga checks
				admin = loginAdminByRequestParameters(request, admin);

				if (admin == null) {
					throw new Exception("Access denied");
				}
			} catch (Exception e) {
				return "ACCESS DENIED";
			}
		}

		String result = "";
		int httpCode;
		boolean foundErroneousDbVersion = false;
		for (VersionStatus versionItem : serverStatusService.getLatestDBVersionsAndErrors()) {
			if (!versionItem.getStatus()) {
				result += "\n\t" + versionItem.toString();
				foundErroneousDbVersion = true;
			}
		}
		if (foundErroneousDbVersion) {
			result = "ERROR: DB is missing some version updates" + result;
			httpCode = 500;
		} else {
			result = "OK";
			httpCode = 200;
		}

		response.setStatus(httpCode);
		return result;
	}

	@Anonymous
	@RequestMapping(value = "/overallstatus.action", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody String showOverallStatus(HttpServletRequest request, HttpServletResponse response) {
		Admin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			try {
				admin = loginAdminByRequestParameters(request, admin);

				if (admin == null) {
					throw new Exception("Access denied");
				}
			} catch (Exception e) {
				return "ACCESS DENIED";
			}
		}

		String result = "";
		int httpCode;
		boolean foundError = false;
		for (VersionStatus versionItem : serverStatusService.getLatestDBVersionsAndErrors()) {
			if (!versionItem.getStatus()) {
				result += "\nDB is missing version " + versionItem.toString();
				foundError = true;
			}
		}

		List<AutoImport> stallingAutoImports = serverStatusService.getStallingAutoImports();
		int stallingImports = serverStatusService.getStallingImportsAmount(configService.getIntegerValue(ConfigValue.MaxUserImportDurationMinutes));
		
		if (serverStatusService.checkActiveNode() && !serverStatusService.isJobQueueRunning()) {
			result = "\nJobqueue is not running";
			foundError = true;
		} else if (!serverStatusService.isJobQueueStatusOK()) {
			result = "\nSome jobqueue jobs have errors";
			foundError = true;
		} else if (stallingAutoImports != null && stallingAutoImports.size() > 0) {
			result = "\nSome auto import job is stalling";
			foundError = true;
		} else if (stallingImports > 0) {
			result = "\nSome import is stalling";
			foundError = true;
		}

		if (foundError) {
			result = "ERROR:" + result;
			httpCode = 500;
		} else {
			result = "OK";
			httpCode = 200;
		}

		response.setStatus(httpCode);
		return result;
	}
	
    @RequestMapping(value = "/licensedata/licenseupload.action", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.TEXT_PLAIN_VALUE, consumes = {"multipart/form-data"})
    public String licenseFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes, Model model, HttpServletRequest request) {
    	Admin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			try {
				admin = loginAdminByRequestParameters(request, admin);

				if (admin == null) {
					throw new Exception("Access denied");
				}
			} catch (Exception e) {
				return "ACCESS DENIED";
			}
		}
		if (!admin.permissionAllowed(Permission.SERVER_STATUS)) {
			return "PERMISSION DENIED";
		} else {
	    	if (file.isEmpty()) {
	            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
	            return "redirect:/serverstatus/view.action";
	        } else {
        		String currentTimeString = new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS_FORFILENAMES).format(new Date());
        		File unzippedLicenseDataDirectory = new File(TEMP_LICENSE_DIRECTORY + "/" + currentTimeString);
	        	try {
	        		if (!new File(TEMP_LICENSE_DIRECTORY).exists()) {
	        			new File(TEMP_LICENSE_DIRECTORY).mkdirs();
	        		}
	        		if (file.getOriginalFilename().toLowerCase().endsWith(".zip")) {
		        		File uploadedLicenseDataFile = new File(TEMP_LICENSE_DIRECTORY + "/" + currentTimeString + ".zip");
		        		try {
							file.transferTo(uploadedLicenseDataFile);
							ZipUtilities.decompress(uploadedLicenseDataFile, unzippedLicenseDataDirectory);
						} finally {
							if (uploadedLicenseDataFile.exists()) {
								uploadedLicenseDataFile.delete();
							}
						}
	        		} else if (file.getOriginalFilename().toLowerCase().endsWith(".tar.gz")) {
		        		File uploadedLicenseDataFile = new File(TEMP_LICENSE_DIRECTORY + "/" + currentTimeString + ".tar.gz");
		        		try {
			        		file.transferTo(uploadedLicenseDataFile);
			        		TarGzUtilities.decompress(uploadedLicenseDataFile, unzippedLicenseDataDirectory);
						} finally {
							if (uploadedLicenseDataFile.exists()) {
								uploadedLicenseDataFile.delete();
							}
						}
	        		} else if (file.getOriginalFilename().toLowerCase().endsWith(".tgz")) {
		        		File uploadedLicenseDataFile = new File(TEMP_LICENSE_DIRECTORY + "/" + currentTimeString + ".tgz");
		        		try {
			        		file.transferTo(uploadedLicenseDataFile);
			        		TarGzUtilities.decompress(uploadedLicenseDataFile, unzippedLicenseDataDirectory);
						} finally {
							if (uploadedLicenseDataFile.exists()) {
								uploadedLicenseDataFile.delete();
							}
						}
	        		} else {
	        			throw new Exception("Unknown license data format");
	        		}
	        		
	        		File licenseDataFile = new File(unzippedLicenseDataDirectory + "/" + "emm.license.xml");
	        		File licenseSignatureDataFile = new File(unzippedLicenseDataDirectory + "/" + "emm.license.xml.sig");
					String currentVersionString = configService.getValue(ConfigValue.ApplicationVersion);
					Version currentVersion = new Version(currentVersionString);
					String majorMinorVersion = String.format("%d.%02d", currentVersion.getMajorVersion(), currentVersion.getMinorVersion());
					Matcher licenseVersionMatcher = Pattern.compile(".*?([0-9]+[.][0-9]+).*").matcher(file.getOriginalFilename());

					if (licenseDataFile.exists() && licenseSignatureDataFile.exists()) {
	        			byte [] licenseDataArray = FileUtils.readFileToByteArray(licenseDataFile);
	    	        	byte [] licenseSignatureDataArray = FileUtils.readFileToByteArray(licenseSignatureDataFile);
						Boolean hasUnlimited = new String(licenseDataArray).contains("<maximumVersion>Unlimited</maximumVersion>");

	    	        	if (licenseDataArray == null || licenseDataArray.length == 0) {
	    	        		throw new Exception("Missing license data content");
	    	        	} else if (licenseSignatureDataArray == null || licenseSignatureDataArray.length == 0) {
	    	        		throw new Exception("Missing license data signature content");
	    	        	}else if (!hasUnlimited && (!licenseVersionMatcher.find() || !new String(majorMinorVersion).equals(licenseVersionMatcher.group(1))  || !new String(licenseDataArray).contains("<maximumVersion>" + licenseVersionMatcher.group(1) + "</maximumVersion>")) ) {
							logger.error(new String(majorMinorVersion).equals(licenseVersionMatcher.group(1)));
							throw new Exception("License version does not match maximum version or application version");
						}else if (hasUnlimited && !(new String(licenseDataArray).contains("<licenseID>" + 1 + "</licenseID>") || new String(licenseDataArray).contains("<licenseID>" + 1000 + "</licenseID>"))) {
							throw new Exception("For this license, maxium version 'Unlimited' is not allowed");
						}else if (!new String(licenseDataArray).contains("<licenseID>" + configService.getLicenseID() + "</licenseID>")) {
	    	        		throw new Exception("Wrong license id in license data content. Expected license id: " + configService.getLicenseID());
	    	        	} else {
							licenseDao.storeLicense(licenseDataArray, licenseSignatureDataArray, new Date());
							userActivityLogService.writeUserActivityLog(admin, "license update", "License update");
				            redirectAttributes.addFlashAttribute("message", "You successfully uploaded new license data '" + file.getOriginalFilename() + "'");
				            serverMessageDao.pushCommand(new ServerCommand(Server.ALL, Command.RELOAD_LICENSE_DATA, new Date(), admin.getAdminID(), "New license data uploaded"));
				            configService.enforceExpiration();
	    	        	}
	        		} else {
	        			throw new Exception("Unknown license data content");
	        		}
		        } catch (Exception e) {
		        	logger.error("Cannot update license data: " + e.getMessage(), e);
		            redirectAttributes.addFlashAttribute("message", "Upload of license data was not successful for '" + file.getOriginalFilename() + "': " + e.getMessage());
		        } finally {
		        	try {
						FileUtils.deleteDirectory(unzippedLicenseDataDirectory);
					} catch (Exception e) {
						logger.error("Error deleting temporary directory", e);

						// do nothing else
					}
		        }
	        	
	    		model.addAttribute("serverStatus", serverStatusService.getServerStatus(request.getServletContext(), admin));

	    		return "redirect:/serverstatus/view.action";
	        }
		}
    }

	@PostMapping(value = "/db-schema/upload.action")
	public String uploadDbSchemaSnapshot(@RequestParam MultipartFile file, @RequestParam(required = false) Boolean overwrite,
										 Popups popups, Model model) {
		ServiceResult<DbSchemaSnapshot> result = dbSchemaSnapshotService.read(file);
		popups.addPopups(result);

		if (!result.isSuccess()) {
			return MESSAGES_VIEW;
		}

		DbSchemaSnapshot snapshot = result.getResult();

		if (!Boolean.TRUE.equals(overwrite) && dbSchemaSnapshotService.exists(snapshot.getVersionNumber())) {
			model.addAttribute("snapshotVersion", snapshot.getVersionNumber());
			return "server_status_db_schema_overwrite";
		}

		dbSchemaSnapshotService.save(snapshot);
		popups.success(CHANGES_SAVED_MSG);

		return "redirect:/serverstatus/view.action";
	}
    
    @GetMapping("/serverstatus/view.action")
    public String uploadStatus() {
        return "server_status_view";
    }

	@RequestMapping(value = "/updatecheck.action", method = { RequestMethod.GET, RequestMethod.POST })
    public String updateCheck(Popups popups) throws Exception {
		String currentVersionString = configService.getValue(ConfigValue.ApplicationVersion);
		Version currentVersion = new Version(currentVersionString);
		if(currentVersion.getHotfixVersion() != 0){
			Version availableVersion = serverStatusService.getAvailableUpdateVersion(currentVersion);

			if (availableVersion.compareTo(currentVersion) >= 1) {
				popups.warning("server.current_version.higher_version_available", availableVersion.toString(), currentVersionString, configService.getValue(ConfigValue.UpdateInformationLink));
			} else {
				popups.success("server.current_version.uptodate", currentVersionString);
			}
		}else {
				popups.success("server.current_version.uptodate", currentVersionString);
		}

        return MESSAGES_VIEW;
    }

	@GetMapping("/schema/check.action")
	public String checkDbSchema(Model model, Popups popups) {
		if (!dbSchemaSnapshotService.exists()) {
			throw new IllegalStateException("DB schema can't be checked due to missing snapshot file!");
		}

		DbSchemaCheckResult result = dbSchemaSnapshotService.check();
		if (result.isSuccessful()) {
			popups.success("GWUA.checkDbSchema.success");
			return MESSAGES_VIEW;
		}

		model.addAttribute("checkResult", result);
		return "server_status_db_schema_diff";
	}

	@GetMapping("/schema/diff/download.action")
	public ResponseEntity<DeleteFileAfterSuccessReadResource> downloadDbSchemaDifferences() {
		File file = dbSchemaSnapshotService.getFileWithDifferences();
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"db-schema-diff.json\"")
				.contentLength(file.length())
				.contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
				.body(new DeleteFileAfterSuccessReadResource(file));
	}
	
	@Anonymous
	@RequestMapping(value = "/killRunningImports.action", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody List<String> killRunningImports() {
		return serverStatusService.killRunningImports();
	}
	
	@Anonymous
	@RequestMapping(value = "/getSystemStatus.action", method = { RequestMethod.GET, RequestMethod.POST }, produces = {MediaType.APPLICATION_JSON_VALUE})
	public @ResponseBody List<Object> getSystemStatus() {
		return serverStatusService.getSystemStatus().toList();
	}

	private Admin loginAdminByRequestParameters(HttpServletRequest request, Admin admin) throws LogonServiceException {
		String basicAuthorizationUsername = HttpUtils.getBasicAuthenticationUsername(request);
		String basicAuthorizationPassword = HttpUtils.getBasicAuthenticationPassword(request);
		String username = StringUtils.isNotBlank(basicAuthorizationUsername) ? basicAuthorizationUsername : request.getParameter("username");
		String password = StringUtils.isNotBlank(basicAuthorizationPassword) ? basicAuthorizationPassword : request.getParameter("password");
		
		if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
			admin = logonService.getAdminByCredentials(username, password, request.getRemoteAddr());
		}
		return admin;
	}

	@Anonymous
	@RequestMapping(value = "/externalView.action", method = { RequestMethod.GET, RequestMethod.POST })
	// TODO: EMMGUI-714: remove when old design will be removed
	public String externalView(HttpServletRequest request, Model model, ServerStatusForm form) {
		model.addAttribute("serverStatus", serverStatusService.getAnonymousServerStatus(request.getServletContext()));
		model.addAttribute("appVersion", configService.getValue(ConfigValue.ApplicationVersion));
		return "server_status_external_view";
	}

	@Anonymous
	@GetMapping(value = "/externalViewRedesigned.action")
	public String externalViewRedesigned(HttpServletRequest request, Model model) {
		model.addAttribute("serverStatus", serverStatusService.getAnonymousServerStatus(request.getServletContext()));
		model.addAttribute("appVersion", configService.getValue(ConfigValue.ApplicationVersion));
		return "server_status_external_view_redesigned";
	}

	@GetMapping(value = "/db-schema/download.action")
	public ResponseEntity<DeleteFileAfterSuccessReadResource> downloadDbSchemaSnapshot() {
		File file = dbSchemaSnapshotService.create();
		String filename = dbSchemaSnapshotService.generateFileName();

		return ResponseEntity.ok()
				.contentLength(file.length())
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.CONTENT_DISPOSITION, HttpUtils.getContentDispositionAttachment(filename))
				.body(new DeleteFileAfterSuccessReadResource(file));
	}
}
