/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MailingComponentType;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.components.form.MailingImagesOverviewFilter;
import com.agnitas.web.CdnImage;

public interface MailingComponentDao {
	 /**
     * Loads mailing component identified by component id and company id.
     *
     * @param compID
     *            The id of the mailing component that should be loaded.
     * @param companyID
     *            The companyID for mailing component.
     * @return The MailingComponent or null on failure.
     */
    MailingComponent getMailingComponent(int compID, int companyID);

    /**
     * Loads mailing component identified by mailing id, component id and company id.
     *
     * @param mailingId
     *            The id of the mailing for mailing component.
     * @param componentId
     *            The id of the mailing component that should be loaded.
     * @param companyId
     *            The companyID for mailing component.
     * @return The MailingComponent or null on failure.
     */
    MailingComponent getMailingComponent(int mailingId, int componentId, int companyId);

    /**
     * Loads mailing component identified by mailing id, company id and name.
     *
     * @param mailingID
     *            The id of the mailing for mailing component.
     * @param companyID
     *            The companyID for mailing component.
     * @param name
     *            The name of the mailing component.
     * @return The MailingComponent or null on failure.
     */
    MailingComponent getMailingComponentByName(int mailingID, int companyID, String name);

    /**
     * Saves or updates mailing component.
     *
     * @param comp
     *          The mailing component that should be saved.
     */
    void saveMailingComponent(MailingComponent comp);

    /**
     * Deletes mailing component.
     *
     * @param comp
     *          The mailing component that should be deleted.
     */
    void deleteMailingComponent(MailingComponent comp);
    
    void deleteMailingComponents(List<MailingComponent> components);

    MailingComponent findComponent(int id, int companyId);

    /**
     * Loads all components identified by mailing id, company id and component type.
     *
     * @param mailingID
     *          The id of the mailing for mailing component.
     * @param companyID
     *          The companyID for mailing component.
     * @param componentType
     *          The type for mailing component.
     * @return Vector of MailingComponents.
     */
    List<MailingComponent> getMailingComponents(int mailingID, int companyID, MailingComponentType componentType);

    /**
     * Loads all components identified by mailing id, company id and component type.
     *
     * @param mailingID
     *          The id of the mailing for mailing component.
     * @param companyID
     *          The companyID for mailing component.
     * @param componentType
     *          The type for mailing component.
     * @param includeContent
     *          Whether ({@code true}) or not ({@code false}) a content data (see {@link MailingComponent#getEmmBlock()} and {@link MailingComponent#getBinaryBlock()}) should be loaded.
     * @return Vector of MailingComponents.
     */
    List<MailingComponent> getMailingComponents(int mailingID, int companyID, MailingComponentType componentType, boolean includeContent);

    /**
     * Loads all components identified by mailing id and company id.
     *
     * @param mailingID
     *          The id of the mailing for mailing component.
     * @param companyID
     *          The companyID for mailing component.
     * @return Vector of MailingComponents.
     */
    List<MailingComponent> getMailingComponents(int mailingID, int companyID);

    /**
     * Loads all components identified by mailing id and company id.
     *
     * @param mailingID
     *          The id of the mailing for mailing component.
     * @param companyID
     *          The companyID for mailing component.
     * @param includeContent
     *          Whether ({@code true}) or not ({@code false}) a content data (see {@link MailingComponent#getEmmBlock()} and {@link MailingComponent#getBinaryBlock()}) should be loaded.
     * @return Vector of MailingComponents.
     */
    List<MailingComponent> getMailingComponents(int mailingID, int companyID, boolean includeContent);

    /**
     * Loads all components identified by mailing id and company id.
     *
     * @param companyID
     *          The companyID for mailing component.
     * @param componentIds
     *          The ids of the mailing components.
     * @return Vector of MailingComponents.
     */
    List<MailingComponent> getMailingComponents(int companyID, int mailingID, Set<Integer> componentIds);
    
    List<MailingComponent> getMailingComponentsByType(int companyID, int mailingID, List<MailingComponentType> types);

    /**
     * Loads all components identified by mailing id, company id.
     * And type for these components should be MailingComponentType.Attachment or MailingComponentType.PersonalizedAttachment.
     *
     * @param mailingID
     *          The id of the mailing for mailing component.
     * @param companyID
     *          The companyID for mailing component.
     * @return Vector of MailingComponents.
     */
	List<MailingComponent> getPreviewHeaderComponents(int mailingID, int companyID);

    void updateHostImage(int mailingID, int companyID, int componentID, byte[] imageBytes);

	Date getComponentTime(int companyID, int mailingID, String name);

	List<MailingComponent> getMailingComponentsByType(MailingComponentType type, int companyID);

    Map<Integer, Integer> getImageSizes(int companyID, int mailingID);

	Map<Integer, String> getImageNames(int companyId, int mailingId, boolean includeExternalImages);

	boolean attachmentExists(int companyId, int mailingId, String name, int targetId);

	boolean deleteMailingComponentsByCompanyID(int companyID);
	
	void deleteMailingComponentsByMailing(int mailingID);

	List<Integer> findTargetDependentMailingsComponents(int targetGroupId, int companyId);

	List<Integer> filterComponentsOfNotSentMailings(List<Integer> components);

	boolean deleteImages(int companyId, int mailingId, Set<Integer> bulkIds);

    int getImageComponent(int companyId, int mailingId, MailingComponentType componentType);

    CdnImage getCdnImage(int companyID, int mailingID, String imageName, boolean mobile);

	MailingComponent getComponentByCdnID(String cdnID);

    /**
     * Method for marking unpresent components of some mailing
     *
     * @param mailingId mailing with components which will be marked as unpresent
     * @param presentComponents components which will not be marked as unpresent
     *
     * @return count of updated rows (unpresent components)
     */
	int setUnPresentComponentsForMailing(int mailingId, List<MailingComponent> presentComponents);

	boolean updateBinBlockBulk(int companyId, Collection<Integer> mailingIds, MailingComponentType componentType, Collection<String> namePatterns, byte[] value);

	List<String> getImagesNames(int mailingId, Set<Integer> ids, int companyID);

	PaginatedList<MailingComponent> getImagesOverview(int companyID, int mailingID, MailingImagesOverviewFilter filter);

	List<String> getMailingImagesNamesForMobileAlternative(int mailingId, int companyId);
}
