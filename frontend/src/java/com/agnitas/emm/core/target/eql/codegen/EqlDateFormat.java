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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

/**
 * Database-independent representation of date formats.
 */
public class EqlDateFormat implements Iterable<EqlDateFormat.DateFragment> {
	
	/**
	 * Fragments of a date.
	 */
	public enum DateFragment {
		/** Fragment representing a year value. */
		YEAR("YYYY", true, "^\\d{4}", "^\\d{1,4}"),

		/** Fragment representing a month value. */
		MONTH("MM", true, "^\\d{2}", "^\\d{1,2}"),

		/** Fragment representing a day value. */
		DAY("DD", true, "^\\d{2}", "^\\d{1,2}"),

		/** Fragments representing different separators. */
		PERIOD(".", false, "^\\."),
		HYPHEN("-", false, "^\\-"),
		UNDERSCORE("_", false, "^_");

		/** Pattern for fragment used in EQL. */
		private final String pattern;
		private final boolean isDigitFragment;
		private final Pattern regex;
		private final Pattern regexLenient;

		/**
		 * Creates a new enum item. 
		 * 
		 * @param pattern EQL-pattern for fragment
		 * @param isDigitFragment whether or not the fragment stands for digits (year, month or day).
		 */
		DateFragment(String pattern, boolean isDigitFragment, String regex, String regexLenient) {
			this.pattern = pattern;
			this.isDigitFragment = isDigitFragment;
			this.regex = Pattern.compile(regex);

			if (regexLenient == null) {
				this.regexLenient = this.regex;
			} else {
				this.regexLenient = Pattern.compile(regexLenient);
			}
		}

		DateFragment(String pattern, boolean isDigitFragment, String regex) {
			this(pattern, isDigitFragment, regex, null);
		}

		/**
		 * Returns the EQL pattern for the date fragment.
		 * 
		 * @return EQL pattern
		 */
		public String pattern() {
			return this.pattern;
		}

		public boolean isDigitFragment() {
			return isDigitFragment;
		}

		private Pattern getRegex(boolean isLenient) {
			if (isLenient) {
				return regexLenient;
			} else {
				return regex;
			}
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

	public String normalizeValue(String value) throws EqlDateValueFormatException {
		if (StringUtils.isBlank(value)) {
			throw new EqlDateValueFormatException(toString(), value);
		}

		StringBuilder builder = new StringBuilder();
		String part = value;
		boolean isPreviousDigits = false;

		for (int i = 0; i < fragments.size(); i++) {
			DateFragment fragment = fragments.get(i);
			boolean isLenient;

			if (fragment.isDigitFragment()) {
				isLenient = true;

				if (isPreviousDigits) {
					isLenient = false;
				} else if (i + 1 < fragments.size() && fragments.get(i + 1).isDigitFragment()) {
					isLenient = false;
				}
			} else {
				isLenient = false;
			}

			Matcher matcher = fragment.getRegex(isLenient).matcher(part);
			if (matcher.find()) {
				String lexeme = matcher.group();

				if (fragment.isDigitFragment()) {
					builder.append(StringUtils.leftPad(lexeme, fragment.pattern().length(), '0'));
				} else {
					builder.append(lexeme);
				}

				part = part.substring(lexeme.length());
			} else {
				throw new EqlDateValueFormatException(toString(), value);
			}

			isPreviousDigits = fragment.isDigitFragment();
		}

		if (part.length() > 0) {
			throw new EqlDateValueFormatException(toString(), value);
		}

		return builder.toString();
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

	@Override
	public String toString() {
		return this.fragments.stream().map(DateFragment::pattern)
				.collect(Collectors.joining());
	}
}
