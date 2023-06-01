/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.beans;

import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.Arrays;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.logon.service.UnexpectedLogonStateException;

public class LogonStateBundle {
	
	/** The logger. */
	private static final Logger LOGGER = LogManager.getLogger(LogonStateBundle.class);
	
    private LogonState state;
    private Admin admin;
    private String hostId;
    private byte[] totpSharedSecret;

    public LogonStateBundle(LogonState state) {
        this.state = Objects.requireNonNull(state);
    }

    /**
     * Sets bundle to pending state. No admin or host ID set.
     */
    public final void toPendingState() {
    	setState(LogonState.PENDING);
    	setAdmin(null);
    	setHostId(null);
    }
    
    public final void toTotpState(final Admin newAdmin) {
    	setAdmin(Objects.requireNonNull(newAdmin, "Admin is null"));
    	setState(LogonState.TOTP);
    }
    
    public final void toNewTotpSecretState(final byte[] newTotpSharedSecret) {
    	this.totpSharedSecret = Arrays.copyOf(newTotpSharedSecret, newTotpSharedSecret.length);
    	
    	setState(LogonState.NEW_TOTP_SECRET);
    }

    public final void toAuthenticationState() {
    	this.totpSharedSecret = null;
    	setState(LogonState.HOST_AUTHENTICATION);
    }
   
    public final void toMaintainPasswordState() {
    	requireLogonState(LogonState.HOST_AUTHENTICATION, LogonState.HOST_AUTHENTICATION_SECURITY_CODE);
    
        setState(LogonState.MAINTAIN_PASSWORD);
    }
   
    public final void toAuthenticateHostSecurityCodeState(final String newHostId) {
    	requireLogonState(LogonState.HOST_AUTHENTICATION);
       	setHostId(Objects.requireNonNull(newHostId, "Host ID is null"));
    	setState(LogonState.HOST_AUTHENTICATION_SECURITY_CODE);
    }
    
    public final void toPasswordChangeState() {
    	requireLogonState(LogonState.MAINTAIN_PASSWORD);
    	
    	if (admin.isSupervisor()) {
    		setState(LogonState.CHANGE_SUPERVISOR_PASSWORD);
    	} else {
    		setState(LogonState.CHANGE_ADMIN_PASSWORD);
    	}
    }
    
    public final void toCompleteState() {
    	if (admin == null) {
            throw new UnexpectedLogonStateException("admin == null");
    	}
    	
    	setState(LogonState.COMPLETE);
    }
    
    public final void toCompleteState(final Admin newAdmin) {
    	if (newAdmin == null) {
            throw new UnexpectedLogonStateException("admin == null");
    	}
    	
    	setAdmin(newAdmin);
    	setState(LogonState.COMPLETE);
    }
    
    public final Admin toCompleteState(final HttpServletRequest request) {
        requireLogonState(LogonState.COMPLETE);

        // Create new session, drop all the temporary data.
        final HttpSession oldSession = request.getSession();
        oldSession.invalidate();
        final HttpSession newSession = request.getSession();

        if (LOGGER.isInfoEnabled()) {
        	LOGGER.info(String.format("Switching session ID from %s to %s", oldSession.getId(), newSession.getId()));
        }

        return admin;
    	
    }
    
    public final byte[] getNewTotpSharedSecret() {
    	return this.totpSharedSecret == null
    			? null
    			: Arrays.copyOf(this.totpSharedSecret, this.totpSharedSecret.length);
    }
   
    public LogonState getState() {
        return state;
    }
    
    private void setState(LogonState state) {
        this.state = Objects.requireNonNull(state);
    }

    public Admin getAdmin() {
        return admin;
    }

    private void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public String getHostId() {
        return hostId;
    }

    private void setHostId(String hostId) {
        this.hostId = hostId;
    }
    
    public final boolean isInState(final LogonState logonState) {
    	return getState() == logonState;
    }
    
    public final void requireLogonState(final LogonState... states) {
        if (ArrayUtils.isEmpty(states)) {
            throw new IllegalArgumentException("states.length must be > 0");
        }

        for (LogonState st : states) {
            if (st == this.getState()) {
                return;
            }
        }
        
        // Encountered unexpected login state.
        final String msg = String.format(
        		"Unexpected logon state: %s. Expected: %s. (hostId: `%s`, admin: `%s`)",
        		this.getState(), 
        		StringUtils.join(states, " or "), 
        		this.getHostId(),
        		getAdminUsername());
        
        // try-catch to get the stack trace
        try {
        	throw new UnexpectedLogonStateException(msg);
        } catch(final UnexpectedLogonStateException e) {
	        LOGGER.error(msg, e);
	        
	        // Re-throw exception
	        throw e;
        }
        
    }

    private String getAdminUsername() {
    	return admin == null
    			? "?"
    			: StringUtils.defaultString(admin.getUsername(), "?");
    }
}
