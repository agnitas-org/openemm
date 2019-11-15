/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverstatus.web;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.JobDto;
import org.agnitas.service.JobQueueService;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.ServerCommand;
import org.agnitas.util.ServerCommand.Command;
import org.agnitas.util.ServerCommand.Server;
import org.agnitas.util.TarGzUtilities;
import org.agnitas.util.ZipUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComServerMessageDao;
import com.agnitas.dao.LicenseDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.logon.forms.LogonForm;
import com.agnitas.emm.core.logon.service.ComLogonService;
import com.agnitas.emm.core.logon.service.Logon;
import com.agnitas.emm.core.serverstatus.bean.VersionStatus;
import com.agnitas.emm.core.serverstatus.dto.ConfigValueDto;
import com.agnitas.emm.core.serverstatus.forms.ServerConfigForm;
import com.agnitas.emm.core.serverstatus.forms.ServerStatusForm;
import com.agnitas.emm.core.serverstatus.service.ServerStatusService;
import com.agnitas.messages.Message;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.Version;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.Anonymous;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@PermissionMapping("server.status")
@RequestMapping("/serverstatus")
public class ServerStatusController {
	private static final Logger logger = Logger.getLogger(ServerStatusController.class);
	
	private static final String TEMP_LICENSE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "License";

	private static final String FILE_NAME_DATE_FORMAT = DateUtilities.YYYY_MM_DD_HH_MM_SS_FORFILENAMES;

	private ServerStatusService serverStatusService;

	private UserActivityLogService userActivityLogService;

	private JobQueueService jobQueueService;

	protected JavaMailService javaMailService;

	protected ComLogonService logonService;
	
	private LicenseDao licenseDao;
	
	private ComServerMessageDao serverMessageDao;
	
	private ConfigService configService;

	public ServerStatusController(ServerStatusService serverStatusService, UserActivityLogService userActivityLogService, JobQueueService jobQueueService, JavaMailService javaMailService, ComLogonService logonService, LicenseDao licenseDao, ComServerMessageDao serverMessageDao, ConfigService configService) {
		this.serverStatusService = serverStatusService;
		this.userActivityLogService = userActivityLogService;
		this.jobQueueService = jobQueueService;
		this.javaMailService = javaMailService;
		this.logonService = logonService;
		this.licenseDao = licenseDao;
		this.serverMessageDao = serverMessageDao;
		this.configService = configService;
	}

	@RequestMapping(value = "/view.action", method = { RequestMethod.GET, RequestMethod.POST })
	public String view(ComAdmin admin, Model model, ServerStatusForm form) {
		model.addAttribute("serverStatus", serverStatusService.getServerStatus(admin));

		return "server_status_view";
	}

	@PostMapping("/config/save.action")
	public String saveConfig(ComAdmin admin, @Validated(ServerConfigForm.Configuration.class) ServerStatusForm form, BindingResult result, Popups popups) throws UnsupportedEncodingException {
		if (result.hasErrors()) {
			return "messages";
		}

		ServerConfigForm configForm = form.getConfigForm();
		boolean saved = serverStatusService.saveServerConfig(configForm.getCompanyId(), configForm.getName(), configForm.getValue(), configForm.getDescription());

		if (saved) {
			userActivityLogService.writeUserActivityLog(admin, new UserAction("server status", "change server configuration: "));
		} else {
			popups.alert("Error");
		}

		return String.format("redirect:/serverstatus/config/view.action?configForm.companyId=%d&configForm.name=%s", configForm.getCompanyId(),
				UriUtils.encodeQueryParam(configForm.getName(), "UTF-8"));
	}

