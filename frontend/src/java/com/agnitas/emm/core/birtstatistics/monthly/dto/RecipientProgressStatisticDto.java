/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.monthly.dto;

public class RecipientProgressStatisticDto {
	
    public static final String RECIPIENT_PROGRESS_REPORT_NAME = "recipient_progress.rptdesign";
    
	private String reportName = RECIPIENT_PROGRESS_REPORT_NAME;
	
	private int startMonth;

	private int startYear;
	
	private int mailinglistId;
	
	private int mediaType;
	
	private int targetId;
	
	private boolean hourScale;
	
	public String getReportName() {
		return reportName;
	}
	
	public void setStartMonth(int startMonth) {
		this.startMonth = startMonth;
	}
	
	public int getStartMonth() {
		return startMonth;
	}
	
	public void setStartYear(int startYear) {
		this.startYear = startYear;
	}
	
	public int getStartYear() {
		return startYear;
	}
	
	public int getMailinglistId() {
		return mailinglistId;
	}
	
	public void setMailinglistId(int mailinglistId) {
		this.mailinglistId = mailinglistId;
	}
	
	public int getMediaType() {
		return mediaType;
	}
	
	public void setMediaType(int mediaType) {
		this.mediaType = mediaType;
	}
	
	public int getTargetId() {
		return targetId;
	}
	
	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}
	
	public boolean isHourScale() {
		return hourScale;
	}
	
	public void setHourScale(boolean hourScale) {
		this.hourScale = hourScale;
	}
}
