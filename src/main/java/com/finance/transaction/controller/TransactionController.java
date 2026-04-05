package com.finance.transaction.controller;

import com.finance.model.FinResponse;
import com.finance.model.PagedRs;
import com.finance.transaction.model.*;
import com.finance.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<FinResponse<TransactionRs>> createTransaction(
            @Valid @RequestBody TransactionCreateRq createRq) {

        FinResponse<TransactionRs> response = transactionService.createTransaction(createRq);
        HttpStatus status = response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    public ResponseEntity<FinResponse<PagedRs<TransactionSummaryRs>>> getAllTransactions(
            @ModelAttribute TransactionFilterRq filterRq) {

        return ResponseEntity.ok(transactionService.getAllTransactions(filterRq));
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    public ResponseEntity<FinResponse<TransactionRs>> getTransactionById(
            @PathVariable Long id) {

        FinResponse<TransactionRs> response = transactionService.getTransactionById(id);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(response);
    }


    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<FinResponse<TransactionRs>> editTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionEditRq editRq) {

        FinResponse<TransactionRs> response = transactionService.editTransaction(id, editRq);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinResponse<Void>> deleteTransaction(@PathVariable Long id) {
        FinResponse<Void> response = transactionService.deleteTransaction(id);
        HttpStatus status = response.isSuccess() ? HttpStatus.NO_CONTENT : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }
}