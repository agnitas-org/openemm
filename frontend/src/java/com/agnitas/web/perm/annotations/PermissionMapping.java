/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.perm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to override action token prefix (to be used as a key in applicationContext-permissions.xml).
 *
 * Have a look at an example:
 * <code>
 *     @PermissionMapping("target-groups")
 *     public class TargetController {
 *         public String overview() {
 *              // ...
 *         }
 *     }
 * </code>
 *
 * An action token for an overview method will be "target-groups.overview" so a developer is required to define the following
 * entry in applicationContext-permissions.xml:
 * <code>
 *     <entry key="target-groups.overview" value="..."/>
 * </code>
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionMapping {
    String value();
}
