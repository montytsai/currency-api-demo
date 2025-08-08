package io.github.montytsai.currencyapi.coindesk.mapper;

import io.github.montytsai.currencyapi.coindesk.dto.CoinDeskResponse;
import io.github.montytsai.currencyapi.coindesk.dto.TransformedCoinDeskResponse;
import io.github.montytsai.currencyapi.currency.entity.Currency;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 負責 CoinDesk 相關資料模型轉換的元件。
 * <p>
 * 此 Mapper 將從不同來源（如外部 API 回應、資料庫實體）獲取的資料，
 * 組合並轉換成最終 API 所需的回應格式 (DTO)。
 */
@Slf4j
@Component
public class CoinDeskMapper {

    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public TransformedCoinDeskResponse toTransformedResponse(CoinDeskResponse originalData, Map<String, Currency> currencyMap) {
        log.debug("Starting transformation of CoinDesk response.");
        TransformedCoinDeskResponse transformedResponse = new TransformedCoinDeskResponse();

        // 1. 轉換時間格式
        if (originalData.getTime() != null && originalData.getTime().getUpdatedISO() != null) {
            transformedResponse.setUpdatedTime(formatUpdatedTime(originalData.getTime().getUpdatedISO()));
        } else {
            log.warn("Original data is missing time information.");
            transformedResponse.setUpdatedTime("N/A");
        }

        // 2. 處理幣別資料轉換
        if (originalData.getBpi() != null) {
            List<TransformedCoinDeskResponse.CurrencyInfo> currencyInfos = originalData.getBpi().values().stream()
                    .map(bpiData -> toCurrencyInfo(bpiData, currencyMap.get(bpiData.getCode())))
                    .collect(Collectors.toList());
            transformedResponse.setCurrencyInfo(currencyInfos);
            log.debug("Transformation complete. Mapped {} currency entries.", currencyInfos.size());
        } else {
            log.warn("Original data is missing BPI information.");
            transformedResponse.setCurrencyInfo(Collections.emptyList());
        }

        return transformedResponse;
    }

    /**
     * 將單一 BPI 資料和對應的 Currency 實體，轉換為新的 CurrencyInfo DTO。
     *
     * @param bpiData  單一幣別的原始 BPI 資料
     * @param currency 從資料庫中找到的對應幣別實體，可能為 null
     * @return 轉換後的 CurrencyInfo 物件
     */
    private TransformedCoinDeskResponse.CurrencyInfo toCurrencyInfo(CoinDeskResponse.BpiData bpiData, Currency currency) {
        TransformedCoinDeskResponse.CurrencyInfo info = new TransformedCoinDeskResponse.CurrencyInfo();
        info.setCode(bpiData.getCode());
        info.setRate(bpiData.getRateFloat());

        // 如果在資料庫中找到對應的幣別，則使用其中文名稱，否則給予預設值
        String chineseName = (currency != null) ? currency.getDisplayName() : "N/A";
        info.setChineseName(chineseName);

        if (currency == null) {
            log.warn("No currency mapping found in database for code: {}. Using 'N/A' as Chinese name.", bpiData.getCode());
        }

        return info;
    }

    /**
     * 將 ISO 8601 時間字串轉換為 OUTPUT_FORMATTER 格式。
     *
     * @param isoDateTime ISO 8601 格式的時間字串 (e.g., "2024-09-02T07:07:20+00:00")
     * @return 格式化後的時間字串
     */
    private String formatUpdatedTime(String isoDateTime) {
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(isoDateTime, DateTimeFormatter.ISO_DATE_TIME);
            return zonedDateTime.format(OUTPUT_FORMATTER);
        } catch (Exception e) {
            log.error("Failed to parse date-time string: {}. Returning original string.", isoDateTime, e);
            return isoDateTime; // 發生錯誤時，回傳原始字串，避免系統崩潰
        }
    }

}