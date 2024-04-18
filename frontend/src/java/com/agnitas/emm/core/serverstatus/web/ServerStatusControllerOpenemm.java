package com.agnitas.emm.core.serverstatus.web;

import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComServerMessageDao;
import com.agnitas.dao.LicenseDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.logon.service.ComLogonService;
import com.agnitas.emm.core.serverstatus.forms.JobQueueFormSearchParams;
import com.agnitas.emm.core.serverstatus.service.ServerStatusService;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.service.JobQueueService;
import org.agnitas.service.UserActivityLogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@PermissionMapping("server.status")
@RequestMapping("/serverstatus")
@SessionAttributes(types = JobQueueFormSearchParams.class)
public class ServerStatusControllerOpenemm extends ServerStatusController {
	public ServerStatusControllerOpenemm(ServerStatusService serverStatusService, UserActivityLogService userActivityLogService, JobQueueService jobQueueService, JavaMailService javaMailService, ComLogonService logonService, LicenseDao licenseDao, ComServerMessageDao serverMessageDao, ConfigService configService, ComCompanyDao companyDao) {
		super(serverStatusService, userActivityLogService, jobQueueService, javaMailService, logonService, licenseDao, serverMessageDao, configService, companyDao);
	}
}
