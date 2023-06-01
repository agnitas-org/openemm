/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.web;

import org.agnitas.web.StrutsActionBase;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Presentation layer related utility methods.
 */
// TODO delete after GWUA-4798 has been successfully tested
public final class MailingViewHelper {
	
	/**
	 * Returns the view URL for given mailing ID.
	 * 
	 * @param mailingId mailing ID
	 * 
	 * @return view URL
	 */
	public static final String mailingBaseViewUrl(int mailingId) {
        return UriComponentsBuilder.fromPath("/mailingbase.do")
                .queryParam("action", StrutsActionBase.ACTION_VIEW)
                .queryParam("mailingID", mailingId)
                .queryParam("init", true)
                .toUriString();
	}

}
