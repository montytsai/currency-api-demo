package io.github.montytsai.currencyapi.coindesk.controller;

import io.github.montytsai.currencyapi.coindesk.service.CoinDeskService;
import io.github.montytsai.currencyapi.coindesk.dto.TransformedCoinDeskResponse;
import io.github.montytsai.currencyapi.exception.GlobalExceptionHandler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * 對 CoinDeskController 的 Web 層進行切片測試 (Slice Test)。
 *
 * @WebMvcTest(CoinDeskController.class) - 作用：只載入 Web 層相關的 Spring 環境 (如 Controller, Filter, Json-converter)，
 * 而不會載入完整的 Service 或 Repository 層，因此測試速度快且目標專注。
 * @MockBean(CoinDeskService.class) - 作用：建立一個 Service 層的 Mock 物件 (假替身) 並放入 Spring Context。
 * 這讓我們可以隔離 Controller，專注於測試請求路由、參數綁定和回應序列化等 Web 相關邏輯，
 * 而不用擔心 Service 層的實際行為。
 * @Import(GlobalExceptionHandler.class) - 作用：在測試環境中明確地匯入我們自訂的全域例外處理器。
 * 這樣，當 Controller 或 Mock Service 拋出例外時，這個處理器才會生效，
 * 我們才能驗證它產生的錯誤回應是否符合預期。
 */
@WebMvcTest(CoinDeskController.class)
@Import(GlobalExceptionHandler.class)
class CoinDeskControllerTest {

    @Autowired
    private MockMvc mockMvc; // 由 @WebMvcTest 自動組態，用於模擬 HTTP 請求。

    @MockBean
    private CoinDeskService coinDeskService; // 建立 Service 的 Mock 替身。

    @Test
    @DisplayName("[P0.1] 成功路徑測試：呼叫轉換 API 應回傳 HTTP 200 與正確的 JSON 內容")
    void testGetTransformedData_Success() throws Exception {
        // --- Arrange ---
        // 1. 準備一個假的 Service 回應 DTO。
        TransformedCoinDeskResponse mockResponse = new TransformedCoinDeskResponse();
        mockResponse.setUpdatedTime("2024/09/02 10:20:30");

        TransformedCoinDeskResponse.CurrencyInfo info = new TransformedCoinDeskResponse.CurrencyInfo();
        info.setCode("TWD");
        info.setChineseName("台幣");
        info.setRate(30.5f);
        mockResponse.setCurrencyInfo(Collections.singletonList(info));

        // 2. 設定 Mock 行為：當 coinDeskService.getTransformedCoinDeskData() 被呼叫時，回傳我們準備好的假物件。
        when(coinDeskService.getTransformedCoinDeskData()).thenReturn(mockResponse);

        // --- Act & Assert ---
        // 3. 透過 mockMvc 模擬發送 GET 請求，並驗證回應。
        mockMvc.perform(get("/coindesk/transformed"))
                .andExpect(status().isOk()) // 驗證 HTTP 狀態碼為 200 (OK)
                .andExpect(jsonPath("$.updatedTime", is("2024/09/02 10:20:30"))) // 使用 JsonPath 驗證回應內容
                .andExpect(jsonPath("$.currencyInfo[0].code", is("TWD")))
                .andExpect(jsonPath("$.currencyInfo[0].chineseName", is("台幣")))
                .andExpect(jsonPath("$.currencyInfo[0].rate", is(30.5)));
    }

    @Test
    @DisplayName("[P0.2] 全域例外處理測試：當 Service 層拋出例外時，應回傳符合 ErrorResponse 格式的 HTTP 500 錯誤")
    void whenServiceThrowsException_thenControllerShouldReturn500() throws Exception {
        // --- Arrange ---
        // 1. 模擬 Service 層在執行時拋出一個未預期的 RuntimeException。
        when(coinDeskService.getTransformedCoinDeskData())
                .thenThrow(new RuntimeException("A critical error occurred in the service layer"));

        // --- Act & Assert ---
        // 2. 驗證 API 是否回傳 500，並檢查 JSON body 是否符合我們在 GlobalExceptionHandler 中定義的 ErrorResponse 格式。
        mockMvc.perform(get("/coindesk/transformed"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", is("An unexpected internal server error occurred. Please contact support.")))
                .andExpect(jsonPath("$.path", is("uri=/coindesk/transformed"))) // 驗證 path 欄位
                .andExpect(jsonPath("$.timestamp", notNullValue())); // 驗證 timestamp 欄位存在且不為 null
    }

}