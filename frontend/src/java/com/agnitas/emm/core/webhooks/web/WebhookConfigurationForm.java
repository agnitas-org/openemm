/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.web;

import java.util.HashSet;
import java.util.Set;

import com.agnitas.emm.core.webhooks.settings.common.WebhookSettings;

public class WebhookConfigurationForm {

	private String url;
	private Set<String> includedProfileFields;
	
	public WebhookConfigurationForm() {
		this.includedProfileFields = new HashSet<>();
	}
	
	public void fillFrom(final WebhookSettings settings) {
		this.url = settings.getUrl();
		
		this.includedProfileFields.clear();
        this.includedProfileFields.addAll(settings.getProfileFields());
	}
	
	public final String getUrl() {
		return this.url;
	}
	
	public final void setUrl(final String url) {
		this.url = url;
	}
	
	public final Set<String> getIncludedProfileFields() {
		return this.includedProfileFields;
	}

    public void setIncludedProfileFields(Set<String> includedProfileFields) {
        this.includedProfileFields = includedProfileFields;
    }
}
