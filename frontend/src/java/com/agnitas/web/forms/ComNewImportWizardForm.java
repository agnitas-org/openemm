/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.forms;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.CustomerImportStatus;
import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.CustomerImportStatusImpl;
import org.agnitas.service.impl.CSVColumnState;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.ImportReportEntry;
import org.agnitas.web.ProfileImportAction;
import org.agnitas.web.forms.ImportBaseFileForm;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.agnitas.emm.core.upload.bean.UploadData;

public class ComNewImportWizardForm extends ImportBaseFileForm {
	private static final long serialVersionUID = 7123335604116366489L;

	private int action;
    private List<ImportProfile> importProfiles;
    private int defaultProfileId = 0;
    private int datasourceId;
    private List<Mailinglist> allMailingLists;
    private List<Integer> listsToAssign;
	private boolean resultPagePrepared;
    private List<Mailinglist> assignedMailingLists;
    private Map<Integer, Integer> mailinglistAssignStats;
    private String calendarDateFormat;
    private String mailinglistAddMessage;
    private int completedPercent = -1;
    private LinkedList<LinkedList<String>> previewParsedContent;
    private int all;
    private File invalidRecipientsFile;
    private File validRecipientsFile;
    private File fixedRecipientsFile;
    private File duplicateRecipientsFile;
    private int downloadFileType;
    private File resultFile;
	private ActionMessages errorsDuringImport;
	private CSVColumnState[] columns = null;
	
	private CustomerImportStatus status = new CustomerImportStatusImpl();
	
    protected Collection<ImportReportEntry> reportEntries;
	private Mailinglist enforceMailinglist;
	private Set<Integer> selectedMailinglists = new HashSet<>();
	
	public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public List<ImportProfile> getImportProfiles() {
        return importProfiles;
    }

    public void setImportProfiles(List<ImportProfile> importProfiles) {
        this.importProfiles = importProfiles;
    }

	public ActionMessages getErrorsDuringImport() {
		return errorsDuringImport;
	}

	public void setErrorsDuringImport(ActionMessages errorsDuringImport) {
		this.errorsDuringImport = errorsDuringImport;
	}

    public void setDefaultProfileId(int defaultProfileId) {
        this.defaultProfileId = defaultProfileId;
    }

    public int getDefaultProfileId() {
        return defaultProfileId;
    }

