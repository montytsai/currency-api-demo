package io.github.montytsai.currencyapi.currency.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 用於「新增」幣別資料的請求 DTO。
 * 所有欄位都是必填的，以確保建立一個完整的資源。
 */
@Getter
@Setter
public class CurrencyCreateRequest {

    @NotBlank(message = "Currency code cannot be blank")
    @Size(min = 3, max = 10, message = "Currency code must be between 3 and 10 characters")
    private String code;

    @NotBlank(message = "Display name cannot be blank")
    @Size(max = 50, message = "Display name cannot exceed 50 characters")
    private String displayName;

    @NotBlank(message = "Symbol cannot be blank")
    @Size(max = 10, message = "Symbol cannot exceed 10 characters")
    private String symbol;

}