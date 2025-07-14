/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.spring.web.view.tiles3;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tiles.TilesContainer;
import org.apache.tiles.access.TilesAccess;
import org.apache.tiles.renderer.DefinitionRenderer;
import org.apache.tiles.request.ApplicationContext;
import org.apache.tiles.request.Request;
import org.apache.tiles.request.render.Renderer;
import org.apache.tiles.request.servlet.ServletRequest;
import org.apache.tiles.request.servlet.ServletUtil;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.JstlUtils;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

import java.util.Locale;
import java.util.Map;

public class ApacheTilesView extends AbstractUrlBasedView {

    private Renderer renderer;
    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        ServletContext servletContext = getServletContext();
        Assert.state(servletContext != null, "No ServletContext");
        this.applicationContext = ServletUtil.getApplicationContext(servletContext);

        if (this.renderer == null) {
            TilesContainer container = TilesAccess.getContainer(this.applicationContext);
            this.renderer = new DefinitionRenderer(container);
        }
    }

    @Override
    public boolean checkResource(final Locale locale) {
        Assert.state(this.renderer != null, "No Renderer set");

        HttpServletRequest servletRequest = null;
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        }

        Request request = new ServletRequest(this.applicationContext, servletRequest, null) {
            @Override
            public Locale getRequestLocale() {
                return locale;
            }
        };

        return this.renderer.isRenderable(getUrl(), request);
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
                                           HttpServletResponse response) throws Exception {

        Assert.state(this.renderer != null, "No Renderer set");

        exposeModelAsRequestAttributes(model, request);
        JstlUtils.exposeLocalizationContext(new RequestContext(request, getServletContext()));

        Request tilesRequest = createTilesRequest(request, response);
        this.renderer.render(getUrl(), tilesRequest);
    }

    /**
     * Create a Tiles {@link Request}.
     * <p>This implementation creates a {@link ServletRequest}.
     * @param request the current request
     * @param response the current response
     * @return the Tiles request
     */
    protected Request createTilesRequest(final HttpServletRequest request, HttpServletResponse response) {
        return new ServletRequest(this.applicationContext, request, response) {
            @Override
            public Locale getRequestLocale() {
                return RequestContextUtils.getLocale(request);
            }
        };
    }
}
