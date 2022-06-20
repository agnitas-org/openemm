/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

public class LightPagetag {
	
	String currency;
	String shortname;
	int type;
	int format;
    String pageTag;
	
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getShortname() {
		return shortname;
	}
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getFormat() {
		return format;
	}
	public void setFormat(int format) {
		this.format = format;
	}

    public String getPageTag() {
        return pageTag;
    }

    public void setPageTag(String pageTag) {
        this.pageTag = pageTag;
    }
}
