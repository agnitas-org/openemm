/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.binding;

import org.agnitas.beans.BindingEntry;
import org.agnitas.emm.springws.jaxb.BindingDateTimeDefault;
import org.agnitas.emm.springws.jaxb.BindingDateTimeISO;

public class SubscriberBindingResponseBuilder {

	public org.agnitas.emm.springws.jaxb.Binding createResponse(BindingEntry binding, boolean useISODateFormat) {
        org.agnitas.emm.springws.jaxb.Binding response;
        if (useISODateFormat) {
            response = new BindingDateTimeISO();
            ((org.agnitas.emm.springws.jaxb.BindingDateTimeISO) response).setChangeDate(binding.getChangeDate());
            ((org.agnitas.emm.springws.jaxb.BindingDateTimeISO) response).setCreationDate(binding.getCreationDate());
        } else {
            response = new BindingDateTimeDefault();
            ((org.agnitas.emm.springws.jaxb.BindingDateTimeDefault) response).setChangeDate(binding.getChangeDate());
            ((org.agnitas.emm.springws.jaxb.BindingDateTimeDefault) response).setCreationDate(binding.getCreationDate());
        }
        response.setCustomerID(binding.getCustomerID());
        response.setMailinglistID(binding.getMailinglistID());
        response.setMediatype(binding.getMediaType());
        response.setStatus(binding.getUserStatus());
        response.setUserType(binding.getUserType());
        response.setRemark(binding.getUserRemark());
        response.setExitMailingID(binding.getExitMailingID());
        return response;
    }

    public org.agnitas.emm.springws.jaxb.Binding createResponse(BindingEntry binding) {
        return createResponse(binding, false);
    }
}
