/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.spring.web.view.tiles3;

import jakarta.servlet.ServletContext;
import org.apache.tiles.request.ApplicationResource;
import org.apache.tiles.request.locale.URLApplicationResource;
import org.apache.tiles.request.servlet.ServletApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

public class SpringWildcardServletTilesApplicationContext extends ServletApplicationContext {

    private final ResourcePatternResolver resolver;

    public SpringWildcardServletTilesApplicationContext(ServletContext servletContext) {
        super(servletContext);
        this.resolver = new ServletContextResourcePatternResolver(servletContext);
    }


    @Override
    public ApplicationResource getResource(String localePath) {
        Collection<ApplicationResource> urlSet = getResources(localePath);
        if (!CollectionUtils.isEmpty(urlSet)) {
            return urlSet.iterator().next();
        }
        return null;
    }

    @Override
    public ApplicationResource getResource(ApplicationResource base, Locale locale) {
        Collection<ApplicationResource> urlSet = getResources(base.getLocalePath(locale));
        if (!CollectionUtils.isEmpty(urlSet)) {
            return urlSet.iterator().next();
        }
        return null;
    }

    @Override
    public Collection<ApplicationResource> getResources(String path) {
        Resource[] resources;
        try {
            resources = this.resolver.getResources(path);
        }
        catch (IOException ex) {
            ((ServletContext) getContext()).log("Resource retrieval failed for path: " + path, ex);
            return Collections.emptyList();
        }
        if (ObjectUtils.isEmpty(resources)) {
            ((ServletContext) getContext()).log("No resources found for path pattern: " + path);
            return Collections.emptyList();
        }

        Collection<ApplicationResource> resourceList = new ArrayList<>(resources.length);
        for (Resource resource : resources) {
            try {
                URL url = resource.getURL();
                resourceList.add(new URLApplicationResource(url.toExternalForm(), url));
            }
            catch (IOException ex) {
                // Shouldn't happen with the kind of resources we're using
                throw new IllegalArgumentException("No URL for " + resource, ex);
            }
        }
        return resourceList;
    }

}
