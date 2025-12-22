/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.linkcheck.beans.LinkReachability;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.beans.TrackableLink;
import com.agnitas.beans.Mailing;
import com.agnitas.messages.Message;

/**
 * Checks availability of links.
 */
public class LinkcheckService {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(LinkcheckService.class);
	
	private ConfigService configService;
	
	/**
	 * Checks all links but returns all links, that have not been found. (not reachable or timed out).
	 * 
	 * @param linkList list of {@link TrackableLink}s to check
	 *
	 * @return list of check results of invalid links
	 */
	public List<LinkReachability> checkLinkReachability(Collection<TrackableLink> linkList) {
		// Check all links...
		Collection<LinkReachability> checkResults = checkReachability(linkList);
		
		// .. and remove links that have been found.
		List<LinkReachability> filteredResults = new Vector<>();
		checkResults.stream().filter(result -> result.getReachability() != LinkReachability.Reachability.OK).forEach(result -> filteredResults.add(result)); 

		return filteredResults;
	}
	
	/**
	 * This method checks the availability of given list of {@link TrackableLink}s.
	 * 
	 * @param linkList of {@link TrackableLink}s to check
	 *
	 * @return list of check results
	 */
	public Collection<LinkReachability> checkReachability(Collection<TrackableLink> linkList) {
		// create usual <String> List
		List<String> checkList = new Vector<>();

		// Convert list of TrackableLinks to list of URLs
		linkList.forEach(link -> checkList.add(link.getFullUrl()));

		return checkRechability(checkList);
	}
	
	/**	 
	 * Checks the availability of a list of URLs.
	 * 
	 * @param linkList list of URLs to check
	 * 
	 * @return list of check results
	 */
	public List<LinkReachability> checkRechability(List<String> linkList) {
		// create Pool
		ExecutorService linkCheckExecutor = null;
		List<LinkReachability> resultList = new ArrayList<>();
		try {
			linkCheckExecutor = Executors.newFixedThreadPool(configService.getIntegerValue(ConfigValue.Linkchecker_Threadcount));
			
			final String userAgent = configService.getValue(ConfigValue.LinkChecker_UserAgent);
			
			// Execute link checks
			for (String url : linkList) {
				linkCheckExecutor.execute(new LinkcheckWorker(configService.getIntegerValue(ConfigValue.Linkchecker_Linktimeout), url, resultList, userAgent));
			}
		} finally {
			if (linkCheckExecutor != null) {
				linkCheckExecutor.shutdown();	// no new task are scheduled
			}
		}

		if (linkCheckExecutor != null) {
			try {
				linkCheckExecutor.awaitTermination((configService.getIntegerValue(ConfigValue.Linkchecker_Linktimeout) + 1000), TimeUnit.MILLISECONDS );
			} catch (InterruptedException e) {
				logger.error("Error occured: " + e.getMessage(), e);
			}
		}
		
		return resultList;
	}

	public SimpleServiceResult checkForUnreachableLinks(Mailing mailing) {
		List<Message> errors = new ArrayList<>();

		try {
			Collection<TrackableLink> links = mailing.getTrackableLinks().values();
			List<LinkReachability> resultList = checkLinkReachability(links);

			for (LinkReachability availability : resultList) {
				String argument = availability.getUrl() + " <br>";

				switch (availability.getReachability()) {
					case TIMED_OUT:
						errors.add(Message.of("error.invalid.link.timeout", argument));
						break;

					case NOT_FOUND:
						errors.add(Message.of("error.invalid.link.notReachable", argument));
						break;

					default:
						errors.add(Message.of("error.invalid.link", argument));
				}
			}
		} catch (Exception e) {
			logger.error("checkForInvalidLinks: " + e, e);
		}

		return new SimpleServiceResult(errors.isEmpty(), errors);
	}

	/**
	 * Sets ConfigService for link checker.
	 * 
	 * @param configService DAO for link checker
	 */
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
}
