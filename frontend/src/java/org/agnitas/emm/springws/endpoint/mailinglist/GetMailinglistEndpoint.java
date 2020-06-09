/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.mailinglist;

import org.agnitas.beans.Mailinglist;
import org.agnitas.emm.core.mailinglist.service.MailinglistModel;
import org.agnitas.emm.core.mailinglist.service.MailinglistService;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.GetMailinglistRequest;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

public class GetMailinglistEndpoint extends AbstractMarshallingPayloadEndpoint {


    private MailinglistService mailinglistService;

    private ObjectFactory objectFactory;

    @Override
    protected Object invokeInternal(Object arg0) throws Exception {
        GetMailinglistRequest request = (GetMailinglistRequest) arg0;

		MailinglistModel model = new MailinglistModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setMailinglistId(request.getMailinglistID());

        Mailinglist mailinglist = mailinglistService.getMailinglist(model);
        
        return objectFactory.createGetMailinglistResponse(new ResponseBuilder(mailinglist, objectFactory).createResponse());
    }


	public void setMailinglistService(MailinglistService service) {
		this.mailinglistService = service;
	}
	
	public void setObjectFactory(ObjectFactory factory) {
		this.objectFactory = factory;
	}
}
