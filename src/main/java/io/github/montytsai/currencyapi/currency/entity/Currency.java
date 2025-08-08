package io.github.montytsai.currencyapi.currency.entity;

import java.time.LocalDateTime;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "CURRENCY")
public class Currency {

    /**
     * 幣別代碼 (Primary Key)，應遵循 ISO 4217 標準以確保通用性。
     * e.g., "USD", "TWD", "EUR"
     */
    @Id
    @Column(name = "code", nullable = false, length = 10)
    private String code;

    /**
     * 幣別的顯示名稱 (Display Name)。
     * 採用通用命名，可以是任何語言，如 "美金" 或 "US Dollar"。
     */
    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    /**
     * 幣別的符號，用於 UI 顯示。
     * e.g., "$", "NT$", "€"
     */
    @Column(name = "symbol", length = 10)
    private String symbol;

    /**
     * 該幣別是否啟用，預設啟用。
     * 此欄位用於實現軟刪除 (Soft Delete) 或暫時停用，而非物理刪除，以保留資料完整性。
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * 資料建立時間。
     * 使用 @PrePersist 在首次儲存前自動設定目前時間，確保資料不可變。
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 資料最後更新時間。
     * 使用 @PreUpdate 在每次更新前自動設定目前時間，用於稽核追蹤。
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}