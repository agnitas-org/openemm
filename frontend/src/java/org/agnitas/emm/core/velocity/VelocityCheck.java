/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Runtime annotation for controlling various checks in Velocity.
 */
@Retention( RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Deprecated // After completion of EMM-8360, this class can be removed without replacement
public @interface VelocityCheck {
	
	/**
	 * Returns the type of checks defined by the annotation.
	 * Default value is {@link CheckType#COMPANY_CONTEXT}.
	 * 
	 * @return type of checks
	 */
	CheckType[] value() default CheckType.COMPANY_CONTEXT;
}
