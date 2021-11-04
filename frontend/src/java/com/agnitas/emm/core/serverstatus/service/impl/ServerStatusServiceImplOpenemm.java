package com.agnitas.emm.core.serverstatus.service.impl;

import java.io.BufferedReader;
import java.io.StringReader;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.JobQueueService;
import org.agnitas.util.HttpUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.agnitas.dao.ComServerStatusDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.serverstatus.service.ServerStatusService;
import com.agnitas.util.Version;

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

	@Override
	public Version getAvailableUpdateVersion() throws Exception {
		String versionData = HttpUtils.executeHttpRequest("https://www.agnitas.de/download/openemm-version/", null, null, configuredSecureTransportLayerProtocol());
		try (BufferedReader reader = new BufferedReader(new StringReader(versionData))) {
			String nextLine;
			while ((nextLine = reader.readLine()) != null) {
				if (nextLine.startsWith("frontend:")) {
					String[] frontendLineParts = nextLine.substring(9).trim().split(" ");
					return new Version(frontendLineParts[0].trim());
				}
			}
			return null;
		}
	}
	
	private final String configuredSecureTransportLayerProtocol() {
		return this.configService.getValue(ConfigValue.SecureTransportLayerProtocol);
	}
}
