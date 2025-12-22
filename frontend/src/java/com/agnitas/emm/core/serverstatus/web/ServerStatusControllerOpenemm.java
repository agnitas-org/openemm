package com.agnitas.emm.core.serverstatus.web;

import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.LicenseDao;
import com.agnitas.dao.ServerMessageDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.db_schema.service.DbSchemaSnapshotService;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.emm.core.serverstatus.forms.JobQueueFormSearchParams;
import com.agnitas.emm.core.serverstatus.service.ServerStatusService;
import com.agnitas.service.JobQueueService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.emm.core.commons.util.ConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping("/serverstatus")
@SessionAttributes(types = JobQueueFormSearchParams.class)
public class ServerStatusControllerOpenemm extends ServerStatusController {

	public ServerStatusControllerOpenemm(ServerStatusService serverStatusService, UserActivityLogService userActivityLogService, JobQueueService jobQueueService,
										 JavaMailService javaMailService, LogonService logonService, LicenseDao licenseDao, ServerMessageDao serverMessageDao,
										 ConfigService configService, CompanyDao companyDao, DbSchemaSnapshotService dbSchemaSnapshotService) {

		super(serverStatusService, userActivityLogService, jobQueueService, javaMailService, logonService, licenseDao, serverMessageDao,
				configService, companyDao, dbSchemaSnapshotService);
	}
}
