/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.springframework.ws.soap.security.xwss.callback.DefaultTimestampValidator;

import com.sun.xml.wss.impl.callback.TimestampValidationCallback;

public class SpringDigestPasswordValidationCallbackHandler extends org.springframework.ws.soap.security.xwss.callback.SpringDigestPasswordValidationCallbackHandler {
	/**
	 * org.springframework.ws.soap.security.xwss.callback.SpringDigestPasswordValidationCallbackHandler missed return fix
	 */
	@Override
	protected void handleInternal(Callback callback) throws IOException,
			UnsupportedCallbackException {
        if (callback instanceof TimestampValidationCallback) {
            TimestampValidationCallback timestampCallback = (TimestampValidationCallback) callback;
            timestampCallback.setValidator(new DefaultTimestampValidator());
            return;
        }
        super.handleInternal(callback);
	}

}
