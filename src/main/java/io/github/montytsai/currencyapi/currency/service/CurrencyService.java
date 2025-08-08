package io.github.montytsai.currencyapi.currency.service;

import java.util.List;

import io.github.montytsai.currencyapi.currency.dto.CurrencyCreateRequest;
import io.github.montytsai.currencyapi.currency.dto.CurrencyReplaceRequest;
import io.github.montytsai.currencyapi.currency.dto.CurrencyUpdateRequest;
import io.github.montytsai.currencyapi.currency.entity.Currency;
import io.github.montytsai.currencyapi.exception.ResourceNotFoundException;
import io.github.montytsai.currencyapi.exception.ResourceAlreadyExistsException;

/**
 * 處理幣別資料相關的商業邏輯服務介面。
 * 定義了幣別資料的 CRUD 與搜尋操作合約。
 */
public interface CurrencyService {

    /**
     * 查詢所有「啟用」的幣別資料。
     *
     * @return 包含所有啟用幣別資料的 List，若無資料則回傳空 List
     */
    List<Currency> findAllActive();

    /**
     * 根據幣別代碼查詢單一筆「啟用」的資料。
     *
     * @param code 幣別代碼 (e.g., "USD")
     * @return 對應的 Currency 物件
     * @throws ResourceNotFoundException 如果找不到對應的幣別或幣別非啟用狀態
     */
    Currency findActiveByCode(String code);

    /**
     * 根據顯示名稱關鍵字進行模糊搜尋，僅搜尋「啟用」的幣別。
     *
     * @param name 顯示名稱的搜尋關鍵字
     * @return 符合條件的幣別資料 List，若無則回傳空 List
     */
    List<Currency> searchActiveByDisplayName(String name);

    /**
     * 根據提供的請求內容，建立一筆新的幣別資料。
     * <p>
     * 此方法具備處理邏輯：
     * <ul>
     * <li>若幣別代碼完全不存在，則建立新資料。</li>
     * <li>若幣別代碼已存在但處於非啟用狀態，則將其重新啟用並更新資料。</li>
     * </ul>
     *
     * @param currencyRequest 包含新幣別資料的請求物件
     * @return 已成功建立或重新啟用的 Currency 物件
     * @throws ResourceAlreadyExistsException 如果該幣別代碼已存在且處於啟用狀態
     */
    Currency create(CurrencyCreateRequest currencyRequest);

    /**
     * 根據幣別代碼，更新對應的「啟用」幣別資料。
     * 執行 PUT 的語義：完整替換。
     *
     * @param code            欲更新的幣別代碼
     * @param currencyRequest 包含更新資訊的請求物件
     * @return 已成功更新的 Currency 物件
     * @throws ResourceNotFoundException 如果找不到對應的幣別或幣別非啟用狀態
     */
    Currency replace(String code, CurrencyReplaceRequest currencyRequest);

    /**
     * 根據幣別代碼，部分更新對應的「啟用」幣別資料。
     * 執行 PATCH 的語義：只會更新請求中有提供的欄位。
     *
     * @param code            欲更新的幣別代碼
     * @param currencyRequest 包含部分更新資訊的請求物件
     * @return 已成功更新的 Currency 物件
     * @throws ResourceNotFoundException 如果找不到對應的幣別或幣別非啟用狀態
     */
    Currency partialUpdate(String code, CurrencyUpdateRequest currencyRequest);

    /**
     * 根據幣別代碼，軟刪除一筆資料 (將其狀態設為非啟用)。
     *
     * @param code 欲軟刪除的幣別代碼
     * @throws ResourceNotFoundException 如果找不到對應的幣別或幣別非啟用狀態
     */
    void softDeleteByCode(String code);

    /**
     * 根據幣別代碼，重新啟用一個已被軟刪除的幣別。
     * 若該幣別已是啟用狀態，則直接回傳該幣別，不進行任何操作。
     *
     * @param code 欲重新啟用的幣別代碼
     * @return 已成功啟用的 Currency 物件
     * @throws ResourceNotFoundException 如果找不到對應的幣別
     */
    Currency reactivateByCode(String code);

}