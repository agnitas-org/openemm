/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mobile.service.impl;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.emm.core.mobile.bean.Client;
import com.agnitas.emm.core.mobile.dao.ClientDao;
import com.agnitas.emm.core.mobile.service.ClientService;

public class ClientServiceImpl implements ClientService {
	private static final transient Logger logger = LogManager.getLogger(ClientServiceImpl.class);
	
	private static final int PATTERN_VALIDITY_MILLIS = 12 * 60 * 60 * 1000; // 12 hours
	
	private ClientDao clientDao;

	private volatile Date patternExpiration = null;
	private Map<Pattern, Integer> clientPatterns = null;
	
	private synchronized void refreshPatterns() {
		if (patternExpiration == null || new Date().after(patternExpiration)) {
			List<Client> clientList = clientDao.getClients();
			if (clientList == null || clientList.isEmpty()) {
				clientPatterns = null;
			} else {
				// Using LinkedHashMap to keep order of client patterns when they are applied
				Map<Pattern, Integer> newClientPatterns = new LinkedHashMap<>();
				for (Client client : clientList) {
					newClientPatterns.put(Pattern.compile(client.getRegEx(), Pattern.CASE_INSENSITIVE), client.getClientID());
				}
				clientPatterns = newClientPatterns;
			}
			
			patternExpiration = new Date(new Date().getTime() + PATTERN_VALIDITY_MILLIS);
		}
	}

	@Override
	public int getClientId(String userAgent) {
		if (StringUtils.isNotEmpty(userAgent)) {
			if (patternExpiration == null || new Date().after(patternExpiration)) {
				refreshPatterns();
			}
			
			if (clientPatterns != null) {
				try {
					// loop over all patterns
					for (Entry<Pattern, Integer> entry : clientPatterns.entrySet()) {
						Pattern clientPattern = entry.getKey();
						Matcher matcher = clientPattern.matcher(userAgent);
						if (matcher.find()) {
							return entry.getValue();
						}
					}

					if (logger.isInfoEnabled()) {
						logger.info("No clientID found for useragent '" + userAgent + "'");
					}
					return CLIENT_UNKNOWN;
				} catch (Exception e) {
					logger.error("Error getting clientID for useragent '" + userAgent + "': " + e.getMessage());
					return CLIENT_UNKNOWN;
				}
			} else {
				return CLIENT_UNKNOWN;
			}
		} else {
			logger.info("Unable to detect clientID - no user agent specified");
			return CLIENT_UNKNOWN;
		}
	}
	
	public void setClientDao(ClientDao clientDao) {
		this.clientDao = clientDao;
	}
}
