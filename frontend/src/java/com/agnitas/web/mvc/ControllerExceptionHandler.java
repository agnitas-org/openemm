/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc;

import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;

import java.util.Map;

import com.agnitas.emm.util.html.xssprevention.HtmlCheckError;
import com.agnitas.emm.util.html.xssprevention.XSSHtmlException;
import com.agnitas.exception.BadRequestException;
import com.agnitas.exception.DetailedUiMessageException;
import com.agnitas.exception.ForbiddenException;
import com.agnitas.exception.ResourceNotFoundException;
import com.agnitas.exception.UiMessageException;
import com.agnitas.util.HttpUtils;
import com.agnitas.web.dto.DataResponseDto;
import com.agnitas.web.exception.NoPreviewImageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<?> onHttpStatusCodeException(HttpStatusCodeException exception) {
        return ResponseEntity.status(exception.getStatusCode()).build();
    }

    @ExceptionHandler(NoPreviewImageException.class)
    public String onNoPreviewImageException() {
        // Use redirect (not forward) to allow browser to use cache.
        return "redirect:" + HttpUtils.IMAGE_PATH_NO_PREVIEW;
    }

    @ExceptionHandler(BindException.class)
    public String onBindException(BindException e, Popups popups) {
        final FieldError fieldError = e.getFieldError();
        if (fieldError != null && fieldError.getRejectedValue() != null) {
            popups.alert("error.input.invalid", fieldError.getRejectedValue());
        } else {
            popups.defaultError();
        }
        return MESSAGES_VIEW;
    }

	@ExceptionHandler(XSSHtmlException.class)
	public String onXSSHtmlException(XSSHtmlException e, Popups popups) {
		for (HtmlCheckError error : e.getErrors()) {
			popups.alert(error.toMessage());
		}

		return MESSAGES_VIEW;
	}

    @ExceptionHandler(BadRequestException.class)
    public ModelAndView onBadRequestException(BadRequestException e, Popups popups) {
        return showErrors(e, popups, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ModelAndView onForbiddenException(ForbiddenException e, Popups popups) {
        return showErrors(e, popups, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView onResourceNotFoundException(ResourceNotFoundException e, Popups popups) {
        return showErrors(e, popups, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UiMessageException.class)
    public ModelAndView onUiMessageException(UiMessageException e, Popups popups) {
        return showErrors(e, popups, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DetailedUiMessageException.class)
    @ResponseBody
    public DataResponseDto<Map<String, Object>> onDetailedRequestErrorException(DetailedUiMessageException e, Popups popups) {
        addErrors(e, popups);
        return new DataResponseDto<>(e.getDetails(), popups, false);
    }

    private ModelAndView showErrors(UiMessageException exception, Popups popups, HttpStatus httpStatus) {
        addErrors(exception, popups);
        return new ModelAndView(MESSAGES_VIEW, httpStatus);
    }

    private void addErrors(UiMessageException exception, Popups popups) {
        exception.getErrors().forEach(popups::alert);
        exception.getFieldsErrors().forEach(popups::fieldError);
    }

}
