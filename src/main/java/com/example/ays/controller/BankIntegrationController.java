package com.example.ays.controller;

import com.example.ays.dto.BankTopUpRequest;
import com.example.ays.entity.AppUser;
import com.example.ays.entity.BankAccount;
import com.example.ays.service.BankIntegrationService;
import com.example.ays.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bank-integrations")
@RequiredArgsConstructor
public class BankIntegrationController {

    private final BankIntegrationService bankIntegrationService;
    private final CurrentUserService currentUserService;

    @PostMapping("/top-up")
    public BankAccount topUp(@RequestHeader("Authorization") String authorization,
                             @Valid @RequestBody BankTopUpRequest request) {
        AppUser user = currentUserService.fromAuthorization(authorization);
        currentUserService.requireAdmin(user);
        return bankIntegrationService.topUp(request, user);
    }
}
