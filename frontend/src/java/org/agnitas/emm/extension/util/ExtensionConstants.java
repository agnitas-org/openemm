/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.util;

import org.agnitas.emm.extension.ExtensionSystem;

/**
 * Collection of constants used by the extension system.
 */
public class ExtensionConstants {
	/** Name of the application scope attribute containing the only instance of the ExtensionSystem. */
	public static final String EXTENSION_SYSTEM_APPLICATION_SCOPE_ATTRIBUTE = ExtensionSystem.class.getCanonicalName();

	/** Name of the request parameter used by the ExtensionService to determinte the plugin to invoke. */
	public static final String FEATURE_REQUEST_PARAMETER = "feature";

	public static final String PLUGIN_NAME_MANIFEST_ATTRIBUTE = "plugin-name";

	public static final String PLUGIN_DESCRIPTION_MANIFEST_ATTRIBUTE = "plugin-description";
}
