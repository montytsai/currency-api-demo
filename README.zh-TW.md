# 📦 專案介紹：Currency API Demo

[![Java CI with Maven](https://github.com/montytsai/currency-api-demo/actions/workflows/ci.yml/badge.svg)](https://github.com/montytsai/currency-api-demo/actions/workflows/ci.yml)

本專案為一個展現後端工程實踐的技術範例，使用 Java 8 與 Spring Boot 2 開發。
專案核心在於演示一個結構清晰、具備完整測試與 CI 自動化的服務是如何建構的，並以 Coindesk API 作為外部服務整合與資料轉換的具體實例。

[English](README.md) | 繁體中文

---

## 📄 互動式 API 文件 Swagger UI

啟動後可透過 Swagger UI 查看並操作 API：

> **Swagger UI**：[`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html)

---

## ✨ 功能亮點

- **分層與模組化設計**：依功能切分獨立模組，並在模組內遵循清晰的分層架構，提升可維護性與擴充性。
- **資料庫操作 (CRUD)**：使用 Spring Data JPA 實作完整的資料庫讀寫、更新與刪除功能。
- **外部服務整合**：演示如何串接、處理並轉換第三方 REST API 資料。
- **資料轉換層設計**：將外部原始資料，透過獨立的 Mapper 轉換為符合內部需求的豐富格式。
- **單元與整合測試**：使用 JUnit 5 與 Mockito，針對核心邏輯與 API 端點撰寫測試，確保程式碼品質。
- **API 文件化**：整合 Swagger (SpringDoc) 自動生成互動式 API 文件。
- **CI 自動化建置**：設定 GitHub Actions，在每次提交後自動執行編譯與測試，確保程式碼穩定性。

---

## 🛠️ 技術棧

- **後端**：Java 8、Spring Boot 2.7.18
- **資料庫**：H2 (In-Memory)
- **ORM**：Spring Data JPA
- **測試**：JUnit 5、Mockito
- **文件**：SpringDoc OpenAPI (Swagger UI)
- **自動化**：Maven、GitHub Actions

---

## 🏛️ 專案架構與模組設計

本專案採用「**按功能模組，內部再分層**」的結構，旨在實現高內聚、低耦合的目標。
> 這種模組化設計讓開發者能快速理解各功能邊界，便於單元測試與未來功能擴充。

```
io.github.montytsai.currencyapi
│
├── currency         # 幣別資料管理模組
│ ├── controller     # 處理請求
│ ├── service        # 業務邏輯 (含介面與實作)
│ ├── repository     # 資料存取 (JPA)
│ ├── entity         # JPA 實體
│ └── dto            # 資料傳輸物件
│
├── coindesk         # 外部 API 整合模組
│ ├── controller
│ ├── service
│ ├── mapper         # 負責資料轉換
│ └── dto
│
├── config           # 全域設定
└── exception        # 全域例外處理
```

---

## 🚀 快速開始

### 執行前置需求

- Java 8
- Maven

### 使用 Maven 執行

```bash
mvn spring-boot:run
```

啟動後應用服務位於：http://localhost:8080

---

## 🧪 執行測試

```bash
mvn test
```

---

## 📦 Docker 支援
(專案已具備容器化能力，提供 Dockerfile)

```bash
# 建置 Docker 映像
docker build -t currency-api-demo .

# 運行容器
docker run -p 8080:8080 currency-api-demo
```

---

## 📄 API 說明

完整的 API 說明與範例，請參考上方的 [Swagger UI](#-互動式-API-文件-Swagger-UI)。

---

## 📃 授權說明
本專案僅用於學習與展示用途，不用於商業部署。
