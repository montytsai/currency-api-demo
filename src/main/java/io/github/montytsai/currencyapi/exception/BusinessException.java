package io.github.montytsai.currencyapi.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * 所有自訂業務例外的抽象基底類別。
 * 封裝了與業務錯誤相關的 HTTP 狀態碼。
 */
@Getter
public abstract class BusinessException extends RuntimeException {

    private final HttpStatus httpStatus;

    public BusinessException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
