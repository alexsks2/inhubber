package com.inhubber.main.dto;

import com.inhubber.main.domain.Company;

public record CompanyResponse(Long id, String name, String domain) {

    public static CompanyResponse from(Company company) {
        return new CompanyResponse(company.getId(), company.getName(), company.getDomain());
    }
}
