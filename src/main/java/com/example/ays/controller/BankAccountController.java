package com.example.ays.controller;

import com.example.ays.dto.BankAccountCreateRequest;
import com.example.ays.entity.AppUser;
import com.example.ays.entity.BankAccount;
import com.example.ays.service.BankAccountService;
import com.example.ays.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BankAccount create(@RequestHeader("Authorization") String authorization,
                              @Valid @RequestBody BankAccountCreateRequest request) {
        AppUser user = currentUserService.fromAuthorization(authorization);
        currentUserService.requireAdmin(user);
        return bankAccountService.create(request, user);
    }

    @GetMapping
    public List<BankAccount> findAll(@RequestHeader("Authorization") String authorization) {
        AppUser user = currentUserService.fromAuthorization(authorization);
        currentUserService.requireAdmin(user);
        return bankAccountService.findAll(user);
    }

    @GetMapping("/{id}")
    public BankAccount findById(@RequestHeader("Authorization") String authorization, @PathVariable Long id) {
        AppUser user = currentUserService.fromAuthorization(authorization);
        currentUserService.requireAdmin(user);
        return bankAccountService.findById(id, user);
    }
}
