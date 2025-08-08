package io.github.montytsai.currencyapi.coindesk.controller;

import io.github.montytsai.currencyapi.coindesk.dto.CoinDeskResponse;
import io.github.montytsai.currencyapi.coindesk.dto.TransformedCoinDeskResponse;
import io.github.montytsai.currencyapi.coindesk.service.CoinDeskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "CoinDesk API", description = "提供 CoinDesk 資料的串接與轉換功能")
@RestController
@RequestMapping("/coindesk")
public class CoinDeskController {

    private final CoinDeskService coinDeskService;

    public CoinDeskController(CoinDeskService coinDeskService) {
        this.coinDeskService = coinDeskService;
    }

    @Operation(summary = "呼叫原始 CoinDesk API", description = "直接回傳 CoinDesk API 的原始 JSON 結構，用於驗證與除錯。")
    @GetMapping("/original")
    public ResponseEntity<CoinDeskResponse> getOriginalData() {
        CoinDeskResponse response = coinDeskService.getOriginalCoinDeskData();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "呼叫資料轉換後的新 API", description = "將 CoinDesk API 資料進行轉換，整合本地資料庫的中文幣別名稱後回傳。")
    @GetMapping("/transformed")
    public ResponseEntity<TransformedCoinDeskResponse> getTransformedData() {
        TransformedCoinDeskResponse response = coinDeskService.getTransformedCoinDeskData();
        return ResponseEntity.ok(response);
    }

}
