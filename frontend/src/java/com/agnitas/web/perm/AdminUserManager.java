/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.perm;

import static org.agnitas.util.AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import com.agnitas.beans.ComAdmin;

@Component
@SessionScope
public class AdminUserManager {

    private ComAdmin admin;

    public AdminUserManager(HttpSession session){
        this.admin = (ComAdmin) session.getAttribute(SESSION_CONTEXT_KEYNAME_ADMIN);
    }

    public int getCompanyId(){
        return admin.getCompanyID();
    }
}
