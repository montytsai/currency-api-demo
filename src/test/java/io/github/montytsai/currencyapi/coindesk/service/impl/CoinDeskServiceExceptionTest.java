package io.github.montytsai.currencyapi.coindesk.service.impl;

import io.github.montytsai.currencyapi.coindesk.dto.CoinDeskResponse;
import io.github.montytsai.currencyapi.coindesk.mapper.CoinDeskMapper;
import io.github.montytsai.currencyapi.currency.repository.CurrencyRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 針對 CoinDeskServiceImpl 的例外與邊界條件進行單元測試。
 * 專注於驗證當下游依賴 (WebClient) 發生問題時，Service 是否能正確地反應 (通常是向上拋出例外)。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CoinDeskServiceImpl 例外處理測試")
class CoinDeskServiceExceptionTest {

    @Mock
    private WebClient webClient;
    @Mock
    private CurrencyRepository currencyRepository;
    @Mock
    private CoinDeskMapper coinDeskMapper;

    // WebClient 呼叫鏈所需的 Mocks
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private CoinDeskServiceImpl coinDeskService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(coinDeskService, "coinDeskApiUrl", "http://fake-url.com");

        // 設定通用的 WebClient Mock 鏈
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("[P0.2] 當 API 連線失敗時，應拋出對應的例外")
    void whenApiConnectionFails_thenShouldThrowException() {
        // Arrange
        // 模擬 WebClient 在 reactive stream 中發生 I/O 錯誤。
        when(responseSpec.bodyToMono(CoinDeskResponse.class))
                .thenReturn(Mono.error(new ResourceAccessException("Connection timed out")));

        // Act & Assert
        // 驗證 .block() 會將底層的非受檢例外直接拋出。
        assertThrows(ResourceAccessException.class, () -> coinDeskService.getOriginalCoinDeskData());
    }

    @Test
    @DisplayName("[P0.2] 當 API 回傳 4xx 錯誤時，應拋出 WebClientResponseException")
    void whenApiReturns4xx_thenShouldThrowException() {
        // Arrange
        // 模擬 API 回傳 404 Not Found 錯誤。
        when(responseSpec.bodyToMono(CoinDeskResponse.class))
                .thenReturn(Mono.error(WebClientResponseException.create(404, "Not Found", null, null, null)));

        // Act & Assert
        // 驗證 service 會將 WebClient 的標準 HTTP 錯誤例外向上拋出。
        assertThrows(WebClientResponseException.class, () -> coinDeskService.getOriginalCoinDeskData());
    }

    @Test
    @DisplayName("[P0.2] 當 API 回傳 5xx 錯誤時，應拋出 WebClientResponseException")
    void whenApiReturns5xx_thenShouldThrowException() {
        // Arrange
        // 模擬 API 回傳 500 Internal Server Error。
        when(responseSpec.bodyToMono(CoinDeskResponse.class))
                .thenReturn(Mono.error(WebClientResponseException.create(500, "Internal Server Error", null, null, null)));

        // Act & Assert
        assertThrows(WebClientResponseException.class, () -> coinDeskService.getOriginalCoinDeskData());
    }

    @Test
    @DisplayName("當 API 回傳格式錯誤的 JSON 時，應拋出相關的解析例外")
    void whenApiReturnsMalformedJson_thenShouldThrowException() {
        // Arrange
        // 模擬在 JSON 轉換為 DTO 物件時發生錯誤。
        when(responseSpec.bodyToMono(CoinDeskResponse.class))
                .thenReturn(Mono.error(new RuntimeException("Error parsing JSON")));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> coinDeskService.getOriginalCoinDeskData());
    }

}