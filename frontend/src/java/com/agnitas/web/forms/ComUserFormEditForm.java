/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.forms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.web.UserFormEditForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;


public class ComUserFormEditForm extends UserFormEditForm {
	private static final long serialVersionUID = -5018481921741506904L;

	private Set<Integer> bulkIDs = new HashSet<>();
    
    /** Holds value of property formUrl. */
    private String formUrl = "";

    /** This parameter is set when we do forward from workflow editor to create new userForm */
    private int workflowId;

    private String forwardParams;

    private Map<Integer, Boolean> activenessMap = new HashMap<>();

    private String activenessFilter;

	private FormFile uploadFile;

    public String getFormUrl() {
		return formUrl;
	}

	public void setFormUrl(String formUrl) {
		this.formUrl = formUrl;
	}

    public ComUserFormEditForm() {
        super();
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);

        clearBulkIds();
        activenessMap.clear();
        activenessFilter = null;

		uploadFile = null;
    }

    public void setBulkID(int id, String value) {
        if (value != null && (value.equals("on") || value.equals("yes") || value.equals("true")))
            this.bulkIDs.add(id);
    }

    public String getBulkID(int id) {
        return this.bulkIDs.contains(id) ? "on" : "";
    }

    public Set<Integer> getBulkIds() {
        return this.bulkIDs;
    }

    public void clearBulkIds() {
        this.bulkIDs.clear();
    }

    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }

    public int getWorkflowId() {
        return workflowId;
    }

    public void setForwardParams(String forwardParams) {
        this.forwardParams = forwardParams;
    }

    public String getForwardParams() {
        return forwardParams;
    }

    public void setActiveness(int id, String value) {
        activenessMap.put(id, Boolean.parseBoolean(value));
    }

    public Map<Integer, Boolean> getActivenessMap() {
        return activenessMap;
    }

    public String getActivenessFilter() {
        return activenessFilter;
    }

    public void setActivenessFilter(String activenessFilter) {
        this.activenessFilter = activenessFilter;
    }

	public FormFile getUploadFile() {
		return uploadFile;
	}

	public void setUploadFile(FormFile uploadFile) {
		this.uploadFile = uploadFile;
	}
}
