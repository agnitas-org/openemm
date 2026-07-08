/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.exception;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.List;

import com.agnitas.emm.util.html.xssprevention.HtmlCheckError;
import com.agnitas.emm.util.html.xssprevention.XSSHtmlException;
import com.agnitas.messages.Message;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.validation.ConstraintViolationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RestV2ExceptionHandler {

    private static final Logger logger = LogManager.getLogger(RestV2ExceptionHandler.class);

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handleAnyThrowable(Throwable t) {
        logger.error("Unhandled exception caught by global handler", t);

        Exception ex = t instanceof Exception exception ? exception : new Exception(t);
        HttpStatus status = (ex instanceof org.springframework.web.ErrorResponse errorResponse)
                ? HttpStatus.valueOf(errorResponse.getStatusCode().value())
                : HttpStatus.INTERNAL_SERVER_ERROR;

        String message = determineMessage(ex, status);
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.builder()
                        .status(status)
                        .detail(message)
                        .build());
    }

    private String determineMessage(Exception ex, HttpStatus status) {
        if (ex instanceof ResponseStatusException rse) {
            return rse.getReason();
        }
        // hide server errors (5xx) to avoid leaking internal details
        if (!status.is5xxServerError() && isNotBlank(ex.getMessage())) {
            return ex.getMessage();
        }
        return status.getReasonPhrase();
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ErrorResponse handleHandlerMethodValidation(@NonNull HandlerMethodValidationException ex) {
        return validationErrorResponse(ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> {
                            String name = (error instanceof FieldError fieldError)
                                    ? fieldError.getField()
                                    : result.getMethodParameter().getParameterName();
                            return new ErrorEntry(name, error.getDefaultMessage());
                        })
                ).toList());
    }


    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ErrorResponse handleHandlerMethodValidation(@NonNull ConstraintViolationException ex) {
        return validationErrorResponse(ex.getConstraintViolations().stream()
                .map(violation -> new ErrorEntry(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                )).toList());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ErrorResponse handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex) {
        return validationErrorResponse(ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorEntry(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof UnrecognizedPropertyException unrecognizedPropertyEx) {
            return validationErrorResponse(new ErrorEntry(unrecognizedPropertyEx.getPropertyName(), "Unknown field"));
        }
        if (ex.getCause() instanceof InvalidFormatException ife && !ife.getPath().isEmpty()) {
            return validationErrorResponse(new ErrorEntry(
                    ife.getPath().getLast().getFieldName(),
                    ife.getOriginalMessage()
            ));
        }
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .detail(ex.getMessage())
                .build();
    }

    @ExceptionHandler(XSSHtmlException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleXSSException(XSSHtmlException ex) {
        List<Message> errors = ex.getErrors().stream()
                .map(HtmlCheckError::toMessage)
                .toList();

        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .messages(errors)
                .build();
    }

    @ExceptionHandler({
            AccessDeniedException.class,
            AuthorizationDeniedException.class
    })
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDenied() {
        return ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN)
                .detail("Forbidden")
                .build();
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(BadRequestException ex) {
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .detail(ex.getReason())
                .addErrors(ex.getErrors().stream().map(ErrorEntry::new).toList())
                .addErrors(ex.getFieldErrors().entrySet().stream()
                        .map(entry -> new ErrorEntry(entry.getKey(), entry.getValue()))
                        .toList())
                .build();
    }

    private static ErrorResponse validationErrorResponse(ErrorEntry... errors) {
        return validationErrorResponse(Arrays.asList(errors));
    }

    private static ErrorResponse validationErrorResponse(List<ErrorEntry> errors) {
        return ErrorResponse.builder()
                .detail("Invalid input.")
                .status(HttpStatus.BAD_REQUEST)
                .addErrors(errors)
                .build();
    }
}
