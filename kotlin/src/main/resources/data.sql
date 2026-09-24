-- Seed data so the two endpoints have something to show out of the box.
-- Only a data source, not a requirement: the assignment doesn't specify an
-- endpoint for creating time deposits/withdrawals, so this is the one
-- logical way to get demo rows into the database.
INSERT INTO "timeDeposits" (id, "planType", days, balance) VALUES
    (1, 'basic', 45, 1000.00),
    (2, 'student', 100, 2000.00),
    (3, 'premium', 60, 5000.00)
ON CONFLICT (id) DO NOTHING;

INSERT INTO "withdrawals" (id, "timeDepositId", amount, date) VALUES
    (1, 1, 50.00, '2026-01-15'),
    (2, 3, 200.00, '2026-02-01')
ON CONFLICT (id) DO NOTHING;
