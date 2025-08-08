package io.github.montytsai.currencyapi.coindesk.service.impl;

import io.github.montytsai.currencyapi.coindesk.dto.CoinDeskResponse;
import io.github.montytsai.currencyapi.coindesk.dto.TransformedCoinDeskResponse;
import io.github.montytsai.currencyapi.coindesk.mapper.CoinDeskMapper;
import io.github.montytsai.currencyapi.currency.entity.Currency;
import io.github.montytsai.currencyapi.currency.repository.CurrencyRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 對 CoinDeskServiceImpl 的核心商業邏輯進行單元測試。
 */
@ExtendWith(MockitoExtension.class)
class CoinDeskServiceImplTest {

    // --- Mock Dependencies ---
    @Mock private WebClient webClient;
    @Mock private CurrencyRepository currencyRepository;

    // WebClient 呼叫鏈所需的 Mocks
    @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    // --- 受測物件 (System Under Test) ---
    private CoinDeskServiceImpl coinDeskService;

    @BeforeEach
    void setUp() {
        // 手動建立受測物件，並將依賴注入。
        coinDeskService = new CoinDeskServiceImpl(
                webClient,
                currencyRepository,
                new CoinDeskMapper() // <-- 直接傳入真實的 Mapper 實例
        );

        // 在測試環境中，手動為 @Value 欄位賦值。
        ReflectionTestUtils.setField(coinDeskService, "coinDeskApiUrl", "http://fake-url.com");
    }

    @Test
    @DisplayName("[P0.1] 測試核心轉換邏輯：應能正確呼叫 API、資料庫，並透過 Mapper 轉換資料")
    void testGetTransformedCoinDeskData_SuccessPath() {
        // --- Arrange ---

        // 準備 Coindesk API 的假回應。
        CoinDeskResponse fakeApiResponse = createFakeCoinDeskResponse();

        // 設定 WebClient 的 Mock 行為。
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CoinDeskResponse.class)).thenReturn(Mono.just(fakeApiResponse));

        // 準備資料庫的假回應。
        Currency usdCurrency = new Currency();
        usdCurrency.setCode("USD");
        usdCurrency.setDisplayName("美金");
        when(currencyRepository.findAll()).thenReturn(Collections.singletonList(usdCurrency));

        // --- Act ---
        TransformedCoinDeskResponse result = coinDeskService.getTransformedCoinDeskData();

        // --- Assert ---
        // 因為我們使用了真實的 Mapper，所以可以直接驗證轉換後的結果內容是否正確。
        assertEquals("2024/09/02 07:07:20", result.getUpdatedTime());
        assertEquals(2, result.getCurrencyInfo().size());

        // 驗證 USD 的轉換結果
        TransformedCoinDeskResponse.CurrencyInfo usdInfo = result.getCurrencyInfo().stream()
                .filter(c -> "USD".equals(c.getCode())).findFirst().orElseThrow(AssertionError::new);
        assertEquals("美金", usdInfo.getChineseName());
        assertEquals(57756.2984f, usdInfo.getRate());

        // 驗證 GBP (資料庫中找不到) 的轉換結果
        TransformedCoinDeskResponse.CurrencyInfo gbpInfo = result.getCurrencyInfo().stream()
                .filter(c -> "GBP".equals(c.getCode())).findFirst().orElseThrow(AssertionError::new);
        assertEquals("N/A", gbpInfo.getChineseName()); // 驗證找不到時的預設值
    }

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