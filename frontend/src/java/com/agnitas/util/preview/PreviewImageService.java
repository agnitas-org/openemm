/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.preview;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComAdmin;

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
    void generateMailingPreview(ComAdmin admin, String sessionId, int mailingId, boolean async);
    
    /**
     * Generate and store a thumbnail for the div child.
     *
     * @param request
     * @param divChildId
     * @param async
     * @deprecated {@link #generateThumbnailForDivChild(int, String, int, boolean)}
     */
    @Deprecated
    default void generateThumbnailForDivChild(HttpServletRequest request, int divChildId, boolean async) {
        //default implementation
    }
    
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
    
    /**
     * Generate and store grid template-associated thumbnails (see {@link com.agnitas.emm.grid.grid.service.ComGridDivContainerService#saveThumbnail(int, int, int, byte[])}) for each
     * div container available (see {@link com.agnitas.emm.grid.grid.service.ComGridDivContainerService#makeAvailableForTemplate(int, int, int)} and
     * {@link com.agnitas.emm.grid.grid.service.ComGridDivContainerService#makeAvailableForTemplate(int, int, java.util.List)})
     * for the grid template referenced by {@code templateId}.
     * Use it to refresh related thumbnails when the grid template's styles are updated and to create missing thumbnails when
     * the grid template got new available div containers.
     *
     * @param request        current request object to retrieve current session information from.
     * @param templateId     an identifier of the grid template.
     * @param updateExisting do ({@code true}) or do not ({@code false}) re-generate and overwrite existing thumbnails (if any).
     * @param async          whether ({@code true}) or not ({@code false}) run in separate thread.
     */
    @Deprecated
    default void generateDivContainerThumbnailsForTemplate(HttpServletRequest request, int templateId, boolean updateExisting, boolean async) {
        //default implementation
    }
    
    default void generateDivContainerThumbnailsForTemplate(@VelocityCheck int companyId, String sessionId, int templateId, boolean updateExisting, boolean async) {
        //default implementation
    }
    
    /**
     * Generate and store a div container's standalone (grid template-independent) thumbnail (see {@link com.agnitas.emm.grid.grid.service.ComGridDivContainerService#saveThumbnail(int, int, byte[])} and
     * grid template-associated thumbnails (see {@link com.agnitas.emm.grid.grid.service.ComGridDivContainerService#saveThumbnail(int, int, int, byte[])}) for each grid template that
     * a referenced div container is available (see {@link com.agnitas.emm.grid.grid.service.ComGridDivContainerService#makeAvailableForTemplate(int, int, int)}) for.
     * Use it to refresh related thumbnails when the div container's body or default content is updated.
     *
     * @param request        current request object to retrieve current session information from.
     * @param containerId    an identifier of the div container to generate thumbnails for.
     * @param updateExisting do ({@code true}) or do not ({@code false}) re-generate and overwrite existing thumbnails (if any).
     *                       Doesn't affect standalone thumbnail generation and overwriting - it always goes on.
     * @param async          whether ({@code true}) or not ({@code false}) run in separate thread.
     */
    @Deprecated
    default void generateDivContainerThumbnails(HttpServletRequest request, int containerId, boolean updateExisting, boolean async) {
        //default implementation
    }
    
    default void generateDivContainerThumbnails(ComAdmin admin, String sessionId, int containerId, boolean updateExisting, boolean async) {
        //default implementation
    }
    
    /**
     * Generate and store thumbnail for a grid template referenced by {@code templateId}.
     *
     * @param request    current request object to retrieve current session information from.
     * @param templateId an identifier of a grid template to generate a thumbnail for.
     * @param async      whether ({@code true}) or not ({@code false}) run in separate thread.
     */
    @Deprecated
    default void generateThumbnailForGridTemplate(HttpServletRequest request, int templateId, boolean async) {
        //default implementation
    }
    
    default void generateThumbnailForGridTemplate(@VelocityCheck int companyId, String sessionId, int templateId, boolean async) {
        //default implementation
    }
}
