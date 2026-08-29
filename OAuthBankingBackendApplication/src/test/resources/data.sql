-- ---------------------------------------------------------------------------
-- Seed data for the integration tests.
--
-- Passwords use the {noop} prefix so the delegating encoder compares them
-- literally. That keeps the tests readable and is safe because this data never
-- leaves the in-memory database. Real rows carry {bcrypt} hashes.
--
-- Two customers exist on purpose: the ownership check needs an "other person"
-- whose records Alice must not be able to read.
-- ---------------------------------------------------------------------------

INSERT INTO customer (customer_id, name, email, mobile_number, pwd, role, create_dt) VALUES
  (1, 'Alice Example', 'alice@example.com', '9876543210', '{noop}Password@12345', 'user',  CURRENT_TIMESTAMP),
  (2, 'Bob Example',   'bob@example.com',   '9876500000', '{noop}Password@12345', 'admin', CURRENT_TIMESTAMP);

INSERT INTO authorities (id, customer_id, name) VALUES
  (1, 1, 'ROLE_USER'),
  (2, 2, 'ROLE_USER'),
  (3, 2, 'ROLE_ADMIN');

INSERT INTO accounts (account_number, customer_id, account_type, branch_address, create_dt) VALUES
  (1865764534, 1, 'Savings', '123 Main Street, Nanded',  CURRENT_DATE),
  (1865764535, 2, 'Savings', '456 Second Street, Pune',  CURRENT_DATE);

INSERT INTO account_transactions
  (transaction_id, account_number, customer_id, transaction_dt, transaction_summary,
   transaction_type, transaction_amt, closing_balance, create_dt) VALUES
  ('TXN-1', 1865764534, 1, CURRENT_DATE, 'Coffee Shop',   'Withdrawal',  30, 34500, CURRENT_DATE),
  ('TXN-2', 1865764534, 1, CURRENT_DATE, 'Salary credit', 'Deposit',   35000, 34530, CURRENT_DATE),
  ('TXN-3', 1865764535, 2, CURRENT_DATE, 'Book store',    'Withdrawal',  50, 12000, CURRENT_DATE);

INSERT INTO cards (card_id, customer_id, card_number, card_type, total_limit, amount_used, available_amount, create_dt) VALUES
  (1, 1, '4565XXXX4656', 'Credit', 10000, 500, 9500, CURRENT_DATE),
  (2, 2, '3455XXXX8673', 'Credit', 7500,  600, 6900, CURRENT_DATE);

INSERT INTO loans (loan_number, customer_id, start_dt, loan_type, total_loan, amount_paid, outstanding_amount, create_dt) VALUES
  (1, 1, CURRENT_DATE, 'Home',    200000, 50000, 150000, CURRENT_DATE),
  (2, 2, CURRENT_DATE, 'Vehicle', 40000,  10000, 30000,  CURRENT_DATE);

-- One notice inside today's window and one already expired, so the active-notice
-- query has something to filter out.
INSERT INTO notice_details (notice_id, notice_summary, notice_details, notice_beg_dt, notice_end_dt, create_dt, update_dt) VALUES
  (1, 'Interest rates revised', 'Savings interest revised with effect from this month.',
      DATEADD('DAY', -7, CURRENT_DATE), DATEADD('DAY', 30, CURRENT_DATE), CURRENT_DATE, NULL),
  (2, 'Expired campaign',       'This notice is outside its display window.',
      DATEADD('DAY', -60, CURRENT_DATE), DATEADD('DAY', -30, CURRENT_DATE), CURRENT_DATE, NULL);
