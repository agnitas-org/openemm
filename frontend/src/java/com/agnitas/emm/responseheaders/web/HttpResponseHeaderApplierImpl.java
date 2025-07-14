/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.responseheaders.web;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.util.ServerCommand.Server;

import com.agnitas.emm.responseheaders.common.HttpHeaderConfig;
import com.agnitas.emm.responseheaders.common.UsedFor;
import com.agnitas.emm.responseheaders.service.HttpResponseHeaderService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class HttpResponseHeaderApplierImpl implements HttpResponseHeaderApplier {

    private static final Map<String, Set<String>> clientCachedResources = Map.of(
            "contains", Set.of(
                    "/application.min.css.action",
                    "/assets/application.redesigned.min.js",
                    "/translations.js.action",
                    "/assets/config.js"),
            "startsWith", Set.of(
                    "/assets/fonts/"
            ));
    
	private final HttpResponseHeaderService headerService;
	private final ConfigService configService;
    
	public HttpResponseHeaderApplierImpl(final HttpResponseHeaderService headerService, final ConfigService configService) {
		this.headerService = Objects.requireNonNull(headerService, "httpResponseHeaderService");
		this.configService = Objects.requireNonNull(configService, "configService");
	}

	@Override
	public void applyHeadersToFilterResponse(final HttpServletRequest request, final HttpServletResponse response) {
		final List<HttpHeaderConfig> headers = headerService.listHeaderConfigsForFilter();	

		applyHeaders(headers, request, response);
	}

	@Override
	public void applyHeadersToResponse(final UsedFor usedFor, final int companyID, final HttpServletRequest request, final HttpServletResponse response) {
		
		final List<HttpHeaderConfig> headers = headerService
				.listHeaderConfigs(usedFor, companyID)
				.stream()
				.collect(Collectors.toList());

		applyHeaders(headers, request, response);
	}

	private final void applyHeaders(final List<HttpHeaderConfig> headers, final HttpServletRequest request, final HttpServletResponse response) {
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
        return clientCachedResources.get("contains").contains(resource)
                || clientCachedResources.get("startsWith").stream().anyMatch(resource::startsWith);
    }
}
