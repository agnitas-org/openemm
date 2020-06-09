/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import org.agnitas.beans.MailingComponent;
import org.agnitas.emm.springws.endpoint.Utils;
import org.apache.log4j.Logger;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.springws.jaxb.GetMailingContentRequest;
import com.agnitas.emm.springws.jaxb.GetMailingContentResponse;
import com.agnitas.emm.springws.jaxb.MailingContent;
import com.agnitas.emm.springws.jaxb.ObjectFactory;

public class GetMailingContentEndpoint extends AbstractMarshallingPayloadEndpoint {
	private static final Logger classLogger = Logger.getLogger(GetMailingContentEndpoint.class);

	private ObjectFactory comObjectFactory;
	private MailingService mailingService;

	@Override
	protected Object invokeInternal(Object arg0) throws Exception {
		if( classLogger.isInfoEnabled()) {
			classLogger.info( "Entered MailingTemplatesContentEndpoint.invokeInternal()");
		}
		
		GetMailingContentRequest request = (GetMailingContentRequest) arg0;
		GetMailingContentResponse response = comObjectFactory.createGetMailingContentResponse();

		int mailingId = request.getMailingId();
		int companyId = Utils.getUserCompany();

		com.agnitas.emm.springws.jaxb.GetMailingContentResponse.Items items
			= comObjectFactory.createGetMailingContentResponseItems();
		for (MailingComponent component
				: mailingService.getMailingComponents(mailingId, companyId)) {
			MailingContent mailingContent = new MailingContent();
			mailingContent.setName(component.getComponentName());
			mailingContent.setContent(component.getEmmBlock());
			items.getItem().add(mailingContent);
		}
		response.setItems(items);
		
		if( classLogger.isInfoEnabled()) {
			classLogger.info( "Leaving GetMailingContentEndpoint.invokeInternal()");
		}
		
		return response;
	}

	public void setComObjectFactory(ObjectFactory comObjectFactory) {
		this.comObjectFactory = comObjectFactory;
	}

	public void setMailingService(MailingService mailingService) {
		this.mailingService = mailingService;
	}

}
