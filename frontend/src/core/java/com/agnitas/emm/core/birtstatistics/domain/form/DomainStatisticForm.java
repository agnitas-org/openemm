/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.domain.form;

public class DomainStatisticForm {

	private int maxDomainNum = 5;
	private Integer targetId;
	private Integer mailinglistId;
	private boolean topLevelDomain;
	
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

	public Integer getMailinglistId() {
		return mailinglistId;
	}

	public void setMailinglistId(Integer mailinglistId) {
		this.mailinglistId = mailinglistId;
	}

	public Integer getTargetId() {
		return targetId;
	}

	public void setTargetId(Integer targetId) {
		this.targetId = targetId;
	}

}
