package com.example.ecommerce.services;

import com.example.ecommerce.models.Transaction;

import java.util.List;

public interface TransactionService {
    List<Transaction> getTransactionsByUser(Long userId);

    Transaction getTransactionByOrderId(Long orderId);

    List<Transaction> getAllTransactions();

    void deleteTransaction(Long id);

    List<Transaction> getTransactionsBySeller(Long sellerId);

}
