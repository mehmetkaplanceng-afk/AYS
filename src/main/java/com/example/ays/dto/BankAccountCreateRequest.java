package com.example.ays.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BankAccountCreateRequest {

    @NotBlank
    private String accountName;

    @NotBlank
    private String iban;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal balance;

    @NotBlank
    private String currency;
}
