/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.workflow.web;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.web.forms.WorkflowParameters;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.springframework.core.MethodParameter;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.support.RequestContextUtils;

public class WorkflowParametersArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterType().isAssignableFrom(WorkflowParameters.class);
    }
    
    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mav, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        return initializeWorkflowParams(mav.getDefaultModel(), nativeWebRequest.getNativeRequest(HttpServletRequest.class));
    }
    
    private WorkflowParameters initializeWorkflowParams(ModelMap model, HttpServletRequest request) {
        WorkflowParameters forwardParams = WorkflowParametersHelper.get(model);
    
        if (forwardParams == null) {
            forwardParams = WorkflowParametersHelper.find(request);
        }
        
        if (forwardParams == null || forwardParams.isEmpty()) {
            forwardParams = new WorkflowParameters();
            WorkflowParametersHelper.put(model, forwardParams);
        }
    
        WorkflowParametersHelper.put(RequestContextUtils.getOutputFlashMap(request), forwardParams);

        return forwardParams;
    }

}
