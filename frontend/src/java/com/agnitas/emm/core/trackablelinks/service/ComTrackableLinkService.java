/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.emm.core.mailinglist.service.MailinglistNotExistException;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.TrackableLinkListItem;
import com.agnitas.beans.TrackableLinkModel;
import com.agnitas.beans.TrackableLinkSettings;
import com.agnitas.emm.core.trackablelinks.exceptions.TrackableLinkException;
import com.agnitas.web.exception.ClearLinkExtensionsException;

/**
 * Service interface for trackable links.
 */
public interface ComTrackableLinkService {
	
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
	void updateLinkTarget(ComTrackableLink link, String newUrl) throws TrackableLinkException;

	/**
	 * Checks, if URL editing is allowed for given mailing.
	 * 
	 *
	 * @param admin
	 * @param mailingID mailing ID
	 * @return true if editing is allowed, otherwise false
	 */
	boolean isUrlEditingAllowed(ComAdmin admin, int mailingID);

    void addExtensions(Mailing aMailing, Set<Integer> linksIds, List<LinkProperty> extensions, List<UserAction> userActions);
	
	void removeLegacyMailingLinkExtension(Mailing aMailing, Set<Integer> bulkLinkIds);
	
	void setMailingLinkExtension(Mailing aMailing, String linkExtension);

    void setLegacyLinkExtensionMarker(Mailing aMailing, Map<Integer, Boolean> linksToExtends);
	
	void setStandardDeeptracking(Mailing aMailing, Set<Integer> bulkLinkIds, int deepTracking, Map<Integer, Integer> getLinkItemsDeepTracking);
	
	void setShortname(Mailing aMailing, Map<Integer, String> linkItemNames);
	
	/**
	 * Gets trackable links
	 * @param mailingID mailing id
	 * @param companyId company id
     * @return list of trackable links. Returns empty list if mailing doesn't belong to the company or
	 * trackable links don't exist for the company and mailing.
     */
    List<TrackableLinkListItem> getMailingLinks(int mailingID, @VelocityCheck int companyId);

	/**
	 * Gets trackable links
	 * @param mailingID mailing id
	 * @param companyId company id
     * @return list of trackable links. Returns empty list when mailing for the company doesn't have trackable links.
	 * @throws MailinglistNotExistException if mailing doesn't belong to the company.
     */
	List<TrackableLinkListItem> getTrackableLinkItems(int mailingID, @VelocityCheck int companyId);

	List<ComTrackableLink> getTrackableLinks(int mailingId, @VelocityCheck int companyId);

	List<ComTrackableLink> getTrackableLinks(@VelocityCheck int companyId, List<Integer> urlIds);

	int saveTrackableLink(ComTrackableLink trackableLink);

	ComTrackableLink getTrackableLink(@VelocityCheck int companyId, int linkId);

	TrackableLinkSettings getTrackableLinkSettings(int linkID, @VelocityCheck int companyId);
	
    void bulkClearExtensions(@VelocityCheck int mailingId, int companyId, Set<Integer> bulkIds) throws ClearLinkExtensionsException;

	void updateTrackableLinkSettings(TrackableLinkModel trackableLinkModel);

	boolean isTrackingOnEveryPositionAvailable(@VelocityCheck int companyId, int mailingId);

    List<LinkProperty> getCommonExtensions(int mailingId, int companyId, Set<Integer> bulkIds);
}
