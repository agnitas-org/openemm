/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.File;

public interface SFtpHelperFactory {

    /**
     * @param fileServerAndAuthConfigString like "[username[:password]@]server[:port][;hostKeyFingerprint][/baseDirectory]". The hostKeyFingerprint should be given without ":"-Characters
     * @return {@link SFtpHelper}
     * @throws Exception
     */
    SFtpHelper createSFtpHelper(String fileServerAndAuthConfigString) throws Exception;

    SFtpHelper createSFtpHelper(String host, String user, String password);

    SFtpHelper createSFtpHelper(String host, String user, String password, int port);

    SFtpHelper createSFtpHelper(String host, String user, File privateSshKeyFile, byte[] privateSshKeyPassphrase);

    SFtpHelper createSFtpHelper(String host, String user, File privateSshKeyFile, byte[] privateSshKeyPassphrase, int port);

    SFtpHelper createSFtpHelper(String host, String user, String privateSshKeyData, byte[] privateSshKeyPassphrase);

    SFtpHelper createSFtpHelper(String host, String user, String privateSshKeyData, byte[] privateSshKeyPassphrase, int port);

}
