/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.forms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.web.forms.StrutsFormBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

public class ComMailingComponentsForm extends StrutsFormBase {
	private static final long serialVersionUID = 9127503705336591039L;
	
	/**
	 * Holds value of property mailingID.
	 */
	private int mailingID;

	/**
	 * Holds value of property shortname.
	 */
	private String shortname;

	/**
	 * Holds value of property action.
	 */
	private int action;

	/**
	 * Holds value of property isTemplate.
	 */
	private boolean isTemplate;

	/**
     * Holds value of property worldMailingSend.
     */
    private boolean worldMailingSend;

	private Map<Integer, String> links = new HashMap<>();
	private Map<Integer, String> descriptions = new HashMap<>();
	private Map<Integer, String> mobileComponentBaseComponents = new HashMap<>();
	private Map<Integer, FormFile> newFiles = new HashMap<>();
	private Map<Integer, String> fileSizes = new HashMap<>();
	private Map<Integer, String> timestamps;
    private Set<Integer> bulkIDs = new HashSet<>();
    private int componentId;
    private String componentName;
    private String sftpFilePath;
	private int workflowId;
	private boolean isMailingUndoAvailable;
	private String imageFile;

    /**
	 * Reset all properties to their default values.
	 * 
	 * @param mapping
	 *            The mapping used to select this instance
	 * @param request
	 *            The servlet request we are processing
	 */
	@Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		mailingID = 0;
		shortname = ""; // text.getMessage(aLoc, "default.Name");

		links = new HashMap<>();
		descriptions = new HashMap<>();
		mobileComponentBaseComponents = new HashMap<>();
		newFiles = new HashMap<>();
		fileSizes = new HashMap<>();
		imageFile = null;
	}

	public String getImageFile() {
		return imageFile;
	}

	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
	}

	/**
	 * Getter for property mailingID.
	 * 
	 * @return Value of property mailingID.
	 */
	public int getMailingID() {
		return this.mailingID;
	}

	/**
	 * Setter for property mailingID.
	 * 
	 * @param mailingID
	 *            New value of property mailingID.
	 */
	public void setMailingID(int mailingID) {
		this.mailingID = mailingID;
	}

	/**
	 * Getter for property shortname.
	 * 
	 * @return Value of property shortname.
	 */
	public String getShortname() {
		return this.shortname;
	}

	/**
	 * Setter for property shortname.
	 * 
	 * @param shortname
	 *            New value of property shortname.
	 */
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	/**
	 * Getter for property action.
	 * 
	 * @return Value of property action.
	 */
	public int getAction() {
		return this.action;
	}

	/**
	 * Setter for property action.
	 * 
	 * @param action
	 *            New value of property action.
	 */
	public void setAction(int action) {
		this.action = action;
	}

	/**
	 * Getter for property isTemplate.
	 * 
	 * @return Value of property isTemplate.
	 */
	public boolean isIsTemplate() {
		return this.isTemplate;
	}

	/**
	 * Setter for property isTemplate.
	 * 
	 * @param isTemplate
	 *            New value of property isTemplate.
	 */
	public void setIsTemplate(boolean isTemplate) {
		this.isTemplate = isTemplate;
	}

    /**
     * Getter for property worldMailingSend.
     *
     * @return Value of property worldMailingSend.
     */
    public boolean isWorldMailingSend() {
        return this.worldMailingSend;
    }

    /**
     * Setter for property worldMailingSend.
     *
     * @param worldMailingSend New value of property worldMailingSend.
     */
    public void setWorldMailingSend(boolean worldMailingSend) {
        this.worldMailingSend = worldMailingSend;
    }

	public Map<Integer, String> getFileSizes() {
		return fileSizes;
	}

	public void setFileSizes(Map<Integer, String> fileSizes) {
		this.fileSizes = fileSizes;
	}

	public Map<Integer, String> getTimestamps() {
		return timestamps;
	}

	public void setTimestamps(Map<Integer, String> timestamps) {
		this.timestamps = timestamps;
	}

	public String getLink(int index) {
		return links.get(index);
	}
	
	public void setLink(int index, String link) {
		links.put(index, link);
	}
	
	public FormFile getNewFile(int index) {
		return newFiles.get(index);
	}

	public void setNewFile(int index, FormFile newImage) {
		if(newImage != null && StringUtils.isNotEmpty(newImage.getFileName()) && newImage.getFileSize() != 0) {
			newFiles.put(index, newImage);
		}
	}
	
	public String getDescriptionByIndex(int index) {
		return descriptions.get(index);
	}

	public void setDescriptionByIndex(int index, String newDescription) {
		descriptions.put(index, newDescription);
	}
	
	public String getMobileComponentBaseComponent(int index) {
		return mobileComponentBaseComponents.get(index);
	}

	public void setMobileComponentBaseComponent(int index, String newMobileComponentBaseComponent) {
		mobileComponentBaseComponents.put(index, newMobileComponentBaseComponent);
	}
	
	public Map<Integer, FormFile> getAllFiles() {
		return newFiles;
	}
	
	public Set<Integer> getIndices() {
		return newFiles.keySet();
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

    public int getComponentId() {
        return componentId;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
	
    /**
     * This Method is only needed for legacy design mailing-components.jsp
     */
	public String getDescription() {
		return descriptions.get(1);
	}

    /**
     * This Method is only needed for legacy design mailing-components.jsp
     */
	public void setDescription(String newDescription) {
		descriptions.put(1, newDescription);
	}

	public String getSftpFilePath() {
		return sftpFilePath;
	}

	public void setSftpFilePath(String sftpFilePath) {
		this.sftpFilePath = sftpFilePath;
	}

	public int getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(int workflowId) {
		this.workflowId = workflowId;
	}

	public boolean getIsMailingUndoAvailable() {
		return isMailingUndoAvailable;
	}

	public void setIsMailingUndoAvailable(boolean isMailingUndoAvailable) {
		this.isMailingUndoAvailable = isMailingUndoAvailable;
	}
}
