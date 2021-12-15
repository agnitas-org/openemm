/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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

import org.agnitas.beans.TrackableLink;
import org.agnitas.dao.TrackableLinkDao;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.emm.core.mailtracking.service.ClickTrackingService;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.web.exception.ClearLinkExtensionsException;

public interface ComTrackableLinkDao extends TrackableLinkDao {
	 
    /**
     * Getter for property trackableLink by link id and company id.
     * 
     * @param linkID ID of link
     * @param companyID company ID
     * @param includeDeleted if <code>true</code>, link is also returned if marked as deleted
     *
     * @return trackable link or <code>null</code>.
     */
    ComTrackableLink getTrackableLink(final int linkID, @VelocityCheck final int companyID, final boolean includeDeleted);
 
	/**
	 * Do not use this method directly for click tracking!
	 * 
	 * Use {@link ClickTrackingService#trackLinkClick(com.agnitas.emm.core.commons.uid.ComExtensibleUID, String, DeviceClass, int, int)} instead. This
	 * method respects the tracking settings of the customer.
	 * 
	 * @see ClickTrackingService#trackLinkClick(com.agnitas.emm.core.commons.uid.ComExtensibleUID, String, DeviceClass, int, int)
	 */
	boolean logClickInDB(TrackableLink link, int customerID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID);
	
	List<LinkProperty> getLinkProperties(ComTrackableLink link);

	void deleteLinkProperties(int linkID);

	boolean deleteRdirUrlsByMailing(int mailingID);

	void storeLinkProperties(int linkId, List<LinkProperty> properties);
    
    List<ComTrackableLink> getTrackableLinks(@VelocityCheck int companyID, int mailingID);
    
    List<ComTrackableLink> getTrackableLinks(@VelocityCheck int companyID, List<Integer> urlIds);
    
    String getLinkUrl(@VelocityCheck int companyID, int mailingID, int linkID);

	void deleteTrackableLinksExceptIds(@VelocityCheck int companyID, int mailingID, Collection<Integer> ids);

	void activateDeeptracking(int companyID, int mailingID);
	
	boolean deleteTrackableLinksReally(@VelocityCheck int companyID);

    void deleteAdminAndTestClicks(int mailingId, @VelocityCheck int companyId);

	void removeGlobalAndIndividualLinkExtensions(@VelocityCheck int companyId, int mailingId) throws Exception;
	
	void removeLinkExtensionsByCompany(int companyID);
	
	Map<Integer, String> getTrackableLinkUrl(@VelocityCheck int companyId, int mailingId, List<Integer> linkIds);

    void bulkClearExtensions(int mailingId, int companyId, Set<Integer> bulkIds) throws ClearLinkExtensionsException;

	Optional<ComTrackableLink> findLinkByFullUrl(String fullUrl, int mailingID, int companyID);
	
	void reactiveLink(final ComTrackableLink link);
}
