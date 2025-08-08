package io.github.montytsai.currencyapi.currency.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.montytsai.currencyapi.currency.dto.CurrencyCreateRequest;
import io.github.montytsai.currencyapi.currency.dto.CurrencyReplaceRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 確保每個測試方法都在獨立的交易中運行，並在結束後自動回滾
@ActiveProfiles("test")
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_PATH = "/currencies";

    // =================================================================
    // == 查詢 (Read) 測試
    // =================================================================

    @Test
    @DisplayName("[C006] 查詢所有幣別應回傳 200 OK 且包含初始「啟用」資料")
    void testGetAllCurrencies() throws Exception {
        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))) // data.sql 中有 3 筆 active 的資料
                .andExpect(jsonPath("$[0].code", is("USD")));
    }

    @Test
    @DisplayName("[C009] 依存在的幣別代碼查詢應回傳 200 OK 及對應資料")
    void testGetCurrencyByCode_Success() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("USD")))
                .andExpect(jsonPath("$.displayName", is("美金")));
    }

    @Test
    @DisplayName("[C010] 依不存在的幣別代碼查詢應回傳 404 Not Found")
    void testGetCurrencyByCode_NotFound() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/JPY"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[C011] 依空白的幣別代碼查詢應回傳 400 Bad Request")
    void testGetCurrencyByCode_WithBlankCode_ShouldReturn400() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/   ")) // 使用空白字串
                .andExpect(status().isBadRequest());
    }

    // =================================================================
    // == 新增 (Create) 測試
    // =================================================================

    @Test
    @DisplayName("[C001] 新增一筆新的幣別應回傳 201 Created")
    void testCreateCurrency_Success() throws Exception {
        CurrencyCreateRequest newCurrency = new CurrencyCreateRequest();
        newCurrency.setCode("JPY");
        newCurrency.setDisplayName("日圓");
        newCurrency.setSymbol("¥");

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCurrency)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is("JPY")));
    }

    @Test
    @DisplayName("[C003] 新增已存在的幣別應回傳 409 Conflict")
    void testCreateCurrency_Conflict() throws Exception {
        CurrencyCreateRequest existingCurrency = new CurrencyCreateRequest();
        existingCurrency.setCode("USD");
        existingCurrency.setDisplayName("美元");
        existingCurrency.setSymbol("$");

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingCurrency)))
                .andExpect(status().isConflict());
    }

    @DisplayName("[C002] 新增時若提供無效的幣別資料，應回傳 400 Bad Request")
    @ParameterizedTest(name = "[{0}] {3}")
    @CsvSource({
            " 'V002',     'J',           '日圓',       '代碼長度不足'",
            " 'V003',     'TOOLONGGGGG', '日圓',       '代碼長度過長'",
            " 'V001',     ''   ,         '日圓',       '代碼為空字串'",
            " 'V005',     'JPY',         ''   ,       '顯示名稱為空字串'"
    })
    void testCreateCurrency_WithInvalidData_ShouldReturn400(String testCaseId, String code, String displayName, String testCaseName) throws Exception {
        CurrencyCreateRequest badRequest = new CurrencyCreateRequest();
        badRequest.setCode(code);
        badRequest.setDisplayName(displayName);
        badRequest.setSymbol("S"); // Symbol 必須提供

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());
    }

    // =================================================================
    // == 更新 (Update) 測試
    // =================================================================

    @Test
    @DisplayName("[C015] PUT - 使用完整資料更新應回傳 200 OK")
    void testReplaceCurrency_WithPut_Success() throws Exception {
        CurrencyReplaceRequest fullUpdateRequest = new CurrencyReplaceRequest();
        fullUpdateRequest.setCode("USD");
        fullUpdateRequest.setDisplayName("美國元");
        fullUpdateRequest.setSymbol("US$");

        mockMvc.perform(put(BASE_PATH + "/USD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fullUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName", is("美國元")))
                .andExpect(jsonPath("$.symbol", is("US$")));
    }

    @Test
    @DisplayName("PATCH - 部分更新應回傳 200 OK 且未提供欄位應保持不變")
    void testPartialUpdateCurrency_WithPatch_ShouldKeepOldValues() throws Exception {
        // 請求中只提供 displayName，不提供 symbol
        String partialRequestJson = "{\"displayName\": \"新台幣\"}";

        mockMvc.perform(patch(BASE_PATH + "/USD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(partialRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName", is("新台幣")))
                .andExpect(jsonPath("$.symbol", is("$"))); // 驗證 symbol 保持原樣
    }

    @Test
    @DisplayName("PATCH - 提供 null 值應能成功將 symbol 更新為 null")
    void testPartialUpdateCurrency_WithNullValue_ShouldUpdateToNull() throws Exception {
        // 請求中只提供 symbol，且值為 null
        String partialRequestJson = "{\"symbol\": null}";

        // USD 的 symbol 初始值為 "$"
        mockMvc.perform(patch(BASE_PATH + "/USD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(partialRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName", is("美金"))) // 驗證 displayName 保持原樣
                .andExpect(jsonPath("$.symbol").doesNotExist()); // 驗證 symbol 已變為 null
    }

    @Test
    @DisplayName("[C018] 更新時若路徑ID與內容ID不符應回傳 400 Bad Request")
    void testUpdateCurrency_MismatchedId() throws Exception {
        CurrencyReplaceRequest request = new CurrencyReplaceRequest();
        request.setCode("CAD");
        request.setDisplayName("Canadian Dollar");
        request.setSymbol("C$");

        mockMvc.perform(put(BASE_PATH + "/USD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =================================================================
    // == 刪除與啟用 (Delete & Reactivate) 測試
    // =================================================================

    @Test
    @DisplayName("[C021] 軟刪除一筆存在的幣別應回傳 204 No Content")
    void testSoftDeleteCurrency_Success() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/USD"))
                .andExpect(status().isNoContent());

        // 驗證是否真的被軟刪除 (查詢啟用列表時應找不到)
        mockMvc.perform(get(BASE_PATH + "/USD"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("重新啟用已被軟刪除的幣別應回傳 200 OK")
    void testReactivateCurrency_Success() throws Exception {
        // data.sql 中 CAD is_active = false
        mockMvc.perform(post(BASE_PATH + "/CAD/reactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("CAD")))
                .andExpect(jsonPath("$.active", is(true)));

        // 驗證是否真的被重新啟用
        mockMvc.perform(get(BASE_PATH + "/CAD"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("新增已軟刪除的幣別應將其重新啟用並更新，回傳 201 Created")
    void testCreateCurrency_WhenInactive_ShouldReactivateAndUpdate() throws Exception {
        CurrencyCreateRequest reactivateRequest = new CurrencyCreateRequest();
        reactivateRequest.setCode("CAD"); // CAD 在 data.sql 中是 inactive
        reactivateRequest.setDisplayName("新加幣");
        reactivateRequest.setSymbol("C$$");

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reactivateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.displayName", is("新加幣")))
                .andExpect(jsonPath("$.active", is(true)));
    }

}