/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import java.util.Date;

import org.agnitas.beans.ExportPredef;
import org.agnitas.emm.core.velocity.VelocityCheck;

public class ExportPredefImpl implements ExportPredef {
	protected int id;

	protected int company;

	protected String charset = "ISO-8859-1";

	protected String columns = "";

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

	private Date timestampStart;
	private Date timestampEnd;
	private int timestampLastDays;

	private Date creationDateStart;
	private Date creationDateEnd;
	private int creationDateLastDays;

	private Date mailinglistBindStart;
	private Date mailinglistBindEnd;
	private int mailinglistBindLastDays;

	public ExportPredefImpl() {
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void setCompanyID(@VelocityCheck int company) {
		this.company = company;
	}

	@Override
	public void setCharset(String charset) {
		this.charset = charset;
	}

	@Override
	public void setColumns(String columns) {
		this.columns = columns;
	}

	@Override
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setMailinglists(String mailinglists) {
		this.mailinglists = mailinglists;
	}

	@Override
	public void setMailinglistID(int mailinglistID) {
		this.mailinglistID = mailinglistID;
	}

	@Override
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	@Override
	public void setSeparator(String separator) {
		this.separator = separator;
	}

	@Override
	public void setTargetID(int targetID) {
		this.targetID = targetID;
	}

	@Override
	public void setUserType(String userType) {
		this.userType = userType;
	}

	@Override
	public void setUserStatus(int userStatus) {
		this.userStatus = userStatus;
	}

	@Override
	public void setDeleted(int deleted) {
		this.deleted = deleted;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public int getCompanyID() {
		return company;
	}

	@Override
	public String getCharset() {
		return charset;
	}

	@Override
	public String getColumns() {
		return columns;
	}

	@Override
	public String getShortname() {
		return shortname;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getMailinglists() {
		return mailinglists;
	}

	@Override
	public int getMailinglistID() {
		return mailinglistID;
	}

	@Override
	public String getDelimiter() {
		return delimiter;
	}

	@Override
	public String getSeparator() {
		return separator;
	}

	@Override
	public int getTargetID() {
		return targetID;
	}

	@Override
	public String getUserType() {
		return userType;
	}

	@Override
	public int getUserStatus() {
		return userStatus;
	}

	@Override
	public int getDeleted() {
		return deleted;
	}

	@Override
	public Date getTimestampStart() {
		return timestampStart;
	}

	@Override
	public void setTimestampStart(Date timestampStart) {
		this.timestampStart = timestampStart;
	}

	@Override
	public Date getTimestampEnd() {
		return timestampEnd;
	}

	@Override
	public void setTimestampEnd(Date timestampEnd) {
		this.timestampEnd = timestampEnd;
	}

	@Override
	public Date getCreationDateStart() {
		return creationDateStart;
	}

	@Override
	public void setCreationDateStart(Date creationDateStart) {
		this.creationDateStart = creationDateStart;
	}

	@Override
	public Date getCreationDateEnd() {
		return creationDateEnd;
	}

	@Override
	public void setCreationDateEnd(Date creationDateEnd) {
		this.creationDateEnd = creationDateEnd;
	}

	@Override
	public Date getMailinglistBindStart() {
		return mailinglistBindStart;
	}

	@Override
	public void setMailinglistBindStart(Date mailinglistBindStart) {
		this.mailinglistBindStart = mailinglistBindStart;
	}

	@Override
	public Date getMailinglistBindEnd() {
		return mailinglistBindEnd;
	}

	@Override
	public void setMailinglistBindEnd(Date mailinglistBindEnd) {
		this.mailinglistBindEnd = mailinglistBindEnd;
	}

	@Override
	public int getTimestampLastDays() {
		return timestampLastDays;
	}

	@Override
	public void setTimestampLastDays(int timestampLastDays) {
		this.timestampLastDays = timestampLastDays;
	}

	@Override
	public int getCreationDateLastDays() {
		return creationDateLastDays;
	}

	@Override
	public void setCreationDateLastDays(int creationDateLastDays) {
		this.creationDateLastDays = creationDateLastDays;
	}

	@Override
	public int getMailinglistBindLastDays() {
		return mailinglistBindLastDays;
	}

	@Override
	public void setMailinglistBindLastDays(int mailinglistBindLastDays) {
		this.mailinglistBindLastDays = mailinglistBindLastDays;
	}

	@Override
	public boolean isAlwaysQuote() {
		return alwaysQuote;
	}

	@Override
	public void setAlwaysQuote(boolean alwaysQuote) {
		this.alwaysQuote = alwaysQuote;
	}
}
