package com.example.ays.service;

import com.example.ays.dto.BankAccountCreateRequest;
import com.example.ays.entity.AppUser;
import com.example.ays.entity.BankAccount;
import com.example.ays.exception.ResourceNotFoundException;
import com.example.ays.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public BankAccount create(BankAccountCreateRequest request, AppUser user) {
        BankAccount bankAccount = BankAccount.builder()
                .accountName(request.getAccountName())
                .iban(request.getIban())
                .balance(request.getBalance())
                .currency(request.getCurrency())
                .owner(user)
                .build();
        return bankAccountRepository.save(bankAccount);
    }

    @Transactional(readOnly = true)
    public List<BankAccount> findAll(AppUser user) {
        return currentUserService.isAdmin(user)
                ? bankAccountRepository.findAll()
                : bankAccountRepository.findAllByOwnerId(user.getId());
    }

    @Transactional(readOnly = true)
    public BankAccount findById(Long id, AppUser user) {
        return currentUserService.isAdmin(user)
                ? bankAccountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bank account not found: " + id))
                : bankAccountRepository.findByIdAndOwnerId(id, user.getId()).orElseThrow(() -> new ResourceNotFoundException("Bank account not found: " + id));
    }
}
