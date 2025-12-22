/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.TrackableLink;
import com.agnitas.beans.TrackableLinkListItem;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.mailtracking.service.ClickTrackingService;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.web.exception.ClearLinkExtensionsException;

public interface TrackableLinkDao {
	 
    /**
     * Getter for property trackableLink by link id and company id.
     * 
     * @param linkID ID of link
     * @param companyID company ID
     * @param includeDeleted if <code>true</code>, link is also returned if marked as deleted
     *
     * @return trackable link or <code>null</code>.
     */
    TrackableLink getTrackableLink(final int linkID, final int companyID, final boolean includeDeleted);
 
	/**
	 * Do not use this method directly for click tracking!
	 * 
	 * Use {@link ClickTrackingService#trackLinkClick(ExtensibleUID, String, DeviceClass, int, int)} instead. This
	 * method respects the tracking settings of the customer.
	 * 
	 * @see ClickTrackingService#trackLinkClick(ExtensibleUID, String, DeviceClass, int, int)
	 */
	boolean logClickInDB(TrackableLink link, int customerID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID, int position);
	
	List<LinkProperty> getLinkProperties(TrackableLink link);

	boolean deleteRdirUrlsByMailing(int mailingID);

    List<TrackableLink> getTrackableLinks(int companyID, int mailingID);

    Map<String, TrackableLink> getTrackableLinksMap(int mailingId, int companyId, boolean includeDeleted);

    List<TrackableLink> getTrackableLinks(int companyId, int mailingId, boolean includeDeleted);
    
	void deleteTrackableLinksExceptIds(int companyID, int mailingID, Collection<Integer> ids);

	void activateDeeptracking(int companyID, int mailingID);
	
	boolean deleteTrackableLinksReally(int companyID);

    void deleteAdminAndTestClicks(int mailingId, int companyId);

	void removeGlobalAndIndividualLinkExtensions(int companyId, int mailingId);
	
	void removeLinkExtensionsByCompany(int companyID);
	
	Map<Integer, String> getTrackableLinkUrl(int companyId, int mailingId, List<Integer> linkIds);

    void bulkClearExtensions(int mailingId, int companyId, Set<Integer> bulkIds) throws ClearLinkExtensionsException;

	Optional<TrackableLink> findLinkByFullUrl(String fullUrl, int mailingID, int companyID);
	
	void reactivateLink(final TrackableLink link);
	
	/**
     * Getter for property trackableLink by link id and company id.
     *
     * @return Value of trackableLink.
     */
    TrackableLink getTrackableLink(int linkID, int companyID);
    
    /**
     * Getter for property trackableLink by link id and company id.
     *
     * @return Value of trackableLink.
     */
    TrackableLink getTrackableLink(String url, int companyID, int mailingID);

    /**
     * Saves trackableLink.
     *
     * @return Saved trackableLink id.
     * @param link
     */
    int saveTrackableLink(TrackableLink link);
    
    void batchSaveTrackableLinks(int companyID, int mailingId, Map<String, TrackableLink> trackableLinksMap, boolean removeUnusedLinks);

	List<TrackableLinkListItem> listTrackableLinksForMailing(int companyID, int mailingID);

    boolean isTrackingOnEveryPositionAvailable(int companyId, int mailingId);
}
