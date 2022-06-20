/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.spring;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public final class ForceClassLoadingBean {
	
	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(ForceClassLoadingBean.class);

	@Required
	public final void setClassesToLoad(final List<String> names) throws Throwable {
		for(final String name : names) {
			try {
				Class.forName(name);
			} catch (ExceptionInInitializerError e) {
				LOGGER.error(String.format("Error force-loading class '%s'", name), e);
				
				throw e;
			}
		}
	}

}
