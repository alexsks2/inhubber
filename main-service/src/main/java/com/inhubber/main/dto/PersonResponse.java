package com.inhubber.main.dto;

import com.inhubber.main.domain.Person;
import com.inhubber.main.domain.Role;

public record PersonResponse(Long id, String name, String email, Role role, Long companyId, String companyName) {

    public static PersonResponse from(Person person) {
        return new PersonResponse(
                person.getId(),
                person.getName(),
                person.getEmail(),
                person.getRole(),
                person.getCompany().getId(),
                person.getCompany().getName()
        );
    }
}
