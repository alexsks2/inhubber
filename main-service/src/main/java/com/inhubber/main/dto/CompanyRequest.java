package com.inhubber.main.dto;

import jakarta.validation.constraints.NotBlank;

public record CompanyRequest(@NotBlank String name,
                             @NotBlank String domain) {}
