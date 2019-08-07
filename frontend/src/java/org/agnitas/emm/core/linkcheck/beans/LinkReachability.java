/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.linkcheck.beans;

import java.util.Objects;

/**
 * Result of availability check of an URL.
 */
public class LinkReachability {

	/**
	 * Enumeration of possible availability states.
	 */
	public static enum Reachability {
		
		/** URL target available. */
		OK,
		
		/** Destination not found. */
		NOT_FOUND,
		
		/** Connection timed out. */
		TIMED_OUT
	}
	
	/** Checked URL. */
	private final String url;
	
	/** Availability result. */
	private final Reachability reachability;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param url checked URL
	 * @param reachability availability result
	 */
	public LinkReachability(final String url, final Reachability reachability) {
		this.url = Objects.requireNonNull(url, "No url specified");
		this.reachability = Objects.requireNonNull(reachability, "No reachability specified");
	}
	
	/**
	 * Returns checked URL.
	 * 
	 * @return checked URL
	 */
	public String getUrl() {
		return this.url;
	}
	
	/**
	 * Returns the availability result for checked URL.
	 * 
	 * @return availability result
	 */
	public Reachability getReachability() {
		return this.reachability;
	}
}
