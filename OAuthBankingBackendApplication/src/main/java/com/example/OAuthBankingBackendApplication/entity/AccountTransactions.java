package com.example.OAuthBankingBackendApplication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

/**
 * One posted movement on an account, plus the balance that resulted from it.
 *
 * <p>Amounts are held as {@code int} to match the existing schema. Money belongs in
 * {@code BigDecimal} with an explicit scale; that migration is tracked in the
 * project roadmap rather than done here, because it needs a schema change too.
 */
@Entity
@Table(name = "account_transactions")
@Getter
@Setter
public class AccountTransactions {

    @Id
    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "account_number")
    private long accountNumber;

    @Column(name = "customer_id")
    private long customerId;

    @Column(name = "transaction_dt")
    private Date transactionDt;

    @Column(name = "transaction_summary")
    private String transactionSummary;

    /** {@code Deposit} or {@code Withdrawal}. */
    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "transaction_amt")
    private int transactionAmt;

    @Column(name = "closing_balance")
    private int closingBalance;

    @Column(name = "create_dt")
    private Date createDt;
}
