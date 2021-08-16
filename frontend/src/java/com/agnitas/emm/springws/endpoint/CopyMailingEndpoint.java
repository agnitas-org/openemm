/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.service.CopyMailingService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.springws.exception.WebServiceInvalidFieldsException;
import com.agnitas.emm.springws.jaxb.CopyMailingRequest;
import com.agnitas.emm.springws.jaxb.CopyMailingResponse;
import com.agnitas.util.TimingLogger;

@Endpoint
public class CopyMailingEndpoint extends BaseEndpoint {

	@Deprecated // Not for common use. Used only for EMM-8126 to measure executing times.
	private static final transient Logger TIMING_LOGGER = Logger.getLogger(TimeMeasurement.class);
	
	@Deprecated // Not for common use. Used only for EMM_8126 to create a separate logger.
	public static final class TimeMeasurement {
		// Auxiliary class to get a separate logger
	}
	
    private CopyMailingService copyMailingService;
    private ConfigService configService;

    @Autowired
    public CopyMailingEndpoint(CopyMailingService copyMailingService, final ConfigService configService) {
        this.copyMailingService = copyMailingService;
        this.configService = configService;
    }

    @PayloadRoot(namespace = Utils.NAMESPACE_COM, localPart = "CopyMailingRequest")
    public @ResponsePayload CopyMailingResponse copyMailing(@RequestPayload CopyMailingRequest request) throws Exception {
    	@Deprecated // Not for public use. Used only for EMM-8126 to measure execution times.
    	final TimingLogger timingLogger = measureTiming() ? new TimingLogger(TIMING_LOGGER, "Entered CopyMailingEndpoint") : null;
    	
        validateRequest(request);
        CopyMailingResponse response = new CopyMailingResponse();
        int copyId = copyMailingService.copyMailing(timingLogger, Utils.getUserCompany(), request.getMailingId(), Utils.getUserCompany(), request.getNameOfCopy(), request.getDescriptionOfCopy());
        response.setCopyId(copyId);
        
        if(timingLogger != null) {
        	timingLogger.log("Leaving CopyMailingEndpoint");
        }
        
        return response;
    }
    
    private final boolean measureTiming() {
    	final int companyID = Utils.getUserCompany();
    	
    	return configService.getBooleanValue(ConfigValue.Development.CopyMailingMeasureTiming, companyID);
    }

    private void validateRequest(CopyMailingRequest request) {
        String nameOfCopy = request.getNameOfCopy();
        if (StringUtils.isBlank(nameOfCopy)) {
            throw new WebServiceInvalidFieldsException("Field nameOfCopy is empty.");
        }
    }
}
