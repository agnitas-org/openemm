/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful;

import java.io.File;
import java.util.Date;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This restful service is available at:
 * https://<system.url>/restful/login
 */
public class LoginRestfulServiceHandler implements RestfulServiceHandler {
	public static final String NAMESPACE = "login";
	
	private RestfulUserActivityLogDao restfulUserActivityLogDao;
	private ConfigService configService;

	public void setRestfulUserActivityLogDao(RestfulUserActivityLogDao restfulUserActivityLogDao) {
		this.restfulUserActivityLogDao = restfulUserActivityLogDao;
	}
	
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) throws Exception {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, Admin admin, byte[] requestData, File requestDataFile, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod, boolean extendedLogging) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getJwtToken(request, admin)));
		} else if (requestMethod == RequestMethod.DELETE) {
			restfulResponse.setError(new RestfulClientException("Invalid http method DELETE for actionExecute"), ErrorCode.REQUEST_DATA_ERROR);
		} else if ((requestData == null || requestData.length == 0) && (requestDataFile == null || requestDataFile.length() <= 0)) {
			restfulResponse.setError(new RestfulClientException("Missing request data"), ErrorCode.REQUEST_DATA_ERROR);
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getJwtToken(request, admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getJwtToken(request, admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Create a login JWT token
	 */
	private JsonObject getJwtToken(HttpServletRequest request, Admin admin) throws Exception {
		if (StringUtils.isNotBlank(HttpUtils.getAuthorizationToken(request))) {
			throw new RestfulClientException("Creation of new JWT token via JWT authenticated request is denied. Please use username and password to create a new JWT token.");
		} else {
			int validityMinutes = configService.getIntegerValue(ConfigValue.RestfulJwtValidityMinutes, admin.getCompanyID());
			if (validityMinutes <= 0) {
				throw new RestfulClientException("Login for authentification by JWT authorization token is not supported");
			} else {
				String restfulJwtSharedSecret = configService.getValue(ConfigValue.RestfulJwtSecret, admin.getCompanyID());
				if (StringUtils.isNotBlank(restfulJwtSharedSecret)) {
					Date validity = DateUtilities.addMinutesToDate(new Date(), validityMinutes);
					Builder tokenBuilder = JWT.create()
				        .withIssuedAt(new Date())
				        .withExpiresAt(validity)
				        .withClaim("username", admin.getUsername() + (admin.getSupervisor() != null ? "/" + admin.getSupervisor().getSupervisorName() : ""));
		
					restfulUserActivityLogDao.addAdminUseOfFeature(admin, "restful/login", new Date());
					writeActivityLog(restfulUserActivityLogDao, admin.getFullUsername(), request, admin);
					
					JsonObject tokenJsonObject = new JsonObject();
					tokenJsonObject.add("jwt", tokenBuilder.sign(Algorithm.HMAC512(restfulJwtSharedSecret)));
					tokenJsonObject.add("validity", validity);
					
					return tokenJsonObject;
				} else {
					throw new RestfulClientException("Login for authentification by JWT authorization token is not supported");
				}
			}
		}
	}
	
	@Override
	public ResponseType getResponseType() {
		return ResponseType.JSON;
	}
}
