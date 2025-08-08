package io.github.montytsai.currencyapi.coindesk.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.github.montytsai.currencyapi.coindesk.mapper.CoinDeskMapper;
import io.github.montytsai.currencyapi.currency.repository.CurrencyRepository;

/**
 * 針對 CoinDeskServiceImpl 的例外與邊界條件進行單元測試。
 */
@ExtendWith(MockitoExtension.class)
class CoinDeskServiceExceptionTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private CurrencyRepository currencyRepository;
    @Mock
    private CoinDeskMapper coinDeskMapper;

    @InjectMocks
    private CoinDeskServiceImpl coinDeskService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(coinDeskService, "coinDeskApiUrl", "http://fake-url.com");
    }

    @Test
    @DisplayName("[TC008] 當外部 API 連線逾時，應拋出 ResourceAccessException")
    void whenApiTimeout_thenShouldThrowException() {
        // Arrange: 模擬 RestTemplate 在呼叫時拋出連線逾時的例外
        when(restTemplate.getForObject(anyString(), eq(io.github.montytsai.currencyapi.coindesk.dto.CoinDeskResponse.class)))
                .thenThrow(new ResourceAccessException("I/O error on GET request: connect timed out"));

        // Act & Assert: 驗證呼叫服務方法時，會拋出預期的例外
        assertThrows(ResourceAccessException.class, () -> {
            coinDeskService.getOriginalCoinDeskData();
        });
    }

    @Test
    @DisplayName("[TC009] 當外部 API 回傳 4xx 錯誤，應拋出 HttpClientErrorException")
    void whenApiReturns4xx_thenShouldThrowException() {
        // Arrange: 模擬 API 回傳 404 Not Found
        when(restTemplate.getForObject(anyString(), eq(io.github.montytsai.currencyapi.coindesk.dto.CoinDeskResponse.class)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        // Act & Assert
        assertThrows(HttpClientErrorException.class, () -> {
            coinDeskService.getOriginalCoinDeskData();
        });
    }

    @Test
    @DisplayName("[TC010] 當外部 API 回傳 5xx 錯誤，應拋出 HttpServerErrorException")
    void whenApiReturns5xx_thenShouldThrowException() {
        // Arrange: 模擬 API 回傳 500 Internal Server Error
        when(restTemplate.getForObject(anyString(), eq(io.github.montytsai.currencyapi.coindesk.dto.CoinDeskResponse.class)))
                .thenThrow(HttpServerErrorException.InternalServerError.class);

        // Act & Assert
        assertThrows(HttpServerErrorException.class, () -> {
            coinDeskService.getOriginalCoinDeskData();
        });
    }

    @Test
    @DisplayName("[TC012] 當外部 API 回傳格式錯誤的 JSON，RestTemplate 應拋出相關例外")
    void whenApiReturnsMalformedJson_thenRestTemplateShouldThrowException() {
        // Arrange: 模擬 RestTemplate 因無法解析 JSON 而拋出例外
        // (實際例外類型可能為 JsonParseException 或其他 MappingException，此處用 RuntimeException 代表)
        when(restTemplate.getForObject(anyString(), eq(io.github.montytsai.currencyapi.coindesk.dto.CoinDeskResponse.class)))
                .thenThrow(new RuntimeException("Error parsing JSON"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            coinDeskService.getOriginalCoinDeskData();
        });
    }

}