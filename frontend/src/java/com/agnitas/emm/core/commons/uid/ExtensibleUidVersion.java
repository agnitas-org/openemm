/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid;

import java.util.NoSuchElementException;
import java.util.Objects;

public enum ExtensibleUidVersion {

	/** Version of the legacy UID. <b>Important note:</b> This version cannot be referenced by version number anymore. Version 0 now denotes "highest available UID version". */
	@Deprecated
	LEGACY_UID(0),
	
	/** Version of XUID using MD5. */
	XUID_WITH_MD5(1),

	/** Version of XUID using SHA-512. */
	XUID_WITH_SHA512(2),
	
	/** Version of UID with bit field using SHA-512- */
	UID_WITH_BITFIELD_USING_SHA512(3);
	
	private final int versionCode;
	
	ExtensibleUidVersion(final int versionCode) {
		this.versionCode = versionCode;
	}
	
	public static final ExtensibleUidVersion fromVersionNumber(final int version) throws NoSuchElementException {
		for(final ExtensibleUidVersion uidVersion : values()) {
			if(uidVersion.versionCode == version) {
				return uidVersion;
			}
		}
		
		throw new NoSuchElementException(String.format("Invalid UID version %d", version));
	}
	
	public static ExtensibleUidVersion latest() {
		return newerOf(values());
	}

	public static ExtensibleUidVersion newerOf(final ExtensibleUidVersion...extensibleUidVersions) {
		ExtensibleUidVersion latest = null;
		
		for(final ExtensibleUidVersion version : extensibleUidVersions) {
			if(version != null) {
				if(latest == null || latest.versionCode > version.versionCode) {
					latest = version;
				}
			}
		}
		
		return latest;
	}
	
	public final int getVersionCode() {
		return this.versionCode;
	}
	
	public final boolean isNewerThan(final int versionCode) {
		return this.versionCode > versionCode;
	}
	
	public final boolean isNewerThan(final ExtensibleUidVersion version) {
		return version == null || isNewerThan(version.versionCode);
	}
	
	public final boolean isOlderThan(final Number versionCode) {
		if (Objects.isNull(versionCode)) {
			return false;
		}

		return this.versionCode < versionCode.intValue();
	}

	public final boolean isOlderThan(final ExtensibleUidVersion version) {
		return !Objects.isNull(version) || !isOlderThan(version.versionCode);
	}

}
