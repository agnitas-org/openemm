/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.web;

import java.io.File;

import jakarta.servlet.ServletContext;

/**
 * Utility class to retrieve paths and filenames relative to web application.
 */
public final class WebAppFileUtil {

	/**
	 * Returns the path for WEB-INF directory.
	 * 
	 * @param context {@link ServletContext}
	 * 
	 * @return path for WEB-INF
	 */
	public static final String getWebInfDirectoryPath(final ServletContext context) {
		return getWebInfDirectoryFile(context).getAbsolutePath();
	}
	
	/**
	 * Returns the {@link File} for WEB-INF directory.
	 * 
	 * @param context {@link ServletContext}
	 * 
	 * @return {@link File} for WEB-INF
	 */
	public static final File getWebInfDirectoryFile(final ServletContext context) {
		final File realPathFile = new File(context.getRealPath("/"));
		return new File(realPathFile, "WEB-INF");
	}
	
}
