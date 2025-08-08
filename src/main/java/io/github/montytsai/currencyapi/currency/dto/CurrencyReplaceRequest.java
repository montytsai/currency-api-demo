package io.github.montytsai.currencyapi.currency.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 用於「完整替換 (PUT)」幣別資料的請求 DTO。
 * 所有欄位都是必填的，以符合 PUT 的完整替換語義。
 */
@Getter
@Setter
public class CurrencyReplaceRequest {

    // 在 PUT 中，body 中的 code 必須提供，且要與 URL 中的 code 一致
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