/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.agnitas.util.importvalues.DateFormat;

public class ExportPredef {
	protected int id;

	protected int company;

	protected String charset = "ISO-8859-1";

	protected List<ExportColumnMapping> exportColumnMappings = new ArrayList<>();

	protected String shortname = "";

	protected String description = "";

	protected String mailinglists = "";

	protected int mailinglistID;

	protected String delimiter = "";
	
	protected boolean alwaysQuote = false;

	protected String separator = ";";

	protected int targetID;

	protected String userType = "E";

	protected int userStatus;

	protected int deleted;

	protected boolean timeLimitsLinkedByAnd = true;

	private Date timestampStart;
	private Date timestampEnd;
	private int timestampLastDays;
	private boolean timestampIncludeCurrentDay;

	private Date creationDateStart;
	private Date creationDateEnd;
	private int creationDateLastDays;
	private boolean creationDateIncludeCurrentDay;

	private Date mailinglistBindStart;
	private Date mailinglistBindEnd;
	private int mailinglistBindLastDays;
	private boolean mailinglistBindIncludeCurrentDay;
	
	private int dateFormat = DateFormat.ddMMyyyy.getIntValue();
	private int dateTimeFormat = DateFormat.ddMMyyyyHHmmss.getIntValue();
	private String timezone = "Europe/Berlin";
	private String decimalSeparator = ",";

	public void setId(int id) {
		this.id = id;
	}

	public void setCompanyID(int company) {
		this.company = company;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public void setExportColumnMappings(List<ExportColumnMapping> exportColumnMappings) {
		this.exportColumnMappings = exportColumnMappings;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setMailinglists(String mailinglists) {
		this.mailinglists = mailinglists;
	}

	public void setMailinglistID(int mailinglistID) {
		this.mailinglistID = mailinglistID;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public void setTargetID(int targetID) {
		this.targetID = targetID;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}
	
	public void setUserStatus(int userStatus) {
		this.userStatus = userStatus;
	}

	public void setDeleted(int deleted) {
		this.deleted = deleted;
	}

	public int getId() {
		return id;
	}

	public int getCompanyID() {
		return company;
	}

	public String getCharset() {
		return charset;
	}

	public List<ExportColumnMapping> getExportColumnMappings() {
		return exportColumnMappings;
	}

	public String getShortname() {
		return shortname;
	}

	public String getDescription() {
		return description;
	}

	public String getMailinglists() {
		return mailinglists;
	}

	public int getMailinglistID() {
		return mailinglistID;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public String getSeparator() {
		return separator;
	}

	public int getTargetID() {
		return targetID;
	}

	public String getUserType() {
		return userType;
	}

	public int getUserStatus() {
		return userStatus;
	}

	public int getDeleted() {
		return deleted;
	}

	public Date getTimestampStart() {
		return timestampStart;
	}

	public void setTimestampStart(Date timestampStart) {
		this.timestampStart = timestampStart;
	}

	public Date getTimestampEnd() {
		return timestampEnd;
	}

	public void setTimestampEnd(Date timestampEnd) {
		this.timestampEnd = timestampEnd;
	}

	public Date getCreationDateStart() {
		return creationDateStart;
	}

	public void setCreationDateStart(Date creationDateStart) {
		this.creationDateStart = creationDateStart;
	}

	public Date getCreationDateEnd() {
		return creationDateEnd;
	}

	public void setCreationDateEnd(Date creationDateEnd) {
		this.creationDateEnd = creationDateEnd;
	}

	public Date getMailinglistBindStart() {
		return mailinglistBindStart;
	}

	public void setMailinglistBindStart(Date mailinglistBindStart) {
		this.mailinglistBindStart = mailinglistBindStart;
	}

	public Date getMailinglistBindEnd() {
		return mailinglistBindEnd;
	}

	public void setMailinglistBindEnd(Date mailinglistBindEnd) {
		this.mailinglistBindEnd = mailinglistBindEnd;
	}

	public int getTimestampLastDays() {
		return timestampLastDays;
	}

	public void setTimestampLastDays(int timestampLastDays) {
		this.timestampLastDays = timestampLastDays;
	}

	public int getCreationDateLastDays() {
		return creationDateLastDays;
	}

	public void setCreationDateLastDays(int creationDateLastDays) {
		this.creationDateLastDays = creationDateLastDays;
	}

	public int getMailinglistBindLastDays() {
		return mailinglistBindLastDays;
	}

	public void setMailinglistBindLastDays(int mailinglistBindLastDays) {
		this.mailinglistBindLastDays = mailinglistBindLastDays;
	}

	public boolean isTimestampIncludeCurrentDay() {
		return timestampIncludeCurrentDay;
	}

	public void setTimestampIncludeCurrentDay(boolean timestampIncludeCurrentDay) {
		this.timestampIncludeCurrentDay = timestampIncludeCurrentDay;
	}

	public boolean isCreationDateIncludeCurrentDay() {
		return creationDateIncludeCurrentDay;
	}

	public void setCreationDateIncludeCurrentDay(boolean creationDateIncludeCurrentDay) {
		this.creationDateIncludeCurrentDay = creationDateIncludeCurrentDay;
	}

	public boolean isMailinglistBindIncludeCurrentDay() {
		return mailinglistBindIncludeCurrentDay;
	}

	public void setMailinglistBindIncludeCurrentDay(boolean mailinglistBindIncludeCurrentDay) {
		this.mailinglistBindIncludeCurrentDay = mailinglistBindIncludeCurrentDay;
	}

	public boolean isAlwaysQuote() {
		return alwaysQuote;
	}

	public void setAlwaysQuote(boolean alwaysQuote) {
		this.alwaysQuote = alwaysQuote;
	}

	public int getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(int dateFormat) {
		this.dateFormat = dateFormat;
	}

	public int getDateTimeFormat() {
		return dateTimeFormat;
	}

	public void setDateTimeFormat(int dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getDecimalSeparator() {
		return decimalSeparator;
	}

	public void setDecimalSeparator(String decimalSeparator) {
		this.decimalSeparator = decimalSeparator;
	}

	public boolean isTimeLimitsLinkedByAnd() {
		return timeLimitsLinkedByAnd;
	}

	public void setTimeLimitsLinkedByAnd(boolean timeLimitsLinkedByAnd) {
		this.timeLimitsLinkedByAnd = timeLimitsLinkedByAnd;
	}
}
