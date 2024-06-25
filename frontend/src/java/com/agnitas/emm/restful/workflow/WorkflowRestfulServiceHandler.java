/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.workflow;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus;
import com.agnitas.emm.core.workflow.service.ComWorkflowActivationService;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.emm.restful.BaseRequestResponse;
import com.agnitas.emm.restful.JsonRequestResponse;
import com.agnitas.emm.restful.ResponseType;
import com.agnitas.emm.restful.RestfulClientException;
import com.agnitas.emm.restful.RestfulNoDataFoundException;
import com.agnitas.emm.restful.RestfulServiceHandler;
import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This restful service is available at:
 * https://<system.url>/restful/workflow
 */
public class WorkflowRestfulServiceHandler implements RestfulServiceHandler {
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(WorkflowRestfulServiceHandler.class);
	
	public static final String NAMESPACE = "workflow";

	private ConfigService configService;
	private RestfulUserActivityLogDao userActivityLogDao;
	private ComWorkflowService workflowService;
	private ComWorkflowActivationService workflowActivationService;

	public WorkflowRestfulServiceHandler(ConfigService configService, RestfulUserActivityLogDao userActivityLogDao, ComWorkflowService workflowService, ComWorkflowActivationService workflowActivationService) {
		this.configService = configService;
		this.userActivityLogDao = userActivityLogDao;
		this.workflowService = workflowService;
		this.workflowActivationService = workflowActivationService;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) throws Exception {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, Admin admin, byte[] requestData, File requestDataFile, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod, boolean extendedLogging) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getWorkflowData(request, admin)));
		} else if (requestMethod == RequestMethod.DELETE) {
			throw new RestfulClientException("Invalid http request method");
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(changeWorkflowStatus(request, admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(changeWorkflowStatus(request, admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Return a single or multiple workflow data sets
	 */
	private Object getWorkflowData(HttpServletRequest request, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.WORKFLOW_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.WORKFLOW_SHOW.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 1);
		
		if (restfulContext.length == 0) {
			// Show workflow entries
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/workflow", new Date());
			writeActivityLog("ALL", request, admin);

			JsonArray workflowJsonArray = new JsonArray();
			
			for (Workflow workflowEntry : workflowService.getWorkflowsOverview(admin)) {
				workflowJsonArray.add(getWorkflowJsonObject(workflowEntry));
			}
			
			return workflowJsonArray;
		} else {
			// Show single workflow data
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/workflow", new Date());
			writeActivityLog(restfulContext[0], request, admin);
			
			Workflow workflowToShow = null;
			if (AgnUtils.isNumber(restfulContext[0])) {
				int workflowID = Integer.parseInt(restfulContext[0]);
				workflowToShow = workflowService.getWorkflow(workflowID, admin.getCompanyID());
			} else {
				String workflowShortname = restfulContext[0];
				for (Workflow workflowEntry : workflowService.getWorkflowsOverview(admin)) {
					if (workflowEntry.getShortname().equals(workflowShortname)) {
						if (workflowToShow != null) {
							throw new RestfulClientException("Multiple workflows found for this name: '" + workflowShortname + "'");
						} else {
							workflowToShow = workflowEntry;
						}
					}
				}
			}
			
			if (workflowToShow == null) {
				throw new RestfulNoDataFoundException("No such data");
			} else {
				return getWorkflowJsonObject(workflowToShow);
			}
		}
	}

	/**
	 * Change a workflow's status (stop/start/pause)
	 */
	private Object changeWorkflowStatus(HttpServletRequest request, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.WORKFLOW_ACTIVATE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.WORKFLOW_ACTIVATE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 2, 2);
		
		Workflow workflow = null;
		if (AgnUtils.isNumber(restfulContext[0])) {
			int workflowID = Integer.parseInt(restfulContext[0]);
			workflow = workflowService.getWorkflow(workflowID, admin.getCompanyID());
		} else {
			String workflowShortname = restfulContext[0];
			for (Workflow workflowEntry : workflowService.getWorkflowsOverview(admin)) {
				if (workflowEntry.getShortname().equals(workflowShortname)) {
					if (workflow != null) {
						throw new RestfulClientException("Multiple workflows found for this name: '" + workflowShortname + "'");
					} else {
						workflow = workflowEntry;
					}
				}
			}
		}
		
		if (workflow == null) {
			throw new RestfulNoDataFoundException("No such data");
		} else {
			String newStatus = restfulContext[1];
			if ("pause".equalsIgnoreCase(newStatus)) {
				if (workflow.getStatus().equals(Workflow.WorkflowStatus.STATUS_ACTIVE)) {
		        	pauseWorkflow(workflow);
		            
					userActivityLogDao.addAdminUseOfFeature(admin, "restful/workflow", new Date());
					writeActivityLog("Pause " + restfulContext[0], request, admin);

					return getWorkflowJsonObject(workflowService.getWorkflow(workflow.getWorkflowId(), admin.getCompanyID()));
		        } else if (workflow.getStatus().equals(Workflow.WorkflowStatus.STATUS_PAUSED)) {
					throw new RestfulClientException("Workflow is already paused");
		        } else {
		        	throw new RestfulClientException("Workflow is not active and can therefore not be paused");
		        }
			} else if ("resume".equalsIgnoreCase(newStatus)) {
				if (workflow.getStatus().equals(Workflow.WorkflowStatus.STATUS_PAUSED)) {
					resumeWorkflow(admin, workflow);
			        
					userActivityLogDao.addAdminUseOfFeature(admin, "restful/workflow", new Date());
					writeActivityLog("Resume " + restfulContext[0], request, admin);

					return getWorkflowJsonObject(workflowService.getWorkflow(workflow.getWorkflowId(), admin.getCompanyID()));
		        } else if (workflow.getStatus().equals(Workflow.WorkflowStatus.STATUS_ACTIVE)) {
					throw new RestfulClientException("Workflow is already active");
		        } else {
		        	throw new IllegalStateException("Workflow is not paused and can therefore not be activated");
		        }
			} else {
				throw new RestfulClientException("Unsupported new workflow status: '" + newStatus + "'");
			}
		}
	}

	public void pauseWorkflow(Workflow workflow) throws Exception {
		workflowService.savePausedSchemaForUndo(workflow);
		workflowService.changeWorkflowStatus(workflow.getWorkflowId(), workflow.getCompanyId(), Workflow.WorkflowStatus.STATUS_PAUSED);
	}

	public void resumeWorkflow(Admin admin, Workflow workflow) throws Exception {
		workflow.setStatus(Workflow.WorkflowStatus.STATUS_OPEN);
		workflowService.saveWorkflow(workflow);
		workflowService.deleteWorkflowTargetConditions(admin.getCompanyID(), workflow.getWorkflowId());
		if (!workflowActivationService.activateWorkflow(workflow.getWorkflowId(), admin, false, true, false, new ArrayList<>(), new ArrayList<>(), new ArrayList<>())) {
		    throw new RuntimeException("Workflow activation failed!");
		}
		workflowService.changeWorkflowStatus(workflow.getWorkflowId(), admin.getCompanyID(), Workflow.WorkflowStatus.STATUS_ACTIVE);
	}

	private JsonObject getWorkflowJsonObject(Workflow workflow) {
		JsonObject workflowJsonObject = new JsonObject();
		workflowJsonObject.add("workflow_id", workflow.getWorkflowId());
		workflowJsonObject.add("shortname", workflow.getShortname());
		workflowJsonObject.add("description", workflow.getDescription());
		workflowJsonObject.add("status", workflow.getStatus().toString());
		
        if (workflow.getStatus() == WorkflowStatus.STATUS_PAUSED) {
        	workflowJsonObject.add("pauseTime", workflowService.getPauseDate(workflow.getWorkflowId(), workflow.getCompanyId()).getTime());
        	workflowJsonObject.add("pauseExpirationHours", configService.getIntegerValue(ConfigValue.WorkflowPauseExpirationHours, workflow.getCompanyId()));
        }
        
		return workflowJsonObject;
	}

	@Override
	public ResponseType getResponseType() {
		return ResponseType.JSON;
	}

	private void writeActivityLog(String description, HttpServletRequest request, Admin admin) {
		writeActivityLog(userActivityLogDao, description, request, admin);
	}
}
