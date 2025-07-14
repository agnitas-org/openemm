/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.spring.web.view.tiles3;

import org.apache.tiles.TilesException;
import org.apache.tiles.preparer.ViewPreparer;
import org.apache.tiles.preparer.factory.PreparerFactory;
import org.apache.tiles.request.Request;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public abstract class AbstractSpringPreparerFactory implements PreparerFactory {

    @Override
    public ViewPreparer getPreparer(String name, Request context) {
        WebApplicationContext webApplicationContext = (WebApplicationContext) context.getContext("request").get(
                DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (webApplicationContext == null) {
            webApplicationContext = (WebApplicationContext) context.getContext("application").get(
                    WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
            if (webApplicationContext == null) {
                throw new IllegalStateException("No WebApplicationContext found: no ContextLoaderListener registered?");
            }
        }
        return getPreparer(name, webApplicationContext);
    }

    /**
     * Obtain a preparer instance for the given preparer name,
     * based on the given Spring WebApplicationContext.
     * @param name the name of the preparer
     * @param context the current Spring WebApplicationContext
     * @return the preparer instance
     * @throws TilesException in case of failure
     */
    protected abstract ViewPreparer getPreparer(String name, WebApplicationContext context) throws TilesException;

}
