/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.exception;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.commons.exception.EntityAlreadyExistsException;
import com.agnitas.emm.core.commons.exception.EntityNotFoundException;
import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.emm.core.validator.ValidationFieldTranslator;
import com.agnitas.emm.rest.v2.infrastructure.config.json.RestEnumException;
import com.agnitas.emm.util.html.xssprevention.HtmlCheckError;
import com.agnitas.emm.util.html.xssprevention.XSSHtmlException;
import com.agnitas.exception.ForbiddenException;
import com.agnitas.messages.Message;
import com.agnitas.web.perm.FieldChangeNotAllowedException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.InvalidTypeIdException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RestV2ExceptionHandler {

    private static final Logger logger = LogManager.getLogger(RestV2ExceptionHandler.class);

    private final ValidationFieldTranslator fieldTranslator;

    public RestV2ExceptionHandler(RestValidationFieldTranslator fieldTranslator) {
        this.fieldTranslator = fieldTranslator;
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsable(AsyncRequestNotUsableException ex) {
        logger.debug("Client disconnected before the response could be written", ex);
    }

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
                        .build()
                );
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthenticationException(AuthenticationException ex) {
        return ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED)
                .addError("Authentication failed.")
                .build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return validationErrorResponse(new ErrorEntry(
                ex.getPropertyName(),
                ex.getMostSpecificCause().getMessage()
        ));
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
    protected ErrorResponse handleHandlerMethodValidation(HandlerMethodValidationException ex) {
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
    protected ErrorResponse handleHandlerMethodValidation(ConstraintViolationException ex) {
        return validationErrorResponse(ex.getConstraintViolations().stream()
                .map(violation -> new ErrorEntry(
                        leafPropertyName(violation.getPropertyPath()),
                        violation.getMessage()
                )).toList());
    }

    private String leafPropertyName(Path propertyPath) {
        Path.Node last = null;
        for (Path.Node node : propertyPath) {
            last = node;
        }
        return last != null ? last.getName() : propertyPath.toString();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ErrorEntry> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .toList();
        return validationErrorResponse(errors);
    }

    private ErrorEntry mapFieldError(FieldError error) {
        if (error.getCodes() != null &&
            Arrays.asList(error.getCodes()).contains("typeMismatch")
            && error.contains(TypeMismatchException.class)
        ) {
                TypeMismatchException tme = error.unwrap(TypeMismatchException.class);
                Throwable cause = tme.getCause();

                while (cause != null) {
                    if (cause instanceof RestEnumException ere) {
                        return new ErrorEntry(error.getField(), ere.getMessage());
                    }
                    cause = cause.getCause();
                }
            }
        return new ErrorEntry(error.getField(), error.getDefaultMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        if (ex.getMostSpecificCause() instanceof InvalidTypeIdException e
            && e.getBaseType() != null
            && AbstractActionOperationParameters.class.isAssignableFrom(e.getBaseType().getRawClass())) {

            return validationErrorResponse(new ErrorEntry(
                    "type",
                    "Allowed values: [%s]".formatted(ActionOperationType.getAllowedValuesStr())
            ));
        }
        if (ex.getMostSpecificCause() instanceof RestEnumException enumEx
            && ex.getCause() instanceof DatabindException bindEx && !bindEx.getPath().isEmpty()) {
            return validationErrorResponse(new ErrorEntry(
                    bindEx.getPath().getFirst().getPropertyName(),
                    enumEx.getMessage()
            ));
        }
        if (ex.getCause() instanceof UnrecognizedPropertyException unrecognizedPropertyEx) {
            return validationErrorResponse(new ErrorEntry(unrecognizedPropertyEx.getPropertyName(), "Unknown field"));
        }
        if (ex.getCause() instanceof InvalidFormatException ife && !ife.getPath().isEmpty()) {
            String field = ife.getPath().getLast().getPropertyName();
            String targetType = ife.getTargetType().getSimpleName();
            return validationErrorResponse(new ErrorEntry(
                    field,
                    "Invalid value. Expected %s".formatted(targetType)
            ));
        }
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .detail("Invalid request body")
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

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDenied() {
        return ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN)
                .detail("Forbidden")
                .build();
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbiddenException(ForbiddenException ex) {
        return ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN)
                .detail("Forbidden")
                .addErrors(ex.getErrors().stream().map(ErrorEntry::new).toList())
                .addErrors(ex.getFieldsErrors().entrySet().stream()
                        .map(entry -> new ErrorEntry(entry.getKey(), entry.getValue()))
                        .toList())
                .build();
    }

    @ExceptionHandler(FieldChangeNotAllowedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleFieldNotAllowedException(FieldChangeNotAllowedException ex) {
        ErrorEntry errorEntry = new ErrorEntry(
                fieldTranslator.translate(ex.getField()),
                Message.of("permission.denied.message")
        );
        return ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN)
                .addError(errorEntry)
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

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(ValidationException ex) {
        List<ErrorEntry> errors = Stream.concat(
                ex.getFieldErrors().entrySet()
                        .stream()
                        .map(e -> new ErrorEntry(
                                fieldTranslator.translate(e.getKey()),
                                e.getValue()
                        )),
                ex.getErrors().stream().map(ErrorEntry::new)
        ).toList();
        return validationErrorResponse(errors);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleCoreNotFoundException(EntityNotFoundException ex) {
        ErrorResponse.Builder builder = ErrorResponse.builder().status(HttpStatus.NOT_FOUND);
        Optional<Message> messageOpt = ex.getMessageOpt();
        if (messageOpt.isPresent()) {
            builder.addError(messageOpt.get());
        } else {
            builder.detail(ex.getMessage());
        }
        return builder.build();
    }

    @ExceptionHandler(HttpStatusException.class)
    public ResponseEntity<ErrorResponse> handleHttpStatusException(HttpStatusException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(ErrorResponse.builder()
                        .status(ex.getStatus())
                        .addErrors(ex.getErrors().stream().map(ErrorEntry::new).toList())
                        .build()
                );
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleCoreAlreadyExistsException(EntityAlreadyExistsException ex) {
        return ErrorResponse.builder()
                .status(HttpStatus.CONFLICT)
                .addError(ex.getMessage())
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
