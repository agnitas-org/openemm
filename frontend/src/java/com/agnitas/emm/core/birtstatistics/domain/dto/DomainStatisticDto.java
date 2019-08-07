/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.domain.dto;

public class DomainStatisticDto {
	
	private String reportName;
	
	private int targetId;
	
	private int mailingListId;
	
	private int maxDomainNum;
	
	private boolean topLevelDomain;
	
	public String getReportName() {
		return reportName;
	}
	
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	public int getTargetId() {
		return targetId;
	}
	
	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}
	
	public int getMailingListId() {
		return mailingListId;
	}
	
	public void setMailingListId(int mailingListId) {
		this.mailingListId = mailingListId;
	}
	
	public int getMaxDomainNum() {
		return maxDomainNum;
	}
	
	public void setMaxDomainNum(int maxDomainNum) {
		this.maxDomainNum = maxDomainNum;
	}
	
	public boolean isTopLevelDomain() {
		return topLevelDomain;
	}
	
	public void setTopLevelDomain(boolean topLevelDomain) {
		this.topLevelDomain = topLevelDomain;
	}
}
