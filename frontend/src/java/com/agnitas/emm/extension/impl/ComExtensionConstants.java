/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.extension.impl;

import org.agnitas.emm.extension.util.ExtensionConstants;

/**
 * EMM-specific constants for the extension system.
 */
public class ComExtensionConstants extends ExtensionConstants {
	
	/** Base directory for all BIRT-related files in the plugin ZIP. */
	public static final String PLUGIN_BIRT_ZIP_BASE = "birt/";
	
	/** Base directory for all BIRT-related files to be transferred to scriptlib directory in the installed ZIP. */
	public static final String PLUGIN_BIRT_INSTALLED_ZIP_SCRIPTLIB_BASE = "scriptlib/";

	/** Base directory for all BIRT report designs in the installed ZIP. */
	public static final String PLUGIN_BIRT_INSTALLED_ZIP_RPTDESIGN_BASE = "rptdesign/";
	
	/** Base directory, where the report designs will be installed on startup. */
	public static final String PLUGIN_BIRT_RPTDESIGN_BASE = "/plugins/";
	
	public static final String PLUGIN_BIRT_SCRIPTLIB_BASE = "/scriptlib/";

}
