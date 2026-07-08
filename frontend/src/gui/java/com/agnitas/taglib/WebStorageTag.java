/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib;

import com.agnitas.beans.WebStorageEntry;
import com.agnitas.service.WebStorage;
import com.agnitas.service.WebStorageBundle;
import org.apache.taglibs.standard.tag.common.core.SetSupport;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class WebStorageTag extends SetSupport {

    private WebStorage webStorage;
    private WebStorageBundle<? extends WebStorageEntry> key;

    private WebStorage getWebStorage() {
        if (webStorage == null) {
            webStorage = WebApplicationContextUtils.getRequiredWebApplicationContext(pageContext.getServletContext()).getBean(WebStorage.class);
        }
        return webStorage;
    }

    public WebStorageBundle<? extends WebStorageEntry> getKey() {
        return key;
    }

    public void setKey(WebStorageBundle<? extends WebStorageEntry> key) {
        this.key = key;
    }

    @Override
    protected boolean isValueSpecified() {
        return true;
    }

    @Override
    protected Object evalValue() {
        return getWebStorage().get(getKey());
    }

    @Override
    protected Object evalTarget() {
        return null;
    }

    @Override
    protected String evalProperty() {
        return null;
    }

}
