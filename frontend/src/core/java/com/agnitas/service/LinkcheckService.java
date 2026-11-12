/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.agnitas.beans.BaseTrackableLink;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.TrackableLink;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.linkcheck.beans.LinkReachability;
import com.agnitas.messages.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Checks availability of links.
 */
public class LinkcheckService {
	
	private static final Logger logger = LogManager.getLogger(LinkcheckService.class);
	
	private final ConfigService configService;

	public LinkcheckService(ConfigService configService) {
		this.configService = configService;
	}

	private List<LinkReachability> checkLinkReachability(Collection<TrackableLink> links) {
		List<String> urls = links.stream()
				.map(BaseTrackableLink::getFullUrl)
				.toList();

		return checkReachability(urls)
				.stream()
				.filter(result -> result.getReachability() != LinkReachability.Reachability.OK)
				.toList();
	}

	private List<LinkReachability> checkReachability(List<String> urls) {
		List<LinkReachability> resultList = Collections.synchronizedList(new ArrayList<>());
		int linkTimeout = configService.getIntegerValue(ConfigValue.Linkchecker_Linktimeout);
		String userAgent = configService.getValue(ConfigValue.LinkChecker_UserAgent);

		ExecutorService linkCheckExecutor = Executors.newFixedThreadPool(configService.getIntegerValue(ConfigValue.Linkchecker_Threadcount));
		try {
			for (String url : urls) {
				linkCheckExecutor.execute(new LinkcheckWorker(linkTimeout, url, resultList, userAgent));
			}
		} finally {
			linkCheckExecutor.shutdown(); // no new tasks are accepted, already submitted ones keep running
		}

		try {
			linkCheckExecutor.awaitTermination(linkTimeout + 1000L, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.warn("Link checking was interrupted", e);
		}

		// Workers that did not finish in time may still add results, so return a snapshot
		synchronized (resultList) {
			return new ArrayList<>(resultList);
		}
	}

	public SimpleServiceResult checkForUnreachableLinks(Mailing mailing) {
		List<Message> errors = new ArrayList<>();

		try {
			Collection<TrackableLink> links = mailing.getTrackableLinks().values();

			for (LinkReachability availability : checkLinkReachability(links)) {
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
			logger.error("checkForInvalidLinks: {}", e.getMessage(), e);
		}

		return new SimpleServiceResult(errors.isEmpty(), errors);
	}

}
