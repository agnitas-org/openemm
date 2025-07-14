/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.TrackableLink;
import com.agnitas.beans.TrackableLinkListItem;
import com.agnitas.beans.TrackableLinkModel;
import com.agnitas.beans.TrackableLinkSettings;
import com.agnitas.emm.core.trackablelinks.exceptions.TrackableLinkException;
import com.agnitas.web.exception.ClearLinkExtensionsException;
import org.agnitas.emm.core.mailinglist.service.MailinglistNotExistException;
import com.agnitas.emm.core.mediatypes.dao.MediatypesDaoException;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import org.json.JSONObject;

/**
 * Service interface for trackable links.
 */
public interface TrackableLinkService {
	
	/**
	 * Update link target for given link ID.
	 * 
	 * These prerequisites must be met to update the link target:
	 * <ul>
	 *   <li>The mailing must be a regular mailing. (For any other mailing, links can be updated by deactivating the mailing and modifying the mailing content.)</li>
	 *   <li>The mailing must be world-sent or scheduled for world-sending. (In any other case, link targets can be updated by modifying the mailing content.)</li>
	 *   <li>The link must belong to the mailing.</li>
	 * </ul>
	 *
	 * @param link link to edit
	 * @param newUrl new link target
	 * 
	 * @throws TrackableLinkException when an error occurs during processing
	 */
	void updateLinkTarget(TrackableLink link, String newUrl) throws TrackableLinkException;

	/**
	 * Checks, if URL editing is allowed for given mailing.
	 * 
	 *
	 * @param admin
	 * @param mailingID mailing ID
	 * @return true if editing is allowed, otherwise false
	 */
	boolean isUrlEditingAllowed(Admin admin, int mailingID);

    void addExtensions(Mailing aMailing, Set<Integer> linksIds, List<LinkProperty> extensions, List<UserAction> userActions);

	void removeLegacyMailingLinkExtension(Mailing aMailing, Set<Integer> bulkLinkIds);

    Map<Integer, String> getMailingLinks(int mailingId, int companyId);

    JSONObject getMailingLinksJson(int mailingId, int companyId);

	/**
	 * Gets trackable links
	 * @param mailingID mailing id
	 * @param companyId company id
     * @return list of trackable links. Returns empty list when mailing for the company doesn't have trackable links.
	 * @throws MailinglistNotExistException if mailing doesn't belong to the company.
     */
	List<TrackableLinkListItem> getTrackableLinkItems(int mailingID, int companyId);

	int saveTrackableLink(TrackableLink trackableLink);

	TrackableLink getTrackableLink(int companyId, int linkId);

	TrackableLinkSettings getTrackableLinkSettings(int linkID, int companyId);
	
    void bulkClearExtensions(int mailingId, int companyId, Set<Integer> bulkIds) throws ClearLinkExtensionsException;

    Mailing getMailingForLinksOverview(int mailingId, int companyId, boolean includeDeleted) throws MediatypesDaoException;

	void updateTrackableLinkSettings(TrackableLinkModel trackableLinkModel);

	boolean isTrackingOnEveryPositionAvailable(int companyId, int mailingId);

    List<LinkProperty> getCommonExtensions(int mailingId, int companyId, Set<Integer> bulkIds);

}
