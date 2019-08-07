/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.forms;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.web.forms.StrutsFormBase;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

// TODO: Auto-generated Javadoc
/**
 * The Class FormComponentsForm.
 */
public class FormComponentsForm extends StrutsFormBase {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8064739614892274458L;

	/** The method. */
	private String method;
	
	/** The form id. */
	private int formID = 0;

	/** The shortname of a form */
	private String formName;

	/**  The filename. */
	private String filename = null;
	
	/** The archive file. */
	private FormFile archiveFile;
	
	/** The overwrite existing. */
	private boolean overwriteExisting = false;
	
	/**  The new files: Used as indexed property in JSP's. */
	private Map<Integer, FormFile> newFiles = new HashMap<>();
	
	/**  The descriptions: Used as indexed property in JSP's. */
	private Map<Integer, String> descriptions = new HashMap<>();

	/**
	 * Gets the method.
	 *
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Sets the method.
	 *
	 * @param method the new method
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Gets the form id.
	 *
	 * @return the form id
	 */
	public int getFormID() {
		return formID;
	}

	/**
	 * Sets the form id.
	 *
	 * @param formID the new form id
	 */
	public void setFormID(int formID) {
		this.formID = formID;
	}

	/**
	 * Gets the form shortname.
	 *
	 * @return the form shortname
     */
	public String getFormName() {
		return formName;
	}

	/**
	 * Sets the form shortname.
	 *
	 * @param formName the new form shortname
     */
	public void setFormName(String formName) {
		this.formName = formName;
	}

	/**
	 * Gets the filename.
	 *
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Sets the filename.
	 *
	 * @param filename the new filename
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * Gets the archive file.
	 *
	 * @return the archive file
	 */
	public FormFile getArchiveFile() {
		return archiveFile;
	}

	/**
	 * Sets the archive file.
	 *
	 * @param archiveFile the new archive file
	 */
	public void setArchiveFile(FormFile archiveFile) {
		this.archiveFile = archiveFile;
	}
	
	/**
	 * Getter for indexed property NewFile.
	 *
	 * @param index the index
	 * @return the new file
	 */
	public FormFile getNewFiles(int index) {
		return newFiles.get(index);
	}

	/**
	 * Setter for indexed property NewFile.
	 *
	 * @param index the index
	 * @param newImage the new image
	 */
	public void setNewFiles(int index, FormFile newImage) {
		newFiles.put(index, newImage);
	}

	/**
	 * Gets all the new files.
	 *
	 * @return the new files
	 */
	public Map<Integer, FormFile> getAllNewFiles() {
		return newFiles;
	}
	
	/**
	 * Getter for indexed property Descriptions.
	 *
	 * @param index the index
	 * @return the descriptions
	 */
	public String getDescriptions(int index) {
		return descriptions.get(index);
	}

	/**
	 * Setter for indexed property Descriptions.
	 *
	 * @param index the index
	 * @param newDescription the new description
	 */
	public void setDescriptions(int index, String newDescription) {
		descriptions.put(index, newDescription);
	}

	public String getDescriptionByIndex(int index) {
		return descriptions.get(index);
	}

	public void setDescriptionByIndex(int index, String newDescription) {
		descriptions.put(index, newDescription);
	}

	/**
	 * Gets all the Descriptions.
	 *
	 * @return the Descriptions
	 */
	public Map<Integer, String> getAllDescriptions() {
		return descriptions;
	}

	/**
	 * Checks if is overwrite existing.
	 *
	 * @return true, if is overwrite existing
	 */
	public boolean isOverwriteExisting() {
		return overwriteExisting;
	}

	/**
	 * Sets the overwrite existing.
	 *
	 * @param overwriteExisting the new overwrite existing
	 */
	public void setOverwriteExisting(boolean overwriteExisting) {
		this.overwriteExisting = overwriteExisting;
	}
	
	/* (non-Javadoc)
	 * @see org.agnitas.web.forms.StrutsFormBase#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		// Reset of overwriteExisting to false is needed because HTML does not transfer unchecked checkboxes in parameters
		overwriteExisting = false;
		newFiles = new HashMap<>();
		descriptions = new HashMap<>();
    }
}
