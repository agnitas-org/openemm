/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import java.util.List;
import java.util.Set;

import org.agnitas.beans.MailingComponent;
import org.agnitas.emm.core.velocity.VelocityCheck;

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
    MailingComponent getMailingComponent(int compID, @VelocityCheck int companyID);

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
    MailingComponent getMailingComponentByName(int mailingID, @VelocityCheck int companyID, String name);

    /**
     * Saves or updates mailing component.
     *
     * @param comp
     *          The mailing component that should be saved.
     * @throws Exception 
     */
    void saveMailingComponent(MailingComponent comp) throws Exception;

    /**
     * Deletes mailing component.
     *
     * @param comp
     *          The mailing component that should be deleted.
     */
    void deleteMailingComponent(MailingComponent comp);
    
    void deleteMailingComponents(List<MailingComponent> components);

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
    List<MailingComponent> getMailingComponents(int mailingID, @VelocityCheck int companyID, int componentType);

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
    List<MailingComponent> getMailingComponents(int mailingID, @VelocityCheck int companyID, int componentType, boolean includeContent);

    /**
     * Loads all components identified by mailing id and company id.
     *
     * @param mailingID
     *          The id of the mailing for mailing component.
     * @param companyID
     *          The companyID for mailing component.
     * @return Vector of MailingComponents.
     */
    List<MailingComponent> getMailingComponents(int mailingID, @VelocityCheck int companyID);
    
    /**
     * Loads all components identified by mailing id and company id.
     *
     * @param companyID
     *          The companyID for mailing component.
     * @param componentIds
     *          The ids of the mailing components.
     * @return Vector of MailingComponents.
     */
    List<MailingComponent> getMailingComponents(@VelocityCheck int companyID, int mailingID, Set<Integer> componentIds);
    
    List<MailingComponent> getMailingComponentsByType(@VelocityCheck int companyID, int mailingID, List<Integer> type);

    /**
     * Loads all components identified by mailing id, company id.
     * And type for these components should be MailingComponent.TYPE_ATTACHMENT or MailingComponent.TYPE_PERSONALIZED_ATTACHMENT.
     *
     * @param mailingID
     *          The id of the mailing for mailing component.
     * @param companyID
     *          The companyID for mailing component.
     * @return Vector of MailingComponents.
     */
	List<MailingComponent> getPreviewHeaderComponents(int mailingID, @VelocityCheck int companyID);

    void updateHostImage(int mailingID, @VelocityCheck int companyID, int componentID, byte[] imageBytes);
}
