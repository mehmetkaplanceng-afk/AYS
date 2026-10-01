package com.example.ays.service;

import com.example.ays.dto.ExpenseCreateRequest;
import com.example.ays.entity.AppUser;
import com.example.ays.entity.BankAccount;
import com.example.ays.entity.BankTransaction;
import com.example.ays.entity.Expense;
import com.example.ays.enums.TransactionType;
import com.example.ays.exception.InsufficientBalanceException;
import com.example.ays.exception.ResourceNotFoundException;
import com.example.ays.repository.BankAccountRepository;
import com.example.ays.repository.BankTransactionRepository;
import com.example.ays.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<Expense> findAll(AppUser user) {
        return expenseRepository.findAll();
    }

    @Transactional
    public Expense createExpense(ExpenseCreateRequest request, AppUser user) {
        BankAccount account = currentUserService.isAdmin(user)
                ? bankAccountRepository.findById(request.getBankAccountId()).orElseThrow(() -> new ResourceNotFoundException("Bank account not found: " + request.getBankAccountId()))
                : bankAccountRepository.findByIdAndOwnerId(request.getBankAccountId(), user.getId()).orElseThrow(() -> new ResourceNotFoundException("Bank account not found: " + request.getBankAccountId()));

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for bank account: " + account.getId());
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));

        BankTransaction transaction = BankTransaction.builder()
                .bankAccount(account)
                .type(TransactionType.EXPENSE)
                .amount(request.getAmount())
                .description(request.getDescription())
                .transactionDate(LocalDateTime.now())
                .build();

        Expense expense = Expense.builder()
                .category(request.getCategory())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .description(request.getDescription())
                .bankTransaction(bankTransactionRepository.save(transaction))
                .owner(user)
                .build();

        bankAccountRepository.save(account);
        return expenseRepository.save(expense);
    }
}
