/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.velocity;

import java.io.Writer;
import java.util.Map;

/**
 * Interface hiding all the Velocity boilerplate code.
 */
public interface VelocityWrapper {
	/**
	 * Evaluates a script. The given IDs are used to create a log tag in case of writing a log message. 
	 * For company ID the value given to constructor is used.
	 * 
	 * @param params identifier and its values made available to Velocity
	 * @param template the Velocity script
	 * @param writer writer for the result
	 * @param formId form ID that contains the script
	 * @param actionId action ID that contains the script
	 * 
	 * @return {@link VelocityResult} containing status of the executing
	 */
	VelocityResult evaluate(Map<String, Object> params, String template, Writer writer, int formId, int actionId);
	
	/**
	 * Evaluates a script with logging.
	 * 
	 * @param params identifier and its values made available to Velocity
	 * @param template the Velocity script
	 * @param writer writer for the result
	 * @param logTag log tag
	 * 
	 * @return {@link VelocityResult} containing status of the executing
	 */
	VelocityResult evaluate(Map<String, Object> params, String template, Writer writer, String logTag);

	/**
	 * Returns the company ID in which the Velocity engine is running.
	 * 
	 * @return company ID
	 */
	int getCompanyId();
}
