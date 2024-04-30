/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.filter.responseheaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.FilterConfig;

/**
 * Simple header configuration source reading a text file.
 * 
 * This configuration source supports these init parameters:
 * <ul>
 *   <li><b>source.config-file</b> name of configuration file relative to deployment directory of the web application</li>
 * </ul> 
 * 
 * Example for a configuration file:
 * 
 * <pre>
#
#
# HTTP header sent in response
#
#
# ("[" app-types "]") <header-name> ("[" <options> "]") <header-value>
#
# app-types: comma-separated list of applications (see org.agnitas.util.ServerCommand.Server for values; compares case-insensitive)
#           - if given: header is only emitted for given application types
#           - if not given: header is emitted for all application types
#         
# options:	- overwrite
#

# Forces browser to use https:// instead of http://
			Strict-Transport-Security		[overwrite]			max-age=15768000; includeSubDomains

# Browser does not sent "Referrer" header
			Referrer-Policy					[overwrite]			no-referrer

# Prevents browsers from loading EMM pages inside a &lt;frame> or &lt;iframe>
# Keep set to 'self' otherwise preview won't work
# Exclude header for RDIRs otherwise, web forms may not be embeddable in iframes
[emm] 		Content-Security-Policy			[overwrite]			frame-ancestors 'self';

</pre>
 */
public final class SimpleFileHeaderConfigSource implements HeaderConfigSource {

	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(SimpleFileHeaderConfigSource.class);
	
	/** Pattern for a single configuration. */
	private static final Pattern CONFIG_PATTERN = Pattern.compile("\\s*(?:\\[(.*?)\\]\\s+)?([^\\s]+)\\s+(?:\\[(.*?)\\]\\s+)?(.*?)\\s*$");

	/** Name of parameter to configure name of configuration file. */
	public static final String CONFIG_FILE_PARAMETER_NAME = "source.config-file";

	/** Configuration file. */
	private File configFile;
	
	@Override
	public final void init(final FilterConfig filterConfig) {
		final String filename = filterConfig.getServletContext().getRealPath(filterConfig.getInitParameter(CONFIG_FILE_PARAMETER_NAME));
		this.configFile = new File(filename);
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Config file is %s", filename));
		}
		
		if(!this.configFile.exists()) {
			throw new RuntimeException(String.format("Configuration file %s does not exist!", filename));
		}
	}

	@Override
	public final List<HttpHeaderConfig> loadHeaderConfiguration() throws Exception {
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info(String.format("Loading HTTP header configuration from %s", configFile.getAbsolutePath()));
		}
		
		try(final FileReader reader = new FileReader(this.configFile)) {
			try(final BufferedReader in = new BufferedReader(reader)) {
				final List<HttpHeaderConfig> list = new ArrayList<>();
				
				String line;
				int lineCount = 0;
				while((line = in.readLine()) != null) {
					lineCount++;
					line = line.trim();
					
					if(!"".equals(line) && !line.startsWith("#")) {
						final Matcher matcher = CONFIG_PATTERN.matcher(line);
						
						if(!matcher.matches()) {
							throw new Exception(String.format("Malformed configuration in line %d of %s", lineCount, this.configFile.getAbsolutePath()));
						}
						
						final String applicationTypesList = matcher.group(1) != null ? matcher.group(1).trim() : "";
						final String name = matcher.group(2);
						final String opts = matcher.group(3) != null ? matcher.group(3) : "";
						final String value = matcher.group(4);
						
						final List<String> applicationTypes = "".equals(applicationTypesList) 
								? new ArrayList<>()
								: Arrays.asList(applicationTypesList.split("\\s*,\\s*"));
												
						if(LOGGER.isDebugEnabled()) {
							LOGGER.debug(String.format("HEADER: name='%s', value='%s', opts='%s', application-types='%s'", name, value, opts, applicationTypes));
						}
						
						list.add(new HttpHeaderConfig(name, value, "overwrite".equals(opts), applicationTypes));
					}
				}
				
				return list;
			}
		}
	}

}
