package io.github.montytsai.currencyapi.coindesk.service;

import io.github.montytsai.currencyapi.coindesk.dto.CoinDeskResponse;
import io.github.montytsai.currencyapi.coindesk.dto.TransformedCoinDeskResponse;

/**
 * 定義 CoinDesk 相關業務邏輯的服務介面。
 */
public interface CoinDeskService {

    /**
     * 呼叫原始的 CoinDesk API 並回傳其未經處理的資料結構。
     *
     * @return CoinDesk API 的原始回應物件
     */
    CoinDeskResponse getOriginalCoinDeskData();

    /**
     * 呼叫 CoinDesk API，並將其資料與本地資料庫整合，轉換為新的 API 格式。
     *
     * @return 包含更新時間與幣別中文名稱的轉換後回應物件
     */
    TransformedCoinDeskResponse getTransformedCoinDeskData();

}