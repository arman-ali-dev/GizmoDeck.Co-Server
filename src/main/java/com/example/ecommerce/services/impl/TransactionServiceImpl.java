package com.example.ecommerce.services.impl;

import com.example.ecommerce.exceptions.transaction.TransactionNotFoundException;
import com.example.ecommerce.models.Transaction;
import com.example.ecommerce.repositories.TransactionRepository;
import com.example.ecommerce.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public List<Transaction> getTransactionsByUser(Long userId) {
        return transactionRepository.findByCustomerId(userId);
    }

    @Override
    public Transaction getTransactionByOrderId(Long orderId) {
        return transactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with order id: " + orderId));
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + id));

        transactionRepository.delete(transaction);
    }

    @Override
    public List<Transaction> getTransactionsBySeller(Long sellerId) {
        return transactionRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }
}
