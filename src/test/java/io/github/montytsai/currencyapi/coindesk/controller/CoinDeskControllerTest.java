package io.github.montytsai.currencyapi.coindesk.controller;

import io.github.montytsai.currencyapi.coindesk.service.CoinDeskService;
import io.github.montytsai.currencyapi.coindesk.dto.TransformedCoinDeskResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@WebMvcTest(CoinDeskController.class)
class CoinDeskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CoinDeskService coinDeskService;

    @Test
    void testGetTransformedData_Success() throws Exception {
        // Arrange
        TransformedCoinDeskResponse mockResponse = new TransformedCoinDeskResponse();
        mockResponse.setUpdatedTime("2024/09/02 10:20:30");

        TransformedCoinDeskResponse.CurrencyInfo info = new TransformedCoinDeskResponse.CurrencyInfo();
        info.setCode("TWD");
        info.setChineseName("台幣");
        info.setRate(30.5f);
        mockResponse.setCurrencyInfo(Collections.singletonList(info));

        when(coinDeskService.getTransformedCoinDeskData()).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/coindesk/transformed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedTime", is("2024/09/02 10:20:30")))
                .andExpect(jsonPath("$.currencyInfo[0].code", is("TWD")))
                .andExpect(jsonPath("$.currencyInfo[0].chineseName", is("台幣")));
    }

    @Test
    @DisplayName("當 Service 層拋出未預期例外時，Controller 應回傳 500 Internal Server Error")
    void whenServiceThrowsException_thenControllerShouldReturn500() throws Exception {
        // Arrange: 模擬 Service 層在執行時拋出一個 RuntimeException
        when(coinDeskService.getTransformedCoinDeskData())
                .thenThrow(new RuntimeException("A critical error occurred in the service layer"));

        // Act & Assert: 驗證 API 回應是否為 500，並檢查回傳的錯誤訊息結構
        mockMvc.perform(get("/coindesk/transformed"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", is("An unexpected internal server error occurred. Please contact support.")));
    }

}