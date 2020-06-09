/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;

public class Version implements Comparable<Version> {
	private int majorVersion;
	private int minorVersion = 0;
	private int microVersion = 0;
	private int hotfixVersion = 0;
	
	private boolean legacyFormat = false;
	
	public Version(String versionString) throws Exception {
		if (StringUtils.isBlank(versionString)) {
			throw new Exception("Invalid version sign: '" + versionString + "'");
		}
			
		String[] versionParts = versionString.split("\\.");
		if (versionParts.length == 1) {
			try {
				majorVersion = Integer.parseInt(versionParts[0]);
			} catch (NumberFormatException e) {
				throw new Exception("Invalid version sign: '" + versionString + "'");
			}
		} else if (versionParts.length == 2) {
			try {
				majorVersion = Integer.parseInt(versionParts[0]);
				minorVersion = Integer.parseInt(versionParts[1]);
			} catch (NumberFormatException e) {
				throw new Exception("Invalid version sign: '" + versionString + "'");
			}
		} else if (versionParts.length == 3) {
			try {
				majorVersion = Integer.parseInt(versionParts[0]);
				minorVersion = Integer.parseInt(versionParts[1]);
				
				versionParts[2] = versionParts[2].toLowerCase();
				if (versionParts[2].contains("-hf")) {
					legacyFormat = true;
					microVersion = Integer.parseInt(versionParts[2].substring(0, versionParts[2].indexOf("-hf")));
					hotfixVersion = Integer.parseInt(versionParts[2].substring(versionParts[2].indexOf("-hf") + 3));
				} else {
					microVersion = Integer.parseInt(versionParts[2]);
				}
			} catch (NumberFormatException e) {
				throw new Exception("Invalid version sign: '" + versionString + "'");
			}
		} else if (versionParts.length == 4) {
			try {
				majorVersion = Integer.parseInt(versionParts[0]);
				minorVersion = Integer.parseInt(versionParts[1]);
				microVersion = Integer.parseInt(versionParts[2]);
				hotfixVersion = Integer.parseInt(versionParts[3]);
			} catch (NumberFormatException e) {
				throw new Exception("Invalid version sign: '" + versionString + "'");
			}
		} else {
			throw new Exception("Invalid version sign: '" + versionString + "'");
		}
	}

	public int getMajorVersion() {
		return majorVersion;
	}

	public int getMinorVersion() {
		return minorVersion;
	}

	public int getMicroVersion() {
		return microVersion;
	}

	public int getHotfixVersion() {
		return hotfixVersion;
	}
	
	@Override
	public String toString() {
		StringBuilder versionFormatBuilder = new StringBuilder();
		versionFormatBuilder.append(majorVersion);
		versionFormatBuilder.append(".");
		versionFormatBuilder.append(new DecimalFormat("00").format(minorVersion));
		versionFormatBuilder.append(".");
		versionFormatBuilder.append(new DecimalFormat("000").format(microVersion));
		
		if (hotfixVersion > 0) {
			if (legacyFormat) {
				versionFormatBuilder.append("-hf");
				versionFormatBuilder.append(hotfixVersion);
			} else {
				versionFormatBuilder.append(".");
				versionFormatBuilder.append(new DecimalFormat("000").format(hotfixVersion));
			}
		}
		return versionFormatBuilder.toString();
	}

	@Override
	public int compareTo(Version otherVersion) {
		if (otherVersion == null || majorVersion > otherVersion.getMajorVersion()) {
			return 1;
		} else if (majorVersion == otherVersion.getMajorVersion()) {
			if (minorVersion > otherVersion.getMinorVersion()) {
				return 1;
			} else if (minorVersion == otherVersion.getMinorVersion()) {
				if (microVersion > otherVersion.getMicroVersion()) {
					return 1;
				} else if (microVersion == otherVersion.getMicroVersion()) {
					if (hotfixVersion > otherVersion.getHotfixVersion()) {
						return 1;
					} else if (hotfixVersion == otherVersion.getHotfixVersion()) {
						return 0;
					} else {
						return -1;
					}
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}
}
