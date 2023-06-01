/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.grid.grid.service;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.Admin;
import com.agnitas.emm.grid.grid.beans.ComGridDefaultContentElement;
import com.agnitas.emm.grid.grid.beans.ComGridDivContainer;
import com.agnitas.emm.grid.grid.beans.ComGridPlaceholder;
import com.agnitas.emm.grid.grid.beans.ContainerBulkDeleteResult;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.ParsingException;

import net.sf.json.JSONObject;

public interface ComGridDivContainerService {
    ComGridDivContainer getDivContainer(int id, @VelocityCheck int companyId);

    ComGridDivContainer getDivContainer(int id, @VelocityCheck int companyId, boolean includeThumbnail);

    ServiceResult<ComGridDivContainer> getDivContainerForDeletion(Admin admin, int id);

    boolean exists(Admin admin, int id);

    /**
     * See {@link com.agnitas.emm.grid.grid.dao.ComGridPlaceholderDao#getPlaceholdersForDivContainer(int, int)}.
     */
    List<ComGridPlaceholder> getPlaceholders(int id, @VelocityCheck int companyId);

    List<ComGridPlaceholder> getOrderedPlaceholders(int containerId, int companyId);

    /**
     * Validate and store the div-container and custom placeholders that its body (see {@link ComGridDivContainer#getBody()}) contains.
     * Validation fails when:
     * - name uniqueness violated (there's another stored div-container that has the same name);
     * - body markup (see {@link ComGridDivContainer#getBody()}) is invalid (see {@link #getPlaceholders(String, java.util.Locale)});
     * - div-container is in use (see {@link #isInUse(int)}) AND existing custom placeholders have been changed/removed or TOC markup presence toggled.
     * @throws Exception
     */
    SimpleServiceResult save(Admin admin, ComGridDivContainer container) throws Exception;

    /**
     * Get paginated list of div containers for list view (overview page) including usage data (see {@link com.agnitas.emm.grid.grid.beans.ComGridDivContainerUsages}.
     * @param companyId an identifier of the current company.
     * @param searchStr a search pattern.
     * @param sortCriterion a column name for sorting.
     * @param sortAscending a sorting order (ascending ({@code true}) or descending ({@code false})).
     * @param pageNumber a 1-based page index.
     * @param pageSize rows count per a page.
     * @return a paginated list of div containers.
     */
    PaginatedListImpl<ComGridDivContainer> getDivContainersOverview(@VelocityCheck int companyId, String searchStr, String sortCriterion, boolean sortAscending, int pageNumber, int pageSize);

    void deleteDivContainer(@VelocityCheck int companyId, int containerId);

    void completelyDeleteContainersForDeletedTemplate(@VelocityCheck int companyId, int templateId);

    Set<Integer> selectDeletedInRecycledChildren(@VelocityCheck int companyId, int templateId);

    boolean isInUse(int containerId);
    
    void generateDivContainerThumbnailsForTemplate(int templateId, boolean updateExisting, @VelocityCheck int companyId, String sessionId, boolean isUseThread);

    void generateDivContainerThumbnails(Admin admin, String sessionId, int containerId, boolean updateExisting, boolean isUseThread);

    /**
     * Check whether a div container uses (see {@link com.agnitas.emm.grid.grid.beans.ComGridDivContainer#getIsThumbnailCustom()})
     * a custom user-defined thumbnail (also referred to as an "avatar") or the one generated out of a div container's content.
     *
     * @param companyId an identifier of a company that owns referenced div container.
     * @param containerId an identifier of a div container to check.
     * @return {@code true} if a div container uses custom thumbnail or {@code false} if it uses generated thumbnail.
     */
    boolean isThumbnailCustom(@VelocityCheck int companyId, int containerId);

    List<ComGridDivContainer> getDivContainersByIds(@VelocityCheck int companyId, Collection<Integer> ids);

    List<ComGridDefaultContentElement> getContentForDivContainerPlaceholders(int containerId, @VelocityCheck int companyId);

    Map<Integer, String> getDefaultContentMap(Admin admin, int containerId);

    Optional<byte[]> getThumbnail(Admin admin, int containerId, int templateId);

    void saveThumbnail(@VelocityCheck int companyId, int containerId, byte[] thumbnail) throws Exception;

    void saveThumbnail(@VelocityCheck int companyId, int containerId, int templateId, byte[] thumbnail) throws Exception;

    Map<Integer, String> getImageNamesMap(int divContainerId, @VelocityCheck int companyId);

    boolean isDivContainerNameInUse(String name, @VelocityCheck int companyId);

