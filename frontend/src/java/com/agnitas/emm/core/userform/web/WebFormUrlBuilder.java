/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import org.agnitas.beans.Company;

public final class WebFormUrlBuilder {

	private final Company company;
	private String formName;
	private Optional<String> companyToken;
	private Optional<String> uid;
	private boolean resolvedUID;
	
	private WebFormUrlBuilder(final Company company, final String formName) {
		this.company = Objects.requireNonNull(company, "Company is null");
		this.resolvedUID = true;
		this.companyToken = Optional.empty();
		this.uid = Optional.empty();
		
		withFormName(formName);
	}
	
	public static final WebFormUrlBuilder from(final Company company, final String formName) {
		return new WebFormUrlBuilder(company, formName);
	}
	
	public final WebFormUrlBuilder withFormName(final String formNameParam) {
		this.formName = Objects.requireNonNull(formNameParam, "Form name is null");
		
		return this;
	}
	
	public final WebFormUrlBuilder withCompanyToken(final String companyTokenParam) {
		this.companyToken = Optional.ofNullable(companyTokenParam);
		
		return this;
	}
	
	public final WebFormUrlBuilder withCompanyToken(final Optional<String> companyTokenParam) {
		this.companyToken = Objects.requireNonNull(companyTokenParam);
		
		return this;
	}
	
	public final WebFormUrlBuilder withUID(final String uidParam) {
		this.uid = Optional.ofNullable(uidParam);
		
		return this;
	}
	
	public final WebFormUrlBuilder withResolvedUID(final boolean resolvedUIDParam) {
		this.resolvedUID = resolvedUIDParam;
		
		return this;
	}
		
	public final String build() {
		final StringBuilder sb = new StringBuilder(company.getRdirDomain());
		
		// Add trailing "/" if necessary
		if(!company.getRdirDomain().endsWith("/")) {
			sb.append("/");
		}
		
		sb.append("form.action");
			
		// Add form name
		sb.append("?agnFN=").append(urlEncode(formName));
		
		// Add company identifier
		if(this.companyToken.isPresent()) {
			sb.append("&agnCTOKEN=").append(urlEncode(this.companyToken.get()));
		} else {
			sb.append("&agnCI=").append(this.company.getId());
		}
		
		// Add recipient identifier
		if(this.uid.isPresent()) {
			sb.append("&agnUID=");
			
			if(this.resolvedUID) {
				sb.append(urlEncode(this.uid.get()));
			} else {
				sb.append("##AGNUID##");
			}
		}
		
		return sb.toString();
	}
	
	private static final String urlEncode(final String s) {
		return URLEncoder.encode(s, StandardCharsets.UTF_8);
	}
}
