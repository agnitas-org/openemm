/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.deeptracking.web;

import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.web.cookies.SameSiteCookie;
import com.agnitas.web.cookies.SameSiteCookiePolicy;

/**
 * Utility class for working with deep tracking cookie.
 */
public final class DeepTrackingCookieUtil {
	
	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(DeepTrackingCookieUtil.class);
	
	/**
	 * Parsed value of deep tracking cookie.
	 */
	public static class DeepTrackingCookieValue {
		/** Session ID. */
		private final int sessionID;
		
		/** Parameter. */
		private final String parameter;
		
		/**
		 * Creates a new instance.
		 * 
		 * @param sessionID session ID
		 * @param parameter parameter
		 * 
		 * @throws NullPointerException if <code>parameter</code> is <code>null</code>
		 */
		public DeepTrackingCookieValue(final int sessionID, final String parameter) {
			this.sessionID = sessionID;
			this.parameter = Objects.requireNonNull(parameter, "Parameter is null");
		}

		/**
		 * Returns the session ID.
		 * 
		 * @return session ID
		 */
		public final int getSessionID() {
			return sessionID;
		}

		/**
		 * Returns the parameter.
		 * 
		 * @return parameter
		 */
		public final String getParameter() {
			return parameter;
		}
	}

	/**
	 * Adds or replaces deep tracking cookie in HTTP response. Can also remove the cookie.
	 *
	 * The maximum age works the same way is defined in {@link Cookie#setMaxAge(int)}:
	 * <ul>
	 *   <li>positive values: Indicates the maximum age in seconds, when the cookie will expire</li>
	 *   <li>negative values: The cookie is not stored persistently and will be deleted when the Web browser exits.</li>
	 *   <li>0: The cookie will be removed.</li>
	 * </ul>
	 * 
	 * @param response HTTP response
	 * @param maximumAge maximum age 
	 * @param companyID company ID
	 * @param deepTrackingSession ID of tracking session
	 * @param deepTrackingUID tracking UID
	 * 
	 * @see Cookie#setMaxAge(int)
	 */
	public static final void addTrackingCookie(final HttpServletResponse response, final int maximumAge, final int companyID, final long deepTrackingSession, final String deepTrackingUID, final SameSiteCookiePolicy sameSite) {
		if(maximumAge != 0) {
			final String sessionIDString = Long.toHexString(deepTrackingSession);
			final String cookieValue = String.format("%s_%s", sessionIDString, deepTrackingUID);
			
			addCookie(response, companyID, maximumAge, cookieValue, sameSite);
		} else {
			removeTrackingCookie(response, companyID);
		}
	}
	
	/**
	 * Removes the tracking cookie.
	 * This is done by adding a cookie with a maximum age of 0 seconds.
	 * 
	 * @param response HTTP response
	 * @param companyID company ID
	 */
	public static final void removeTrackingCookie(final HttpServletResponse response, final int companyID) {
		addCookie(response, companyID, 0, "", null);
	}
	
	/**
	 * Adds a deep tracking cookie.
	 * 
	 * @param response HTTP response
	 * @param companyID company ID
	 * @param maxAge maximum age
	 * @param value cookie value
	 */
	private static final void addCookie(final HttpServletResponse response, final int companyID, final int maxAge, final String value, final SameSiteCookiePolicy sameSite) {
		final SameSiteCookie cookie = new SameSiteCookie(cookieName(companyID), value)
			.setMaxAge(maxAge)
			.setSecure(sameSite == SameSiteCookiePolicy.NONE);  // SameSite=None requires Secure-attribute (see https://developer.mozilla.org/de/docs/Web/HTTP/Headers/Set-Cookie/SameSite#samesitenone_requires_secure)
		
		if(sameSite != null) {
			cookie.setSameSite(sameSite);
		}
		
		cookie.addHeader(response);		// Emit Set-Cookie header for this cookie
	}
	
	/**
	 * Returns the name of the tracking cookie.
	 * 
	 * @param companyID company ID
	 * 
	 * @return cookie name
	 */
	public static final String cookieName(final int companyID) {
		return String.format("agnTrk%d", companyID);
	}
	
	/**
	 * Reads the value of the deep tracking cookie and returns it as a String. 
	 * If cookie does not exists, {@link Optional#empty()} is returned.
	 * 
	 * @param request HTTP request
	 * @param companyID company ID
	 * 
	 * @return value of tracking cookie or {@link Optional#empty()}
	 */
	public static final Optional<String> readCookieValueAsString(final HttpServletRequest request, final int companyID) {
		final Cookie[] cookies = request.getCookies();
		
		if(cookies != null) {
			final String trackingCookieName = DeepTrackingCookieUtil.cookieName(companyID);
			
			for(final Cookie cookie : cookies) {
				if(trackingCookieName.equals(cookie.getName())) {
					return Optional.of(cookie.getValue());
				}
			}
		}

		return Optional.empty();
	}
	
	/**
	 * Parses given value of the deep tracking cookie.
	 * If value cannot be parsed, {@link Optional#empty()} is returned.
	 * 
	 * @param value cookie value
	 * 
	 * @return parsed value or {@link Optional#empty()}
	 */
	public static final Optional<DeepTrackingCookieValue> parseCookieValue(final String value) {
		final int index = value.indexOf('_');
		
		if(index != -1) {
			final String param = value.substring(index + 1);
			
			try {
				final int sessionID = Integer.parseInt(value.substring(0, index), 16); // Hex decoding
				return Optional.of(new DeepTrackingCookieValue(sessionID, param));
			} catch(final Exception e) {
				LOGGER.info(String.format("could not parse session ID from deep tracking cookie value: %s", value), e);

				return Optional.of(new DeepTrackingCookieValue(0, param));
			}
		} else {
			return Optional.empty();
		}
		
	}
	
	/**
	 * Reads the value of the deep tracking cookie and returns it as a parsed
	 * {@link DeepTrackingCookieValue} instance. 
	 * If cookie does not exists, {@link Optional#empty()} is returned.
	 * 
	 * @param request HTTP request
	 * @param companyID company ID
	 * 
	 * @return value of tracking cookie or {@link Optional#empty()}
	 */
	public static final Optional<DeepTrackingCookieValue> readCookieValue(final HttpServletRequest request, final int companyID) {
		final Optional<String> strValueOpt = readCookieValueAsString(request, companyID);
		
		if(strValueOpt.isPresent()) {
			final String strValue = strValueOpt.get();
			
			return parseCookieValue(strValue);
		} else {
			return Optional.empty();
		}
	}
	
}
