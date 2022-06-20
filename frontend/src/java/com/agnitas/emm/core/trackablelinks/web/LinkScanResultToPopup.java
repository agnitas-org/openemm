/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.web;

import java.util.List;
import java.util.stream.Collectors;

import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.linkcheck.service.LinkService.LinkScanResult;
import com.agnitas.emm.core.linkcheck.service.LinkService.LinkWarning;
import com.agnitas.emm.core.linkcheck.service.LinkService.LinkWarning.WarningType;
import com.agnitas.web.mvc.Popups;

/**
 * Utility class to convert a {@link LinkService.LinkScanResult} to {@link Popups}.
 */
public final class LinkScanResultToPopup {

	/** Maximum number of links shown in one warning (current: {@value #MAX_LINKS_PER_WARNING}). */
	public static final int MAX_LINKS_PER_WARNING = 15;
	
	/** Message key for list of links less than or equals to {@link #MAX_LINKS_PER_WARNING}. */
	public static final String MESSAGE_KEY_INSECURE_LINKS = "warning.mailing.links.insecureProtocol";

	/** Message key for list of links greater than {@link #MAX_LINKS_PER_WARNING}. */
	public static final String MESSAGE_KEY_INSECURE_LINKS_WITH_MORE = "warning.mailing.links.insecureProtocol.withMore";

	/**
	 * Converts the link warnings.
	 * 
	 * @param result LinkScanResult
	 * @param warnings popup container
	 */
	public static final void linkWarningsToPopups(final LinkScanResult result, final Popups warnings) {
		insecureLinksToActionMessages(result, warnings);
		// Add more warnings here in the order to be displayed
	}
	
	/**
	 * Processes the warning of type {@link LinkWarning#WarningType#INSECURE}.
	 * 
	 * @param result link scan result
	 * @param warnings container for warnings
	 */
	private static final void insecureLinksToActionMessages(final LinkScanResult result, final Popups warnings) {
		final List<LinkWarning> list = filterWarningsByType(result, WarningType.INSECURE);
		
		if(!list.isEmpty()) {
			final int numMoreLinks = Math.max(0, list.size() - MAX_LINKS_PER_WARNING);
			final List<LinkWarning> listToView = list.subList(0, Math.min(list.size(), MAX_LINKS_PER_WARNING));
			
			final String links = listToView
					.stream()
					.map(l -> l.getLink())
					.collect(Collectors.joining("</li><li>", "<ul><li>", "</li></ul>"));

			if(numMoreLinks == 0) {
				warnings.warning(MESSAGE_KEY_INSECURE_LINKS, links);
			} else {
				warnings.warning(MESSAGE_KEY_INSECURE_LINKS, links, numMoreLinks);
			}
		}
	}
	
	/**
	 * Filters warnings by warning type.
	 * 
	 * @param result link scan result
	 * @param warningType warning type
	 * 
	 * @return filtered warning types
	 */
	private static final List<LinkService.LinkWarning> filterWarningsByType(final LinkScanResult result, final WarningType warningType) {
		return result.getLinkWarnings()
				.stream()
				.filter(w -> w.getWarningType() == warningType)
				.collect(Collectors.toList());
	}

}
