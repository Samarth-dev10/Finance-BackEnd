package com.finance.core.enums;

/**
 * TransactionType — whether money is coming in or going out.
 *
 * Stored as a STRING in the DB column (not an integer ordinal)
 * so the database stays readable and migrations are safe.
 *
 * Example DB value: "INCOME" or "EXPENSE"
 */
public enum TransactionType {
    INCOME,
    EXPENSE
}