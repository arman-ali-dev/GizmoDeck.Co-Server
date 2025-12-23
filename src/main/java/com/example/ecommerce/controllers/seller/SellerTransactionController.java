package com.example.ecommerce.controllers.seller;

import com.example.ecommerce.exceptions.seller.SellerNotFoundException;
import com.example.ecommerce.models.Seller;
import com.example.ecommerce.models.Transaction;
import com.example.ecommerce.models.User;
import com.example.ecommerce.repositories.SellerRepository;
import com.example.ecommerce.services.SellerService;
import com.example.ecommerce.services.TransactionService;
import com.example.ecommerce.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/transactions")
public class SellerTransactionController {

    private final TransactionService transactionService;
    private final UserService userService;
    private final SellerRepository sellerRepository;

    @Autowired
    public SellerTransactionController(TransactionService transactionService,
                                       UserService userService, SellerRepository sellerRepository) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.sellerRepository = sellerRepository;
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactionsHandlerHandler() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Transaction> getTransactionByOrderIdHandler(@PathVariable Long orderId) {
        Transaction transaction = transactionService.getTransactionByOrderId(orderId);
        return ResponseEntity.ok(transaction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransactionHandler(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok("Transaction deleted successfully");
    }

    @GetMapping("/all")
    public ResponseEntity<List<Transaction>> getTransactionsBySeller(@RequestHeader("Authorization") String jwt) {
        User user = userService.getUserProfile(jwt);
        Seller seller = sellerRepository.findByUser(user).orElseThrow(() -> new SellerNotFoundException("Seller Not Found!"));
        List<Transaction> transactions = transactionService.getTransactionsBySeller(seller.getId());
        return ResponseEntity.ok(transactions);
    }

}
