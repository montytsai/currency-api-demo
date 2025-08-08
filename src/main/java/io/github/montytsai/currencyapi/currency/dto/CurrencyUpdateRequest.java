package io.github.montytsai.currencyapi.currency.dto;

import java.util.Optional;

import lombok.Getter;
import lombok.Setter;

/**
 * 用於「部分更新 (PATCH)」幣別資料的請求 DTO。
 * 所有欄位都是可選的，客戶端只需提供想要變更的欄位。
 * <br>
 * 使用 Optional 來區分「未提供」與「提供 null 值」的語義。
 * 涉及業務邏輯判斷，不在 Controller 使用 @Size，驗證移至 Service 層。
 */
@Getter
@Setter
public class CurrencyUpdateRequest {

    private Optional<String> displayName;

    private Optional<String> symbol;

}