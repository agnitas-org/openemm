package com.agnitas.emm.core.serverstatus.service.impl;

import com.agnitas.dao.ComServerStatusDao;
import com.agnitas.emm.core.JavaMailService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.service.JobQueueService;
import org.springframework.stereotype.Service;

@Service
public class ServerStatusServiceImplOpenemm extends ServerStatusServiceImplBasic {
	public ServerStatusServiceImplOpenemm(ComServerStatusDao serverStatusDao, ConfigService configService, JobQueueService jobQueueService, JavaMailService javaMailService) {
		this.serverStatusDao = serverStatusDao;
		this.configService = configService;
		this.jobQueueService = jobQueueService;
		this.javaMailService = javaMailService;
	}
}
