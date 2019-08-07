/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.service;

import java.util.function.Supplier;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.logon.beans.LogonState;

public interface Logon {
    boolean is(LogonState state);
    void require(LogonState... states);
    ComAdmin getAdmin();
    String getHostId();
    String getHostId(Supplier<String> generateHostId);
    String getCookieHostId();
    void initialize(Supplier<String> generateHostId);
    void authenticate(ComAdmin admin);
    void authenticateHost();
    void authenticateHost(String hostId);
    void expectHostAuthenticationCode();
    void expectPasswordChange();
    void complete(ComAdmin admin);
    void complete();
    ComAdmin end();
}
