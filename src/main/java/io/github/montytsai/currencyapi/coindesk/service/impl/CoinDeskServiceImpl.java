package io.github.montytsai.currencyapi.coindesk.service.impl;

import io.github.montytsai.currencyapi.coindesk.dto.CoinDeskResponse;
import io.github.montytsai.currencyapi.coindesk.dto.TransformedCoinDeskResponse;
import io.github.montytsai.currencyapi.coindesk.mapper.CoinDeskMapper;
import io.github.montytsai.currencyapi.coindesk.service.CoinDeskService;
import io.github.montytsai.currencyapi.currency.entity.Currency;
import io.github.montytsai.currencyapi.currency.repository.CurrencyRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CoinDeskServiceImpl implements CoinDeskService {

    @Value("${coin-desk.api.url}")
    private String coinDeskApiUrl;

    private final RestTemplate restTemplate;
    private final CurrencyRepository currencyRepository;
    private final CoinDeskMapper coinDeskMapper; // 注入新的 Mapper

    public CoinDeskServiceImpl(RestTemplate restTemplate, CurrencyRepository currencyRepository, CoinDeskMapper coinDeskMapper) {
        this.restTemplate = restTemplate;
        this.currencyRepository = currencyRepository;
        this.coinDeskMapper = coinDeskMapper;
    }

    @Override
    public CoinDeskResponse getOriginalCoinDeskData() {
        log.info("Attempting to call CoinDesk API from URL: {}", coinDeskApiUrl);
        CoinDeskResponse response = restTemplate.getForObject(coinDeskApiUrl, CoinDeskResponse.class);
        log.info("Successfully received response from CoinDesk API.");
        log.debug("Raw CoinDesk API response: {}", response);
        return response;
    }

    @Override
    public TransformedCoinDeskResponse getTransformedCoinDeskData() {
        log.info("Starting process to get transformed CoinDesk data.");

        // 1. 呼叫原始 API
        CoinDeskResponse originalData = this.getOriginalCoinDeskData();

        // 2. 從資料庫獲取所有幣別資料，並轉成 Map 方便快速查詢
        log.debug("Fetching all currency data from database for mapping.");
        List<Currency> currencies = currencyRepository.findAll();
        Map<String, Currency> currencyMap = currencies.stream()
                .collect(Collectors.toMap(Currency::getCode, Function.identity()));
        log.debug("Found {} currency entries in the database.", currencies.size());

        // 3. 將所有資料交給 Mapper 進行轉換
        log.info("Mapping original data to transformed response.");
        TransformedCoinDeskResponse transformedResponse = coinDeskMapper.toTransformedResponse(originalData, currencyMap);
        log.info("Successfully transformed CoinDesk data.");

        return transformedResponse;
    }

}