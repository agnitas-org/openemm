/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.taglib;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.TagSupport;
import com.agnitas.emm.core.workflow.beans.parameters.WorkflowParameters;
import com.agnitas.emm.core.workflow.beans.parameters.WorkflowParametersHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorkflowParamsTag extends TagSupport {
	private static final long serialVersionUID = -5936536463929869069L;

	private static final Logger logger = LogManager.getLogger(WorkflowParamsTag.class);
	
    private boolean disabled;
    
    public boolean isDisabled() {
        return disabled;
    }
    
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    
    @Override
    public int doStartTag() throws JspException {
        try {
	
			WorkflowParameters workflowParameters =
					WorkflowParametersHelper.find((HttpServletRequest) this.pageContext.getRequest());
		
        	JspWriter jspWriter = pageContext.getOut();
			if (isTagActive(workflowParameters)) {
			    assert workflowParameters != null;
				writeFieldInput(jspWriter, WorkflowParametersHelper.WORKFLOW_ID, workflowParameters.getWorkflowId());
				writeFieldInput(jspWriter, WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS, workflowParameters.getParams());
				writeFieldInput(jspWriter, WorkflowParametersHelper.WORKFLOW_FORWARD_TARGET_ITEM_ID, workflowParameters.getTargetItemId());
				writeFieldInput(jspWriter, WorkflowParametersHelper.WORKFLOW_KEEP_FORWARD, workflowParameters.getKeepForward());
				writeFieldInput(jspWriter, WorkflowParametersHelper.WORKFLOW_NODE_ID, workflowParameters.getNodeId());
			}
        } catch (IOException e) {
            logger.error("Could not write workflow parameters!");
            throw new JspException("Could not write workflow parameters!", e);
        }
    
        return SKIP_BODY;
    }
	
	private void writeFieldInput(JspWriter writer, String attributeName, Object value) throws IOException {
    	writer.append("<");
    	writer.append("input");
		writer.append(" ");
		writer.append("name=").append(attributeName);
		writer.append(" ");
		writer.append("type=hidden");
		writer.append(" ");
		writer.append("value=").append(String.valueOf(value));
		writer.append(" ");
		writer.append("/>");
	}
	
	private boolean isTagActive(WorkflowParameters workflowParameters) {
		return !this.disabled && WorkflowParametersHelper.isNotEmpty(workflowParameters);
	}
}
