package com.inhubber.main.service;

import com.inhubber.main.domain.Company;
import com.inhubber.main.dto.CompanyRequest;
import com.inhubber.main.dto.CompanyResponse;
import com.inhubber.main.exception.DuplicateException;
import com.inhubber.main.exception.NotFoundException;
import com.inhubber.main.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyResponse create(CompanyRequest request) {
        if (companyRepository.existsByDomain(request.domain())) {
            throw new DuplicateException("Company with domain '" + request.domain() + "' already exists");
        }
        Company company = new Company(request.name(), request.domain());
        return CompanyResponse.from(companyRepository.save(company));
    }

    public CompanyResponse findById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found: " + id));
        return CompanyResponse.from(company);
    }
}
