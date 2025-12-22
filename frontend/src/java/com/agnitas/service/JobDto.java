/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.Date;
import java.util.Map;

public class JobDto {

	private int id;
	private String name;
	private String description;
	private Date created;
	private Date lastStart;
	private boolean running;
	private String lastResult;
	private boolean startAfterError;
	private int lastDuration; // in seconds
	private String interval;
	private Date nextStart;
	private String runClass;
	private String runOnlyOnHosts;
	private String emailOnError;
	private boolean deleted;
	private Map<String, String> parameters; // e.g.: ZipAttachement, SendWhenEmpty, SqlStatement, CompanyID
	private int criticality;
	private boolean acknowledged;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Date getCreated() {
		return created;
	}
	
	public void setCreated(Date created) {
		this.created = created;
	}
	
	public Date getLastStart() {
		return lastStart;
	}
	
	public void setLastStart(Date lastStart) {
		this.lastStart = lastStart;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public String getLastResult() {
		return lastResult;
	}
	
	public void setLastResult(String lastResult) {
		this.lastResult = lastResult;
	}
	
	public boolean isStartAfterError() {
		return startAfterError;
	}
	
	public void setStartAfterError(boolean startAfterError) {
		this.startAfterError = startAfterError;
	}
	
	public int getLastDuration() {
		return lastDuration;
	}
	
	public void setLastDuration(int lastDuration) {
		this.lastDuration = lastDuration;
	}
	
	public String getInterval() {
		return interval;
	}
	
	public void setInterval(String interval) {
		this.interval = interval;
	}
	
	public Date getNextStart() {
		return nextStart;
	}
	
	public void setNextStart(Date nextStart) {
		this.nextStart = nextStart;
	}
	
	public String getRunClass() {
		return runClass;
	}
	
	public void setRunClass(String runClass) {
		this.runClass = runClass;
	}
	
	public boolean isDeleted() {
		return deleted;
	}
	
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	
	public String getRunOnlyOnHosts() {
		return runOnlyOnHosts;
	}
	
	public void setRunOnlyOnHosts(String runOnlyOnHosts) {
		this.runOnlyOnHosts = runOnlyOnHosts;
	}
	
	public String getEmailOnError() {
		return emailOnError;
	}
	
	public void setEmailOnError(String emailOnError) {
		this.emailOnError = emailOnError;
	}

	public int getCriticality() {
		return criticality;
	}

	public void setCriticality(int criticality) {
		this.criticality = criticality;
	}

	public boolean isAcknowledged() {
		return acknowledged;
	}

	public void setAcknowledged(boolean acknowledged) {
		this.acknowledged = acknowledged;
	}
	
	/**
	 * Output-Method to simplify debugging
	 */
	@Override
	public String toString() {
		return description + "(" + id + ")";
	}
}
