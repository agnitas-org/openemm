/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.ftp;

import java.util.regex.Pattern;

public enum FtpUriTypes {

    JUST_HOST(Pattern.compile("(?<host>.+)")),
    HOST_WITH_USER_NAME(Pattern.compile("(?<username>.+)@(?<host>.+)")),
    HOST_WITH_ALL_CREDENTIAL(Pattern.compile("(?<username>.+?):(?<password>.+)@(?<host>.+)"));

    private Pattern pattern;

    FtpUriTypes(Pattern pattern) {
        this.pattern = pattern;
    }

    public static FtpUriTypes getByUri(String uri) {
        FtpUriTypes[] uriTypes = values();
        for (int i = uriTypes.length - 1; i >= 0; i--) {
            if (uriTypes[i].getPattern().matcher(uri).matches()) {
                return uriTypes[i];
            }
        }
        return null;
    }

    public Pattern getPattern() {
        return pattern;
    }
}
