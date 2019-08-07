/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.service.impl;

import java.util.Objects;
import java.util.function.Supplier;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.util.WebUtils;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.logon.LogonUtils;
import com.agnitas.emm.core.logon.beans.LogonState;
import com.agnitas.emm.core.logon.beans.LogonStateBundle;
import com.agnitas.emm.core.logon.service.Logon;
import com.agnitas.emm.core.logon.service.UnexpectedLogonStateException;

public class LogonImpl implements Logon {
    private static Logger logger = Logger.getLogger(LogonImpl.class);

    private static final int MAX_COOKIE_AGE = 60 * 60 * 24 * 90;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private boolean useSecureCookies;
    private LogonStateBundle bundle;
    private final String hostIdCookieName;

    public LogonImpl(HttpServletRequest request, HttpServletResponse response, boolean useSecureCookies, final String hostIdCookieName) {
        this.request = request;
        this.response = response;
        this.useSecureCookies = useSecureCookies;
        this.bundle = LogonUtils.getBundle(request, true);
        this.hostIdCookieName = Objects.requireNonNull(hostIdCookieName, "Name of Host ID cookie is null");
    }

    @Override
    public boolean is(LogonState state) {
        return state == bundle.getState();
    }

    @Override
    public void require(LogonState... states) {
        if (ArrayUtils.isEmpty(states)) {
            throw new IllegalArgumentException("states.length must be > 0");
        }

        for (LogonState state : states) {
            if (state == bundle.getState()) {
                return;
            }
        }

        throw new UnexpectedLogonStateException(String.format(
                "Unexpected logon state: %s. Expected: %s. (hostId: `%s`, admin: `%s`)",
                bundle.getState(), StringUtils.join(states, " or "), bundle.getHostId(), StringUtils.defaultString(getUsername(), "?")
        ));
    }

    @Override
    public ComAdmin getAdmin() {
        return bundle.getAdmin();
    }

    @Override
    public String getHostId() {
        return bundle.getHostId();
    }

    @Override
    public String getHostId(Supplier<String> generateHostId) {
        String hostId = bundle.getHostId();

        if (hostId == null) {
            hostId = getCookieHostId();

            if (hostId == null) {
                hostId = generateHostId.get();
                setCookieHostId(hostId);
                bundle.setHostId(hostId);
            }
        }

        return hostId;
    }

    @Override
    public String getCookieHostId() {
        Cookie cookie = WebUtils.getCookie(request, hostIdCookieName);

        if (cookie == null) {
            if (logger.isInfoEnabled()) {
                logger.info("No host ID cookie found for host " + request.getRemoteHost() + " (" + request.getRemoteAddr() + ")");
            }

            return null;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Found host ID '" + cookie.getValue() + "' found for host " + request.getRemoteHost() + " (" + request.getRemoteAddr() + ")");
        }

        return cookie.getValue();
    }

    @Override
    public void initialize(Supplier<String> generateHostId) {
        String hostId = getCookieHostId();

        if (hostId == null) {
            hostId = generateHostId.get();
            setCookieHostId(hostId);
        }

        bundle.setState(LogonState.PENDING);
        bundle.setAdmin(null);
        bundle.setHostId(hostId);
    }

    @Override
    public void authenticate(ComAdmin admin) {
        if (admin == null) {
            throw new IllegalArgumentException("admin == null");
        }

        bundle.setState(LogonState.HOST_AUTHENTICATION);
        bundle.setAdmin(admin);
    }

    @Override
    public void authenticateHost() {
        require(LogonState.HOST_AUTHENTICATION, LogonState.HOST_AUTHENTICATION_SECURITY_CODE);
        bundle.setState(LogonState.MAINTAIN_PASSWORD);
    }

    @Override
    public void authenticateHost(String hostId) {
        if (hostId == null) {
            throw new IllegalArgumentException("hostId == null");
        }

        require(LogonState.HOST_AUTHENTICATION, LogonState.HOST_AUTHENTICATION_SECURITY_CODE);
        bundle.setState(LogonState.MAINTAIN_PASSWORD);
        bundle.setHostId(hostId);
    }

    @Override
    public void expectHostAuthenticationCode() {
        require(LogonState.HOST_AUTHENTICATION);
        bundle.setState(LogonState.HOST_AUTHENTICATION_SECURITY_CODE);
    }

    @Override
    public void expectPasswordChange() {
        require(LogonState.MAINTAIN_PASSWORD);

        if (getAdmin().isSupervisor()) {
            bundle.setState(LogonState.CHANGE_SUPERVISOR_PASSWORD);
        } else {
            bundle.setState(LogonState.CHANGE_ADMIN_PASSWORD);
        }
    }

    @Override
    public void complete(ComAdmin admin) {
        if (admin == null) {
            throw new IllegalArgumentException("admin == null");
        }

        bundle.setAdmin(admin);
        bundle.setState(LogonState.COMPLETE);
    }

    @Override
    public void complete() {
        if (bundle.getAdmin() == null) {
            throw new UnexpectedLogonStateException("admin == null");
        }

        bundle.setState(LogonState.COMPLETE);
    }

    @Override
    public ComAdmin end() {
        require(LogonState.COMPLETE);

        // Create new session, drop all the temporary data.
        HttpSession oldSession = request.getSession();
        oldSession.invalidate();
        HttpSession newSession = request.getSession();

        if (logger.isInfoEnabled()) {
            logger.info("Switching session ID from " + oldSession.getId() + " to " + newSession.getId());
        }

        return bundle.getAdmin();
    }

    private void setCookieHostId(String hostId) {
        Cookie cookie = new Cookie(hostIdCookieName, hostId);

        cookie.setMaxAge(MAX_COOKIE_AGE);
        cookie.setSecure(useSecureCookies);
		cookie.setHttpOnly(true);

        response.addCookie(cookie);
    }

    private String getUsername() {
        ComAdmin admin = bundle.getAdmin();

        if (admin == null) {
            return null;
        }

        return admin.getUsername();
    }
}
