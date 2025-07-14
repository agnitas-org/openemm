/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import jakarta.annotation.Resource;

import com.agnitas.service.UserActivityLogService;
import org.springframework.context.annotation.Lazy;

public abstract class BaseEndpoint {

    @Resource
    @Lazy
	protected com.agnitas.emm.springws.jaxb.ObjectFactory objectFactory;

    @Resource
    @Lazy
	protected com.agnitas.emm.springws.jaxb.extended.ObjectFactory comObjectFactory;
	
    @Deprecated // Inject UserActivityLogAccess instead in endpoint classes
    @Resource
    @Lazy
	protected UserActivityLogService userActivityLogService;
    
    public final void setObjectFactory(final com.agnitas.emm.springws.jaxb.ObjectFactory objectFactory) {
    	this.objectFactory = objectFactory;
    }
    
}
