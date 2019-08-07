/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid.parser.impl;

public class V2Sha512ComExtensibleUIDParserImpl extends V2ComExtensibleUIDParserImpl {

    private static final int MIN_GROUPS_LENGTH = 6;
    private static final int MAX_GROUPS_LENGTH = 7;
    private static final int SIGNATURE_LENGTH = 86;

    @Override
    protected boolean isValidBaseFormat(final String[] parts) {
        // UID string should contain 6 or 7 parts divided by point and the last part should contain 86 characters
        return (parts.length == MIN_GROUPS_LENGTH || parts.length == MAX_GROUPS_LENGTH)
                && parts[parts.length - 1].length() == SIGNATURE_LENGTH;
    }
}
