/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import org.agnitas.dao.SourceGroupType;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.springws.jaxb.CreateDataSourceRequest;
import com.agnitas.emm.springws.jaxb.CreateDataSourceResponse;
import com.agnitas.service.DataSourceService;
import com.agnitas.service.FailedCreateDataSourceException;

@Endpoint
public class CreateDataSourceEndpoint extends BaseEndpoint {
	private static final Logger classLogger = LogManager.getLogger(CreateDataSourceEndpoint.class);

	private DataSourceService dataSourceService;

	public CreateDataSourceEndpoint(DataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_COM, localPart = "CreateDataSourceRequest")
	public @ResponsePayload CreateDataSourceResponse createDataSource(@RequestPayload CreateDataSourceRequest request) throws Exception {
		if (classLogger.isInfoEnabled()) {
			classLogger.info( "Entered CreateDataSourceEndpoint.createDataSource()");
		}

		validateDescriptor(request);
		
		CreateDataSourceResponse response = new CreateDataSourceResponse();

		int rs = dataSourceService.createDataSource(Utils.getUserCompany(), SourceGroupType.SoapWebservices,
				request.getDescription(), request.getUrl());
		
		response.setId(rs);
		
		if (classLogger.isInfoEnabled()) {
			classLogger.info( "Leaving GetMailingContentEndpoint.createDataSource()");
		}
		
		return response;
	}

    private void validateDescriptor(CreateDataSourceRequest request) {
        if (StringUtils.isBlank(request.getDescription())) {
		    throw new FailedCreateDataSourceException("The description of the datasource is empty.");
		}
		
		if (StringUtils.isBlank(request.getUrl())) {
		    throw new FailedCreateDataSourceException("The URL of the datasource is empty.");
		}
    }
}
