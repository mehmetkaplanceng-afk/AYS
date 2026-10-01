package com.example.ays.service;

import com.example.ays.dto.DuesCreateRequest;
import com.example.ays.dto.DuesPayRequest;
import com.example.ays.entity.AppUser;
import com.example.ays.entity.BankAccount;
import com.example.ays.entity.BankTransaction;
import com.example.ays.entity.Dues;
import com.example.ays.enums.PaymentStatus;
import com.example.ays.enums.TransactionType;
import com.example.ays.exception.ResourceNotFoundException;
import com.example.ays.repository.BankAccountRepository;
import com.example.ays.repository.BankTransactionRepository;
import com.example.ays.repository.DuesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DuesService {

    private final DuesRepository duesRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<Dues> findAll(AppUser user) {
        return duesRepository.findAll();
    }

    @Transactional
    public Dues create(DuesCreateRequest request, AppUser user) {
        Dues dues = Dues.builder()
                .apartmentNo(request.getApartmentNo())
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .status(PaymentStatus.UNPAID)
                .owner(user)
                .build();
        return duesRepository.save(dues);
    }

    @Transactional
    public Dues payDues(Long duesId, DuesPayRequest request, AppUser user) {
        Dues dues = currentUserService.isAdmin(user)
                ? duesRepository.findById(duesId).orElseThrow(() -> new ResourceNotFoundException("Dues not found: " + duesId))
                : duesRepository.findByIdAndOwnerId(duesId, user.getId()).orElseThrow(() -> new ResourceNotFoundException("Dues not found: " + duesId));

        if (dues.getStatus() == PaymentStatus.PAID) {
            return dues;
        }

        BankAccount account = currentUserService.isAdmin(user)
                ? bankAccountRepository.findById(request.getBankAccountId()).orElseThrow(() -> new ResourceNotFoundException("Bank account not found: " + request.getBankAccountId()))
                : bankAccountRepository.findByIdAndOwnerId(request.getBankAccountId(), user.getId()).orElseThrow(() -> new ResourceNotFoundException("Bank account not found: " + request.getBankAccountId()));

        account.setBalance(account.getBalance().add(dues.getAmount()));

        BankTransaction transaction = BankTransaction.builder()
                .bankAccount(account)
                .type(TransactionType.INCOME)
                .amount(dues.getAmount())
                .description("Aidat ödemesi - Daire: " + dues.getApartmentNo())
                .transactionDate(LocalDateTime.now())
                .build();

        dues.setStatus(PaymentStatus.PAID);
        dues.setBankTransaction(bankTransactionRepository.save(transaction));

        bankAccountRepository.save(account);
        return duesRepository.save(dues);
    }
}
