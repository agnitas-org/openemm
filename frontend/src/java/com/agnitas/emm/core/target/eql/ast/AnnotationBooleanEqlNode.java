/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.ast;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.agnitas.emm.core.target.eql.ast.traversal.EqlNodeVisitor;
import com.agnitas.emm.core.target.eql.ast.traversal.TraversalUtil;
import com.agnitas.emm.core.target.eql.codegen.CodeLocation;
import com.agnitas.emm.core.target.eql.referencecollector.ReferenceCollector;

public final class AnnotationBooleanEqlNode extends AbstractBooleanEqlNode {
	
	public enum AnnotationType {
		PARENTHESIS
	}
	
	private AbstractBooleanEqlNode child;
	private final Set<AnnotationType> annotationsReadOnly;
	
	public AnnotationBooleanEqlNode(final AbstractBooleanEqlNode child, final Collection<AnnotationType> annotations) {
		this.child = child;
		this.annotationsReadOnly = annotations != null 
				? Collections.unmodifiableSet(new HashSet<>(annotations)) 
						: Collections.emptySet();
	}

	public AnnotationBooleanEqlNode(final AbstractBooleanEqlNode child, final AnnotationType... annotations) {
		this(child, Arrays.asList(annotations));
	}
	
	public final AbstractBooleanEqlNode getChild() {
		return this.child;
	}
	
	public final Set<AnnotationType> getAnnotations() {
		return this.annotationsReadOnly;
	}
	
	public final boolean hasAnnotation(final AnnotationType type) {
		return this.annotationsReadOnly.contains(type);
	}

	@Override
	public final CodeLocation getStartLocation() {
		return codeLocationFromEqlNode(this.child);
	}

	@Override
	public final String toString() {
		return "[" + annotationsReadOnly + ":" + this.child + "]";
	}

	@Override
	public final void collectReferencedItems(ReferenceCollector collector) {
		this.child.collectReferencedItems(collector);
	}

	@Override
	public final void traverse(final EqlNodeVisitor visitor) {
		visitor.enteredNode(this);
		TraversalUtil.traverse(child, visitor);
		visitor.leavingNode(this);
	}
}
