/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.io.InputStream;
import java.util.List;
import java.util.function.BiConsumer;

import org.agnitas.util.AgnUtils;
import org.agnitas.util.SFtpHelper;
import org.agnitas.util.SFtpHelperFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.service.ComSftpService;
import com.agnitas.service.exceptions.SftpServerConnectionException;
import com.agnitas.service.exceptions.SftpServerCredentialException;
import com.agnitas.util.ImageUtils;

public class ComSftpServiceImpl implements ComSftpService {

    protected SFtpHelperFactory sFtpHelperFactory;

    @Required
    public void setsFtpHelperFactory(SFtpHelperFactory sFtpHelperFactory) {
        this.sFtpHelperFactory = sFtpHelperFactory;
    }

    @Override
    public void retrieveFiles(String serverAndCredentials, String privateKey, String dir, String filenameMask, BiConsumer<String, InputStream> consumer) throws Exception {
        String pattern = AgnUtils.getPatternForWildcard(filenameMask);
        String path = StringUtils.trimToEmpty(dir);

        if (dir.endsWith("/") && dir.length() > 1) {
            path = path.substring(0, dir.length() - 1);
        }

        try (SFtpHelper connection = open(serverAndCredentials, privateKey)) {
            List<String> entries;

            if ("**".equals(path)) {
                entries = connection.scanForFiles(pattern, true);
                path = ".";
            } else {
                if (StringUtils.isEmpty(path)) {
                    path = ".";
                } else if (!connection.directoryExists(path)) {
                    throw new Exception("Directory not found on SFTP server");
                }
                entries = connection.scanForFiles(path, pattern, false);
            }

            for (String name : entries) {
                try (InputStream stream = connection.get(path + "/" + name)) {
                    consumer.accept(name, stream);
                } catch (Exception e) {
                    throw new Exception("Cannot read file from SFTP server", e);
                }
            }
        }
    }

    protected SFtpHelper open(String serverAndCredentials, String privateKey) throws Exception {
    	SFtpHelper connection = null;
        try {
            connection = sFtpHelperFactory.createSFtpHelper(serverAndCredentials);

            if (StringUtils.isNotBlank(privateKey)) {
                connection.setPrivateSshKeyData(privateKey);
            }

            connection.setAllowUnknownHostKeys(true);
            connection.connect();

            return connection;
        } catch (Exception e) {
        	if (connection != null) {
        		connection.close();
        		connection = null;
        	}
            throw new Exception("Cannot connect to SFTP server", e);
        }
    }

    @Override
    public String getServer(String serverAndCredentials, String privateKey) throws SftpServerCredentialException, SftpServerConnectionException {
        if (StringUtils.isEmpty(serverAndCredentials)) {
            throw new SftpServerCredentialException();
        }

        try (SFtpHelper sFtpHelper = open(serverAndCredentials, privateKey)) {
            String server = sFtpHelper.toString();
            server += !server.endsWith("/") ? "/" : "";
            return server;
        } catch (Exception e) {
            throw new SftpServerConnectionException();
        }
    }

    @Override
    public List<String> scanForImages(String serverAndCredentials, String privateKey) throws SftpServerConnectionException, SftpServerCredentialException {
        if (StringUtils.isEmpty(serverAndCredentials)) {
            throw new SftpServerCredentialException();
        }

        try (SFtpHelper sFtpHelper = open(serverAndCredentials, privateKey)) {
            String extensionMask = ImageUtils.getFileMask("*", "*");
            String patternForWildcard = AgnUtils.getPatternForWildcard(extensionMask);
            return sFtpHelper.scanForFiles(patternForWildcard, true);
        } catch (Exception e) {
            throw new SftpServerConnectionException();
        }
    }
}
