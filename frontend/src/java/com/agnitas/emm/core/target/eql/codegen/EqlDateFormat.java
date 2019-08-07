/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Database-independent representation of date formats.
 */
public class EqlDateFormat implements Iterable<EqlDateFormat.DateFragment> {
	
	/** 
	 * Fragments of a date.
	 */
	public enum DateFragment {
		/** Fragment representing a year value. */
		YEAR("YYYY"),

		/** Fragment representing a month value. */
		MONTH("MM"),

		/** Fragment representing a day value. */
		DAY("DD");
		
		/** Pattern for fragment used in EQL. */
		private final String pattern;
		
		/**
		 * Creates a new enum item. 
		 * 
		 * @param pattern EQL-pattern for fragment
		 */
		DateFragment(String pattern) {
			this.pattern = pattern;
		}
		
		/**
		 * Returns the EQL pattern for the date fragment.
		 * 
		 * @return EQL pattern
		 */
		public String pattern() {
			return this.pattern;
		}
		
	}
	
	/** List of date fragments as specified in EQL date format. */
	private final List<DateFragment> fragments;
	
	/**
	 * Creates a new instance with given date fragments. The order of the
	 * date fragments is the same as in EQL code.
	 * 
	 * @param fragments list of date fragments
	 */
	private EqlDateFormat(final List<DateFragment> fragments) {
		this.fragments = Collections.unmodifiableList(fragments);
	}
	
	/**
	 * Parses a given EQL date format string.
	 * 
	 * @param spec EQL date format string
	 * 
	 * @return {@link EqlDateFormat}
	 * 
	 * @throws InvalidEqlDateFormatException on errors parsing date format string.
	 */
	public static EqlDateFormat parse(final String spec) throws InvalidEqlDateFormatException {
		if(spec == null) {
			return null;
		}
		
		String remnant = spec;
		
		List<DateFragment> list = new Vector<>();
		boolean found;
		
		// This code relies on the fact, that the format patterns are prefix-free
		while(remnant.length() > 0) {
			found = false;
			
			for(DateFragment fragment : DateFragment.values()) {
				if(remnant.startsWith(fragment.pattern())) {
					found = true;
					list.add(fragment);
					remnant = remnant.substring(fragment.pattern().length());
				}
			}
			
			if(!found) {
				throw new InvalidEqlDateFormatException("Date pattern '" + spec + "' contains invalid formatting pattern");
			}
		}
		
		return new EqlDateFormat(list);
	}
	
	/**
	 * Returns the number of fragments contained in the {@link EqlDateFormat}.
	 * 
	 * @return number of date fragments
	 */
	public int numFragments() {
		return this.fragments.size();
	}
	
	/**
	 * Returns the i-th date fragment.
	 * 
	 * @param i index of date fragment (starting at 0)
	 * 
	 * @return date fragment
	 */
	public DateFragment fragment(final int i) {
		return this.fragments.get(i);
	}
	
	/**
	 * Returns an iterator over the date fragments.
	 */
	@Override
	public Iterator<DateFragment> iterator() {
		return this.fragments.iterator();
	}
}
