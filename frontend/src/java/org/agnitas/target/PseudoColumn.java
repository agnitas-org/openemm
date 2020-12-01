/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.target;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Enumeration of pseudo columns (columns that can be used in target rules, but that does
 * not represent a profile field).
 */
public class PseudoColumn {
	/** 
	 * List of all registered pseudo columns. 
	 * 
	 * <b>Note: When subclassing load all subclasses on startup to initialize this
	 * collection properly!</b>
	 */
	private static final List<PseudoColumn> values = new ArrayList<>(); // This must be the first static field, otherwise you'll get NullPointerExceptions

	public static final PseudoColumn INTERVAL_MAILING = new PseudoColumn("interval_mailing_pseudo_column_name");
	public static final PseudoColumn CLICKED_IN_MAILING = new PseudoColumn("pseudo_column_mailing_clicked");
	public static final PseudoColumn CLICKED_SPECIFIC_LINK_IN_MAILING = new PseudoColumn("pseudo_column_mailing_clicked_on_specific_link");
	public static final PseudoColumn OPENED_MAILING = new PseudoColumn("pseudo_column_mailing_opened");
	public static final PseudoColumn RECEIVED_MAILING = new PseudoColumn("pseudo_column_mailing_received");

	/** Name of this pseudo column. */
	private final String name;
	
	
	PseudoColumn(final String name) {
		this.name = Objects.requireNonNull(name);
		
		PseudoColumn.values.add(this);
	}
	
	public final String getName() {
		return this.name;
	}
	
	public final boolean isThisPseudoColumn(final String otherName) {
		return this.name.equalsIgnoreCase(otherName);		// Must be equalsIgnoreCase(), because names are defined in differed cases
	}
	
	public static final PseudoColumn[] values() {
		return values.toArray(new PseudoColumn[values.size()]);
	}
	
	@Override
	public final String toString() {
		return getName();
	}
}
