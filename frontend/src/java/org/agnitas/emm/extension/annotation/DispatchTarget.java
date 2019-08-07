/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.agnitas.emm.extension.AnnotatedDispatchingEmmFeatureExtension;

/**
 * Annotation used by AnnotatedDispatchingEmmFeatureExtension to
 * mark methods as targets for the dispatching logic.
 * 
 * An annotated method is invoked, when the value of the request 
 * parameter &quot;method&quot; matches the &quot;attribute&quot; of the
 * annotation.
 *
 * @see AnnotatedDispatchingEmmFeatureExtension
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DispatchTarget {
	/** Dispatch name. */
	String name();
}
