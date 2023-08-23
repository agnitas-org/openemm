/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.filter;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.agnitas.util.AgnUtils;
import org.agnitas.util.CaseInsensitiveSet;
import org.antlr.v4.runtime.misc.Nullable;
import org.apache.commons.lang3.StringUtils;

public class CharacterEncodingFilter extends org.springframework.web.filter.CharacterEncodingFilter {

	private Set<String> iso_8859_1RdirDomains = new CaseInsensitiveSet();
	private Set<String> utf8RdirDomains = new CaseInsensitiveSet();
	private Set<String> jpRdirDomains = new CaseInsensitiveSet();
	
	public void setIsoEncodingDomains(@Nullable String isoEncodingDomainList) {
		iso_8859_1RdirDomains = new CaseInsensitiveSet();
		for (String domain : AgnUtils.splitAndTrimList(isoEncodingDomainList.replace("\n", " ").replace("\r", " ").replace("\t", " "))) {
			if (StringUtils.isNotBlank(domain)) {
				iso_8859_1RdirDomains.add(domain.trim().toLowerCase());
			}
		}
	}
	
	public void setUtf8EncodingDomains(@Nullable String utf8EncodingDomainList) {
		utf8RdirDomains = new CaseInsensitiveSet();
		for (String domain : AgnUtils.splitAndTrimList(utf8EncodingDomainList.replace("\n", " ").replace("\r", " ").replace("\t", " "))) {
			if (StringUtils.isNotBlank(domain)) {
				utf8RdirDomains.add(domain.trim().toLowerCase());
			}
		}
	}
	
	public void setJpEncodingDomains(@Nullable String utf8EncodingDomainList) {
		jpRdirDomains = new CaseInsensitiveSet();
		for (String domain : AgnUtils.splitAndTrimList(utf8EncodingDomainList.replace("\n", " ").replace("\r", " ").replace("\t", " "))) {
			if (StringUtils.isNotBlank(domain)) {
				jpRdirDomains.add(domain.trim().toLowerCase());
			}
		}
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if (request.getCharacterEncoding() == null) {
			if (iso_8859_1RdirDomains.contains(request.getServerName())) {
				request.setCharacterEncoding("ISO-8859-1");
			} else if (utf8RdirDomains.contains(request.getServerName())) {
				request.setCharacterEncoding("UTF-8");
			} else if (jpRdirDomains.contains(request.getServerName())) {
				request.setCharacterEncoding("ISO-2022-JP");
			}
		}

		String encoding = getEncoding();

		if (StringUtils.isNotBlank(encoding)) {
			if (isForceRequestEncoding() || request.getCharacterEncoding() == null) {
				request.setCharacterEncoding(encoding);
			}
			if (isForceResponseEncoding()) {
				response.setCharacterEncoding(encoding);
			}
		}
		
		filterChain.doFilter(request, response);
	}
}
