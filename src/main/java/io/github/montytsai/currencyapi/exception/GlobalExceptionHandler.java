package io.github.montytsai.currencyapi.exception;

import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * 全域例外處理器。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 統一處理所有繼承自 BusinessException 的自訂業務例外。
     *
     * @param ex      捕獲到的業務例外
     * @param request 當前的網頁請求
     * @return 包含結構化錯誤訊息的 ResponseEntity
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, WebRequest request) {
        log.warn("Business exception occurred: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getHttpStatus(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    /**
     * 400: 處理 Bean Validation 失敗的例外。
     *
     * @param ex      捕獲到的驗證例外
     * @param request 當前的網頁請求
     * @return 包含詳細欄位錯誤訊息的 ResponseEntity
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        // 將所有欄位的驗證錯誤訊息收集起來，組合成一個字串
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("'%s': %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));

        log.warn("Validation failed for request: {}", message);
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, message, request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 400: 處理無效參數例外 (e.g., 路徑與內容 ID 不符)。
     *
     * @param ex      捕獲到的例外
     * @param request 當前的網頁請求
     * @return 包含錯誤訊息的 ResponseEntity，狀態碼為 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.warn("Invalid argument provided: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 400: 處理方法參數 (如 @PathVariable, @RequestParam) 的驗證失敗例外。
     *
     * @param ex      捕獲到的約束違反例外
     * @param request 當前的網頁請求
     * @return 包含詳細錯誤訊息的 ResponseEntity
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(cv -> String.format("'%s': %s", cv.getPropertyPath(), cv.getMessage()))
                .collect(Collectors.joining(", "));

        log.warn("Constraint violation for request parameters: {}", message);
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, message, request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 500: 處理所有其他未被捕獲的未知異常 (HTTP 500 Internal Server Error)。
     * 作為最終的 fallback 處理器，確保任何未預期的錯誤都能以標準格式回傳。
     *
     * @param ex      捕獲到的未知例外
     * @param request 當前的網頁請求
     * @return 包含通用錯誤訊息的 ResponseEntity
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUncaughtException(Exception ex, WebRequest request) {
        log.error("An unexpected internal server error occurred for URI: {}", request.getDescription(false), ex);

        String message = "An unexpected internal server error occurred. Please contact support.";
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}