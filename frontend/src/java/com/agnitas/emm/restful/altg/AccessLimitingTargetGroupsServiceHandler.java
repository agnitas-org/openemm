/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.altg;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import com.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.lang3.StringUtils;
import com.agnitas.beans.Admin;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.restful.BaseRequestResponse;
import com.agnitas.emm.restful.JsonRequestResponse;
import com.agnitas.emm.restful.ResponseType;
import com.agnitas.emm.restful.RestfulClientException;
import com.agnitas.emm.restful.RestfulServiceHandler;
import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This restful service is available at:
 * https://<system.url>/restful/altg
 */
public class AccessLimitingTargetGroupsServiceHandler implements RestfulServiceHandler {
	
	public static final String NAMESPACE = "altg";

	protected RestfulUserActivityLogDao userActivityLogDao;
	protected AdminService adminService;
	protected TargetService targetService;

	public void setUserActivityLogDao(RestfulUserActivityLogDao userActivityLogDao) {
		this.userActivityLogDao = userActivityLogDao;
	}
	
	public void setAdminService(AdminService adminService) {
		this.adminService = adminService;
	}
	
	public void setTargetService(TargetService targetService) {
		this.targetService = targetService;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, Admin admin, byte[] requestData, File requestDataFile, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod, boolean extendedLogging) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getAltgUsersData(request, admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Return a all altg target groups and their users
	 * 
	 */
	private Object getAltgUsersData(HttpServletRequest request, Admin admin) throws Exception {
		RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 0);
		
		userActivityLogDao.addAdminUseOfFeature(admin, "restful/altg", new Date());
		writeActivityLog(userActivityLogDao, "Requested all ALTG users list", request, admin);

		JsonArray result = new JsonArray();
		for (TargetLight target : targetService.getAccessLimitationTargetLights(admin.getCompanyID())) {
			JsonObject targetJsonObject = new JsonObject();
			targetJsonObject.add("target_id", target.getId());
			targetJsonObject.add("name", target.getTargetName());
			if (StringUtils.isNotBlank(target.getTargetDescription())) {
				targetJsonObject.add("description", target.getTargetDescription());
			}
			targetJsonObject.add("valid", target.isValid());
			targetJsonObject.add("creation_date", target.getCreationDate());
			targetJsonObject.add("change_date", target.getChangeDate());
			
			List<Integer> listOfAdminIDs = adminService.getAccessLimitingAdmins(target.getId());
			if (listOfAdminIDs != null && listOfAdminIDs.size() > 0) {
				JsonArray adminArray = new JsonArray();
				for (int adminID : listOfAdminIDs) {
					adminArray.add(adminID);
				}
				targetJsonObject.add("users", adminArray);
			}
			
			result.add(targetJsonObject);
		}
		return result;
	}

	@Override
	public ResponseType getResponseType() {
		return ResponseType.JSON;
	}
}
