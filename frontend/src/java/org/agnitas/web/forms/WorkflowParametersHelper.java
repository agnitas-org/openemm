/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web.forms;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

public class WorkflowParametersHelper {
    
    public static final String WORKFLOW_FORWARD_PARAMS = "workflowForwardParams";
    public static final String WORKFLOW_ID = "workflowId";
    public static final String WORKFLOW_FORWARD_TARGET_ITEM_ID = "workflowForwardTargetItemId";
    public static final String WORKFLOW_KEEP_FORWARD = "keepForward";
    public static final String WORKFLOW_NODE_ID = "nodeId";
    public static final String WORKFLOW_PARAMS_FORM = "workflowParamsForm";
    
    public static boolean isWorkflowDriven(HttpServletRequest request) {
        WorkflowParameters params = find(request);
        return params != null && params.getWorkflowId() != null && params.getWorkflowId() > 0;
    }

    public static WorkflowParameters find(HttpServletRequest request) {
        WorkflowParameters form = get(request);
        
        if (form == null || form.isEmpty()) {
            form = fromParams(request);
        }
        
        if (form != null && form.isEmpty()) {
            return null;
        }
        return form;
    }
    
    public static WorkflowParameters get(Map<String, Object> map) {
        WorkflowParameters parameters = (WorkflowParameters) map.get(WORKFLOW_PARAMS_FORM);
        
        if (parameters == null) {
            Integer workflowId = (Integer) map.get(WORKFLOW_ID);
            String params = (String) map.get(WORKFLOW_FORWARD_PARAMS);
            Integer targetItem = (Integer) map.get(WORKFLOW_FORWARD_TARGET_ITEM_ID);
            Boolean keepForward = (Boolean) map.get(WORKFLOW_KEEP_FORWARD);
            Integer nodeId = (Integer) map.get(WORKFLOW_NODE_ID);
    
            parameters = from(workflowId, params, targetItem, keepForward, nodeId);
        }
        
         if (parameters!= null && parameters.isEmpty()) {
            return null;
        }
        
        return parameters;
    }
 
    private static WorkflowParameters get(HttpServletRequest request) {
        WorkflowParameters parameters = (WorkflowParameters) request.getAttribute(WORKFLOW_PARAMS_FORM);
        
        if (parameters != null && !parameters.isEmpty()) {
            return parameters;
        }
        
        HttpSession session = request.getSession(false);
        
        boolean checkSessionScope = session != null;
        
        Boolean keepForward = (Boolean) request.getAttribute(WORKFLOW_KEEP_FORWARD);
        if (keepForward == null && checkSessionScope) {
            keepForward = (Boolean) session.getAttribute(WORKFLOW_KEEP_FORWARD);
        }
        
        Integer workflowId = (Integer) request.getAttribute(WORKFLOW_ID);
        if (workflowId == null && checkSessionScope) {
            workflowId = (Integer) session.getAttribute(WORKFLOW_ID);
        }
        
        String params = (String) request.getAttribute(WORKFLOW_FORWARD_PARAMS);
        if (StringUtils.isEmpty(params) && checkSessionScope) {
            params = (String) session.getAttribute(WORKFLOW_FORWARD_PARAMS);
        }
        
        Integer targetItem = (Integer) request.getAttribute(WORKFLOW_FORWARD_TARGET_ITEM_ID);
        if (targetItem == null && checkSessionScope) {
            targetItem = (Integer) session.getAttribute(WORKFLOW_FORWARD_TARGET_ITEM_ID);
        }
        
        Integer nodeId = (Integer) request.getAttribute(WORKFLOW_NODE_ID);
        if (nodeId == null && checkSessionScope) {
            nodeId = (Integer) session.getAttribute(WORKFLOW_NODE_ID);
        }
    
        return from(workflowId, params, targetItem, keepForward, nodeId);
    }
    
    private static WorkflowParameters from(Integer workflowId, String params, Integer targetItem, Boolean keepForward, Integer nodeId) {
        if (workflowId == null && params == null && targetItem == null && keepForward == null && nodeId == null) {
            return null;
        }
        
        return new WorkflowParameters(workflowId, nodeId, params, targetItem, keepForward);
    }
    
