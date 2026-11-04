-- ===================================================================
-- SMART SPEND - SAMPLE DEMO DATA (PORTFOLIO & INTERVIEW READY)
-- Realistic Indian personal finance records in Indian Rupees (₹)
-- Passwords for both demo accounts: Password@123
-- ===================================================================

USE `smartspend`;

-- 1. Insert Demo Users (BCrypt hash for "Password@123")
INSERT INTO `users` (`id`, `full_name`, `email`, `username`, `password_hash`, `currency`, `is_active`)
VALUES
(101, 'Kartik Vairagade', 'kartik.demo@smartspend.in', 'kartik_demo', '$2a$12$YGV4eJXgiEQm7EFzLU.xTuBdmsMunMWE8qSzZtnU.4AZmXuBxk76i', 'INR', TRUE),
(102, 'Priya Sharma', 'priya.demo@smartspend.in', 'priya_demo', '$2a$12$YGV4eJXgiEQm7EFzLU.xTuBdmsMunMWE8qSzZtnU.4AZmXuBxk76i', 'INR', TRUE)
ON DUPLICATE KEY UPDATE `full_name` = VALUES(`full_name`);

-- 2. Insert Custom Categories for User 101 (Kartik)
INSERT INTO `categories` (`id`, `user_id`, `name`, `type`, `icon`, `color`, `is_default`)
VALUES
(201, 101, 'Stock Dividends', 'INCOME', 'trending-up', '#10b981', FALSE),
(202, 101, 'Gym & Fitness', 'EXPENSE', 'activity', '#06b6d4', FALSE),
(203, 101, 'OTT Subscriptions', 'EXPENSE', 'tv', '#a855f7', FALSE)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 3. Insert Monthly Budgets for User 101 (May 2026)
-- Food & Dining (Cat 7): ₹12,000 | Groceries (Cat 8): ₹8,000 | Utilities (Cat 10): ₹4,000
INSERT INTO `budgets` (`user_id`, `category_id`, `monthly_limit`, `month`, `year`)
VALUES
(101, 7, 12000.00, 5, 2026),
(101, 8, 8000.00, 5, 2026),
(101, 10, 4000.00, 5, 2026)
ON DUPLICATE KEY UPDATE `monthly_limit` = VALUES(`monthly_limit`);

-- 4. Insert Realistic INR Transactions for User 101 (Kartik)
INSERT INTO `transactions` (`user_id`, `category_id`, `type`, `amount`, `description`, `transaction_date`)
VALUES
(101, 1, 'INCOME', 75000.00, 'Monthly Software Developer Salary', '2026-05-01'),
(101, 2, 'INCOME', 20000.00, 'Freelance Java Web Application Project', '2026-05-08'),
(101, 201, 'INCOME', 3500.00, 'Quarterly TCS Equity Dividend', '2026-05-15'),
(101, 9, 'EXPENSE', 18000.00, 'House Rent Payment', '2026-05-02'),
(101, 8, 'EXPENSE', 6450.00, 'Monthly Grocery Supermarket Restock', '2026-05-05'),
(101, 10, 'EXPENSE', 2250.00, 'Electricity and Fiber Internet Bill', '2026-05-10'),
(101, 7, 'EXPENSE', 3800.00, 'Weekend Dinner with Friends', '2026-05-12'),
(101, 11, 'EXPENSE', 3200.00, 'Fuel and Metro Smart Card Recharge', '2026-05-16'),
(101, 202, 'EXPENSE', 2500.00, 'Gold Gym Quarterly Membership', '2026-05-18'),
(101, 14, 'EXPENSE', 1800.00, 'Java Concurrency in Practice & Cloud Books', '2026-05-20');

-- 5. Insert Separate Independent Transactions for User 102 (Priya) [Demonstrating Multi-User Isolation]
INSERT INTO `transactions` (`user_id`, `category_id`, `type`, `amount`, `description`, `transaction_date`)
VALUES
(102, 1, 'INCOME', 85000.00, 'Senior Analyst Monthly Salary', '2026-05-01'),
(102, 9, 'EXPENSE', 22000.00, 'Apartment Lease Rent', '2026-05-03'),
(102, 7, 'EXPENSE', 4500.00, 'Fine Dining & Coffee', '2026-05-07'),
(102, 12, 'EXPENSE', 6500.00, 'Zara Summer Apparel Shopping', '2026-05-14');
