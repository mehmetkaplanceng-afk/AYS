package com.example.ays.controller;

import com.example.ays.dto.DuesCreateRequest;
import com.example.ays.dto.DuesPayRequest;
import com.example.ays.entity.AppUser;
import com.example.ays.entity.Dues;
import com.example.ays.service.CurrentUserService;
import com.example.ays.service.DuesService;
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
@RequestMapping("/api/dues")
@RequiredArgsConstructor
public class DuesController {

    private final DuesService duesService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Dues create(@RequestHeader("Authorization") String authorization,
                       @Valid @RequestBody DuesCreateRequest request) {
        AppUser user = currentUserService.fromAuthorization(authorization);
        currentUserService.requireAdmin(user);
        return duesService.create(request, user);
    }

    @GetMapping
    public List<Dues> findAll(@RequestHeader("Authorization") String authorization) {
        AppUser user = currentUserService.fromAuthorization(authorization);
        return duesService.findAll(user);
    }

    @PostMapping("/{id}/pay")
    public Dues payDues(@RequestHeader("Authorization") String authorization,
                        @PathVariable Long id,
                        @Valid @RequestBody DuesPayRequest request) {
        AppUser user = currentUserService.fromAuthorization(authorization);
        currentUserService.requireAdmin(user);
        return duesService.payDues(id, request, user);
    }
}
