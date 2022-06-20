/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web.forms;

import java.util.HashMap;
import java.util.Map;

import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class WorkflowParameters {
    
    private Boolean keepForward;
    
    private Integer workflowId;
    
    private Integer nodeId;
    
    private String workflowForwardParams;
    
    private Map<String, String> paramsAsMap = new HashMap<>();
    
    private Integer workflowForwardTargetItemId;
    
    public WorkflowParameters() {
        this.workflowId = 0;
        this.nodeId = 0;
        this.workflowForwardParams = "";
        this.workflowForwardTargetItemId = 0;
        this.keepForward = false;
    }
    
    public WorkflowParameters(Integer workflowId, Integer nodeId, String params, Integer targetItemId, Boolean keepForward) {
        if (workflowId == null) {
            workflowId = 0;
        }

        if (nodeId == null) {
            nodeId = 0;
        }
        
        if (params == null) {
            params = "";
        }

        if (targetItemId == null) {
            targetItemId = 0;
        }
        
        if (keepForward == null) {
            keepForward = false;
        }


        this.workflowId = workflowId;
        this.nodeId = nodeId;
        setWorkflowForwardParams(params);
        this.workflowForwardTargetItemId = targetItemId;
        this.keepForward = keepForward;
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        map.put(WorkflowParametersHelper.WORKFLOW_ID, workflowId);
        
        map.put(WorkflowParametersHelper.WORKFLOW_FORWARD_TARGET_ITEM_ID, workflowForwardTargetItemId);
        
        map.put(WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS, workflowForwardParams);
        
        map.put(WorkflowParametersHelper.WORKFLOW_KEEP_FORWARD, keepForward);
        map.put(WorkflowParametersHelper.WORKFLOW_NODE_ID, nodeId);

        return map;
    }
    
    public Boolean getKeepForward() {
        return keepForward;
    }
    
    public void setKeepForward(Boolean keepForward) {
        this.keepForward = keepForward;
    }
    
    public Integer getWorkflowId() {
        return workflowId;
    }
    
    public void setWorkflowId(Integer workflowId) {
        this.workflowId = workflowId;
    }
    
    public Integer getNodeId() {
        return nodeId;
    }
    
    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }
    
    public String getParams() {
        return workflowForwardParams;
    }
    
    public String getWorkflowForwardParams() {
        return workflowForwardParams;
    }
    
    public void setWorkflowForwardParams(String params) {
        this.workflowForwardParams = params;
        setParamsAsMap(AgnUtils.getParamsMap(params));
        setNodeId(NumberUtils.toInt(paramsAsMap.get(WorkflowParametersHelper.WORKFLOW_NODE_ID)));
    }
    
    public Integer getWorkflowForwardTargetItemId() {
        return workflowForwardTargetItemId;
    }
    
    public void setWorkflowForwardTargetItemId(Integer workflowForwardTargetItemId) {
        this.workflowForwardTargetItemId = workflowForwardTargetItemId;
    }
    
    public void setWorkflowForwardTargetItemId(String workflowForwardTargetItemId) {
        this.workflowForwardTargetItemId = NumberUtils.toInt(workflowForwardTargetItemId);
    }
    
    public Integer getTargetItemId() {
        return workflowForwardTargetItemId;
    }
    
    public void setParamsAsMap(Map<String, String> paramsAsMap) {
        this.paramsAsMap = paramsAsMap;
    }
    
    public Map<String, String> getParamsAsMap() {
        return paramsAsMap;
    }
    
    public boolean isEmpty() {
        return workflowId == 0 && nodeId == 0 && workflowForwardTargetItemId == 0 && !keepForward && StringUtils.isEmpty(workflowForwardParams);
    }
    
    @Override
    public String toString() {
        return "workflowID: " + workflowId +
                ", keepForward=" + keepForward +
                ", params='" + workflowForwardParams +
                ", targetID=" + workflowForwardTargetItemId;
    }
}
