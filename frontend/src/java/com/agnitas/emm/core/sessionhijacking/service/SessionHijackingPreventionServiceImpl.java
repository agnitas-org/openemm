/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.sessionhijacking.service;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.util.NetworkUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.sessionhijacking.beans.IpSettings;
import com.agnitas.emm.core.sessionhijacking.beans.ParsedIpSettings;
import com.agnitas.emm.core.sessionhijacking.dao.SessionHijackingPreventionDataDao;

public final class SessionHijackingPreventionServiceImpl implements SessionHijackingPreventionService {

	/** Default time for caching configuration data. */
	public static final transient int DEFAULT_CACHE_TIME_SECONDS = 300; 
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(SessionHijackingPreventionServiceImpl.class);

	/** Configured value to refresh cache. */
	private int cacheTimeSeconds = DEFAULT_CACHE_TIME_SECONDS;

	/** DAO to access configuration. */
	private SessionHijackingPreventionDataDao sessionHijackingPreventionDao;
	
	/** Cache for whitelisted IP addresses. */
	private Set<InetAddress> whitelist;
	
	/** Cache for IP addresses and their assigned groups. */
	private Map<InetAddress, Integer> groups;

	/** Timestamp of last cache refresh. */
	private Date lastRefresh = null;
		
	@Override
	public final boolean isAddressAllowed(final InetAddress sessionIpAddress, final InetAddress clientIpAddress) {
		if(logger.isDebugEnabled()) {
			logger.debug(String.format(
					"IP addresses are: session=%s, client=%s", 
					sessionIpAddress != null ? sessionIpAddress.getHostAddress() : "<unknown>",
					clientIpAddress != null ? clientIpAddress.getHostAddress() : "<unknown>"
							));
		}
		
		try {
			refreshCache();
		} catch(final Exception e) {
			logger.error("Unable to refresh cache - using old cache state", e);
		}
		
		// The client IP address is always allowed, if it is the same as the IP address bound to the session
		if(sessionIpAddress.equals(clientIpAddress)) {
			if(logger.isInfoEnabled()) {
				logger.info("IP of session equals to IP of client - accepted");
			}
			
			return true;
		}
		
		// Whitelisted IP addresses of client are never checked and always allowed
		if(whitelist.contains(clientIpAddress)) {
			if(logger.isInfoEnabled()) {
				logger.info("IP of client is whitelisted - accepted");
			}
			
			return true;
		}
		
		final Integer sessionGroup = this.groups.get(sessionIpAddress);
		final Integer clientGroup = this.groups.get(clientIpAddress);

		// Here, we have a client IP address, that differs from the IP address bound to the session and that is not whitelisted. We need to check group ID.
		if(sessionGroup != null && clientGroup != null && sessionGroup.equals(clientGroup)) {
			if(logger.isInfoEnabled()) {
				logger.info("IP of session and IP of client in same group - accepted");
			}
			
			return true;
		} else {
			if(logger.isInfoEnabled()) {
				logger.info("IP of session and IP of client in different groups or one or both IPs have no group assigned - rejected");
			}
			
			return false;
		}
	}

	private final void refreshCache() throws Exception {
		if(lastRefresh == null || (lastRefresh.getTime() + cacheTimeSeconds * 1000 < (new Date()).getTime())) {
			if(logger.isDebugEnabled()) {
				logger.debug("Refreshing internal caches");
			}
			
			final Set<InetAddress> newWhitelist = new HashSet<>();
			
			// First, mark all local IP addresses (IPs of current machine) as whitelisted
			newWhitelist.addAll(listLocalIpAddresses());
			
			final List<ParsedIpSettings> parsedList = parseSettings(this.sessionHijackingPreventionDao.listIpSettings());

			// Add all IPs with group == null to whitelist
			parsedList.stream()
				.filter(ip -> ip.getGroupOrNull() == null)
				.map(ip -> ip.getIp())
				.forEach(ip -> newWhitelist.add(ip));
			
			// Add all IPs with group != null to groups
			final Map<InetAddress, Integer> newGroups = parsedList.stream()
					.filter(ip -> ip.getGroupOrNull() != null)
					.collect(Collectors.toMap(ip -> ip.getIp(), ip -> ip.getGroupOrNull()));
			
			// Update caches
			this.whitelist = newWhitelist;
			this.groups = newGroups;
			
			this.lastRefresh = new Date();
		}
	}
	
	private static final List<ParsedIpSettings> parseSettings(final List<IpSettings> list) {
		final List<ParsedIpSettings> parsedList = new ArrayList<>();
		
		for(final IpSettings settings : list) {
			try {
				parsedList.add(new ParsedIpSettings(settings));
			} catch(final UnknownHostException e) {
				logger.warn(String.format("Cannot parse IP settings for hijacking prevention. IP address '%s' ignored and not whitelisted.", settings.getIp()), e);
			}
		}
		
		return parsedList;
	}
	
	/**
	 * Creates a set of all local IP adresses.
	 * 
	 * @return Set of local IP addresses
	 * 
	 * @throws SocketException on errors retrieving local IP addresses
	 */
	private final Set<InetAddress> listLocalIpAddresses() throws SocketException {
		if(logger.isInfoEnabled()) {
			logger.info("Listing local IP addresses");
		}

		return new HashSet<>(NetworkUtil.listLocalInetAddresses());
	}

	
	public final void setCacheTimeSeconds(final int time) {
		this.cacheTimeSeconds = time;
	}
	
	@Required
	public final void setSessionHijackingPreventionDao(final SessionHijackingPreventionDataDao dao) {
		this.sessionHijackingPreventionDao = Objects.requireNonNull(dao, "SessionHijackingPreventionDataDao is null");
	}
}
