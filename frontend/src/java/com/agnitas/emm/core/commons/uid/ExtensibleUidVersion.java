/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid;

import java.util.NoSuchElementException;
import java.util.Objects;

public enum ExtensibleUidVersion {

	/*
	 * These UID version are not longer support:
	 * 
	 * LEGACY_UID(0): 		Legacy UID. This version cannot be referenced by version number anymore. Version 0 now denotes "highest available UID version".
	 * XUID_WITH_MD5(1):	Version of XUID using MD5.
	 */

	/** Version of XUID using SHA-512. */
	XUID_WITH_SHA512(2),
	
	/** Version of UID with bit field using SHA-512- */
	UID_WITH_BITFIELD_USING_SHA512(3),
	
	/** Version of UID with company ID. */
	V4_WITH_COMPANY_ID(4),
	
	/** Version of UID with agnostic data storage */
	V5_AGNOSTIC(5);
	
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
	
	/*
	 * Do not use anymore!
	 * 
	 * The enabledUidVersion=0 for latest version is very dangerous.
	 * It is very likely, that code using the newest (and untested)
	 * UID version is deployed before database is configures correctly.
	 */
	@Deprecated(forRemoval = true)
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
	
	public final boolean isVersionCode(final int versionCodeToCheck) {
		return this.versionCode == versionCodeToCheck;
	}
	
	public final boolean isNewerThan(final int otherVersionCode) {
		return this.versionCode > otherVersionCode;
	}
	
	public final boolean isNewerThan(final ExtensibleUidVersion version) {
		return version == null || isNewerThan(version.versionCode);
	}
	
	public final boolean isOlderThan(final Number otherVersionCode) {
		if (Objects.isNull(otherVersionCode)) {
			return false;
		}

		return this.versionCode < otherVersionCode.intValue();
	}
}
