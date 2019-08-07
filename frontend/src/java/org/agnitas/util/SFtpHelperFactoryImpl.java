/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.File;

public class SFtpHelperFactoryImpl implements SFtpHelperFactory {
    @Override
    public SFtpHelper createSFtpHelper(String fileServerAndAuthConfigString) throws Exception {
        return new SFtpHelper(fileServerAndAuthConfigString);
    }

    @Override
    public SFtpHelper createSFtpHelper(String host, String user, String password) {
        return new SFtpHelper(host, user, password);
    }

    @Override
    public SFtpHelper createSFtpHelper(String host, String user, String password, int port) {
        return new SFtpHelper(host, user, password, port);
    }

    @Override
    public SFtpHelper createSFtpHelper(String host, String user, File privateSshKeyFile, byte[] privateSshKeyPassphrase) {
        return new SFtpHelper(host, user, privateSshKeyFile, privateSshKeyPassphrase);
    }

    @Override
    public SFtpHelper createSFtpHelper(String host, String user, File privateSshKeyFile, byte[] privateSshKeyPassphrase, int port) {
        return new SFtpHelper(host, user, privateSshKeyFile, privateSshKeyPassphrase, port);
    }

    @Override
    public SFtpHelper createSFtpHelper(String host, String user, String privateSshKeyData, byte[] privateSshKeyPassphrase) {
        return new SFtpHelper(host, user, privateSshKeyData, privateSshKeyPassphrase);
    }

    @Override
    public SFtpHelper createSFtpHelper(String host, String user, String privateSshKeyData, byte[] privateSshKeyPassphrase, int port) {
        return new SFtpHelper(host, user, privateSshKeyData, privateSshKeyPassphrase, port);
    }
}
