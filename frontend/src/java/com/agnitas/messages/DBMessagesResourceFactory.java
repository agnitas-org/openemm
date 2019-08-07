/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.messages;

import org.apache.log4j.Logger;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.MessageResourcesFactory;

public class DBMessagesResourceFactory extends MessageResourcesFactory {
	private static final long serialVersionUID = 9123593116598301338L;
	
	/** The logger. */
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(DBMessagesResourceFactory.class);
	
	public DBMessagesResourceFactory() {
		super();
	}

	@Override
	public MessageResources createResources(String config) {
		DBMessagesResourceWrapperForStruts dbMessagesResource = new DBMessagesResourceWrapperForStruts(this, config);
		dbMessagesResource.init();
		return dbMessagesResource;
	}
}
