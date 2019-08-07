/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.ajax;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.util.AgnUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.TrackableLinkListItem;
import com.agnitas.emm.core.trackablelinks.service.ComTrackableLinkService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class GetMailingTrackableLinks extends ComAjaxJsonServletBase {
	private static final long serialVersionUID = -4477515321321115984L;
	
	private static final transient Logger logger = Logger.getLogger(GetMailingTrackableLinks.class);
    @Override
    protected JSONObject processRequestJson(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject responseJson = new JSONObject();

        ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        ComTrackableLinkService trackableLinkService = applicationContext.getBean("TrackeableLinkService", ComTrackableLinkService.class);

        try {
            int mailingId = Integer.parseInt(request.getParameter("mailingId"));
            List<TrackableLinkListItem> mailingLinks = trackableLinkService.getMailingLinks(mailingId, AgnUtils.getCompanyID(request));

            if(!mailingLinks.isEmpty()) {
	            JSONArray items = new JSONArray();
	            for (TrackableLinkListItem mailingLink : mailingLinks) {
		            JSONObject item = new JSONObject();
		            item.element("id", mailingLink.getId());
		            item.element("value", mailingLink.getFullUrl());
		            items.add(item);
	            }
	            responseJson.element("mailingId", mailingId);
	            responseJson.element("links", items);
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid mailingId parameter: " + e.getMessage(), e);
            return null;
        }

        return responseJson;
    }
}
