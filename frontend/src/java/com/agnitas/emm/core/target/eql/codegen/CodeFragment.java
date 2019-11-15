/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

import java.util.HashSet;
import java.util.Set;

/**
 * Fragment of generated code. A code fragment consists of the generated code and a type, the code fragement
 * evaluates to. For example, this type can be {@link DataType#BOOLEAN}, if the code of the fragment will 
 * "return" boolean values or {@link DataType#NUMERIC} for arithmetic expressions.
 */
public final class CodeFragment {

	/** Type, this code fragment evaluates to. */
	private final DataType evaluatesToType;
	
	/** Generated code of this fragment. */
	private final String code;
	
	/** Set of reference table names, currently not used for code generation. */
	private final Set<String> unusedReferenceTables;
	
	/**
	 * Creates a new code fragment.
	 * 
	 * @param code generated code
	 * @param evaluatesToType result type of the code fragment
	 * @param unusedReferenceTables set of reference table names currently not used for code generation
	 */
	public CodeFragment(final String code, final DataType evaluatesToType, final Set<String> unusedReferenceTables) {
		this.evaluatesToType = evaluatesToType;
		this.code = code;
		this.unusedReferenceTables = unusedReferenceTables != null ? unusedReferenceTables : new HashSet<>();
	}
	
	/**
	 * Returns the type the code fragment evaluates to.
	 * 
	 * @return result type of code fragment
	 */
	public final DataType evaluatesToType() {
		return this.evaluatesToType;
	}
	
	/**
	 * Returns <code>true</code> if {@link CodeFragment} evaluates to given {@link DataType}.
	 * 
	 * @param expectedType expected {@link DataType}
	 *  
	 * @return <code>true</code> if code evalutes to given {@link DataType}, otherwise false
	 */
	public final boolean evaluatesToType(final DataType expectedType) {
		return this.evaluatesToType.equals(expectedType);
	}
	
	/**
	 * Returns generated code.
	 * 
	 * @return generated code
	 */
	public final String getCode() {
		return this.code;
	}
	
	/**
	 * Returns the set of names of reference tables, referenced but currently not used for
	 * code generator.
	 * 
	 * @return set of names of unsused reference tables
	 */
	public final Set<String> getUnusedReferenceTables() {
		return this.unusedReferenceTables;
	}
}
