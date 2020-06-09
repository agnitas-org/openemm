/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.ftp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public abstract class FtpUtil {

    private static final String FTP = "ftp";
    private static final String SFTP = "sftp";

    public static String getProtocolOrDefault(String uri, String defaultProtocol) {
        if (StringUtils.isBlank(uri)) {
            return defaultProtocol;
        }
        Pattern ftpUriPattern = Pattern.compile("((?<protocol>.+?):/+)?(?<host>.+)");
        Matcher matcher = ftpUriPattern.matcher(uri);
        return matcher.matches() && StringUtils.isNotBlank(matcher.group("protocol")) ? matcher.group("protocol") : defaultProtocol;
    }

    public static String getUriWithoutProtocol(String uri) {
        if (StringUtils.isBlank(uri)) {
            return StringUtils.EMPTY;
        }
        Pattern hostPattern = Pattern.compile("((?<protocol>.+?):/+)?(?<host>.+)");
        Matcher matcher = hostPattern.matcher(uri);
        return matcher.matches() && StringUtils.isNotBlank(matcher.group("host")) ? matcher.group("host") : uri;
    }

    public static String getUri(String host, String username, String password) {
        String protocol = getProtocolOrDefault(host, SFTP).toLowerCase();
        String hostWithoutProtocol = getUriWithoutProtocol(host);

        if (StringUtils.isBlank(password)) {
            if (StringUtils.isBlank(username)) {
                return String.format("%s://%s", protocol, hostWithoutProtocol);
            }
            return String.format("%s://%s@%s", protocol, username, hostWithoutProtocol);
        }

        return String.format("%s://%s:%s@%s", protocol, username, password, hostWithoutProtocol);
    }

    public static String getUsername(String ftpUri) {
        if (StringUtils.isBlank(ftpUri)) {
            return StringUtils.EMPTY;
        }

        String uriWithoutProtocol = getUriWithoutProtocol(ftpUri);
        FtpUriTypes uriType = FtpUriTypes.getByUri(uriWithoutProtocol);
        if (uriType == FtpUriTypes.JUST_HOST) {
            return StringUtils.EMPTY;
        }


        Pattern pattern = uriType.getPattern();
        Matcher matcher = pattern.matcher(uriWithoutProtocol);
        return matcher.matches() ? matcher.group("username") : StringUtils.EMPTY;
    }

    public static String getPassword(String ftpUri) {
        if (StringUtils.isBlank(ftpUri)) {
            return StringUtils.EMPTY;
        }

        String uriWithoutProtocol = getUriWithoutProtocol(ftpUri);
        FtpUriTypes uriType = FtpUriTypes.getByUri(uriWithoutProtocol);
        if (uriType != FtpUriTypes.HOST_WITH_ALL_CREDENTIAL) {
            return StringUtils.EMPTY;
        }

        Pattern pattern = uriType.getPattern();
        Matcher matcher = pattern.matcher(uriWithoutProtocol);

        return matcher.matches() ? matcher.group("password") : StringUtils.EMPTY;
    }

    public static String getHostBlock(String ftpUri) {
        if (StringUtils.isBlank(ftpUri)) {
            return StringUtils.EMPTY;
        }

        String protocol = getProtocolOrDefault(ftpUri, SFTP);
        String uriWithoutProtocol = getUriWithoutProtocol(ftpUri);
        FtpUriTypes uriType = FtpUriTypes.getByUri(uriWithoutProtocol);
        Pattern pattern = uriType.getPattern();
        Matcher matcher = pattern.matcher(uriWithoutProtocol);

        return matcher.matches() ? protocol + "://" + matcher.group("host") : StringUtils.EMPTY;
    }

    public static boolean hasFtpProtocol(String ftpUri) {
        return getProtocolOrDefault(ftpUri, SFTP).equalsIgnoreCase(FTP);
    }
}
