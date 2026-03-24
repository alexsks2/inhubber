package com.inhubber.main.repository;

import com.inhubber.main.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByDomain(String domain);
    boolean existsByDomain(String domain);
}
