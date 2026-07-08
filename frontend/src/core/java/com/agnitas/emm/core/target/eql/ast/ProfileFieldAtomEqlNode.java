/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.ast;

import com.agnitas.emm.core.target.eql.ast.traversal.EqlNodeVisitor;
import com.agnitas.emm.core.target.eql.codegen.CodeLocation;
import com.agnitas.emm.core.target.eql.referencecollector.ReferenceCollector;

/**
 * Node in syntax tree representing a profile field name.
 */
public class ProfileFieldAtomEqlNode extends AbstractAtomEqlNode {

	private final CodeLocation startLocation;
	
	/** Name of profile field. */
	private final String name;
	
	/**
	 * Creates new node with name of profile field.
	 * 
	 * @param name name of profile field
	 */
	public ProfileFieldAtomEqlNode(String name, CodeLocation startLocation) {
		this.name = name;
		this.startLocation = startLocation;
	}
	
	@Override
	public CodeLocation getStartLocation() {
		return this.startLocation;
	}
	
	/**
	 * Returns the name of the profile field.
	 * 
	 * @return name of profile field
	 */
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return "(*profilefield* " + name + ")";
	}
	
	@Override	
	public void collectReferencedItems(ReferenceCollector collector) {
		collector.addProfileFieldReference(name);
	}

	@Override
	public final void traverse(final EqlNodeVisitor visitor) {
		visitor.enteredNode(this);
		visitor.leavingNode(this);
	}
}
