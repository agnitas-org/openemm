/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverstatus.forms;

public class ServerStatusForm {

	private String jobStart;
	private String sendTestEmail;
	private String sendDiagnosis;

	private ServerConfigForm configForm = new ServerConfigForm();
	
	public String getJobStart() {
		return jobStart;
	}
	
	public void setJobStart(String jobStart) {
		this.jobStart = jobStart;
	}
	
	public String getSendTestEmail() {
		return sendTestEmail;
	}
	
	public void setSendTestEmail(String sendTestEmail) {
		this.sendTestEmail = sendTestEmail;
	}
	
	public String getSendDiagnosis() {
		return sendDiagnosis;
	}
	
	public void setSendDiagnosis(String sendDiagnosis) {
		this.sendDiagnosis = sendDiagnosis;
	}
	
	public ServerConfigForm getConfigForm() {
		return configForm;
	}
}
