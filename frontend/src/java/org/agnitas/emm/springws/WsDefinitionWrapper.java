/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue.Webservices;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * This wrapper class allows the configuration values for DefaultWsdl11Definition to be set via db (configservice)
 */
public class WsDefinitionWrapper extends org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(WsDefinitionWrapper.class);
	
	public WsDefinitionWrapper(ConfigService configService) {
		String webservicesUrl = configService.getValue(Webservices.WebservicesUrl);
		if (StringUtils.isBlank(webservicesUrl)) {
			logger.error("Configvalue 'webservicesUrl' is missing or empty");
		} else {
			setLocationUri(webservicesUrl);
		}
	}
}
