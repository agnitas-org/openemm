/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc;

import java.util.Map;

import com.agnitas.exception.DetailedValidationException;
import com.agnitas.exception.ValidationException;
import com.agnitas.emm.util.html.xssprevention.HtmlCheckError;
import com.agnitas.emm.util.html.xssprevention.XSSHtmlException;
import com.agnitas.web.dto.DataResponseDto;
import org.agnitas.util.HttpUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpStatusCodeException;

import com.agnitas.web.exception.NoPreviewImageException;

@ControllerAdvice
public class ControllerExceptionHandler {

    private final String MESSAGES_VIEW = "messages";

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
    public String onBindException(final BindException e, final Popups popups) {
        final FieldError fieldError = e.getFieldError();
        if (fieldError != null && fieldError.getRejectedValue() != null) {
            popups.alert("error.input.invalid", fieldError.getRejectedValue());
        } else {
            popups.alert("Error");
        }
        return MESSAGES_VIEW;
    }

	@ExceptionHandler(XSSHtmlException.class)
	public String onXSSHtmlException(final XSSHtmlException e, final Popups popups) {
		for (HtmlCheckError error : e.getErrors()) {
			popups.alert(error.toMessage());
		}

		return MESSAGES_VIEW;
	}

	@ExceptionHandler(ValidationException.class)
    public String onValidationException(ValidationException e, Popups popups) {
        e.getErrors().forEach(popups::alert);
        e.getFieldsErrors().forEach((f, m) -> popups.fieldError(f, m));
        return MESSAGES_VIEW;
    }

    @ExceptionHandler(DetailedValidationException.class)
    @ResponseBody
    public DataResponseDto<Map<String, Object>> onDetailedValidationException(DetailedValidationException e, Popups popups) {
        e.getErrors().forEach(popups::alert);
        e.getFieldsErrors().forEach(popups::fieldError);
        return new DataResponseDto<>(e.getDetails(), popups, false);
    }
}
