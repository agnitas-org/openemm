/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.stat.service.MailingSummaryStatisticJobService;
import com.agnitas.emm.springws.jaxb.MailingSummaryStatisticJobRequest;
import com.agnitas.emm.springws.jaxb.MailingSummaryStatisticJobResponse;

@Endpoint
public class MailingSummaryStatisticJobEndpoint extends BaseEndpoint {
	private static final Logger classLogger = LogManager.getLogger(MailingSummaryStatisticJobEndpoint.class);

	private MailingSummaryStatisticJobService mailingSummaryStatisticJobService;

	public MailingSummaryStatisticJobEndpoint(MailingSummaryStatisticJobService mailingSummaryStatisticJobService) {
		this.mailingSummaryStatisticJobService = mailingSummaryStatisticJobService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_COM, localPart = "MailingSummaryStatisticJobRequest")
	public @ResponsePayload MailingSummaryStatisticJobResponse mailingSummaryStatisticJob(@RequestPayload MailingSummaryStatisticJobRequest request) throws Exception {
		if (classLogger.isInfoEnabled()) {
			classLogger.info( "Entered MailingSummaryStatisticJobEndpoint.mailingSummaryStatisticJob()");
		}
		
		MailingSummaryStatisticJobResponse response = new MailingSummaryStatisticJobResponse();
		
		int id = mailingSummaryStatisticJobService.startSummaryStatisticJob(request.getMailingID(),
				request.getTargetGroups(), null /*request.getRecipientsType()*/);
		
		if (classLogger.isInfoEnabled()) {
			classLogger.info( "Leaving MailingSummaryStatisticJobEndpoint.mailingSummaryStatisticJob()");
		}
		
		response.setStatisticJobID(id);
		return response;
	}
}
