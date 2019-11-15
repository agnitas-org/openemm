/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.binding; // FIXME Wrong package

import javax.annotation.Resource;

import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.DecryptLinkDataRequest;
import org.agnitas.emm.springws.jaxb.DecryptLinkDataResponse;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.agnitas.emm.core.commons.uid.ComExtensibleUID;

public class DecryptLinkDataEndpoint extends AbstractMarshallingPayloadEndpoint {

	@Resource
	private ObjectFactory objectFactory;
	
	@Resource
	private ExtensibleUIDService uidService;

	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		final DecryptLinkDataRequest request = (DecryptLinkDataRequest) requestObject;
		final DecryptLinkDataResponse response = objectFactory.createDecryptLinkDataResponse();

		final ComExtensibleUID uid = uidService.parse(request.getLinkparam());
		
		if(uid.getCompanyID() == Utils.getUserCompany()) {
			response.setCompanyID(Utils.getUserCompany());
			response.setCustomerID(uid.getCustomerID());
			response.setMailingID(uid.getMailingID());
			response.setUrlID(uid.getUrlID());
	
			return response;
		} else {
			throw new UIDParseException("Unable to parse UID", request.getLinkparam());
		}
	}

}
