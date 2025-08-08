package io.github.montytsai.currencyapi.coindesk.service.impl;

import io.github.montytsai.currencyapi.coindesk.dto.CoinDeskResponse;
import io.github.montytsai.currencyapi.coindesk.dto.TransformedCoinDeskResponse;
import io.github.montytsai.currencyapi.coindesk.mapper.CoinDeskMapper;
import io.github.montytsai.currencyapi.currency.entity.Currency;
import io.github.montytsai.currencyapi.currency.repository.CurrencyRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CoinDeskServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CurrencyRepository currencyRepository;

    // 我們要測試 Service 和 Mapper 的整合，所以直接 new 一個真實的 Mapper
    private CoinDeskMapper coinDeskMapper = new CoinDeskMapper();

    // 這是我們要測試的對象
    private CoinDeskServiceImpl coinDeskService;

    @BeforeEach
    void setUp() {
        // 初始化所有 @Mock 註解的欄位
        MockitoAnnotations.openMocks(this);
        // 手動建立 Service 實例，並注入所有依賴（包含真實的 Mapper）
        coinDeskService = new CoinDeskServiceImpl(restTemplate, currencyRepository, coinDeskMapper);
        // 為 @Value 欄位設定假值
        ReflectionTestUtils.setField(coinDeskService, "coinDeskApiUrl", "http://fake-url.com");
    }

    @Test
    @DisplayName("測試資料轉換邏輯應能正確格式化時間並整合中文名稱")
    void testGetTransformedCoinDeskData() {
        // 1. Arrange (準備假資料)
        // 準備 Coindesk API 的假回應
        CoinDeskResponse fakeResponse = createFakeCoinDeskResponse();
        when(restTemplate.getForObject(anyString(), eq(CoinDeskResponse.class))).thenReturn(fakeResponse);

        // 準備資料庫的假回應 (對應 Service 中的 findAll() 呼叫)
        Currency usdCurrency = new Currency();
        usdCurrency.setCode("USD");
        usdCurrency.setDisplayName("美金");
        // 模擬資料庫回傳一個包含 USD 的列表
        when(currencyRepository.findAll()).thenReturn(Collections.singletonList(usdCurrency));

        // 2. Act (執行要測試的方法)
        TransformedCoinDeskResponse result = coinDeskService.getTransformedCoinDeskData();

        // 3. Assert (驗證結果)
        assertEquals("2024/09/02 07:07:20", result.getUpdatedTime());
        assertEquals(2, result.getCurrencyInfo().size());

        // 驗證 USD 的轉換結果
        TransformedCoinDeskResponse.CurrencyInfo usdInfo = result.getCurrencyInfo().stream()
                .filter(c -> c.getCode().equals("USD")).findFirst().get();
        assertEquals("美金", usdInfo.getChineseName());
        assertEquals(57756.2984f, usdInfo.getRate());

        // 驗證 GBP (資料庫中找不到) 的轉換結果
        TransformedCoinDeskResponse.CurrencyInfo gbpInfo = result.getCurrencyInfo().stream()
                .filter(c -> c.getCode().equals("GBP")).findFirst().get();
        assertEquals("N/A", gbpInfo.getChineseName()); // 驗證找不到時的預設值
    }

    // 建立假 CoinDesk 回應的輔助方法
    private CoinDeskResponse createFakeCoinDeskResponse() {
        CoinDeskResponse response = new CoinDeskResponse();

        CoinDeskResponse.TimeData timeData = new CoinDeskResponse.TimeData();
        timeData.setUpdatedISO("2024-09-02T07:07:20+00:00");
        response.setTime(timeData);

        Map<String, CoinDeskResponse.BpiData> bpiMap = new HashMap<>();

        CoinDeskResponse.BpiData usdData = new CoinDeskResponse.BpiData();
        usdData.setCode("USD");
        usdData.setRateFloat(57756.2984f);
        bpiMap.put("USD", usdData);

        CoinDeskResponse.BpiData gbpData = new CoinDeskResponse.BpiData();
        gbpData.setCode("GBP");
        gbpData.setRateFloat(43984.0203f);
        bpiMap.put("GBP", gbpData);

        response.setBpi(bpiMap);
        return response;
    }

}