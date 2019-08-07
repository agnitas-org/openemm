/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.exceptionresolver;

import org.agnitas.emm.springws.exceptionresolver.CommonExceptionResolver;
import org.agnitas.service.ImportException;
import org.agnitas.util.UnknownAutoImportException;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;

public class ImportExceptionResolver extends CommonExceptionResolver {

    @Override
    protected SoapFaultDefinition getFaultDefinition(Object endpoint, Exception ex) {
        if (ex instanceof UnknownAutoImportException) {
            SoapFaultDefinition definition = getDefaultDefinition(ex);
            UnknownAutoImportException exception = (UnknownAutoImportException) ex;
            definition.setFaultStringOrReason("Unknown auto import ID " + exception.getAutoImportId() + " for the company ID " + exception.getCompanyId());
            return definition;
        } else if (ex instanceof ImportException) {
            SoapFaultDefinition definition = getDefaultDefinition(ex);
            definition.setFaultStringOrReason("Auto Import was failed: " + ex.getMessage());
            return definition;
        }

        return super.getFaultDefinition(endpoint, ex);
    }
}
