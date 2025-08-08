package io.github.montytsai.currencyapi.exception;

import org.springframework.http.HttpStatus;

/**
 * 409: 用於表示資源已存在，無法重複建立的例外。
 */
public class ResourceAlreadyExistsException extends BusinessException {

    public ResourceAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

}