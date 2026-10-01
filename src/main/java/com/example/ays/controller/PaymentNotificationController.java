package com.example.ays.controller;

import com.example.ays.dto.PaymentNotificationCreateRequest;
import com.example.ays.entity.AppUser;
import com.example.ays.entity.PaymentNotification;
import com.example.ays.service.CurrentUserService;
import com.example.ays.service.PaymentNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class PaymentNotificationController {

    private final PaymentNotificationService paymentNotificationService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentNotification create(@RequestHeader("Authorization") String authorization,
                                      @Valid @RequestBody PaymentNotificationCreateRequest request) {
        AppUser user = currentUserService.fromAuthorization(authorization);
        return paymentNotificationService.create(request, user);
    }

    @GetMapping
    public List<PaymentNotification> findAll(@RequestHeader("Authorization") String authorization) {
        AppUser user = currentUserService.fromAuthorization(authorization);
        return paymentNotificationService.findAll(user);
    }

    @PostMapping("/{id}/status")
    public PaymentNotification updateStatus(@RequestHeader("Authorization") String authorization,
                                            @org.springframework.web.bind.annotation.PathVariable Long id,
                                            @org.springframework.web.bind.annotation.RequestParam String status) {
        AppUser user = currentUserService.fromAuthorization(authorization);
        return paymentNotificationService.updateStatus(id, status, user);
    }
}
