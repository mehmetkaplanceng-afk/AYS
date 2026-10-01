package com.example.ays.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DuesPayRequest {

    @NotNull
    private Long bankAccountId;
}
