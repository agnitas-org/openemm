/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.clickranking;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.trackablelinks.dto.ClickRanking;
import com.agnitas.emm.core.trackablelinks.service.ClickRankingService;
import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import com.agnitas.emm.restful.BaseRequestResponse;
import com.agnitas.emm.restful.JsonRequestResponse;
import com.agnitas.emm.restful.ResponseType;
import com.agnitas.emm.restful.RestfulClientException;
import com.agnitas.emm.restful.RestfulServiceHandler;
import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.util.HttpUtils.RequestMethod;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

/**
 * This restful service is available at:
 * https://<system.url>/restful/topics
 */
@Component("RestfulServiceHandler_topics")
public class TopicsClickRankingRestfulServiceHandler implements RestfulServiceHandler {

    public static final String NAMESPACE = "topics";

    private final RestfulUserActivityLogDao userActivityLogDao;
    private final ClickRankingService clickRankingService;

    public TopicsClickRankingRestfulServiceHandler(
            RestfulUserActivityLogDao userActivityLogDao,
            ClickRankingService clickRankingService
    ) {
        this.userActivityLogDao = userActivityLogDao;
        this.clickRankingService = clickRankingService;
    }

    @Override
    public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) {
        // No redirect needed
        return this;
    }

    @Override
    public void doService(HttpServletRequest request, HttpServletResponse response, Admin admin, byte[] requestData, File requestDataFile, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod, boolean extendedLogging) throws Exception {
        if (requestMethod == RequestMethod.GET) {
            ((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getRanking(request, admin)));
        } else {
            throw new RestfulClientException("Invalid http request method");
        }
    }

    private Object getRanking(HttpServletRequest request, Admin admin) throws Exception {
        String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 2);
        if (restfulContext.length != 2) {
            throw new RestfulClientException("Invalid request");
        }
        String requestedTopic = restfulContext[0];
        if (isBlank(requestedTopic) || !"click-ranking".equals(restfulContext[1])) {
            throw new RestfulClientException("Invalid request");
        }
        userActivityLogDao.addAdminUseOfFeature(admin, "restful/click-ranking", new Date());
        writeActivityLog("Click-ranking for topic: %s".formatted(requestedTopic), request, admin);

        JsonArray jsonArray = new JsonArray();

        for (ClickRanking ranking : clickRankingService.findByTopic(requestedTopic, admin.getCompanyID())) {
            jsonArray.add(toJson(ranking));
        }
        return jsonArray;
    }

    private JsonObject toJson(ClickRanking ranking) {
        JsonObject json = new JsonObject();
        json.add("urlId", ranking.urlId());
        json.add("companyId", ranking.companyId());
        json.add("url", ranking.url());
        json.add("topic", ranking.topic());
        json.add("count", ranking.count());
        json.add("rank", ranking.rank());
        json.add("firstClickDate", DateTimeFormatter.ISO_INSTANT.format(ranking.firstClickDate()));
        return json;
    }

    @Override
    public ResponseType getResponseType() {
        return ResponseType.JSON;
    }

    private void writeActivityLog(String description, HttpServletRequest request, Admin admin) {
        writeActivityLog(userActivityLogDao, description, request, admin);
    }
}
