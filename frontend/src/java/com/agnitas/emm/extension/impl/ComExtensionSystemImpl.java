/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.extension.impl;

import org.agnitas.emm.extension.PluginInstaller;
import org.agnitas.emm.extension.dao.PluginDao;
import org.agnitas.emm.extension.impl.ExtensionSystemConfiguration;
import org.agnitas.emm.extension.impl.ExtensionSystemImpl;
import org.agnitas.emm.extension.impl.JspRestoreUtil;

public class ComExtensionSystemImpl extends ExtensionSystemImpl {

	private static final String CORE_PLUGIN_ID = "emm_core";
	
	public ComExtensionSystemImpl(ExtensionSystemConfiguration configuration, JspRestoreUtil jspRestoreUtil, PluginInstaller pluginInstaller, PluginDao pluginDao) {
		super(configuration, jspRestoreUtil, pluginInstaller, pluginDao);
	}


	@Override
	protected String getCorePluginId() {
		return CORE_PLUGIN_ID;
	}

	
}
