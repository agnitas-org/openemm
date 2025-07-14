/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.preview.service;

import com.agnitas.preview.Page;

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
	 */
	Page renderPreview(int mailingID, int customerID);
	
	/**
	 * Renders the HTML preview for given mailing with default preview options.
	 * 
	 * @param mailingID mailing ID
	 * @param customerID customer ID
	 * 
	 * @return mailing preview
	 */
	String renderHtmlPreview(int mailingID, int customerID);

	/**
	 * Renders the HTML preview for given mailing using given preview options.
	 * 
	 * @param mailingID mailing ID
	 * @param customerID customer ID
	 * @param options preview options
	 * 
	 * @return mailing preview
	 */
	String renderHtmlPreview(int mailingID, int customerID, HtmlPreviewOptions options);

	/**
	 * Renders the text preview for given mailing.
	 * 
	 * @param mailingID mailing ID
	 * @param customerID customer ID
	 * 
	 * @return mailing preview
	 */
	String renderTextPreview(int mailingID, int customerID);

	String renderSmsPreview(int mailingId, int customerID);
	/**
	 * Renders the preview for given content fragment based on given mailing.
	 * 
	 * @param mailingID mailing ID
	 * @param customerID customer ID
	 * 
	 * @return preview of content fragment
	 */
	String renderPreviewFor(int mailingID, int customerID, String fragment);

}
