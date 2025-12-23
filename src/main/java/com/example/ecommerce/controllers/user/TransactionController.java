package com.example.ecommerce.controllers.user;

import com.example.ecommerce.models.Transaction;
import com.example.ecommerce.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Transaction>> getUserTransactionsHandler(@PathVariable Long userId) {
        List<Transaction> transactions = transactionService.getTransactionsByUser(userId);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Transaction> getTransactionByOrderIdHandler(@PathVariable Long orderId) {
        Transaction transaction = transactionService.getTransactionByOrderId(orderId);
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

}
