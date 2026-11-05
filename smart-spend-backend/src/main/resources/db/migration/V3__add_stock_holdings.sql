-- ===================================================================
-- V3: Add Stock Holdings Table for Live Investment Portfolio
-- Tracks stocks, quantities, purchase price in INR, and live valuation
-- ===================================================================

CREATE TABLE IF NOT EXISTS stock_holdings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(30) NOT NULL,
    company_name VARCHAR(150) NOT NULL,
    quantity INT NOT NULL,
    buy_price DECIMAL(12, 2) NOT NULL,
    buy_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_stocks_user (user_id),
    INDEX idx_stocks_user_symbol (user_id, symbol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
