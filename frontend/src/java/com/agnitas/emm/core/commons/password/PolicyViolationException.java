/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.commons.password;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class PolicyViolationException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = 1974471927137059247L;

	/** (Unmodifiable) set of policy violation indicators. */
	private final Set<PolicyViolation> violations;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param violations set of policy violation indicators
	 */
	public PolicyViolationException(final Set<PolicyViolation> violations) {
		super(String.format("Policy violations: %s", violations));
		
		this.violations = Collections.unmodifiableSet(new HashSet<>(violations));
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param violations set of policy violation indicators
	 */
	public PolicyViolationException(final PolicyViolation... violations) {
		this(new HashSet<>(Arrays.asList(violations)));
	}

	/**
	 * Returns an unmodifiable set of policy violation indicators.
	 * 
	 * @return set of policy violation indicators
	 */
	public final Set<PolicyViolation> getViolations() {
		return this.violations;
	}

}
