package com.readour.common.exception;

import com.readour.common.dto.ErrorResponseDto;
import com.readour.common.enums.ErrorCode;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String TRACE = "trace";

    @Value("${error.printStackTrace:false}")
    private boolean printStackTrace;

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        return buildErrorResponse(ex, ex.getMessage(), HttpStatus.valueOf(statusCode.value()), request);
    }

    private ResponseEntity<Object> buildErrorResponse(Exception exception, String message, HttpStatus httpStatus, WebRequest request) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(httpStatus.value(), message, LocalDateTime.now());
        if (printStackTrace && isTraceOn(request)) {
            errorResponseDto.setStackTrace(ExceptionUtils.getStackTrace(exception));
        }
        return ResponseEntity.status(httpStatus).body(errorResponseDto);
    }

    private boolean isTraceOn(WebRequest request) {
        String[] value = request.getParameterValues(TRACE);
        return Objects.nonNull(value) && value.length > 0 && value[0].contentEquals("true");
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(CustomException ex, WebRequest request) {
        HttpStatus status = ex.getErrorCode().getHttpStatus();
        return buildErrorResponse(ex, ex.getMessage(), status, request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return buildErrorResponse(ex, ex.getReason() == null ? status.getReasonPhrase() : ex.getReason(), status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ErrorResponseDto dto = new ErrorResponseDto(ErrorCode.BAD_REQUEST.getStatus(), ErrorCode.BAD_REQUEST.getDefaultMessage(), LocalDateTime.now());
        ex.getBindingResult().getFieldErrors().forEach(err -> dto.addValidationError(err.getField(), err.getDefaultMessage()));
        if (printStackTrace && isTraceOn(request)) dto.setStackTrace(ExceptionUtils.getStackTrace(ex));
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatus()).body(dto);
    }

    /*@Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ErrorResponseDto dto = new ErrorResponseDto(ErrorCode.BAD_REQUEST.getStatus(), ErrorCode.BAD_REQUEST.getDefaultMessage(), LocalDateTime.now());
        ex.getFieldErrors().forEach(err -> dto.addValidationError(err.getField(), err.getDefaultMessage()));
        if (printStackTrace && isTraceOn(request)) dto.setStackTrace(ExceptionUtils.getStackTrace(ex));
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatus()).body(dto);
    }*/

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        ErrorResponseDto dto = new ErrorResponseDto(ErrorCode.BAD_REQUEST.getStatus(), ErrorCode.BAD_REQUEST.getDefaultMessage(), LocalDateTime.now());
        ex.getConstraintViolations().forEach(v -> dto.addValidationError(String.valueOf(v.getPropertyPath()), v.getMessage()));
        if (printStackTrace && isTraceOn(request)) dto.setStackTrace(ExceptionUtils.getStackTrace(ex));
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatus()).body(dto);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return buildErrorResponse(ex, ErrorCode.BAD_REQUEST.getDefaultMessage(), ErrorCode.BAD_REQUEST.getHttpStatus(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        return buildErrorResponse(ex, ErrorCode.BAD_REQUEST.getDefaultMessage(), ErrorCode.BAD_REQUEST.getHttpStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return buildErrorResponse(ex, ErrorCode.METHOD_NOT_ALLOWED.getDefaultMessage(), ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return buildErrorResponse(ex, ErrorCode.UNPROCESSABLE_ENTITY.getDefaultMessage(), ErrorCode.UNPROCESSABLE_ENTITY.getHttpStatus(), request);
    }
/*
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Object> handleNoHandlerFound(NoHandlerFoundException ex, WebRequest request) {
        return buildErrorResponse(ex, ErrorCode.NOT_FOUND.getDefaultMessage(), ErrorCode.NOT_FOUND.getHttpStatus(), request);
    }
*/
    /*@ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return buildErrorResponse(ex, ErrorCode.FORBIDDEN.getDefaultMessage(), ErrorCode.FORBIDDEN.getHttpStatus(), request);
    }*//*

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        return buildErrorResponse(ex, ErrorCode.CONFLICT.getDefaultMessage(), ErrorCode.CONFLICT.getHttpStatus(), request);
    }
/*
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, WebRequest request) {
        return buildErrorResponse(ex, ErrorCode.UNPROCESSABLE_ENTITY.getDefaultMessage(), ErrorCode.UNPROCESSABLE_ENTITY.getHttpStatus(), request);
    }*//*

    @ExceptionHandler(Exception.class)
    @Hidden
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleAllUncaughtException(Exception exception, WebRequest request) {
        return buildErrorResponse(exception, ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(), ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus(), request);
    }
*/
}
