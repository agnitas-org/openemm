/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.cookies;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;


/**
 * Very simple class to define cookies supporting the
 * SameSite attribute.
 */

/* 
 * With Tomcat 10.1 this class becomes deprecated because the required API methods
 * to set the SameSite policy becomes available with that version.
 * 
 * Tomcat 10.0 and prior have no method to set SameSite or arbitrary attributes.
 */
@Deprecated
public final class SameSiteCookie {
	/** Name of the cookie. */
	private final String name;
	
	/** Value of the cookie. */
	private final String value;
	
	/** Optional comment. */
	private String comment;
	
	/** Optional domain. */
	private String domain;
	
	/** Optional max age. */
	private int maxAge = -1;
	
	/** Optional path. */
	private String path;
	
	/** Secure flag. */
	private boolean secure;
	
	/** HTTP-only flag. */
	private boolean httpOnly;
	
	/** Optional SameSite policy. */
	private SameSiteCookiePolicy sameSite;
	
	public SameSiteCookie(final String name, final String value) {
		this.name = name;
		this.value = value;
		
		if(StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("Invalid cookie name");
		}
		
//		if(StringUtils.isBlank(value)) {
//			throw new IllegalArgumentException("Invalid cookie value");
//		}
	}
	
	private static final void appendIfNotBlank(final String valueString, final String pattern, final StringBuffer buffer) {
		if (StringUtils.isNotBlank(valueString)) {
			buffer.append(String.format(pattern, valueString));
		}
	}
	
	public final void addHeader(final HttpServletResponse response) {
		final StringBuffer headerValue = new StringBuffer();
		
		headerValue.append(String.format("%s=%s", this.name.trim(), this.value));

		appendIfNotBlank(this.domain, "; Domain=%s", headerValue);
		appendIfNotBlank(this.path, "; Path=%s", headerValue);
		
		if(this.sameSite != null) {
			appendIfNotBlank(this.sameSite.getValue(), "; SameSite=%s", headerValue);
		}

		if(this.secure) {
			headerValue.append("; Secure");
		}

		if(this.httpOnly) {
			headerValue.append("; HttpOnly");
		}
		
		if(this.maxAge != -1) {
			headerValue.append(String.format("; Max-Age=%d", this.maxAge));
		}

		appendIfNotBlank(this.comment, "; Comment=%s", headerValue);
		
		response.setHeader("Set-Cookie", headerValue.toString().trim());
	}
	
	public final String getComment() {
		return comment;
	}
	
	public final String getDomain() {
		return domain;
	}
	
	public final int getMaxAge() {
		return maxAge;
	}
	
	public final String getName() {
		return name;
	}
	
	public final String getPath() {
		return path;
	}
	
	public final boolean isSecure() {
		return secure;
	}
	
	public final String getValue() {
		return value;
	}
	
	public final boolean isHttpOnly() {
		return httpOnly;
	}
	
	public final SameSiteCookiePolicy getSameSite() {
		return sameSite;
	}
	
	public final SameSiteCookie setComment(String comment) {
		this.comment = comment;
		
		return this;
	}
	
	public final SameSiteCookie setDomain(String domain) {
		this.domain = domain;
		
		return this;
	}
	
	public final SameSiteCookie setMaxAge(int maxAge) {
		this.maxAge = maxAge;
		
		return this;
	}
	
	public final SameSiteCookie setPath(String path) {
		this.path = path;
		
		return this;
	}
	
	public final SameSiteCookie setSecure(boolean secure) {
		this.secure = secure;
		
		return this;
	}
	
	public final SameSiteCookie setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
		
		return this;
	}
	
	public final SameSiteCookie setSameSite(SameSiteCookiePolicy sameSite) {
		this.sameSite = sameSite;
		
		return this;
	}
		
}
