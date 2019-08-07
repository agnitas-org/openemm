/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.io.InputStream;
import java.util.List;
import java.util.function.BiConsumer;

import com.agnitas.service.exceptions.SftpServerConnectionException;
import com.agnitas.service.exceptions.SftpServerCredentialException;

public interface ComSftpService {
    /**
     * Connect to SFTP server and retrieve (pass file name and input stream to {@code consumer}) each file in directory {@code dir}. Use "**" pseudo-directory to
     * scan all the subdirectories.
     *
     * @param serverAndCredentials URL of the server (credentials required, base dir is optional)
     * @param privateKey private key for SSH protocol
     * @param dir a remote directory
     * @param filenameMask file mask (asterisk and vertical line are special characters)
     * @param consumer a consumer of the found files
     * @throws Exception
     */
    void retrieveFiles(String serverAndCredentials, String privateKey, String dir, String filenameMask, BiConsumer<String, InputStream> consumer) throws Exception;

    String getServer(String serverAndCredentials, String privateKey) throws SftpServerCredentialException, SftpServerConnectionException;

    List<String> scanForImages(String serverAndCredentials, String privateKey) throws SftpServerConnectionException, SftpServerCredentialException;
}
