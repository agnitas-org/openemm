/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

import java.util.HashSet;
import java.util.Set;

/**
 * Flags controlling EQL-to-X code generation.
 */
public class CodeGenerationFlags {
	
	public enum Flag {
		IGNORE_TRACKING_VETO
	}

	/**
	 * Default flags.
	 * 
	 * <ul>
	 *   <li>respects tracking veto</li>
	 * </ul>
	 */
	public static final CodeGenerationFlags DEFAULT_FLAGS = new CodeGenerationFlags();
	
	/** Set of flags */
	private final Set<Flag> flagSet;
	
	/**
	 * Creates the default settings.
	 * 
	 * @see #DEFAULT_FLAGS
	 */
	public CodeGenerationFlags() {
		this.flagSet = new HashSet<>();
	}

	private CodeGenerationFlags(final CodeGenerationFlags flags) {
		this.flagSet = new HashSet<>(flags.flagSet);
	}

	public final CodeGenerationFlags setFlag(final Flag flag) {
		final CodeGenerationFlags flags = new CodeGenerationFlags(this);
		flags.flagSet.add(flag);
		
		return flags;
	}

	public final CodeGenerationFlags clearFlag(final Flag flag) {
		final CodeGenerationFlags flags = new CodeGenerationFlags(this);
		flags.flagSet.remove(flag);
		
		return flags;
	}

	@Override
	public final String toString() {
		return this.flagSet.toString();
	}
	
	/**
	 * Returns <code>true</code> if tracking veto is to be ignored.
	 * 
	 * @return <code>true</code> if tracking veto is to be ignored
	 */
	public final boolean isIgnoreTrackingVeto() {
		return flagSet.contains(Flag.IGNORE_TRACKING_VETO);
	}
	
	
}
