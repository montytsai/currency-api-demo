package io.github.montytsai.currencyapi.currency.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.github.montytsai.currencyapi.currency.dto.CurrencyCreateRequest;
import io.github.montytsai.currencyapi.currency.dto.CurrencyReplaceRequest;
import io.github.montytsai.currencyapi.currency.dto.CurrencyResponse;
import io.github.montytsai.currencyapi.currency.dto.CurrencyUpdateRequest;
import io.github.montytsai.currencyapi.currency.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Validated
@Tag(name = "Currency Management", description = "提供幣別資料的新增、查詢、修改、刪除功能")
@RestController
@RequestMapping("/currencies")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    @Operation(summary = "查詢所有「啟用」的幣別資料")
    @GetMapping
    public ResponseEntity<List<CurrencyResponse>> getAllCurrencies() {
        List<CurrencyResponse> response = currencyService.findAllActive().stream()
                .map(CurrencyResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "依代碼查詢單一「啟用」的幣別", description = "透過幣別代碼 (如: USD) 取得單一筆 is_active 為 true 的幣別資料。")
    @ApiResponse(responseCode = "200", description = "成功找到幣別資料")
    @ApiResponse(responseCode = "404", description = "找不到對應的幣別資料或該幣別非啟用狀態", content = @Content)
    @GetMapping("/{code}")
    public ResponseEntity<CurrencyResponse> getCurrencyByCode(
            @Parameter(description = "要查詢的幣別代碼", required = true, example = "USD")
            @NotBlank @Size(min = 3, max = 10) @PathVariable String code) {
        CurrencyResponse response = CurrencyResponse.fromEntity(currencyService.findActiveByCode(code));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "依顯示名稱模糊搜尋「啟用」的幣別", description = "輸入關鍵字，查詢顯示名稱包含該關鍵字且 is_active 為 true 的所有幣別。")
    @GetMapping("/search")
    public ResponseEntity<List<CurrencyResponse>> searchCurrencies(
            @Parameter(description = "顯示名稱搜尋關鍵字", required = true, example = "元")
            @NotBlank @RequestParam String name) {
        List<CurrencyResponse> response = currencyService.searchActiveByDisplayName(name).stream()
                .map(CurrencyResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "新增一筆幣別資料", description = "建立一筆新的幣別資料。代碼不可重複。")
    @ApiResponse(responseCode = "201", description = "成功建立")
    @ApiResponse(responseCode = "400", description = "請求內容格式錯誤或驗證失敗", content = @Content)
    @ApiResponse(responseCode = "409", description = "該幣別代碼已存在，發生衝突", content = @Content)
    @PostMapping
    public ResponseEntity<CurrencyResponse> createCurrency(@Valid @RequestBody CurrencyCreateRequest request) {
        CurrencyResponse response = CurrencyResponse.fromEntity(currencyService.create(request));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "「完整替換」指定幣別資料 (PUT)",
            description = "執行**完整替換 (Full Replace)** 操作，此操作具備冪等性 (idempotent)。<br>" +
                    "請求中**必須**提供目標資源的所有可修改欄位 (`code`, `displayName`, `symbol`)。" +
                    "伺服器將使用請求的內容，完整地覆蓋掉現有的幣別資料。"
    )
    @PutMapping("/{code}")
    public ResponseEntity<CurrencyResponse> replaceCurrency(
            @Parameter(description = "要更新的幣別代碼", required = true)
            @NotBlank @Size(min = 3, max = 10) @PathVariable String code,
            @Valid @RequestBody CurrencyReplaceRequest currencyRequest) {
        CurrencyResponse response = CurrencyResponse.fromEntity(currencyService.replace(code, currencyRequest));
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "「部分更新」指定幣別資料 (PATCH)",
            description = "執行**部分更新 (Partial Update)** 操作。<br><br>" +
                    "<ul>" +
                    "<li>只有在請求本文 (request body) 中**明確提供的欄位**會被更新。</li>" +
                    "<li>任何**未包含**在請求中的欄位，將會**保持其現有值不變**。</li>" +
                    "<li>若要將一個可選欄位（如 `symbol`）的值清空，必須在請求中明確地將其值設為 `null`，例如：`{\"symbol\": null}`。</li>" +
                    "</ul>"
    )
    @PatchMapping("/{code}")
    public ResponseEntity<CurrencyResponse> partialUpdateCurrency(
            @Parameter(description = "要更新的幣別代碼", required = true)
            @NotBlank @Size(min = 3, max = 10) @PathVariable String code,
            @Valid @RequestBody CurrencyUpdateRequest request) { // 注意: 此處 @Valid 對 Optional DTO 無效，實際驗證在 Service 層
        CurrencyResponse response = CurrencyResponse.fromEntity(currencyService.partialUpdate(code, request));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "軟刪除指定幣別資料", description = "根據幣別代碼，將其狀態設為非啟用 (is_active = false)。")
    @ApiResponse(responseCode = "204", description = "成功刪除 (沒有回傳內容)")
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteCurrency(
            @Parameter(description = "要刪除的幣別代碼", required = true, example = "USD")
            @NotBlank @Size(min = 3, max = 10) @PathVariable String code) {
        currencyService.softDeleteByCode(code);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "重新啟用指定幣別", description = "將一個已被軟刪除的幣別狀態重新設為啟用 (is_active = true)。")
    @ApiResponse(responseCode = "200", description = "成功重新啟用")
    @PostMapping("/{code}/reactivate")
    public ResponseEntity<CurrencyResponse> reactivateCurrency(
            @Parameter(description = "要重新啟用的指定幣別", required = true, example = "CAD")
            @NotBlank @Size(min = 3, max = 10) @PathVariable String code) {
        CurrencyResponse response = CurrencyResponse.fromEntity(currencyService.reactivateByCode(code));
        return ResponseEntity.ok(response);
    }

}