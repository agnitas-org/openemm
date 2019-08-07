/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class I18NFactory {
	private static final transient Logger logger = Logger.getLogger( I18NFactory.class);
	
	private final ClassLoader classLoader;
	private final String bundleName;
	
	public I18NFactory( ClassLoader classLoader, String bundleName) {
		this.classLoader = classLoader;
		this.bundleName = bundleName;
	}

	public ResourceBundle getMessages( String i18nPrefix) throws IOException {
		String fullName = (i18nPrefix.equals( "") ? bundleName : bundleName + "_" + i18nPrefix) + ".properties";
		
		try (InputStream stream = classLoader.getResourceAsStream(fullName)) {
			if (stream != null) {
	    		try {
	        		try {
	        			PropertyResourceBundle bundle = new PropertyResourceBundle( stream);
	        			return bundle;
	        		} finally {				
	        			stream.close();
	        		}
	    		} catch( IOException e) {
	    			logger.error( "Error reading i18n bundle '" + fullName + "'", e);
	    			
	    			return null;
	    		}
			} else {
				logger.info( "No i18n bundle '" + fullName + "'");
				
				return null;
			}
		}
	}
}
