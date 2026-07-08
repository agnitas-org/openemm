/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

/**
 * Package containing the login component.
 * 
 * The login process is separated into these steps:
 * <ol>
 *   <li><i>User authentication</i> with user name and password</li>
 *   <li><i>Authentication of device</i> (host authentication, two-way authentication)</i>
 *   <li><i>Optional or mandatory change of password</i></li>
 *   <li><i>Proceeding to users start page</i></li>
 * </ol>
 * 
 * To protect the system against passing over steps (like host authentication)
 * a token (&quot;workflow token&quot;) is used and validated in each step.
 *   
 */
package com.agnitas.emm.core.logon;

