/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.agnitas.messages.Message;
import com.agnitas.web.mvc.impl.StrutsPopups;

public class PopupsArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(Popups.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mav, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) {
        Popups popups = initializePopups(mav.getDefaultModel(), nativeWebRequest.getNativeRequest(HttpServletRequest.class));

        for (BindingResult result : getBindingResult(mav)) {
            for (ObjectError error : result.getAllErrors()) {
                Message message = asMessage(error);

                if (error instanceof FieldError) {
                    popups.field(((FieldError) error).getField(), message);
                } else {
                    popups.alert(message);
                }
            }
        }

        return popups;
    }

    private List<BindingResult> getBindingResult(ModelAndViewContainer mav) {
        return mav.getModel().values().stream()
                .filter(BindingResult.class::isInstance)
                .map(BindingResult.class::cast)
                .collect(Collectors.toList());
    }

    private Message asMessage(ObjectError error) {
        if (ArrayUtils.isEmpty(error.getCodes())) {
            return Message.exact(StringUtils.defaultString(error.getDefaultMessage()));
        } else {
            return Message.of(error.getCode(), error.getArguments());
        }
    }

    private StrutsPopups initializePopups(ModelMap model, HttpServletRequest request) {
        StrutsPopups popups = StrutsPopups.get(model);

        if (popups == null) {
            popups = new StrutsPopups();

            StrutsPopups.put(model, popups);
        }
        StrutsPopups.put(RequestContextUtils.getOutputFlashMap(request), popups);

        return popups;
    }
}
