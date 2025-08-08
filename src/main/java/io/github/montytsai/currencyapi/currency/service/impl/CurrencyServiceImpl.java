package io.github.montytsai.currencyapi.currency.service.impl;

import io.github.montytsai.currencyapi.currency.dto.CurrencyCreateRequest;
import io.github.montytsai.currencyapi.currency.dto.CurrencyReplaceRequest;
import io.github.montytsai.currencyapi.currency.dto.CurrencyUpdateRequest;
import io.github.montytsai.currencyapi.currency.entity.Currency;
import io.github.montytsai.currencyapi.currency.repository.CurrencyRepository;
import io.github.montytsai.currencyapi.currency.service.CurrencyService;
import io.github.montytsai.currencyapi.exception.ResourceAlreadyExistsException;
import io.github.montytsai.currencyapi.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    public CurrencyServiceImpl(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Override
    public List<Currency> findAllActive() {
        log.info("Fetching all active currencies.");
        return currencyRepository.findAllByIsActiveTrue();
    }

    @Override
    public Currency findActiveByCode(String code) {
        log.info("Fetching active currency with code: {}", code);
        return this.getActiveCurrencyOrThrow(code);
    }

    @Override
    public List<Currency> searchActiveByDisplayName(String name) {
        log.info("Searching for active currencies with display name containing: '{}'", name);
        return currencyRepository.findByDisplayNameContainingAndIsActiveTrue(name);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 採用函數式風格的 Optional.map().orElseGet() 處理，讓程式碼更具表達力且簡潔。
     */
    @Override
    @Transactional
    public Currency create(CurrencyCreateRequest request) {
        String code = request.getCode();
        log.info("Attempting to create or reactivate currency with code: {}", code);

        return currencyRepository.findById(code)
                .map(existingCurrency -> this.handleExistingCurrencyOnCreate(existingCurrency, request))
                .orElseGet(() -> this.createNewCurrency(request));
    }

    @Override
    @Transactional
    public Currency replace(String code, CurrencyReplaceRequest currencyRequest) {
        log.info("Performing full update for currency with code: {}", code);

        // 比對 url 與 dto 的幣別是否相同
        if (!code.equals(currencyRequest.getCode())) {
            throw new IllegalArgumentException("Path variable code '" + code + "' does not match request body code '" + currencyRequest.getCode() + "'.");
        }

        Currency existingCurrency = this.getActiveCurrencyOrThrow(code);

        // 完整替換：無論 request DTO 的欄位是否為 null，都直接設定
        existingCurrency.setDisplayName(currencyRequest.getDisplayName());
        existingCurrency.setSymbol(currencyRequest.getSymbol());

        return currencyRepository.save(existingCurrency);
    }

    @Override
    @Transactional
    public Currency partialUpdate(String code, CurrencyUpdateRequest currencyRequest) {
        log.info("Performing partial update for currency with code: {}", code);
        Currency existingCurrency = this.getActiveCurrencyOrThrow(code);

        this.validateAndSetDisplayName(existingCurrency, currencyRequest.getDisplayName());
        this.validateAndSetSymbol(existingCurrency, currencyRequest.getSymbol());

        return currencyRepository.save(existingCurrency);
    }

    @Override
    @Transactional
    public void softDeleteByCode(String code) {
        log.info("Performing soft delete for currency with code: {}", code);

        Currency currency = this.getActiveCurrencyOrThrow(code); // 只允許刪除啟用的幣別
        currency.setActive(false);

        currencyRepository.save(currency);

        log.info("Successfully soft-deleted currency with code: {}", code);
    }

    @Override
    @Transactional
    public Currency reactivateByCode(String code) {
        log.info("Attempting to reactivate currency with code: {}", code);
        Currency currency = currencyRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot reactivate. Currency not found with code: " + code));

        if (currency.isActive()) {
            log.warn("Attempted to reactivate an already active currency: {}. No action taken.", code);
            return currency; // 直接回傳，保持冪等性
        }

        currency.setActive(true);
        Currency reactivatedCurrency = currencyRepository.save(currency);
        log.info("Successfully reactivated currency with code: {}", code);
        return reactivatedCurrency;
    }

    // =================================================================
    // == Private Helper Methods
    // =================================================================

    /**
     * 根據代碼獲取一個「啟用」的幣別實體，若找不到或非啟用則拋出例外。
     *
     * @param code 幣別代碼
     * @return 啟用狀態的幣別實體
     * @throws ResourceNotFoundException 如果找不到或幣別非啟用
     */
    private Currency getActiveCurrencyOrThrow(String code) {
        return currencyRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> {
                    log.warn("Active currency not found with code: {}", code);
                    return new ResourceNotFoundException("Active currency not found with code: " + code);
                });
    }

    /**
     * 處理在 create 操作中遇到已存在幣別的邏輯。
     *
     * @param existingCurrency 已存在的幣別實體
     * @param request          傳入的請求 DTO
     * @return 更新並重新啟用後的幣別實體
     * @throws ResourceAlreadyExistsException 如果幣別已存在且處於啟用狀態
     */
    private Currency handleExistingCurrencyOnCreate(Currency existingCurrency, CurrencyCreateRequest request) {
        if (existingCurrency.isActive()) {
            log.warn("Failed to create currency. Code already exists and is active: {}", existingCurrency.getCode());
            throw new ResourceAlreadyExistsException("Currency with code '" + existingCurrency.getCode() + "' already exists.");
        }

        log.info("Currency with code {} exists but is inactive. Reactivating and updating.", existingCurrency.getCode());
        existingCurrency.setActive(true);
        existingCurrency.setDisplayName(request.getDisplayName());
        existingCurrency.setSymbol(request.getSymbol());
        return currencyRepository.save(existingCurrency);
    }

    /**
     * 處理建立一筆全新幣別的邏輯。
     *
     * @param request 傳入的請求 DTO
     * @return 新建立的幣別實體
     */
    private Currency createNewCurrency(CurrencyCreateRequest request) {
        log.info("Currency with code {} does not exist. Creating new one.", request.getCode());
        Currency currency = new Currency();
        currency.setCode(request.getCode());
        currency.setDisplayName(request.getDisplayName());
        currency.setSymbol(request.getSymbol());
        return currencyRepository.save(currency);
    }

    /**
     * 根據請求內容，驗證並設定幣別的顯示名稱 (displayName)。
     * <p>
     * 此方法專門處理 PATCH 的部分更新邏輯：
     * <ul>
     * <li>如果 `displayNameOpt` 為 `null`，代表客戶端請求中未包含此欄位，不執行任何操作。</li>
     * <li>如果 `displayNameOpt` 不為 `null`，則取出其中的值進行驗證並更新。</li>
     * </ul>
     *
     * @param currency 要更新的 Currency 實體
     * @param displayNameOpt 從 DTO 傳入的 Optional<String>，可能為 null
     * @throws IllegalArgumentException 如果提供的 displayName 為空或格式不符
     */
    @SuppressWarnings("OptionalAssignedToNull") // 這是針對 Jackson 反序列化行為的特定模式，因此抑制 IDE 警告。
    private void validateAndSetDisplayName(Currency currency, Optional<String> displayNameOpt) {
        // 檢查 Optional 物件本身是否為 null，以確定 JSON key 是否存在。
        if (displayNameOpt == null) {
            return;
        }

        String displayName = displayNameOpt.orElseThrow(() ->
                new IllegalArgumentException("Display name cannot be null when provided.")
        );
        if (displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Display name cannot be blank.");
        }
        if (displayName.length() > 50) {
            throw new IllegalArgumentException("Display name cannot exceed 50 characters.");
        }

        // 都驗證完成才進行修改
        log.debug("Applying partial update. Setting displayName to: '{}'", displayName);
        currency.setDisplayName(displayName);
    }

    /**
     * 根據請求內容，驗證並設定幣別的符號 (symbol)。
     * <p>
     * 此方法專門處理 PATCH 的部分更新邏輯：
     * <ul>
     * <li>如果 `symbolOpt` 為 `null`，代表客戶端請求中未包含此欄位，不執行任何操作。</li>
     * <li>如果 `symbolOpt` 不為 `null`，則取出其中的值（允許為 null）進行驗證並更新。</li>
     * </ul>
     *
     * @param currency 要更新的 Currency 實體
     * @param symbolOpt 從 DTO 傳入的 Optional<String>，可能為 null
     * @throws IllegalArgumentException 如果提供的 symbol 格式不符
     */
    @SuppressWarnings("OptionalAssignedToNull")  // 這是針對 Jackson 反序列化行為的特定模式，因此抑制 IDE 警告。
    private void validateAndSetSymbol(Currency currency, Optional<String> symbolOpt) {
        // 檢查 Optional 物件本身是否為 null，以確定 JSON key 是否存在。
        if (symbolOpt == null) {
            return;
        }

        // orElse(null) 允許客戶端透過傳入 {"symbol": null} 來清空 symbol
        String symbol = symbolOpt.orElse(null);

        if (symbol != null && symbol.length() > 10) {
            throw new IllegalArgumentException("Symbol cannot exceed 10 characters.");
        }

        log.debug("Applying partial update. Setting symbol to: '{}'", symbol);
        currency.setSymbol(symbol);
    }

}