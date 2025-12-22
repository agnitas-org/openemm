/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

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

import com.agnitas.emm.core.sessionhijacking.beans.IpSettings;
import com.agnitas.emm.core.sessionhijacking.beans.ParsedIpSettings;
import com.agnitas.emm.core.sessionhijacking.dao.SessionHijackingPreventionDataDao;
import com.agnitas.util.NetworkUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class SessionHijackingPreventionServiceImpl implements SessionHijackingPreventionService {

	/** Default time for caching configuration data. */
	public static final int DEFAULT_CACHE_TIME_SECONDS = 300;
	
	private static final Logger logger = LogManager.getLogger(SessionHijackingPreventionServiceImpl.class);

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
	public boolean isAddressAllowed(InetAddress sessionIpAddress, InetAddress clientIpAddress) {
		if (logger.isDebugEnabled()) {
			logger.debug(
					"IP addresses are: session={}, client={}",
					sessionIpAddress != null ? sessionIpAddress.getHostAddress() : "<unknown>",
					clientIpAddress != null ? clientIpAddress.getHostAddress() : "<unknown>"
			);
		}
		
		try {
			refreshCache();
		} catch (Exception e) {
			logger.error("Unable to refresh cache - using old cache state", e);
		}
		
		// The client IP address is always allowed, if it is the same as the IP address bound to the session
		if (sessionIpAddress != null && sessionIpAddress.equals(clientIpAddress)) {
			logger.info("IP of session equals to IP of client - accepted");
			return true;
		}
		
		// Whitelisted IP addresses of client are never checked and always allowed
		if (whitelist.contains(clientIpAddress)) {
			logger.info("IP of client is whitelisted - accepted");
			return true;
		}

		final Integer sessionGroup = this.groups.get(sessionIpAddress);
		final Integer clientGroup = this.groups.get(clientIpAddress);

		// Here, we have a client IP address, that differs from the IP address bound to the session and that is not whitelisted. We need to check group ID.
		if (sessionGroup != null && sessionGroup.equals(clientGroup)) {
			logger.info("IP of session and IP of client in same group - accepted");
			return true;
		}

		if (isGoogleProxy(clientIpAddress)) {
			logger.info("IP of client is verified google proxy - accepted");
			return true;
		}

		logger.info("IP of session and IP of client in different groups or one or both IPs have no group assigned - rejected");
		return false;
	}

	private boolean isGoogleProxy(InetAddress clientIpAddress) {
		if (clientIpAddress == null) {
			return false;
		}

		String hostName = clientIpAddress.getCanonicalHostName();
		if (!hostName.startsWith("google-proxy-") || !hostName.endsWith(".google.com")) {
			return false;
		}

        try {
            return InetAddress.getByName(hostName)
					.getHostAddress()
					.equals(clientIpAddress.getHostAddress());
        } catch (UnknownHostException e) {
            return false;
        }
    }

	private void refreshCache() throws Exception {
		if (lastRefresh == null || (lastRefresh.getTime() + cacheTimeSeconds * 1000 < (new Date()).getTime())) {
			logger.debug("Refreshing internal caches");

			// First, mark all local IP addresses (IPs of current machine) as whitelisted
			final Set<InetAddress> newWhitelist = new HashSet<>(listLocalIpAddresses());
			final List<ParsedIpSettings> parsedList = parseSettings(this.sessionHijackingPreventionDao.listIpSettings());

			// Add all IPs with group == null to whitelist
			parsedList.stream()
				.filter(ip -> ip.getGroupOrNull() == null)
				.map(ParsedIpSettings::getIp)
				.forEach(newWhitelist::add);
			
			// Add all IPs with group != null to groups
			final Map<InetAddress, Integer> newGroups = parsedList.stream()
					.filter(ip -> ip.getGroupOrNull() != null)
					.collect(Collectors.toMap(ParsedIpSettings::getIp, ParsedIpSettings::getGroupOrNull));
			
			// Update caches
			this.whitelist = newWhitelist;
			this.groups = newGroups;
			
			this.lastRefresh = new Date();
		}
	}
	
	private static List<ParsedIpSettings> parseSettings(List<IpSettings> list) {
		final List<ParsedIpSettings> parsedList = new ArrayList<>();
		
		for(final IpSettings settings : list) {
			try {
				parsedList.add(new ParsedIpSettings(settings));
			} catch(UnknownHostException e) {
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
	private Set<InetAddress> listLocalIpAddresses() throws SocketException {
		logger.info("Listing local IP addresses");
		return new HashSet<>(NetworkUtil.listLocalInetAddresses());
	}

	public void setCacheTimeSeconds(int time) {
		this.cacheTimeSeconds = time;
	}
	
	public void setSessionHijackingPreventionDao(SessionHijackingPreventionDataDao dao) {
		this.sessionHijackingPreventionDao = Objects.requireNonNull(dao, "SessionHijackingPreventionDataDao is null");
	}
}