	@RequestMapping(value = "/config/view.action", method = { RequestMethod.GET, RequestMethod.POST })
	public String viewConfig(ComAdmin admin, RedirectAttributes model, ServerStatusForm form, Popups popups) {
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

	@RequestMapping(value = "/job/start.action", method = { RequestMethod.GET, RequestMethod.POST })
	public String startJob(ComAdmin admin, RedirectAttributes model, @Validated(ServerStatusForm.StartJob.class) ServerStatusForm form, BindingResult result, Popups popups) {
		if (result.hasErrors()) {
			return "messages";
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
			popups.alert("Error");
		}

		model.addFlashAttribute(form);

		return "redirect:/serverstatus/view.action";
	}

	@RequestMapping(value = "/testemail/send.action", method = { RequestMethod.GET, RequestMethod.POST })
	public String sendTestEmail(ComAdmin admin, @Validated(ServerStatusForm.SendTestEmail.class) ServerStatusForm form, BindingResult result, RedirectAttributes model, Popups popups) {
		if (result.hasErrors()) {
			return "messages";
		}

		String testEmail = form.getSendTestEmail();
		SimpleServiceResult mailSendResult = serverStatusService.sendTestMail(admin, testEmail);

		userActivityLogService.writeUserActivityLog(admin, new UserAction("server status", "send test email to " + testEmail), logger);

		model.addFlashAttribute("serverStatusForm", form);

		showPopups(popups, mailSendResult.isSuccess(), mailSendResult.getMessages());

		return "redirect:/serverstatus/view.action";
	}

	@RequestMapping(value = "/diagnosis/show.action", method = { RequestMethod.GET, RequestMethod.POST })
	public String diagnosisView(ComAdmin admin, @Validated(ServerStatusForm.SendDiagnosis.class) ServerStatusForm form, BindingResult result, RedirectAttributes model, Popups popups) {
		if (result.hasErrors()) {
			return "messages";
		}

		SimpleServiceResult diagnosisSendResult = serverStatusService.sendDiagnosisInfo(admin, form.getSendDiagnosis());

		userActivityLogService.writeUserActivityLog(admin, new UserAction("server status", "send diagnosis to " + form.getSendDiagnosis()), logger);

		model.addFlashAttribute(form);
		showPopups(popups, diagnosisSendResult.isSuccess(), diagnosisSendResult.getMessages());

		return "redirect:/serverstatus/view.action";
	}

	@RequestMapping(value = "/logfile/download.action", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public @ResponseBody FileSystemResource logFileDownload(ComAdmin admin, HttpServletResponse response) throws IOException {
		String logFilePath = System.getProperty("user.home") + "/logs/webapps/emm.log";
		File zippedLogFile = ZipUtilities.zipFile(new File(logFilePath));
		String downloadFileName = String.format("emm_logfile_%s.zip", new SimpleDateFormat(FILE_NAME_DATE_FORMAT).format(new Date()));

		userActivityLogService.writeUserActivityLog(admin, new UserAction("server status", "download log file"), logger);

		HttpUtils.setDownloadFilenameHeader(response, downloadFileName);
		response.setContentType("application/zip");

		return new DeleteFileAfterSuccessReadResource(zippedLogFile);
	}

	@RequestMapping(value = "/logfile/view.action", method = { RequestMethod.GET, RequestMethod.POST })
	public String logFileView(ComAdmin admin, Model model) {
		String logFilePath = System.getProperty("user.home") + "/logs/webapps/emm.log";

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
	public String jobQueueView(ComAdmin admin, Model model) {
		model.addAttribute("activeJobQueueList", jobQueueService.getAllActiveJobs());
		model.addAttribute("dateTimeFormat", admin.getDateTimeFormat());
		userActivityLogService.writeUserActivityLog(admin, new UserAction("server status", "job queue view"), logger);

		return "server_job_queue_view";
	}

	private void showPopups(Popups popups, boolean isSuccess, List<Message> messages) {
		if (isSuccess) {
			messages.forEach(popups::success);
		} else {
			messages.forEach(popups::alert);
		}
	}

	@Anonymous
	@RequestMapping(value = "/version.action", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody String showVersion(Logon logon, @Valid @ModelAttribute("form") LogonForm form, Errors errors, Model model, HttpServletRequest request) {
		return serverStatusService.getVersion();
	}

	@Anonymous
	@RequestMapping(value = "/jobqueuestatus.action", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody String showJobqueueStatus(Logon logon, @Valid @ModelAttribute("form") LogonForm form, Errors errors, Model model, HttpServletRequest request, HttpServletResponse response) {
		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			try {
				// User has no session, but try to logon user by given
				// parameters (this is
				// especially for nagios checks)
				if (StringUtils.isNotEmpty(request.getParameter("username")) && StringUtils.isNotEmpty(request.getParameter("password"))) {
					admin = logonService.getAdminByCredentials(request.getParameter("username"), request.getParameter("password"), request.getRemoteAddr());
				}

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
			String result;
			int httpCode;
			if (!serverStatusService.isJobQueueRunning()) {
				result = "ERROR: Jobqueue is not running";
				httpCode = 500;
			} else if (!serverStatusService.isJobQueueStatusOK()) {
				result = "WARNING: Some jobqueue jobs have errors";
				List<JobDto> errorneousJobs = serverStatusService.getErrorneousJobs();
				for (JobDto jobDto : errorneousJobs) {
					result += "\n\t" + jobDto.getDescription() + ": " + StringUtils.substring(jobDto.getLastResult(), 0, 20) + " Laststart: "
							+ new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM_SS).format(jobDto.getLastStart());
				}
				httpCode = 409;
			} else if (serverStatusService.isImportStalling()) {
				result = "WARNING: Some import job is stalling";
				httpCode = 409;
			} else {
				result = "OK";
				httpCode = 200;
			}
			response.setStatus(httpCode);
			return result;
		}
	}

	@Anonymous
	@RequestMapping(value = "/dbstatus.action", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody String showDbStatus(Logon logon, @Valid @ModelAttribute("form") LogonForm form, Errors errors, Model model, HttpServletRequest request, HttpServletResponse response) {
		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			try {
				// User has no session, but try to logon user by given
				// parameters (this is
				// especially for nagios checks)
				if (StringUtils.isNotEmpty(request.getParameter("username")) && StringUtils.isNotEmpty(request.getParameter("password"))) {
					admin = logonService.getAdminByCredentials(request.getParameter("username"), request.getParameter("password"), request.getRemoteAddr());
				}

				if (admin == null) {
					throw new Exception("Access denied");
				}
			} catch (Exception e) {
				return "ACCESS DENIED";
			}
		}

		String result = "";
		int httpCode;
		boolean foundErrorneousDbVersion = false;
		for (VersionStatus versionItem : serverStatusService.getLatestDBVersionsAndErrors()) {
			if (!versionItem.getStatus()) {
				result += "\n\t" + versionItem.toString();
				foundErrorneousDbVersion = true;
			}
		}
		if (foundErrorneousDbVersion) {
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
	public @ResponseBody String showOverallStatus(Logon logon, @Valid @ModelAttribute("form") LogonForm form, Errors errors, Model model, HttpServletRequest request, HttpServletResponse response) {
		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			try {
				// User has no session, but try to logon user by given
				// parameters (this is
				// especially for nagios checks)
				if (StringUtils.isNotEmpty(request.getParameter("username")) && StringUtils.isNotEmpty(request.getParameter("password"))) {
					admin = logonService.getAdminByCredentials(request.getParameter("username"), request.getParameter("password"), request.getRemoteAddr());
				}

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
		if (!serverStatusService.isJobQueueRunning()) {
			result = "\nJobqueue is not running";
			foundError = true;
		} else if (!serverStatusService.isJobQueueStatusOK()) {
			result = "\nSome jobqueue jobs have errors";
			foundError = true;
		} else if (serverStatusService.isImportStalling()) {
			result = "\nSome import job is stalling";
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
    public String licenseFileUpload(@Valid @RequestParam("file") MultipartFile file, ServerStatusForm form, RedirectAttributes redirectAttributes, Model model, HttpServletRequest request) {
    	ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			try {
				// User has no session, but try to logon user by given
				// parameters (this is
				// especially for nagios checks)
				if (StringUtils.isNotEmpty(request.getParameter("username")) && StringUtils.isNotEmpty(request.getParameter("password"))) {
					admin = logonService.getAdminByCredentials(request.getParameter("username"), request.getParameter("password"), request.getRemoteAddr());
				}

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
	        		} else {
	        			throw new Exception("Unknown license data format");
	        		}
	        		
	        		File licenseDataFile = new File(unzippedLicenseDataDirectory + "/" + "emm.license.xml");
	        		File licenseSignatureDataFile = new File(unzippedLicenseDataDirectory + "/" + "emm.license.xml.sig");
	        		if (licenseDataFile.exists() && licenseSignatureDataFile.exists()) {
	        			byte [] licenseDataArray = FileUtils.readFileToByteArray(licenseDataFile);
	    	        	byte [] licenseSignatureDataArray = FileUtils.readFileToByteArray(licenseSignatureDataFile);
	    	        	if (licenseDataArray == null || licenseDataArray.length == 0) {
	    	        		throw new Exception("Missing license data content");
	    	        	} else if (licenseDataArray == null | licenseDataArray.length == 0) {
	    	        		throw new Exception("Missing license data signature content");
	    	        	} else {
							licenseDao.storeLicenseData(licenseDataArray);
							licenseDao.storeLicenseSignatureData(licenseSignatureDataArray);
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
						// do nothing
						e.printStackTrace();
					}
		        }
	        	
	    		model.addAttribute("serverStatus", serverStatusService.getServerStatus(admin));

	    		return "redirect:/serverstatus/view.action";
	        }
		}
    }
    
    @GetMapping("/serverstatus/view.action")
    public String uploadStatus() {
        return "server_status_view";
    }

	@RequestMapping(value = "/updatecheck.action", method = { RequestMethod.GET, RequestMethod.POST })
    public String updateCheck(ComAdmin admin, Model model, ServerStatusForm form, Popups popups) {
    	try {
			String currentVersionString = configService.getValue(ConfigValue.ApplicationVersion);
			Version currentVersion = new Version(currentVersionString);
			
			Version availableVersion = serverStatusService.getAvailableUpdateVersion();
			
			if (availableVersion.compareTo(currentVersion) >= 1) {
				popups.warning("server.current_version.higher_version_available", availableVersion.toString(), currentVersionString, configService.getValue(ConfigValue.UpdateInformationLink));
			} else {
				popups.success("server.current_version.uptodate", currentVersionString);
			}
		} catch (Exception e) {
			popups.alert("error.exception", e.getMessage());
		}
		model.addAttribute("serverStatus", serverStatusService.getServerStatus(admin));
		
        return "server_status_view";
    }
}
