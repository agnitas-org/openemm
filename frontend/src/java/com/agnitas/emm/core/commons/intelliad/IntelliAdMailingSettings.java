/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.intelliad;

/**
 * Contains the IntelliAd settings for a specific mailing.
 */
public class IntelliAdMailingSettings {
	
	/** Flag, if IntelliAd is enabled. */
	private final boolean enabled;
	
	/** The IntelliAd tracking string. */
	private final String trackingString;
	
	/**
	 * Creates new IntelliAd settings.
	 * 
	 * @param enabled true, if IntelliAd is enabled
	 * @param trackingString the tracking string
	 */
	public IntelliAdMailingSettings( boolean enabled, String trackingString) {
		this.enabled = enabled;
		this.trackingString = trackingString;
	}
	
	/**
	 * Returns whether IntelliAd is enabled or not.
	 * 
	 * @return true if IntelliAd is enabled
	 */
	public boolean isIntelliAdEnabled() {
		return this.enabled;
	}
	
	/**
	 * Returns the IntelliAd tracking string
	 * @return IntelliAd tracking string
	 */
	public String getTrackingString() {
		return this.trackingString;
	}
}
