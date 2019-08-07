/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agnitas.emm.core.commons.packages.AbstractPackageInclusionChecker;
import org.apache.log4j.Logger;

/**
 * Implementation of {@link AbstractPackageInclusionChecker} including all org.agnitas.* packages 
 * and sub-packages.
 */
public class VelocityCheckPackageInclusionCheckerImpl extends AbstractPackageInclusionChecker {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger( VelocityCheckPackageInclusionCheckerImpl.class);
	
	/** Regular expression for package names. */
	private static final transient Pattern pattern = Pattern.compile( "^org\\.agnitas(?:\\..+)?");
	

	@Override
	public boolean includePackage(String packageName) {
		if( logger.isInfoEnabled())
			logger.info( "Check exclusion of package " + packageName);
		
		Matcher matcher = pattern.matcher( packageName);

		return matcher.matches();
	}

}
