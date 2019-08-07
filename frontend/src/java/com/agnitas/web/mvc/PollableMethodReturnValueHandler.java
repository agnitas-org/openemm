/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.method.support.AsyncHandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.agnitas.service.PollingService;

public class PollableMethodReturnValueHandler implements AsyncHandlerMethodReturnValueHandler {
    private PollingService pollingService;

    @Autowired
    public PollableMethodReturnValueHandler(PollingService pollingService) {
        this.pollingService = pollingService;
    }

    @Override
    public boolean isAsyncReturnValue(Object returnValue, MethodParameter returnType) {
        return returnValue instanceof Pollable;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return Pollable.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        if (returnValue == null) {
            mavContainer.setRequestHandled(true);
        } else {
            Pollable<?> pollable = (Pollable<?>) returnValue;

            pollingService.submit(pollable);

            WebAsyncUtils.getAsyncManager(webRequest).startDeferredResultProcessing(pollable.getDeferredResult(), mavContainer);
        }
    }
}