    public CustomerImportStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerImportStatus status) {
    	this.status = status;
    }

    public Collection<ImportReportEntry> getReportEntries() {
        return reportEntries;
    }

    public void setReportEntries(Collection<ImportReportEntry> reportEntries) {
        this.reportEntries = reportEntries;
    }

    public int getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(int datasourceId) {
        this.datasourceId = datasourceId;
    }

    public List<Mailinglist> getAllMailingLists() {
        return allMailingLists;
    }

    public void setAllMailingLists(List<Mailinglist> allMailingLists) {
        this.allMailingLists = allMailingLists;
    }

    public List<Mailinglist> getAssignedMailingLists() {
        return assignedMailingLists;
    }

    public void setAssignedMailingLists(List<Mailinglist> assignedMailingLists) {
        this.assignedMailingLists = assignedMailingLists;
    }

    public Map<Integer, Integer> getMailinglistAssignStats() {
        return mailinglistAssignStats;
    }

    public void setMailinglistAssignStats(Map<Integer, Integer> mailinglistAssignStats) {
        this.mailinglistAssignStats = mailinglistAssignStats;
    }

    public String getCalendarDateFormat() {
        return calendarDateFormat;
    }

    public void setCalendarDateFormat(String calendarDateFormat) {
        this.calendarDateFormat = calendarDateFormat;
    }

    public String getMailinglistAddMessage() {
        return mailinglistAddMessage;
    }

    public void setMailinglistAddMessage(String mailinglistAddMessage) {
        this.mailinglistAddMessage = mailinglistAddMessage;
    }

    @Override
	public ActionErrors formSpecificValidate(ActionMapping actionMapping, HttpServletRequest request) {
        ActionErrors actionErrors = super.formSpecificValidate(actionMapping, request);
        if (actionErrors == null) {
            actionErrors = new ActionErrors();
        }
        
        if (action == ProfileImportAction.ACTION_START) {
        	status = new CustomerImportStatusImpl();
        }
        
        if (request.getParameter("start_proceed") != null) {
            if (defaultProfileId == 0) {
                actionErrors.add("global", new ActionMessage("error.import.no_profile_exists"));
            }
        }
        return actionErrors;
    }

    public LinkedList<LinkedList<String>> getPreviewParsedContent() {
        return previewParsedContent;
    }

    public void setPreviewParsedContent(LinkedList<LinkedList<String>> previewParsedContent) {
        this.previewParsedContent = previewParsedContent;
    }

    public void setAll(int all) {
        this.all = all;
    }

    public int getAll() {
        return all;
    }

    public File getInvalidRecipientsFile() {
        return invalidRecipientsFile;
    }

    public void setInvalidRecipientsFile(File invalidRecipientsFile) {
        this.invalidRecipientsFile = invalidRecipientsFile;
    }

    public File getValidRecipientsFile() {
        return validRecipientsFile;
    }

    public void setValidRecipientsFile(File validRecipientsFile) {
        this.validRecipientsFile = validRecipientsFile;
    }

    public File getFixedRecipientsFile() {
        return fixedRecipientsFile;
    }

    public void setFixedRecipientsFile(File fixedRecipientsFile) {
        this.fixedRecipientsFile = fixedRecipientsFile;
    }

    public int getDownloadFileType() {
        return downloadFileType;
    }

    public void setDownloadFileType(int downloadFileType) {
        this.downloadFileType = downloadFileType;
    }

    public File getDuplicateRecipientsFile() {
        return duplicateRecipientsFile;
    }

    public void setDuplicateRecipientsFile(File duplicateRecipientsFile) {
        this.duplicateRecipientsFile = duplicateRecipientsFile;
    }

	public List<Integer> getListsToAssign() {
		return listsToAssign;
	}

	public void setListsToAssign(List<Integer> listsToAssign) {
		this.listsToAssign = listsToAssign;
	}

	public boolean isResultPagePrepared() {
		return resultPagePrepared;
	}

	public void setResultPagePrepared(boolean resultPagePrepared) {
		this.resultPagePrepared = resultPagePrepared;
	}

    public File getResultFile() {
        return resultFile;
    }

    public void setResultFile(File resultFile) {
        this.resultFile = resultFile;
    }

    public void setCompletedPercent(int completedPercent) {
        this.completedPercent = completedPercent;
    }

    public int getCompletedPercent() {
    	return completedPercent;
    }

	public void setColumns(CSVColumnState[] columns) {
		this.columns = columns;
	}

	public CSVColumnState[] getColumns() {
		return columns;
	}
	
	/**
     *  Holds value of property attachmentCsvFileID.
     */
    private List<UploadData> csvFiles;

    /** Getter for property csvFiles.
     * @return Value of property csvFiles.
     */
    public List<UploadData> getCsvFiles() {
        return csvFiles;
    }

    /** Setter for property csvFiles.
     * @param csvFiles New value of property csvFiles.
     */
    public void setCsvFiles(List<UploadData> csvFiles) {
        this.csvFiles = csvFiles;
    }
	
	public void setEnforceMailinglist(Mailinglist enforceMailinglist) {
		this.enforceMailinglist = enforceMailinglist;
	}
	
	public Mailinglist getEnforceMailinglist() {
		return enforceMailinglist;
	}
	
	public Set<Integer> getSelectedMailinglists() {
		return selectedMailinglists;
	}
	
	public void setSelectedMailinglist(int id, String value) {
		if (AgnUtils.interpretAsBoolean(value)) {
			selectedMailinglists.add(id);
		} else {
			selectedMailinglists.remove(id);
		}
	}
	
	public String getSelectedMailinglist(int id) {
		return selectedMailinglists.contains(id) ? "on" : "";
	}
	
	@Override
	public void reset(ActionMapping map, HttpServletRequest request) {
		super.reset(map, request);
		defaultProfileId = 0;
		selectedMailinglists = new HashSet<>();
		setNumberOfRows(-1);
	}
	
	public void clearLists() {
    	if(listsToAssign != null) {
    		listsToAssign.clear();
	    }
		selectedMailinglists.clear();
	}
}
