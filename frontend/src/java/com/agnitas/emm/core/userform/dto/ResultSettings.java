/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.dto;

import org.agnitas.emm.core.velocity.VelocityDirectiveScriptUtil;

public class ResultSettings {
	
	private boolean success;
	private int startActionId;
	private int finalActionId;
	
	private String template;
	
	private boolean useUrl;
	private boolean useVelocity;
	private String url;
	
	public ResultSettings(boolean success) {
		this.success = success;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public int getStartActionId() {
		return startActionId;
	}
	
	public void setStartActionId(int startActionId) {
		this.startActionId = startActionId;
	}
	
	public int getFinalActionId() {
		return finalActionId;
	}
	
	public void setFinalActionId(int finalActionId) {
		this.finalActionId = finalActionId;
	}
	
	public String getTemplate() {
		return template;
	}
	
	public void setTemplate(String template) {
		this.template = template;
		this.useVelocity = VelocityDirectiveScriptUtil.containsAnyStatement(template);
	}
	
	public boolean isUseUrl() {
		return useUrl;
	}
	
	public void setUseUrl(boolean useUrl) {
		this.useUrl = useUrl;
	}
	
	public boolean isUseVelocity() {
		return useVelocity;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	//TODO: GWUA-4279 populate with necessary fields
}
