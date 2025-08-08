package io.github.montytsai.currencyapi.exception;

import org.springframework.http.HttpStatus;

/**
 * 404: 用於表示請求的資源不存在的例外。
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

}