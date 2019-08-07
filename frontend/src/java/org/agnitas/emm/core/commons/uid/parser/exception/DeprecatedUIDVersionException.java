/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.uid.parser.exception;

import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUidVersion;

public class DeprecatedUIDVersionException extends UIDVersionException {

	private static final long serialVersionUID = 6314753665714256770L;
	private static final String MESSAGE_TEMPLATE = "Deprecated UID version. version: %d, encoded string: %s, decoded uid object: %s";

	private final ComExtensibleUID uid;

	public DeprecatedUIDVersionException(String message, ComExtensibleUID uid) {
		super(message + " decoded uid object: " + uid);
		this.uid = uid;
	}

	public DeprecatedUIDVersionException(final String uidString, final ComExtensibleUID uid, final ExtensibleUidVersion version) {
		super(String.format(MESSAGE_TEMPLATE, version.getVersionCode(), uidString, uid));
		this.uid = uid;
	}

	public final ComExtensibleUID getUID() {
		return this.uid;
	}
}
