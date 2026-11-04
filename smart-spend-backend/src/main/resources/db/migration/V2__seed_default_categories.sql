-- ===================================================================
-- V2: Seed Default Categories for Income and Expenses
-- Available to all users out-of-the-box
-- ===================================================================

-- Income Categories (is_default = true, user_id = NULL)
INSERT INTO categories (user_id, name, type, icon, color, is_default) VALUES
(NULL, 'Salary', 'INCOME', 'briefcase', '#16a34a', TRUE),
(NULL, 'Freelancing', 'INCOME', 'laptop', '#06b6d4', TRUE),
(NULL, 'Business', 'INCOME', 'building', '#6366f1', TRUE),
(NULL, 'Investments', 'INCOME', 'trending-up', '#10b981', TRUE),
(NULL, 'Rental Income', 'INCOME', 'home', '#3b82f6', TRUE),
(NULL, 'Other Income', 'INCOME', 'plus-circle', '#64748b', TRUE);

-- Expense Categories (is_default = true, user_id = NULL)
INSERT INTO categories (user_id, name, type, icon, color, is_default) VALUES
(NULL, 'Food & Dining', 'EXPENSE', 'utensils', '#f97316', TRUE),
(NULL, 'Groceries', 'EXPENSE', 'shopping-cart', '#eab308', TRUE),
(NULL, 'Rent & Housing', 'EXPENSE', 'home', '#8b5cf6', TRUE),
(NULL, 'Utilities & Bills', 'EXPENSE', 'zap', '#f59e0b', TRUE),
(NULL, 'Transportation', 'EXPENSE', 'car', '#2563eb', TRUE),
(NULL, 'Shopping', 'EXPENSE', 'shopping-bag', '#ec4899', TRUE),
(NULL, 'Healthcare & Medical', 'EXPENSE', 'heart-pulse', '#ef4444', TRUE),
(NULL, 'Education', 'EXPENSE', 'book-open', '#14b8a6', TRUE),
(NULL, 'Entertainment', 'EXPENSE', 'film', '#a855f7', TRUE),
(NULL, 'Travel', 'EXPENSE', 'plane', '#0284c7', TRUE),
(NULL, 'Personal Care', 'EXPENSE', 'smile', '#f43f5e', TRUE),
(NULL, 'Other Expense', 'EXPENSE', 'tag', '#64748b', TRUE);
