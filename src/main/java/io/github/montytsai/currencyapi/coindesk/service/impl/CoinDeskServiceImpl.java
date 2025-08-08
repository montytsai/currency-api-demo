package io.github.montytsai.currencyapi.coindesk.service.impl;

import io.github.montytsai.currencyapi.coindesk.dto.CoinDeskResponse;
import io.github.montytsai.currencyapi.coindesk.dto.TransformedCoinDeskResponse;
import io.github.montytsai.currencyapi.coindesk.mapper.CoinDeskMapper;
import io.github.montytsai.currencyapi.coindesk.service.CoinDeskService;
import io.github.montytsai.currencyapi.currency.entity.Currency;
import io.github.montytsai.currencyapi.currency.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoinDeskServiceImpl implements CoinDeskService {

    @Value("${coin-desk.api.url}")
    private String coinDeskApiUrl;

    private final WebClient webClient;
    private final CurrencyRepository currencyRepository;
    private final CoinDeskMapper coinDeskMapper;

    @Override
    public CoinDeskResponse getOriginalCoinDeskData() {
        log.info("Attempting to call CoinDesk API using WebClient. URL: {}", coinDeskApiUrl);

        CoinDeskResponse response = webClient.get()
                .uri(coinDeskApiUrl)
                .retrieve()
                .bodyToMono(CoinDeskResponse.class)
                .block();

        log.info("Successfully received response from CoinDesk API.");
        log.debug("Raw CoinDesk API response: {}", response);
        return response;
    }

    @Override
    public TransformedCoinDeskResponse getTransformedCoinDeskData() {
        log.info("Starting process to get transformed CoinDesk data.");

        // 1. 呼叫 API
        CoinDeskResponse originalData = this.getOriginalCoinDeskData();
        if (originalData == null || originalData.getBpi() == null) {
            log.error("Failed to get original data from CoinDesk API; response or BPI data is null.");
            // 回傳一個預設或空的物件，避免下游發生 NullPointerException
            return new TransformedCoinDeskResponse();
        }

        // 2. 從資料庫獲取資料並轉為 Map
        log.debug("Fetching all currency data from database for mapping.");
        List<Currency> currencies = currencyRepository.findAll();
        // 處理資料庫中可能重複的 code，讓程式更穩健
        Map<String, Currency> currencyMap = currencies.stream()
                .collect(Collectors.toMap(
                        Currency::getCode,
                        Function.identity(),
                        (existing, replacement) -> existing // 若 key 重複，保留舊的
                ));
        log.debug("Found {} currency entries in the database.", currencies.size());

        // 3. 執行轉換
        log.info("Mapping original data to transformed response.");
        TransformedCoinDeskResponse transformedResponse = coinDeskMapper.toTransformedResponse(originalData, currencyMap);
        log.info("Successfully transformed CoinDesk data.");

        return transformedResponse;
    }

}