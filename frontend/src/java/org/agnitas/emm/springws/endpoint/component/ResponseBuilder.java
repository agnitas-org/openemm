/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.component;

import org.agnitas.beans.MailingComponent;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.apache.log4j.Logger;

public class ResponseBuilder {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ResponseBuilder.class);
	
	private final ObjectFactory objectFactory;

	public ResponseBuilder(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}
	
	public org.agnitas.emm.springws.jaxb.Attachment createResponse(MailingComponent component, boolean copyData, boolean useISODateFormat) {
		org.agnitas.emm.springws.jaxb.Attachment response;
		if (useISODateFormat) {
            response = objectFactory.createAttachmentDateTimeISO();
            ((org.agnitas.emm.springws.jaxb.AttachmentDateTimeISO) response).setTimestamp(component.getTimestamp());
        } else {
            response = objectFactory.createAttachmentDateTimeDefault();
            ((org.agnitas.emm.springws.jaxb.AttachmentDateTimeDefault) response).setTimestamp(component.getTimestamp());
        }
		response.setComponentID(component.getId());
		response.setMimeType(component.getMimeType());
		response.setComponentType(component.getType());
		response.setComponentName(component.getComponentName());

		byte[] data = component.getBinaryBlock();
		response.setSize(data != null ? data.length : 0);
		if (copyData) {
			response.setData(data);
		}
		
		return response;
	}

    public org.agnitas.emm.springws.jaxb.Attachment createResponse(MailingComponent component, boolean copyData) {
        return createResponse(component, copyData, false);
    }
}
