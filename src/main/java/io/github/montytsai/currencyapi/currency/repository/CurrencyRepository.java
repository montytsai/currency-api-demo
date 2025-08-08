package io.github.montytsai.currencyapi.currency.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.github.montytsai.currencyapi.currency.entity.Currency;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {

    /**
     * 查詢所有狀態為「啟用 (active)」的幣別。
     * 這是主要的列表查詢方法，過濾掉已被軟刪除的資料。
     *
     * @return 符合條件的幣別列表，若無則回傳空 List
     */
    List<Currency> findAllByIsActiveTrue();

    /**
     * 根據幣別代碼，查詢狀態為「啟用 (active)」的單一幣別。
     *
     * @param code 幣別代碼
     * @return 包含幣別的 Optional，若找不到或幣別非啟用狀態則回傳空 Optional
     */
    Optional<Currency> findByCodeAndIsActiveTrue(String code);

    /**
     * 根據顯示名稱進行模糊搜尋，並只回傳狀態為「啟用 (active)」的幣別。
     *
     * @param displayName 搜尋的關鍵字
     * @return 符合條件的幣別列表，若無則回傳空 List
     */
    List<Currency> findByDisplayNameContainingAndIsActiveTrue(String displayName);

}