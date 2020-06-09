/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web.forms;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.ExportPredef;
import org.agnitas.beans.Mailinglist;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.ExportWizardAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import com.agnitas.beans.TargetLight;

public class ExportWizardForm extends StrutsFormBase {

	/** Serial version UID. */
    private static final long serialVersionUID = -1678091898444495551L;

	/**
     * Holds value of property charset.
     */
    private String charset="UTF-8";
    
    /**
     * Holds value of property action.
     */
    private int action;
    
    /**
     * Holds value of property csvFile.
     */
    private String exportedFile;
    
    /**
     * Holds value of property linesOK.
     */
    private int linesOK;
    
    /**
     * Holds value of property dbExportStatus.
     */
    private int dbExportStatus;
    
    /**
     * Holds value of property separator.
     */
    private String separator;
    
    /**
     * Holds value of property delimiter.
     */
    private String delimiter;
    
    /**
     * Holds value of property downloadName.
     */
    private String downloadName;
    
    /**
     * Holds value of property dbExportStatusMessages.
     */
    private LinkedList<String> dbExportStatusMessages;
    
    /**
     * Holds value of property mailinglists.
     */
    private String[] mailinglists;
    
    /**
     * Holds value of property targetID.
     */
    private int targetID;
    
    /**
     * Holds value of property columns.
     */
    private String[] columns;
    
    /**
     * Holds value of property mailinglistID.
     */
    private int mailinglistID;
    
    /**
     * Holds value of property userType.
     */
    private String userType;
    
    /**
     * Holds value of property userStatus.
     */
    private int userStatus;

	private boolean recipientFilterVisible = true;

	private boolean columnsPanelVisible = true;

	private boolean mlistsPanelVisible = true;

	private boolean fileFormatPanelVisible = true;

	private boolean datesPanelVisible = true;

     /**
     * Holds collection of rows from ExportPredef table.
     */

    private List<ExportPredef> exportPredefList;

     /**
     * Holds number of rows of ExportPredef table.
     */

    private int exportPredefCount;

    private List<TargetLight> targetGroups;

    private List<Mailinglist> mailinglistObjects;

    private String timestampStart;
    private String timestampEnd;
    private String timestampLastDays;

    private String creationDateStart;
    private String creationDateEnd;
    private String creationDateLastDays;

    private String mailinglistBindStart;
    private String mailinglistBindEnd;
    private String mailinglistBindLastDays;
    
    private Date exportStartDate = null;

    private String localeDatePattern;

    private boolean backButtonPressed = false;
    
    private int alwaysQuote = 0;

	/**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    @Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
        if(action != ExportWizardAction.ACTION_COLLECT_DATA
                && action != ExportWizardAction.ACTION_PROCEED
                && action != ExportWizardAction.ACTION_VIEW_STATUS_WINDOW
                && !backButtonPressed){
            clearData();
        }

        backButtonPressed = false;
    }
    
    /**
     * Initialization
     */
    public void clearData() {
        columns = new String[] {};
        mailinglists = new String[] {};
        shortname = "";
        description = "";
        targetID = 0;
        mailinglistID = 0;
        userStatus = 0;
        userType = null;
        targetGroups = new ArrayList<>();
        mailinglistObjects = new ArrayList<>();
        timestampStart = "";
        timestampEnd = "";
        timestampLastDays = "";
        creationDateStart = "";
        creationDateEnd = "";
        creationDateLastDays = "";
        mailinglistBindStart = "";
        mailinglistBindEnd = "";
        mailinglistBindLastDays = "";
        exportStartDate = null;
    }
    
	/**
	 * Validate the properties that have been set from this HTTP request,
	 * and return an <code>ActionErrors</code> object that encapsulates any
	 * validation errors that have been found.  If no errors are found,
	 * return <code>null</code> or an <code>ActionErrors</code> object with
	 * no recorded error messages.
	 * 
	 * @param mapping The mapping used to select this instance
	 * @param request The servlet request we are processing
	 * @return errors
	 */
    
	@Override
	public ActionErrors formSpecificValidate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();

