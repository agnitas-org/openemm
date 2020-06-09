/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.workflow.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.web.forms.WorkflowParameters;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

public class WorkflowParamsRedirectionInterceptor extends HandlerInterceptorAdapter {
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mav) {
        if (isRedirection(mav)) {
            WorkflowParameters forwardParams = WorkflowParametersHelper.get(mav.getModelMap());

            if (forwardParams == null) {
                forwardParams = WorkflowParametersHelper.find(request);
            }

            WorkflowParametersHelper.put(RequestContextUtils.getOutputFlashMap(request), forwardParams);
            
            if (forwardParams != null && !forwardParams.isEmpty()) {
                // add forwarded parameters to url
                mav.addAllObjects(forwardParams.toMap());
            }
        }
    }

    private boolean isRedirection(ModelAndView mav) {
        return mav != null && StringUtils.startsWith(mav.getViewName(), "redirect:");
    }
}
