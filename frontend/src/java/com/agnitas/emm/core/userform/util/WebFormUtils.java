/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.util;

import static org.agnitas.util.AgnUtils.getNormalizedRdirDomain;


public final class WebFormUtils {

	private static final String IMAGE_SRC_PATTERN = "{rdir-domain}formImage/{license-id}/{company-id}/{form-id}/{name}";
    private static final String IMAGE_SRC_PATTERN_NO_CACHE = "{rdir-domain}formImage/nc/{license-id}/{company-id}/{form-id}/{name}";
    private static final String IMAGE_THUMBNAIL_PATTERN = "{rdir-domain}formImage/thb/{company-id}/{form-id}/{name}";

    public static final String getImageSrcPattern(final String rdirDomain, final int licenceId, final int companyId, final int formId, final boolean noCache) {
        if (noCache) {
			return getImageSrcPattern(IMAGE_SRC_PATTERN_NO_CACHE, rdirDomain, licenceId, companyId, formId);
		} else {
			return getImageSrcPattern(IMAGE_SRC_PATTERN, rdirDomain, licenceId, companyId, formId);
		}
    }

    public static final String getImageThumbnailPattern(final String rdirDomain, final int licenceId, final int companyId, final int formId) {
       return getImageSrcPattern(IMAGE_THUMBNAIL_PATTERN, rdirDomain, licenceId, companyId, formId);
    }

	private static final String getImageSrcPattern(final String pattern, final String rdirDomain, final int licenceId, final int companyId, final int formId) {
		return pattern
				.replace("{form-id}", Integer.toString(formId))
                .replace("{license-id}", Integer.toString(licenceId))
                .replace("{rdir-domain}", getNormalizedRdirDomain(rdirDomain))
                .replace("{company-id}", Integer.toString(companyId));
    }
}
