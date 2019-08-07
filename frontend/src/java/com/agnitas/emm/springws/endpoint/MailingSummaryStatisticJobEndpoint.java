/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import org.apache.log4j.Logger;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.agnitas.emm.core.stat.service.MailingSummaryStatisticJobService;
import com.agnitas.emm.springws.jaxb.MailingSummaryStatisticJobRequest;
import com.agnitas.emm.springws.jaxb.MailingSummaryStatisticJobResponse;
import com.agnitas.emm.springws.jaxb.ObjectFactory;

@SuppressWarnings("deprecation")
public class MailingSummaryStatisticJobEndpoint extends AbstractMarshallingPayloadEndpoint {
	
	@SuppressWarnings("hiding")
	private static final Logger logger = Logger.getLogger(MailingSummaryStatisticJobEndpoint.class);

	private MailingSummaryStatisticJobService mailingSummaryStatisticJobService; 
	private ObjectFactory comObjectFactory; 

	@Override
	protected Object invokeInternal(Object arg0) throws Exception {
		if( logger.isInfoEnabled()) {
			logger.info( "Entered MailingSummaryStatisticJobEndpoint.invokeInternal()");
		}
		
		MailingSummaryStatisticJobRequest request = (MailingSummaryStatisticJobRequest) arg0;
		MailingSummaryStatisticJobResponse response = comObjectFactory.createMailingSummaryStatisticJobResponse();
		
		int id = mailingSummaryStatisticJobService.startSummaryStatisticJob(request.getMailingID(), 
				request.getTargetGroups(), null /*request.getRecipientsType()*/);
		
		if( logger.isInfoEnabled()) {
			logger.info( "Leaving MailingSummaryStatisticJobEndpoint.invokeInternal()");
		}
		
		response.setStatisticJobID(id);
		return response;
	}

	public void setMailingSummaryStatisticJobService(
			MailingSummaryStatisticJobService mailingSummaryStatisticJobService) {
		this.mailingSummaryStatisticJobService = mailingSummaryStatisticJobService;
	}

	public void setComObjectFactory(ObjectFactory comObjectFactory) {
		this.comObjectFactory = comObjectFactory;
	}

}
