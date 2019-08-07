/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class WebserviceUserDetailService extends BaseDaoImpl implements UserDetailsService {
    /**
     * The logger.
     */
    private static final transient Logger logger = Logger.getLogger(WebserviceUserDetailService.class);

    private WebservicePasswordEncryptor webservicePasswordEncryptor;

    @Required
    public void setWebservicePasswordEncryptor(WebservicePasswordEncryptor webservicePasswordEncryptor) {
        this.webservicePasswordEncryptor = webservicePasswordEncryptor;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        String usersByUsernameQuery = "SELECT w.password_encrypted, w.company_id FROM webservice_user_tbl w, company_tbl c WHERE w.username = ? AND w.active = 1 AND w.company_id=c.company_id and c.status='active'";
        List<Map<String, Object>> result = select(logger, usersByUsernameQuery, username);
        if (result.size() == 0) {
            throw new UsernameNotFoundException("Username " + username + " not found");
        } else {
            Map<String, Object> userRow = result.get(0);
            String password;
            int companyID = ((Number) userRow.get("company_id")).intValue();
            String encryptedPasswordBase64 = (String) userRow.get("password_encrypted");
            // Decrypt the encrypted password
            try {
                password = webservicePasswordEncryptor.decrypt(username, encryptedPasswordBase64);
            } catch (Exception e) {
                throw new UsernameNotFoundException("Userpassword of user " + username + " cannot be decrypted");
            }
            Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            grantedAuthorities.add(new SimpleGrantedAuthority("USER_" + companyID));
            return new User(username, password, true, true, true, true, grantedAuthorities);
        }
    }
}