    public static void put(Map<String, Object> map, WorkflowParameters params) {
        if (params == null) {
            map.remove(WORKFLOW_PARAMS_FORM);
            map.remove(WORKFLOW_ID);
            map.remove(WORKFLOW_FORWARD_TARGET_ITEM_ID);
            map.remove(WORKFLOW_FORWARD_PARAMS);
            map.remove(WORKFLOW_KEEP_FORWARD);
            map.remove(WORKFLOW_NODE_ID);
        } else {
            map.put(WORKFLOW_PARAMS_FORM, params);
            map.put(WORKFLOW_ID, params.getWorkflowId());
            map.put(WORKFLOW_FORWARD_TARGET_ITEM_ID, params.getTargetItemId());
            map.put(WORKFLOW_FORWARD_PARAMS, params.getParams());
            map.put(WORKFLOW_KEEP_FORWARD, params.getKeepForward());
            map.put(WORKFLOW_NODE_ID, params.getNodeId());
        }
    }
    
    public static void put(HttpSession session, WorkflowParameters params, boolean override) {
        if (!override) {
            return;
        }
        
        if (params == null || params.isEmpty()) {
            session.removeAttribute(WORKFLOW_ID);
            session.removeAttribute(WORKFLOW_FORWARD_TARGET_ITEM_ID);
            session.removeAttribute(WORKFLOW_FORWARD_PARAMS);
            session.removeAttribute(WORKFLOW_KEEP_FORWARD);
            session.removeAttribute(WORKFLOW_NODE_ID);
        } else {
            session.setAttribute(WORKFLOW_ID, params.getWorkflowId());
            session.setAttribute(WORKFLOW_FORWARD_TARGET_ITEM_ID, params.getTargetItemId());
            session.setAttribute(WORKFLOW_FORWARD_PARAMS, params.getParams());
            session.setAttribute(WORKFLOW_KEEP_FORWARD, params.getKeepForward());
            session.setAttribute(WORKFLOW_NODE_ID, params.getNodeId());
        }
    }
    
    public static void put(HttpServletRequest request, WorkflowParameters params) {
        if (params == null || params.isEmpty()) {
            request.removeAttribute(WORKFLOW_PARAMS_FORM);
            request.removeAttribute(WORKFLOW_ID);
            request.removeAttribute(WORKFLOW_FORWARD_TARGET_ITEM_ID);
            request.removeAttribute(WORKFLOW_FORWARD_PARAMS);
            request.removeAttribute(WORKFLOW_KEEP_FORWARD);
            request.removeAttribute(WORKFLOW_NODE_ID);
        } else {
            request.setAttribute(WORKFLOW_PARAMS_FORM, params);
            request.setAttribute(WORKFLOW_ID, params.getWorkflowId());
            request.setAttribute(WORKFLOW_FORWARD_TARGET_ITEM_ID, params.getTargetItemId());
            request.setAttribute(WORKFLOW_FORWARD_PARAMS, params.getParams());
            request.setAttribute(WORKFLOW_KEEP_FORWARD, params.getKeepForward());
            request.setAttribute(WORKFLOW_NODE_ID, params.getNodeId());
        }
    }
    
    public static WorkflowParameters fromParams(HttpServletRequest request) {
        String workflowId = request.getParameter(WORKFLOW_ID);
        String targetItem = request.getParameter(WORKFLOW_FORWARD_TARGET_ITEM_ID);
        String nodeId = request.getParameter(WORKFLOW_NODE_ID);
        String keepForward = request.getParameter(WORKFLOW_KEEP_FORWARD);
        String params = request.getParameter(WORKFLOW_FORWARD_PARAMS);
        
        if (workflowId == null && params == null && targetItem == null && keepForward == null && nodeId == null) {
            return null;
        }
    
        WorkflowParameters forwardParams = new WorkflowParameters();
        
        if (workflowId != null) {
            forwardParams.setWorkflowId(Integer.valueOf(workflowId));
        }
        
        if (targetItem != null) {
            forwardParams.setWorkflowForwardTargetItemId(Integer.valueOf(targetItem));
        }
        
        if (nodeId != null) {
            forwardParams.setNodeId(Integer.valueOf(nodeId));
        }
        
        if (keepForward != null) {
            forwardParams.setKeepForward(BooleanUtils.toBoolean(keepForward));
        }
        
        if (params != null) {
            forwardParams.setWorkflowForwardParams(params);
        }
        
        if (forwardParams.isEmpty()) {
            return null;
        }
        
        return forwardParams;
    }
}
