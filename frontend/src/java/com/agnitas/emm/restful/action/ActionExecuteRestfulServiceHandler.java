/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.action;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Map.Entry;

import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import com.agnitas.emm.core.velocity.Constants;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.servicemail.ExecutingActionFailedException;
import com.agnitas.emm.restful.BaseRequestResponse;
import com.agnitas.emm.restful.ErrorCode;
import com.agnitas.emm.restful.JsonRequestResponse;
import com.agnitas.emm.restful.ResponseType;
import com.agnitas.emm.restful.RestfulClientException;
import com.agnitas.emm.restful.RestfulServiceHandler;
import com.agnitas.json.Json5Reader;
import com.agnitas.json.JsonDataType;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This restful service is available at:
 * https://<system.url>/restful/actionExecute
 */
public class ActionExecuteRestfulServiceHandler implements RestfulServiceHandler {
	
	public static final String NAMESPACE = "actionExecute";

	private RestfulUserActivityLogDao userActivityLogDao;
	private EmmActionService emmActionService;

	public void setUserActivityLogDao(RestfulUserActivityLogDao userActivityLogDao) {
		this.userActivityLogDao = userActivityLogDao;
	}
	
	public void setEmmActionService(EmmActionService emmActionService) {
		this.emmActionService = emmActionService;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, Admin admin, byte[] requestData, File requestDataFile, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod, boolean extendedLogging) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			restfulResponse.setError(new RestfulClientException("Invalid http method GET for actionExecute"), ErrorCode.REQUEST_DATA_ERROR);
		} else if (requestMethod == RequestMethod.DELETE) {
			restfulResponse.setError(new RestfulClientException("Invalid http method DELETE for actionExecute"), ErrorCode.REQUEST_DATA_ERROR);
		} else if ((requestData == null || requestData.length == 0) && (requestDataFile == null || requestDataFile.length() <= 0)) {
			restfulResponse.setError(new RestfulClientException("Missing request data"), ErrorCode.REQUEST_DATA_ERROR);
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(executeEmmAction(request, requestData, requestDataFile, admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(executeEmmAction(request, requestData, requestDataFile, admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Execute an existing action
	 * 
	 */
	private Object executeEmmAction(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.ACTIONS_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.ACTIONS_SHOW.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		int actionID;
		if (AgnUtils.isNumber(restfulContext[0])) {
			actionID = Integer.parseInt(restfulContext[0]);
			if (!emmActionService.actionExists(actionID, admin.getCompanyID())) {
				throw new RestfulClientException("Invalid non-existing action_id: " + actionID);
			}
		} else {
			throw new RestfulClientException("Missing parameter action_id");
		}
		
		userActivityLogDao.addAdminUseOfFeature(admin, "restful/actionExecute", new Date());
		writeActivityLog(userActivityLogDao, "action_id:" + actionID, request, admin);

		CaseInsensitiveMap<String, Object> params = new CaseInsensitiveMap<>();
		try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				JsonNode jsonNode = jsonReader.read();
				if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
					JsonObject jsonObject = (JsonObject) jsonNode.getValue();
					for (Entry<String, Object> entry : jsonObject.entrySet()) {
						params.put(entry.getKey(), entry.getValue());
					}
				} else {
					throw new RestfulClientException("Expected root JSON item type 'JsonObject' but was: " + jsonNode.getJsonDataType());
				}
			}
		}
		
		// Enduser convenience
		if (params.containsKey("customerid") && !params.containsKey("customer_id")) {
			params.put("customer_id", params.get("customerid"));
		} else if (params.containsKey("customer_id") && !params.containsKey("customerid")) {
			params.put("customerid", params.get("customer_id"));
		}
		
		// Enduser convenience
		if (!params.containsKey("agnci")) {
			params.put("agnci", admin.getCompanyID());
		}

		// Enduser convenience, to reuse existing scripts with "requestParameters" function calls
		params.put("requestparameters", params);
		
		final EmmActionOperationErrors actionOperationErrors = new EmmActionOperationErrors();
		params.put(Constants.ACTION_OPERATION_ERRORS_CONTEXT_NAME, actionOperationErrors);
		
		boolean result = emmActionService.executeActions(actionID, admin.getCompanyID(), params, actionOperationErrors);
		
		if (!result) {
			throw new ExecutingActionFailedException(actionID);
		} else {
			return "action executed";
		}
	}
	
	@Override
	public ResponseType getResponseType() {
		return ResponseType.JSON;
	}
}
