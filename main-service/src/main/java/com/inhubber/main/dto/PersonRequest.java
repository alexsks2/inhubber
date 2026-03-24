package com.inhubber.main.dto;

import com.inhubber.main.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PersonRequest(@NotBlank String name,
                            @NotBlank @Email String email,
                            @NotNull Role role,
                            @NotNull Long companyId) {}
