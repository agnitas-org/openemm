/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.forms;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class ComBirtStatForm extends ActionForm {
	/**
	 * 
	 */
	private static final long serialVersionUID = -821878434044603832L;

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
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();

        validateDate(this.startDay, this.endDay, request, errors);
        validateDate(this.selectDay, request, errors);

		return errors;
	}

	private String getLocalePattern(HttpServletRequest request) {
		return AgnUtils.getLocaleDateFormatSpecific(request).toPattern();
	}

    private void validateDate(String date, HttpServletRequest request, ActionErrors errors) {
	    String localePattern = getLocalePattern(request);
	    if (!StringUtils.isEmpty(date) && !AgnUtils.isDateValid(date, localePattern)) {
            errors.add("global", new ActionMessage("error.date.format"));
        }
    }

	private void validateDate(String startDate, String endDate, HttpServletRequest request, ActionErrors errors) {
		String localePattern = getLocalePattern(request);
        if (!StringUtils.isEmpty(startDate) && !AgnUtils.isDateValid(startDate, localePattern)) {
            errors.add("global", new ActionMessage("error.date.format"));
        }
        if (!StringUtils.isEmpty(endDate) && !AgnUtils.isDateValid(endDate, localePattern)) {
            errors.add("global", new ActionMessage("error.date.format"));
        }
        if ((!StringUtils.isEmpty(startDate) || !StringUtils.isEmpty(endDate)) && !AgnUtils.isDatePeriodValid(startDate, endDate, localePattern)) {
            errors.add("global", new ActionMessage("error.period.format"));
        }
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
	 * Compute report start-/end date by different modes.
	 */
	public enum DateMode {
		NONE,
		LAST_TENHOURS,
		LAST_DAY,
		LAST_FORTNIGHT,
		LAST_MONTH,
		LAST_WEEK,
		SELECT_DAY,
		SELECT_MONTH,
		SELECT_PERIOD
	}

    private String shortname;

    private String description;
	
	
	// private static final long serialVersionUID = -1L; // you really have to read something about serialization !

	/**
	 * show report for thisy company
	 */
	private int companyID;
	
	/**
   	 * which date selection mode to use? (select month, select period, shortcuts)
   	 */
   	private DateMode dateSelectMode;

	/**
   	 * Selected end date of period (arbitrary selected period). 
   	 */
   	private String endDay;
    /**
     * restrict report by mailinglist
     */
	private int mailingListID;
   	/**
   	 * Restrict report by mediatype
   	 */
	private int mediaType;
	/**
	 * Selected month (one month period).
	 */
	private Integer month;
   	/**
     * render report as html, pdf or csv
     */
	private String reportFormat;
	/**
	 * name of the report that should be rendered (*.rptdesign)
	 */
	private String reportName;
	/**
	 * complete url to call the report via the webviewer
	 */
   	private String reportUrl;

//   	/**
//   	 * show the report in the iframe or not.
//   	 */
//   	private boolean showReport;
   	
   	/**
   	 * show hourly stats for selected day. 
   	 */
   	private String selectDay;
	/**
	 * list of selected targetids (for checkboxes)
	 */
   	private String[] selectedTargets;
  	/**
   	 * Selected beginning date of period (arbitrary selected period). 
   	 */
    private String startDay; 	
    /**
     * restrict report by this target
     */
  	private int targetID;
   	/**
	 * Selected year (one month period).
	 */
	private Integer year;

	/**
	 * for mailing bounces: hard/soft/both @see BounceType
	 */
	private String bouncetype;
	
	
	/**
	 * which kind of recipients should be included in the statistics value ? 
	 */
	
	private String recipientType = "ALL_SUBSCRIBERS";
	
	
	private boolean deepTracking;
	
	private String startDate_localized;
	
	private String stopDate_localized;
	
	private Integer urlID;
	
	private boolean reportHasDateParameters;

    private int sector;

    private boolean showNumStat;

    private boolean showAlphaStat;

    private boolean showSimpleStat;

    private boolean topLevelDomain;

	private boolean mailtrackingActive;

	private boolean mailtrackingExpired;

	private int mailtrackingExpirationDays;

	private boolean everSent;

	private String maxDevices;

	/**
	 * Shows percentage value with base of delivered mails instead of sent.
	 */
	private boolean showNetto;

	private boolean isMailingGrid;

	public boolean isDeepTracking() {
		return deepTracking;
	}

	public void setDeepTracking(boolean deepTracking) {
		this.deepTracking = deepTracking;
	}

	public String getRecipientType() {
		return recipientType;
	}

	public void setRecipientType(String recipientType) {
		this.recipientType = recipientType;
	}
	
	public String getBouncetype() {
		return bouncetype;
	}

	public void setBouncetype(String bouncetype) {
		this.bouncetype = bouncetype;
	}

	public int getCompanyID() {
		return companyID;
	}

	public DateMode getDateMode() {
		return dateSelectMode;
	}

	public String getDateSelectMode() {
		return (null != dateSelectMode)? dateSelectMode.toString() : null;
	}

	public String getEndDay() {
		return endDay;
	}

	public int getMailingListID() {
		return mailingListID;
	}

	public int getMediaType() {
		return mediaType;
	}

	public Integer getMonth() {
		return this.month;
	}

	public String getReportFormat() {
		return reportFormat;
	}    

 

	public String getReportName() {
		return reportName;
	}

	public String getReportUrl() {
		return reportUrl;
	}

	public String[] getSelectedTargets() {
		return selectedTargets;
	}

	public String getStartDay() {   
		return startDay;
	}
	
    public int getTargetID() {
		return targetID;
	}

	
	public Integer getYear() {
		return year;
	}

	public void setCompanyID(@VelocityCheck int companyID) {
		this.companyID = companyID;
	}

	public void setDateMode(DateMode dateMode) {
		this.dateSelectMode = dateMode;
	}

	public void setDateSelectMode(String dateSelectMode) {
		if (StringUtils.isEmpty(dateSelectMode))
			this.dateSelectMode = DateMode.SELECT_MONTH;
		else
			this.dateSelectMode = DateMode.valueOf(dateSelectMode);
	}
	
	public void setEndDay(String endDay)  {
		this.endDay = endDay;
	}
	
	public void setMailingListID(int mailingListID) {
		this.mailingListID = mailingListID;
	}

	
	public void setMediaType(int mediaType) {
		this.mediaType = mediaType;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public void setReportFormat(String reportFormat) {
		this.reportFormat = reportFormat;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public void setReportUrl(String reportUrl) {
		this.reportUrl = reportUrl;
	}

	public void setSelectedTargets(String[] selectedTargets) {
		this.selectedTargets = selectedTargets;
	}


	public void setStartDay(String startDay){		
		this.startDay = startDay;
	}
	public void setTargetID(int targetID) {
		this.targetID = targetID;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

//	public boolean isShowReport() {
//		return showReport;
//	}
//
//	public void setShowReport(boolean showReport) {
//		this.showReport = showReport;
//	}

	public String getMaxDevices() {
		return maxDevices;
	}

	public void setMaxDevices(String maxDevices) {
		this.maxDevices = maxDevices;
	}

	private String maxDomains;


	public String getMaxDomains() {
		return maxDomains;
	}

	public void setMaxDomains(String maxDomains) {
		this.maxDomains = maxDomains;
	}
	
	public String getSelectDay() {
		return selectDay;
	}

	public void setSelectDay(String selectDay) {
		this.selectDay = selectDay;
	}
	
	
	/** belongs to mailing stat */
	Integer mailingID = 0;

	private int showReportOnly;


	public Integer getMailingID() {
		return mailingID;
	}

	public void setMailingID(Integer mailingId) {
		this.mailingID = mailingId;
	}

	public String getStartDate_localized() {
		return startDate_localized;
	}

	public void setStartDate_localized(String startDate_localized) {
		this.startDate_localized = startDate_localized;
	}

	public String getStopDate_localized() {
		return stopDate_localized;
	}

	public void setStopDate_localized(String stopDate_localized) {
		this.stopDate_localized = stopDate_localized;
	}

	public Integer getUrlID() {
		return urlID;
	}

	public void setUrlID(Integer urlID) {
		this.urlID = urlID;
	}

	public void setShowReportOnly( int showReportOnly) {
		this.showReportOnly = showReportOnly;
	}
	
	public int getShowReportOnly() {
		return this.showReportOnly;
	}

	public boolean isReportHasDateParameters() {
		return reportHasDateParameters;
	}

	public void setReportHasDateParameters(boolean reportHasDateParameters) {
		this.reportHasDateParameters = reportHasDateParameters;
	}

    public int getSector() {
        return sector;
    }

    public void setSector(int sector) {
        this.sector = sector;
    }

    public boolean getShowNumStat() {
        return showNumStat;
    }

    public void setShowNumStat(boolean showNumStat) {
        this.showNumStat = showNumStat;
    }

    public boolean getShowAlphaStat() {
        return showAlphaStat;
    }

    public void setShowAlphaStat(boolean showAlphaStat) {
        this.showAlphaStat = showAlphaStat;
    }

    public boolean getShowSimpleStat() {
        return showSimpleStat;
    }

    public void setShowSimpleStat(boolean showSimpleStat) {
        this.showSimpleStat = showSimpleStat;
    }

    public boolean isTopLevelDomain() {
        return topLevelDomain;
    }

    public void setTopLevelDomain(boolean topLevelDomain) {
        this.topLevelDomain = topLevelDomain;
    }

	public boolean isMailtrackingActive() {
		return mailtrackingActive;
	}

	public void setMailtrackingActive(boolean mailtrackingActive) {
		this.mailtrackingActive = mailtrackingActive;
	}

	public boolean isMailtrackingExpired() {
		return mailtrackingExpired;
	}

	public void setMailtrackingExpired(boolean mailtrackingExpired) {
		this.mailtrackingExpired = mailtrackingExpired;
	}

	public int getMailtrackingExpirationDays() {
		return mailtrackingExpirationDays;
	}

	public void setMailtrackingExpirationDays(int mailtrackingExpirationDays) {
		this.mailtrackingExpirationDays = mailtrackingExpirationDays;
	}

	public boolean isEverSent() {
		return everSent;
	}

	public void setEverSent(boolean everSent) {
		this.everSent = everSent;
	}

	public boolean isShowNetto() {
		return showNetto;
	}

	public void setShowNetto(boolean showNetto) {
		this.showNetto = showNetto;
	}

	public boolean getIsMailingGrid() {
		return isMailingGrid;
	}

	public void setIsMailingGrid(boolean mailingGrid) {
		isMailingGrid = mailingGrid;
	}
}
