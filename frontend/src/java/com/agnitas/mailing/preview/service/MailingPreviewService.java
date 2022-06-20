/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.preview.service;

import org.agnitas.preview.Page;

/**
 * Service layer interface for generation of mailing previews.
 */
public interface MailingPreviewService {

	/**
	 * Renders the preview for given mailing.
	 * 
	 * @param mailingID mailing ID
	 * @param customerID customer ID
	 * 
	 * @return mailing preview
	 * 
	 * @throws Exception on errors rendering preview
	 */
	public Page renderPreview(final int mailingID, final int customerID) throws Exception;
	
	/**
	 * Renders the HTML preview for given mailing with default preview options.
	 * 
	 * @param mailingID mailing ID
	 * @param customerID customer ID
	 * 
	 * @return mailing preview
	 * 
	 * @throws Exception on errors rendering preview
	 */
	public String renderHtmlPreview(final int mailingID, final int customerID) throws Exception;

	/**
	 * Renders the HTML preview for given mailing using given preview options.
	 * 
	 * @param mailingID mailing ID
	 * @param customerID customer ID
	 * @param options preview options
	 * 
	 * @return mailing preview
	 * 
	 * @throws Exception on errors rendering preview
	 */
	public String renderHtmlPreview(final int mailingID, final int customerID, final HtmlPreviewOptions options) throws Exception;

	/**
	 * Renders the text preview for given mailing.
	 * 
	 * @param mailingID mailing ID
	 * @param customerID customer ID
	 * 
	 * @return mailing preview
	 * 
	 * @throws Exception on errors rendering preview
	 */
	public String renderTextPreview(final int mailingID, final int customerID) throws Exception;

	public String renderSmsPreview(final int mailingId, final int customerID) throws Exception;
	/**
	 * Renders the preview for given content fragment based on given mailing.
	 * 
	 * @param mailingID mailing ID
	 * @param customerID customer ID
	 * 
	 * @return preview of content fragment
	 * 
	 * @throws Exception on errors rendering preview
	 */
	public String renderPreviewFor(final int mailingID, final int customerID, final String fragment) throws Exception;

	
}