    /**
     * See {@link com.agnitas.emm.grid.grid.dao.ComGridDivContainerToTemplateDao#getTemplatesHavingDivContainerAvailable(int, int)}.
     */
    List<Integer> getTemplatesHavingDivContainerAvailable(@VelocityCheck int companyId, int containerId);

    /**
     * See {@link com.agnitas.emm.grid.grid.dao.ComGridDivContainerToTemplateDao#getTemplatesHavingDivContainerAvailable(int, int, boolean)}.
     */
    List<Integer> getTemplatesHavingDivContainerAvailable(@VelocityCheck int companyId, int containerId, boolean isHavingThumbnails);

    /**
     * See {@link com.agnitas.emm.grid.grid.dao.ComGridDivContainerToTemplateDao#getAvailableDivContainerIds(int, int)}.
     */
    List<Integer> getAvailableForTemplate(@VelocityCheck int companyId, int templateId);

    /**
     * See {@link com.agnitas.emm.grid.grid.dao.ComGridDivContainerToTemplateDao#getAvailableDivContainerIds(int, int, boolean)}.
     */
    List<Integer> getAvailableForTemplate(@VelocityCheck int companyId, int templateId, boolean isHavingThumbnails);

    /**
     * Make all the active (not hidden) div containers available for a grid template.
     *
     * @param companyId an identifier of a company that owns referenced grid template.
     * @param templateId an identifier of a grid template to make div containers available for.
     */
    void makeActiveAvailableForTemplate(@VelocityCheck int companyId, int templateId);

    /**
     * Make a div container {@code divContainerId} available for a grid template {@code templateId}.
     *
     * @param companyId an identifier of a company that owns referenced div container and grid template.
     * @param templateId an identifier of a grid template to make div container available for.
     * @param divContainerId an identifier of a div container to be made available for a grid template.
     */
    void makeAvailableForTemplate(@VelocityCheck int companyId, int templateId, int divContainerId);

    /**
     * Make a div containers {@code divContainerIds} available for a grid template {@code templateId}.
     *
     * @param companyId an identifier of a company that owns referenced div containers and grid template.
     * @param templateId an identifier of a grid template to make div containers available for.
     * @param divContainerIds a list of a div container identifiers to be made available for a grid template.
     */
    void makeAvailableForTemplate(@VelocityCheck int companyId, int templateId, List<Integer> divContainerIds);

    /**
     * Validate an HTML code ignoring the div-container markup.
     *
     * @param admin a current admin.
     * @param code an HTML code of a div-container to be validated.
     * @return {@link com.agnitas.service.SimpleServiceResult} instance that indicates validation result (and errors if validation failed).
     */
    SimpleServiceResult validateHtml(Admin admin, String code);

    SimpleServiceResult checkDeprecatedTags(int companyId, String html);

    boolean validateNameUniqueness(Admin admin, String newName, int id);

    /**
     * Parse a div-container's template and retrieve all the custom (user-defined) placeholders.
     * Provides complete validation of the markup.
     *
     * @param template a div-container content to parse.
     * @param locale a locale to be used to translate error messages (when thrown).
     * @return an object representation of custom placeholders present in div-container's markup.
     * @throws ParsingException if markup is invalid.
     */
    List<ComGridPlaceholder> getPlaceholders(String template, Locale locale) throws ParsingException;

    /**
     * Represent a div-container, its markup (placeholders) and default content as json object.
     *
     * @param companyId an identifier of a current user's company.
     * @param containerId an identifier of a div-container.
     * @return a json object representing div-container or {@code null}.
     */
    JSONObject asJson(@VelocityCheck int companyId, int containerId);

    /**
     * Represent a div-container, its markup (placeholders) and default content as json string.
     *
     * @param companyId an identifier of a current user's company.
     * @param containerId an identifier of a div-container.
     * @return a json string representing div-container or {@code null}.
     */
    String asJsonString(@VelocityCheck int companyId, int containerId);

    boolean setActiveness(Admin admin, Map<Integer, Boolean> changeMap, List<UserAction> userActions);

    ContainerBulkDeleteResult bulkDelete(Admin admin, List<Integer> ids, List<UserAction> userActions);

    void clonePlaceholderContent(Admin admin, int targetId, int originId);

    String getPreviewStyles(Admin admin, int templateId);

    /**
     * See {@link GridTemplateGenerationService#generateContainerPreview(Admin, ComGridDivContainer)}.
     */
    String generatePreview(Admin admin, ComGridDivContainer container);

    String getMediaPoolImageSrcPattern(Admin admin);

    void saveDefaultContent(Admin admin, int containerId, Map<Integer, String> contentMap);

    void removeDivContainersSamples(@VelocityCheck int companyId);
}
