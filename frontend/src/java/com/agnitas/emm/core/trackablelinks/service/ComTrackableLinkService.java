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
import org.springframework.context.ApplicationContext;

import com.agnitas.beans.ComMailing;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.TrackableLinkListItem;
import com.agnitas.beans.TrackableLinkModel;
import com.agnitas.beans.TrackableLinkSettings;
import com.agnitas.emm.core.trackablelinks.exceptions.TrackableLinkException;

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
	 * @param mailingID mailing ID
	 * @param companyId company ID of mailing
	 * 
	 * @return true if editing is allowed, otherwise false
	 */
	boolean isUrlEditingAllowed(int mailingID, @VelocityCheck int companyId);

    void addExtensions(ComMailing aMailing, Set<Integer> linksIds, List<LinkProperty> passedLinkProperties);

	void replaceCommonExtensions(ComMailing aMailing, List<LinkProperty> passedLinkProperties, Set<Integer> bulkLinkIds, List<UserAction> userActions);
	
	void removeLegacyMailingLinkExtension(ComMailing aMailing, Set<Integer> bulkLinkIds);
	
	void setMailingLinkExtension(ComMailing aMailing, String linkExtension);

    void setLegacyLinkExtensionMarker(ComMailing aMailing, Map<Integer, Boolean> linksToExtends);
	
	boolean saveEveryPositionLinks(ComMailing aMailing, ApplicationContext aContext, Set<Integer> bulkLinks) throws Exception;

	void setStandardDeeptracking(ComMailing aMailing, Set<Integer> bulkLinkIds, int deepTracking, Map<Integer, Integer> getLinkItemsDeepTracking);
	
	void setShortname(ComMailing aMailing, Map<Integer, String> linkItemNames);
	
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
	List<TrackableLinkListItem> getTrackableLinks(int mailingID, @VelocityCheck int companyId);
	
	List<ComTrackableLink> getTrackableLinks(@VelocityCheck int companyId, List<Integer> urlIds);

	TrackableLinkSettings getTrackableLinkSettings(int linkID, @VelocityCheck int companyId);

    /**
     * Removes any link extension (global and individual) from all links of given mailing.
	 *
     * @param companyId company ID
     * @param mailingId mailing ID
     * 
     * @throws Exception on errors removing link extensions
     */
    void removeGlobalAndIndividualLinkExtensions(@VelocityCheck int companyId, int mailingId) throws Exception;

	void updateTrackableLinkSettings(TrackableLinkModel trackableLinkModel);

}
