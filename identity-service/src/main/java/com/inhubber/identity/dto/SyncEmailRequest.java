package com.inhubber.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SyncEmailRequest(@NotBlank @Email String oldEmail,
                               @NotBlank @Email String newEmail) {}
