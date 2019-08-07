/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.springws.endpoint.Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.agnitas.emm.springws.jaxb.CreateDataSourceRequest;
import com.agnitas.emm.springws.jaxb.CreateDataSourceResponse;
import com.agnitas.emm.springws.jaxb.ObjectFactory;
import com.agnitas.service.DataSourceService;
import com.agnitas.service.FailedCreateDataSourceException;

@SuppressWarnings("deprecation")
public class CreateDataSourceEndpoint extends AbstractMarshallingPayloadEndpoint {
	
	@SuppressWarnings("hiding")
	private static final Logger logger = Logger.getLogger(CreateDataSourceEndpoint.class);

	private ObjectFactory comObjectFactory; 
	private DataSourceService dataSourceService;
	private ConfigService configService;
	
	@Override
	protected Object invokeInternal(Object arg0) throws Exception {
		if( logger.isInfoEnabled()) {
			logger.info( "Entered CreateDataSourceEndpoint.invokeInternal()");
		}
		
		CreateDataSourceRequest request = (CreateDataSourceRequest) arg0;
		
		validateDescriptor(request);
		
		CreateDataSourceResponse response = comObjectFactory.createCreateDataSourceResponse();
		
		int dsGroup;
		try {
			dsGroup = configService.getIntegerValue(ConfigValue.WebserviceDatasourceGroupId);
		} catch (Exception e) {
			// Use default value
			dsGroup = 1;
		}

		int rs = dataSourceService.createDataSource(Utils.getUserCompany(), dsGroup, 
				request.getDescription(), request.getUrl());
		
		response.setId(rs);
		
		if( logger.isInfoEnabled()) {
			logger.info( "Leaving GetMailingContentEndpoint.invokeInternal()");
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

	public void setComObjectFactory(ObjectFactory comObjectFactory) {
		this.comObjectFactory = comObjectFactory;
	}

	public void setDataSourceService(DataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}
	
	@Required
	public void setConfigService(final ConfigService configService) {
		this.configService = configService;
	}
}
