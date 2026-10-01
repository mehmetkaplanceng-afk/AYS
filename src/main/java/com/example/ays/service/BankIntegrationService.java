package com.example.ays.service;

import com.example.ays.dto.BankTopUpRequest;
import com.example.ays.entity.AppUser;
import com.example.ays.entity.BankAccount;
import com.example.ays.entity.BankTransaction;
import com.example.ays.enums.TransactionType;
import com.example.ays.exception.ResourceNotFoundException;
import com.example.ays.repository.BankAccountRepository;
import com.example.ays.repository.BankTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BankIntegrationService {

    private final BankAccountRepository bankAccountRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public BankAccount topUp(BankTopUpRequest request, AppUser user) {
        BankAccount account = currentUserService.isAdmin(user)
                ? bankAccountRepository.findById(request.getBankAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found: " + request.getBankAccountId()))
                : bankAccountRepository.findByIdAndOwnerId(request.getBankAccountId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found: " + request.getBankAccountId()));

        account.setBalance(account.getBalance().add(request.getAmount()));

        bankTransactionRepository.save(BankTransaction.builder()
                .bankAccount(account)
                .type(TransactionType.INCOME)
                .amount(request.getAmount())
                .description("Banka yükleme simülasyonu - " + request.getExternalBankName() + " / " + request.getSenderIban())
                .transactionDate(LocalDateTime.now())
                .build());

        return bankAccountRepository.save(account);
    }
}
