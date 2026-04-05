package com.finance.transaction.service;

import com.finance.model.FinResponse;
import com.finance.model.PagedRs;
import com.finance.transaction.model.*;

public interface TransactionService {


    FinResponse<TransactionRs> createTransaction(TransactionCreateRq createRq);


    FinResponse<TransactionRs> getTransactionById(Long id);


    FinResponse<PagedRs<TransactionSummaryRs>> getAllTransactions(TransactionFilterRq filterRq);


    FinResponse<TransactionRs> editTransaction(Long id, TransactionEditRq editRq);


    FinResponse<Void> deleteTransaction(Long id);
}