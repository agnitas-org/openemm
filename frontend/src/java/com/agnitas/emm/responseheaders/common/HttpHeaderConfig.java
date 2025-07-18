/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.responseheaders.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.agnitas.util.ServerCommand.Server;

/**
 * Configuration of a single header.
 */
public final class HttpHeaderConfig {

	/** Header name. */
	private final String headerName;
	
	/** Header value. */
	private final String headerValue;
	
	/** Flag, if existing header should be overwritten. */
	private final boolean overwrite;
	
	/** Set of lower-case application types. */
	private final Set<String> applicationTypes;
	
	/** Flag to indicate for which component this configuration is used for. */
	private final UsedFor usedFor;
	
	/** Company ID. Not used, when usedFor is set to {@link UsedFor#REQUEST_FILTER}. */
	private final int companyID;
	
	/** Regular expression to match remote host name. */
	private final Optional<Pattern> remoteHostnamePattern;
	
	private final Optional<Pattern> queryPattern;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param headerName header name
	 * @param headerValue header value
	 * @param overwrite <code>true</code> if existing header should be overwritten
	 */
	public HttpHeaderConfig(final String headerName, final String headerValue, final boolean overwrite, final Collection<String> applicationTypes, final UsedFor usedFor, final int companyID, final Pattern remoteHostnamePatternOrNull, final Pattern queryPatternOrNull) {
		this.headerName = headerName;
		this.headerValue = headerValue;
		this.overwrite = overwrite;
		this.applicationTypes = applicationTypes != null 
				? applicationTypes.stream()
						.filter(type -> type != null)
						.map(type -> type.toLowerCase())
						.distinct()
						.collect(Collectors.toSet())
				: new HashSet<>();
		this.usedFor = Objects.requireNonNull(usedFor);
		this.companyID = companyID;
		this.remoteHostnamePattern = Optional.ofNullable(remoteHostnamePatternOrNull);
		this.queryPattern = Optional.ofNullable(queryPatternOrNull);
	}

	/**
	 * Returns the header name.
	 * 
	 * @return header name
	 */
	public final String getHeaderName() {
		return headerName;
	}

	/**
	 * Returns the header value.
	 * 
	 * @return header value
	 */
	public final String getHeaderValue() {
		return headerValue;
	}

	/**
	 * Returns <code>true</code> if existing header should be overwritten.
	 * 
	 * @return <code>true</code> if existing header should be overwritten
	 */
	public final boolean isOverwrite() {
		return overwrite;
	}
	
	public final UsedFor getUsedFor() {
		return usedFor;
	}

	public final int getCompanyID() {
		return companyID;
	}

	public final Optional<Pattern> getRemoteHostnamePattern() {
		return remoteHostnamePattern;
	}

	public final Optional<Pattern> getQueryPattern() {
		return queryPattern;
	}

	@Override
	public final String toString() {
		return String.format(
				"%s [%s] %s",
				this.headerName,
				this.overwrite ? "overwrite" : "",
				this.headerValue);
	}

	public final boolean isApplicableForApplicationType(final Server applicationType) {
		return this.applicationTypes.isEmpty() || this.applicationTypes.contains(applicationType.name().toLowerCase());
	}
	
}
