package io.github.montytsai.currencyapi.coindesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 用於反序列化 Coindesk API 回應的 DTO。
 * <p>
 * 使用 Map<String, BpiData> 來接收 bpi 物件，因為它的 key (USD, GBP, EUR) 是動態的。
 * 使用巢狀內部類別 (TimeData, BpiData) 可以讓相關的 DTO 保持在同一個檔案內，結構更清晰。
 * <p>
 * 此類別手動實現了 time 和 bpi 欄位的 getter/setter，
 * 以執行「防禦性複製 (Defensive Copying)」，
 * 從而解決 SpotBugs 的 EI_EXPOSE_REP 和 EI_EXPOSE_REP2 警告，
 * 確保物件狀態的封裝性與不變性。
 */
public class CoinDeskResponse {

    @Getter
    @Setter
    private String disclaimer;

    @Getter
    @Setter
    private String chartName;

    /**
     * mutable object
     */
    private TimeData time;
    private Map<String, BpiData> bpi;

    /**
     * TimeData: mutable object
     * time 欄位的 Getter，回傳一個 time 物件的防禦性複本。
     *
     * @return TimeData 的一個新實例，或在 time 為 null 時回傳 null。
     */
    public TimeData getTime() {
        return (this.time == null) ? null : new TimeData(this.time);
    }

    /**
     * time 欄位的 Setter，儲存一個傳入 time 物件的防禦性複本。
     *
     * @param time 要設定的 TimeData 物件。
     */
    public void setTime(TimeData time) {
        this.time = (time == null) ? null : new TimeData(time);
    }

    /**
     * bpi 欄位的 Getter，回傳一個 bpi Map 的防禦性複本。
     *
     * @return 一個新的 HashMap 實例，或在 bpi 為 null 時回傳 null。
     */
    public Map<String, BpiData> getBpi() {
        return (this.bpi == null) ? null : new HashMap<>(this.bpi);
    }

    /**
     * bpi 欄位的 Setter，儲存一個傳入 bpi Map 的防禦性複本。
     *
     * @param bpi 要設定的 Map<String, BpiData> 物件。
     */
    public void setBpi(Map<String, BpiData> bpi) {
        this.bpi = (bpi == null) ? null : new HashMap<>(bpi);
    }


    // --- 巢狀類別定義 ---

    @Data
    @NoArgsConstructor
    public static class TimeData {
        private String updated;
        private String updatedISO;
        private String updateduk;

        /**
         * 複製建構子 (Copy Constructor)，用於防禦性複製。
         *
         * @param other 要複製的來源 TimeData 物件。
         */
        public TimeData(TimeData other) {
            if (other != null) {
                this.updated = other.updated;
                this.updatedISO = other.updatedISO;
                this.updateduk = other.updateduk;
            }
        }
    }

    @Data
    public static class BpiData {
        private String code;
        private String symbol;
        private String rate;
        private String description;
        @JsonProperty("rate_float")
        private float rateFloat;
    }

}