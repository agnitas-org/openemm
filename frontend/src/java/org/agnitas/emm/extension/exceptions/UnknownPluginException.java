/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.exceptions;

/**
 * Exception to signal an unknown plugin name.
 */
public class UnknownPluginException extends Exception {
	private static final long serialVersionUID = 6978260355617988893L;
	
	/** Name of unknown plugin. */
	private final String pluginName;
	
	/**
	 * Create a new UnknownPluginException.
	 * 
	 * @param pluginName name of unknown plugin
	 */
	public UnknownPluginException( String pluginName) {
		super( "unknown plugin: " + pluginName);
		
		this.pluginName = pluginName;
	}
	
	/**
	 * Returns the plugin name.
	 * 
	 * @return name of unknown plugin
	 */
	public String getPluginName() {
		return this.pluginName;
	}
}
