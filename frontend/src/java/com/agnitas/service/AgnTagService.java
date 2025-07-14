/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.agnitas.beans.TagDetails;
import com.agnitas.util.DynTagException;

import com.agnitas.beans.AgnTagDto;
import com.agnitas.beans.Admin;
import com.agnitas.beans.DynamicTag;

public interface AgnTagService {
    /**
     * A shortcut for {@link #getDynTags(String, AgnDynTagGroupResolver)} that doesn't resolve groups (zero value assigned).
     */
    List<DynamicTag> getDynTags(String content) throws DynTagException;

    /**
     * Collect dynamic tags information, ignore the rest of agn-tags.
     *
     * @param content a text content to parse.
     * @param resolver an instance of {@link AgnDynTagGroupResolver} to be used to translate group name into group identifier.
     * @return collection of entities representing found dynamic tags.
     * @throws DynTagException if parsing error occurred.
     */
    List<DynamicTag> getDynTags(String content, AgnDynTagGroupResolver resolver) throws DynTagException;

    List<String> getDynTagsNames(String content, AgnDynTagGroupResolver resolver);

    /**
     * Perform a search for all agn-tags (except dynamic tags) and filter them by given {@code predicate}.
     *
     * @param content a text content to parse and collect tags from.
     * @param predicate a predicate to be used to filter found tags before returning.
     * @return a list of found agn-tags (except dynamic tags) filtered by {@code predicate}.
     */
    List<TagDetails> collectTags(String content, Predicate<TagDetails> predicate);

    /**
     * A shortcut for {@link #resolveTags(String, boolean, AgnTagResolver)} with {@code recursive = true}.
     */
    String resolveTags(String content, AgnTagResolver resolver) throws Exception;

    /**
     * Perform substitution for all found agn-tags (except dynamic tags). This is recursive algorithm so if some tag
     * is replaced with a text containing another tags then all these tags will be resolved as well.
     * Attention: keep in mind that improper implementation of {@code resolveTag} may cause infinite recursion!
     *
     * @param content a text content to parse and resolve tags (if any) within.
     * @param recursive whether ({@code true}) or not ({@code false}) a content produced by {@code resolver} should be scanned for tags again.
     * @param resolver an instance of {@link AgnTagResolver} to be used to translate found tags (replace with text string).
     * @return translated content.
     * @throws Exception if parsing error occurred.
     */
    String resolveTags(String content, boolean recursive, AgnTagResolver resolver) throws Exception;

    /**
     * Perform substitution for all found agn-tags (except dynamic tags). An optimized (due to caching) combination of
     * {@link #resolveTags(String, AgnTagResolver)} and {@link #resolve(TagDetails, int, int, int, int)} methods.
     *
     * @param content a text content to parse and resolve tags (if any) within.
     * @param companyId an identifier of current user's company.
     * @param mailingId an identifier of a mailing or 0.
     * @param mailingListId an identifier of a mailing list or 0.
     * @param customerId an identifier of a recipient or 0.
     * @return translated content.
     * @throws Exception if parsing error occurred.
     */
    String resolveTags(String content, int companyId, int mailingId, int mailingListId, int customerId) throws Exception;

    /**
     * Generate a text content that a {@code tag} should be replaced with.
     *
     * @param tag a tag to be replaced.
     * @param companyId an identifier of current user's company.
     * @param mailingId an identifier of a mailing or 0.
     * @param mailingListId an identifier of a mailing list or 0.
     * @param customerId an identifier of a recipient or 0.
     * @return a text content that a referenced tag produces.
     */
    String resolve(TagDetails tag, int companyId, int mailingId, int mailingListId, int customerId);

    List<AgnTagDto> getSupportedAgnTags(Admin admin);

    boolean isContainsThirdPartyText(String text);

    Set<String> parseDeprecatedTagNamesFromString(String stringWithTag, int companyId);

    Set<String> parseTagNamesFromString(String stringWithTag);
}
