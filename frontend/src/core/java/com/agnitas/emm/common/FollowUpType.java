/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.common;

public enum FollowUpType {
    TYPE_FOLLOWUP_NON_OPENER("non-opener"),
    TYPE_FOLLOWUP_OPENER("opener"),
    TYPE_FOLLOWUP_NON_CLICKER("non-clicker"),
    TYPE_FOLLOWUP_CLICKER("clicker"),
    TYPE_FOLLOWUP_NON_OPENER_WITHOUT_TRACKING_VETO("non-opener-without-tracking-veto"),
    TYPE_FOLLOWUP_NON_CKLICKER_WITHOUT_TRACKING_VETO("non-clicker-without-tracking-veto"),
    TYPE_FOLLOWUP_TRACKING_VETO("tracking-veto");
	
	private String key;
	
	FollowUpType(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
}
