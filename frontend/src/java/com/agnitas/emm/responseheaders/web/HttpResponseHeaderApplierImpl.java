/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.responseheaders.web;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.responseheaders.common.HttpHeaderConfig;
import com.agnitas.emm.responseheaders.common.UsedFor;
import com.agnitas.emm.responseheaders.service.HttpResponseHeaderService;
import com.agnitas.util.ServerCommand.Server;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class HttpResponseHeaderApplierImpl implements HttpResponseHeaderApplier {

	private static final List<Pattern> CLIENT_CACHED_PATTERNS = Stream.of(
					"^/application\\.min\\.css\\.action$",
					"^/assets/application\\.min\\.js$",
					"^/assets/jodit\\.css$",
					"^/js/lib/jodit/[^/]+/emoji\\.json$",
					"^/translations\\.js\\.action$",
					"^/assets/config\\.js$",
					"^/assets/fonts/.*"
			).map(Pattern::compile)
			.toList();
    
	private final HttpResponseHeaderService headerService;
	private final ConfigService configService;
    
	public HttpResponseHeaderApplierImpl(HttpResponseHeaderService headerService, ConfigService configService) {
		this.headerService = Objects.requireNonNull(headerService, "httpResponseHeaderService");
		this.configService = Objects.requireNonNull(configService, "configService");
	}

	@Override
	public void applyHeadersToFilterResponse(HttpServletRequest request, HttpServletResponse response) {
		final List<HttpHeaderConfig> headers = headerService.listHeaderConfigsForFilter();	

		applyHeaders(headers, request, response);
	}

	@Override
	public void applyHeadersToResponse(UsedFor usedFor, int companyID, HttpServletRequest request, HttpServletResponse response) {
		List<HttpHeaderConfig> headers = headerService.listHeaderConfigs(usedFor, companyID);
		applyHeaders(headers, request, response);
	}

	private void applyHeaders(List<HttpHeaderConfig> headers, HttpServletRequest request, HttpServletResponse response) {
		final String remoteHostname = request.getRemoteHost();
		final String queryString = request.getServletPath();
		final Server applicationType = this.configService.getApplicationType();
		
		/*
		 * Determine headers to add.
		 * 
		 * Determination of headers to add and adding headers are implemented as separate steps, because headers can
		 * occur more than one in the configuration source. When doing in one step, this breaks the overwrite mechanism.
		 */
		final List<HttpHeaderConfig> headersToAdd = headers
				.stream()
				.filter(headerConfig -> headerConfig.isApplicableForApplicationType(applicationType))
				.filter(headerConfig -> headerConfig.isOverwrite() || !response.containsHeader(headerConfig.getHeaderName()))
				.filter(headerConfig -> headerConfig.getRemoteHostnamePattern().isEmpty() || (remoteHostname != null && headerConfig.getRemoteHostnamePattern().get().matcher(remoteHostname).matches()))
				.filter(headerConfig -> headerConfig.getQueryPattern().isEmpty() || (queryString != null && headerConfig.getQueryPattern().get().matcher(queryString).matches()))
				.collect(Collectors.toList());

		// Add selected headers to response or replaces existing header
        headersToAdd.forEach(header -> addHeader(response, header, queryString));
        if (isClientCachedResource(queryString)) {
            response.setHeader("cache-control", "max-age=86400, private");
        }
	}

    private static void addHeader(HttpServletResponse response, HttpHeaderConfig header, String queryStr) {
        if (!isCachedHeaderRequired(queryStr, header.getHeaderName())) {
            response.setHeader(header.getHeaderName(), header.getHeaderValue());
        }
    }

    private static boolean isCachedHeaderRequired(String resource, String headerName) {
        return ("cache-control".equalsIgnoreCase(headerName) || "pragma".equalsIgnoreCase(headerName))
                && isClientCachedResource(resource);
    }

	private static boolean isClientCachedResource(String resource) {
		return CLIENT_CACHED_PATTERNS.stream()
				.anyMatch(p -> p.matcher(resource).matches());
	}

}
