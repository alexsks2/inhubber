package com.inhubber.main.security;

import com.inhubber.main.domain.Role;

public record UserPrincipal(String email, Role role) {}
