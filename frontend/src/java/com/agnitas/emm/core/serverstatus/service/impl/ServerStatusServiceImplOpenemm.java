package com.agnitas.emm.core.serverstatus.service.impl;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.service.JobQueueService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.agnitas.dao.ComServerStatusDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.serverstatus.service.ServerStatusService;

@Service
public class ServerStatusServiceImplOpenemm extends ServerStatusServiceImplBasic {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ServerStatusService.class);
	
	public ServerStatusServiceImplOpenemm(ComServerStatusDao serverStatusDao, ConfigService configService, JobQueueService jobQueueService, JavaMailService javaMailService) {
		this.serverStatusDao = serverStatusDao;
		this.configService = configService;
		this.jobQueueService = jobQueueService;
		this.javaMailService = javaMailService;
	}
}