		if (action == ExportWizardAction.ACTION_COLLECT_DATA) {
			/* Make sure there were columns selected */
			if (request.getParameter("columns") == null) {
				columns = new String[] {};
			}
			if (columns != null && columns.length == 0) {
				errors.add("global", new ActionMessage("error.export.no_columns_selected"));
			}

            String pattern = AgnUtils.getAdmin(request).getDateFormat().toPattern();
			validateDate(timestampStart, timestampEnd, pattern, errors);
			validateDate(creationDateStart, creationDateEnd, pattern, errors);
			validateDate(mailinglistBindStart, mailinglistBindEnd, pattern, errors);
		}

		return errors;
	}

    private void validateDate(String startDate, String endDate, String pattern, ActionErrors errors) {
        if (!StringUtils.isEmpty(startDate) && !AgnUtils.isDateValid(startDate, pattern)) {
            errors.add("global", new ActionMessage("error.date.format"));
        } else if (!StringUtils.isEmpty(endDate) && !AgnUtils.isDateValid(endDate, pattern)) {
            errors.add("global", new ActionMessage("error.date.format"));
        } else if (StringUtils.isNotEmpty(startDate) && StringUtils.isNotEmpty(endDate) && !AgnUtils.isDatePeriodValid(startDate, endDate, pattern)) {
            errors.add("global", new ActionMessage("error.period.format"));
        }
    }

    /**
     * Getter for property charset.
     *
     * @return Value of property charset.
     */
    public String getCharset() {
        return charset;
    }
    
    /**
     * Setter for property charset.
     *
     * @param charset New value of property charset.
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }
    
    /**
     * Getter for property action.
     *
     * @return Value of property action.
     */
    public int getAction() {
        return action;
    }
    
    /**
     * Setter for property action.
     *
     * @param action New value of property action.
     */
    public void setAction(int action) {
        this.action = action;
    }
    
    /**
     * Getter for property exportedFile.
     *
     * @return Value of property exportedFile.
     */
    public String getExportedFile() {
        return exportedFile;
    }
    
    /**
     * Setter for property exportedFile.
     *
     * @param exportedFile New value of property csvFile.
     */
    public void setExportedFile(String exportedFile) {
        this.exportedFile = exportedFile;
    }
    
    /**
     * Getter for property linesOK.
     *
     * @return Value of property linesOK.
     */
    public int getLinesOK() {
        return linesOK;
    }
    
    /**
     * Setter for property linesOK.
     *
     * @param linesOK New value of property linesOK.
     */
    public void setLinesOK(int linesOK) {
        this.linesOK = linesOK;
    }
    
    /**
     * Getter for property dbExportStatus.
     *
     * @return Value of property dbExportStatus.
     */
    public int getDbExportStatus() {
        return dbExportStatus;
    }
    
    /**
     * Setter for property dbExportStatus.
     * 
     * @param dbExportStatus
     */
    public void setDbExportStatus(int dbExportStatus) {
        this.dbExportStatus = dbExportStatus;
    }
    
    /**
     * Getter for property separator.
     *
     * @return Value of property separator.
     */
    public String getSeparator() {
        return separator;
    }
    
    /**
     * Setter for property separator.
     *
     * @param separator New value of property separator.
     */
    public void setSeparator(String separator) {
    	if(separator.equals("t")) {
        	this.separator = "\t";
        } else {
        	this.separator = separator;
        }
    }
    
    /**
     * Setter for property separator.
     *
     * @param separator New value of property separator.
     */
    public void setSeparatorInternal(String separator) {
       	this.separator=separator;
    }
    
    /**
     * Getter for property delimitor.
     *
     * @return Value of property delimitor.
     */
    public String getDelimiter() {
        return this.delimiter;
    }
    
    /**
     * Setter for property delimitor.
     * 
     * @param delimiter
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }
    
    /**
     * Getter for property downloadName.
     *
     * @return Value of property downloadName.
     */
    public String getDownloadName() {
        return this.downloadName;
    }
    
    /**
     * Setter for property downloadName.
     *
     * @param downloadName New value of property downloadName.
     */
    public void setDownloadName(String downloadName) {
        this.downloadName = downloadName;
    }
    
    /**
     * Getter for property dbInsertStatusMessages.
     *
     * @return Value of property dbInsertStatusMessages.
     */
    /*
     *  TODO: This seems not to be used anymore...
     * 
     *  No reference to this method in Java code
     *  No reference to bean in JSPs.
     * 
     *  If it can be removed, remove also setter method and field.
     * 
     *  Note: Setter is still called.
     */
    public LinkedList<String> getDbExportStatusMessages() {
        return this.dbExportStatusMessages;
    }
    
    /**
     * Setter for property dbInsertStatusMessages.
     * 
     * @param dbExportStatusMessages
     */
    public void setDbExportStatusMessages(LinkedList<String> dbExportStatusMessages) {
        this.dbExportStatusMessages = dbExportStatusMessages;
    }
    
    /**
     * Adds a database export status message.
     */
    public void addDbExportStatusMessage(String message) {
        if(this.dbExportStatusMessages==null) {
            this.dbExportStatusMessages=new LinkedList<>();
        }
        
        this.dbExportStatusMessages.add(message);
    }
    
    /**
     * Getter for property mailinglists.
     *
     * @return Value of property mailinglists.
     */
    public String[] getMailinglists() {
        return this.mailinglists;
    }
    
    /**
     * Setter for property mailinglists.
     *
     * @param mailinglists New value of property mailinglists.
     */
    public void setMailinglists(String[] mailinglists) {
        this.mailinglists = mailinglists;
    }
    
    /**
     * Getter for property targetID.
     *
     * @return Value of property targetID.
     */
    public int getTargetID() {
        return this.targetID;
    }
    
    /**
     * Setter for property targetID.
     *
     * @param targetID New value of property targetID.
     */
    public void setTargetID(int targetID) {
        this.targetID = targetID;
    }
    
    /**
     * Getter for property columns.
     *
     * @return Value of property columns
     */
    public String[] getColumns() {
        return this.columns;
    }
    
    /**
     * Setter for property columns.
     *
     * @param columns New value of property columns.
     */
    public void setColumns(String[] columns) {
        this.columns = columns;
    }
    
    /**
     * Getter for property mailinglistID.
     *
     * @return Value of property mailinglistID.
     */
    public int getMailinglistID() {
        return this.mailinglistID;
    }
    
    /**
     * Setter for property mailinglistID.
     *
     * @param mailinglistID New value of property mailinglistID.
     */
    public void setMailinglistID(int mailinglistID) {
        this.mailinglistID = mailinglistID;
    }
    
    /**
     * Getter for property userType.
     *
     * @return Value of property userType.
     */
    public String getUserType() {
        return this.userType;
    }
    
    /**
     * Setter for property userType.
     *
     * @param userType New value of property userType.
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }
    
    /**
     * Getter for property userStatus.
     *
     * @return Value of property userStatus.
     */
    public int getUserStatus() {
        return this.userStatus;
    }
    
    /**
     * Setter for property userStatus.
     *
     * @param userStatus New value of property userStatus.
     */
    public void setUserStatus(int userStatus) {
        this.userStatus = userStatus;
    }

    /**
     * Holds value of property shortname.
     */
    private String shortname = "";

    /**
     * Getter for property shortname.
     *
     * @return Value of property shortname.
     */
    public String getShortname() {
        return shortname;
    }

    /**
     * Setter for property shortname.
     *
     * @param shortname New value of property shortname.
     */
    public void setShortname(String shortname) {

        this.shortname = shortname;
    }

    /**
     * Holds value of property description.
     */
    private String description;

    /**
     * Getter for property description.
     *
     * @return Value of property description.
     */
    public String getDescription() {

        return this.description;
    }

    /**
     * Setter for property description.
     *
     * @param description New value of property description.
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * Holds value of property exportPredefID.
     */
    private int exportPredefID;

    /**
     * Getter for property exportPredefID.
     *
     * @return Value of property exportPredefID.
     */
    public int getExportPredefID() {

        return this.exportPredefID;
    }

    /**
     * Setter for property exportPredefID.
     *
     * @param exportPredefID New value of property exportPredefID.
     */
    public void setExportPredefID(int exportPredefID) {

        this.exportPredefID = exportPredefID;
    }

	public boolean isRecipientFilterVisible() {
		return recipientFilterVisible;
	}

	public void setRecipientFilterVisible(boolean recipientFilterVisible) {
		this.recipientFilterVisible = recipientFilterVisible;
	}

	public boolean isColumnsPanelVisible() {
		return columnsPanelVisible;
	}

	public void setColumnsPanelVisible(boolean columnsPanelVisible) {
		this.columnsPanelVisible = columnsPanelVisible;
	}

	public boolean isMlistsPanelVisible() {
		return mlistsPanelVisible;
	}

	public void setMlistsPanelVisible(boolean mlistsPanelVisible) {
		this.mlistsPanelVisible = mlistsPanelVisible;
	}

    public boolean isDatesPanelVisible() {
        return datesPanelVisible;
    }

    public void setDatesPanelVisible(boolean datesPanelVisible) {
        this.datesPanelVisible = datesPanelVisible;
    }

    public boolean isFileFormatPanelVisible() {
		return fileFormatPanelVisible;
	}

	public void setFileFormatPanelVisible(boolean fileFormatPanelVisible) {
		this.fileFormatPanelVisible = fileFormatPanelVisible;
	}

    public List<ExportPredef> getExportPredefList() {
        return exportPredefList;
    }

    public void setExportPredefList(List<ExportPredef> exportPredefList) {
        this.exportPredefList = exportPredefList;
    }

    public int getExportPredefCount() {
        return exportPredefCount;
    }

    public void setExportPredefCount(int exportPredefCount) {
        this.exportPredefCount = exportPredefCount;
    }

    public List<TargetLight> getTargetGroups() {
        return targetGroups;
    }

    public void setTargetGroups(List<TargetLight> targetGroups) {
        this.targetGroups = targetGroups;
    }

    public List<Mailinglist> getMailinglistObjects() {
        return mailinglistObjects;
    }

    public void setMailinglistObjects(List<Mailinglist> mailinglistObjects) {
        this.mailinglistObjects = mailinglistObjects;
    }

    public String getTimestampStart() {
        return timestampStart;
    }

    public void setTimestampStart(String timestampStart) {
        this.timestampStart = timestampStart;
    }

    public String getTimestampEnd() {
        return timestampEnd;
    }

    public void setTimestampEnd(String timestampEnd) {
        this.timestampEnd = timestampEnd;
    }

    public String getTimestampLastDays() {
    	if (timestampLastDays != null) {
    		return timestampLastDays.trim();
    	} else {
    		return null;
    	}
    }

    public void setTimestampLastDays(String timestampLastDays) {
        this.timestampLastDays = timestampLastDays;
    }

    public String getCreationDateStart() {
        return creationDateStart;
    }

    public void setCreationDateStart(String creationDateStart) {
        this.creationDateStart = creationDateStart;
    }

    public String getCreationDateEnd() {
        return creationDateEnd;
    }

    public void setCreationDateEnd(String creationDateEnd) {
        this.creationDateEnd = creationDateEnd;
    }

    public String getCreationDateLastDays() {
    	if (creationDateLastDays != null) {
    		return creationDateLastDays.trim();
    	} else {
    		return null;
    	}
    }

    public void setCreationDateLastDays(String creationDateLastDays) {
        this.creationDateLastDays = creationDateLastDays;
    }

    public String getMailinglistBindStart() {
        return mailinglistBindStart;
    }

    public void setMailinglistBindStart(String mailinglistBindStart) {
        this.mailinglistBindStart = mailinglistBindStart;
    }

    public String getMailinglistBindEnd() {
        return mailinglistBindEnd;
    }

    public void setMailinglistBindEnd(String mailinglistBindEnd) {
        this.mailinglistBindEnd = mailinglistBindEnd;
    }

    public String getMailinglistBindLastDays() {
    	if (mailinglistBindLastDays != null) {
    		return mailinglistBindLastDays.trim();
    	} else {
    		return null;
    	}
    }

    public void setMailinglistBindLastDays(String mailinglistBindLastDays) {
        this.mailinglistBindLastDays = mailinglistBindLastDays;
    }

	public Date getExportStartDate() {
		return exportStartDate;
	}

	public void setExportStartDate(Date exportStartDate) {
		this.exportStartDate = exportStartDate;
	}

    public String getLocaleDatePattern() {
        return localeDatePattern;
    }

    public void setLocaleDatePattern(String localeDatePattern) {
        this.localeDatePattern = localeDatePattern;
    }

    public boolean isBackButtonPressed() {
        return backButtonPressed;
    }

    public void setBackButtonPressed(boolean backButtonPressed) {
        this.backButtonPressed = backButtonPressed;
    }

	public int getAlwaysQuote() {
		return alwaysQuote;
	}

	public void setAlwaysQuote(int alwaysQuote) {
		this.alwaysQuote = alwaysQuote;
	}
}
