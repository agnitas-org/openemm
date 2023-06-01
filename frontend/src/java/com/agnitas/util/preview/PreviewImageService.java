/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.preview;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.Admin;

public interface PreviewImageService {
    int VIEWPORT_WIDTH = 800;
    int VIEWPORT_HEIGHT = 600;
    int IMAGE_LOADING_TIMEOUT = 15;

    /**
     * Generate and store a thumbnail for the mailing.
     *
     * @param admin
     * @param sessionId
     * @param mailingId an identifier of the mailing to generate a thumbnail for.
     * @param async whether ({@code true}) or not ({@code false}) run in separate thread.
     */
    void generateMailingPreview(Admin admin, String sessionId, int mailingId, boolean async);
       
    /**
     * Generate and store a thumbnail for the div child.
     *
     * @param companyId  an identifier of the company.
     * @param sessionId
     * @param divChildId an identifier of the div child to generate a thumbnail for.
     * @param async      whether ({@code true}) or not ({@code false}) run in separate thread.
     */
    default void generateThumbnailForDivChild(@VelocityCheck int companyId, String sessionId, int divChildId, boolean async) {
        //default implementation
    }
    
    default void generateDivContainerThumbnailsForTemplate(@VelocityCheck int companyId, String sessionId, int templateId, boolean updateExisting, boolean async) {
        //default implementation
    }
        
    default void generateDivContainerThumbnails(Admin admin, String sessionId, int containerId, boolean updateExisting, boolean async) {
        //default implementation
    }
    
    default void generateThumbnailForGridTemplate(@VelocityCheck int companyId, String sessionId, int templateId, boolean async) {
        //default implementation
    }
}
