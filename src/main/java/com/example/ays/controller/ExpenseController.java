package com.example.ays.controller;

import com.example.ays.dto.ExpenseCreateRequest;
import com.example.ays.entity.AppUser;
import com.example.ays.entity.Expense;
import com.example.ays.service.CurrentUserService;
import com.example.ays.service.ExpenseService;
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
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Expense create(@RequestHeader("Authorization") String authorization,
                          @Valid @RequestBody ExpenseCreateRequest request) {
        AppUser user = currentUserService.fromAuthorization(authorization);
        currentUserService.requireAdmin(user);
        return expenseService.createExpense(request, user);
    }

    @GetMapping
    public List<Expense> findAll(@RequestHeader("Authorization") String authorization) {
        AppUser user = currentUserService.fromAuthorization(authorization);
        return expenseService.findAll(user);
    }
}
