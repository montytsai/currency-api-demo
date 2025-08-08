package io.github.montytsai.currencyapi.coindesk.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 用於 資料轉換後的新 API 回應的資料傳輸物件 (DTO)。
 * <p>
 * 此類別手動實現了 currencyInfo 欄位的 getter/setter，
 * 以執行「防禦性複製 (Defensive Copying)」，
 * 解決 SpotBugs 警告，確保物件狀態的封裝性。
 */
public class TransformedCoinDeskResponse {

    @Getter
    @Setter
    private String updatedTime;

    private List<CurrencyInfo> currencyInfo;

    /**
     * currencyInfo 欄位的 Getter，回傳一個 List 的防禦性複本。
     *
     * @return 一個新的 ArrayList 實例，或在 currencyInfo 為 null 時回傳一個不可變的空 List。
     */
    public List<CurrencyInfo> getCurrencyInfo() {
        if (this.currencyInfo == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(this.currencyInfo);
    }

    /**
     * currencyInfo 欄位的 Setter，儲存一個傳入 List 的防禦性複本。
     *
     * @param currencyInfo 要設定的 List<CurrencyInfo> 物件。
     */
    public void setCurrencyInfo(List<CurrencyInfo> currencyInfo) {
        if (currencyInfo == null) {
            this.currencyInfo = null;
        } else {
            this.currencyInfo = new ArrayList<>(currencyInfo);
        }
    }

    @Data
    public static class CurrencyInfo {
        private String code;
        private String chineseName;
        private float rate;
    }

}