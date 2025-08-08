package io.github.montytsai.currencyapi.currency.dto;

import java.time.LocalDateTime;
import io.github.montytsai.currencyapi.currency.entity.Currency;
import lombok.Getter;
import lombok.Setter;

/**
 * 用於 API 回應的幣別資料傳輸物件 (DTO)。
 * 作為 API 的公開合約，將內部資料模型 (Entity) 與外部表示分離。
 */
@Getter
@Setter
public class CurrencyResponse {

    /**
     * 幣別代碼 (Primary Key)，應遵循 ISO 4217 標準以確保通用性。
     * e.g., "USD", "TWD", "EUR"
     */
    private String code;

    /**
     * 幣別的顯示名稱 (Display Name)。
     */
    private String displayName;

    /**
     * 幣別的符號，用於 UI 顯示。 e.g., "$", "NT$", "€"
     */
    private String symbol;

    /**
     * 該幣別是否啟用。
     */
    private boolean isActive;

    /**
     * 資料建立時間。
     */
    private LocalDateTime createdAt;

    /**
     * 資料最後更新時間。
     */
    private LocalDateTime updatedAt;

    /**
     * 一個靜態工廠方法，用於將 Currency Entity 轉換為 CurrencyResponse DTO。
     * @param currency 來源 Currency 實體
     * @return 轉換後的 CurrencyResponse 物件
     */
    public static CurrencyResponse fromEntity(Currency currency) {
        CurrencyResponse dto = new CurrencyResponse();
        dto.setCode(currency.getCode());
        dto.setDisplayName(currency.getDisplayName());
        dto.setSymbol(currency.getSymbol());
        dto.setActive(currency.isActive());
        dto.setCreatedAt(currency.getCreatedAt());
        dto.setUpdatedAt(currency.getUpdatedAt());
        return dto;
    }

}