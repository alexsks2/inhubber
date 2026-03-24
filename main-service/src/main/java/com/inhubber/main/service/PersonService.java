package com.inhubber.main.service;

import com.inhubber.main.domain.Company;
import com.inhubber.main.domain.Person;
import com.inhubber.main.domain.Role;
import com.inhubber.main.dto.PersonRequest;
import com.inhubber.main.dto.PersonResponse;
import com.inhubber.main.dto.UpdateEmailRequest;
import com.inhubber.main.dto.UpdateNameRequest;
import com.inhubber.main.client.IdentityClient;
import com.inhubber.main.exception.DomainMismatchException;
import com.inhubber.main.exception.DuplicateException;
import com.inhubber.main.exception.ForbiddenException;
import com.inhubber.main.exception.NotFoundException;
import com.inhubber.main.repository.CompanyRepository;
import com.inhubber.main.repository.PersonRepository;
import com.inhubber.main.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final CompanyRepository companyRepository;
    private final IdentityClient identityClient;

    public PersonResponse create(PersonRequest request) {
        if (personRepository.existsByEmail(request.email())) {
            throw new DuplicateException("Person with email '" + request.email() + "' already exists");
        }
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new NotFoundException("Company not found: " + request.companyId()));

        validateEmailDomain(request.email(), company.getDomain());

        Person person = new Person(request.name(), request.email(), request.role(), company);
        return PersonResponse.from(personRepository.save(person));
    }

    public List<PersonResponse> findAll() {
        return personRepository.findAllByOrderByNameAsc().stream()
                .map(PersonResponse::from)
                .toList();
    }

    public PersonResponse findById(Long id, UserPrincipal requester) {
        Person person = getPersonOrThrow(id);
        if (requester.role() == Role.USER && !person.getEmail().equals(requester.email())) {
            throw new ForbiddenException("Access denied");
        }
        return PersonResponse.from(person);
    }

    public PersonResponse updateName(Long id, UpdateNameRequest request, UserPrincipal requester) {
        Person person = getPersonOrThrow(id);
        if (requester.role() == Role.USER && !person.getEmail().equals(requester.email())) {
            throw new ForbiddenException("Users can only update their own name");
        }
        person.updateName(request.name());
        return PersonResponse.from(personRepository.save(person));
    }

    public PersonResponse updateEmail(Long id, UpdateEmailRequest request, UserPrincipal requester) {
        Person person = getPersonOrThrow(id);
        if (person.getEmail().equals(requester.email())) {
            throw new ForbiddenException("Cannot update own email");
        }
        if (personRepository.existsByEmail(request.email())) {
            throw new DuplicateException("Email '" + request.email() + "' is already taken");
        }
        validateEmailDomain(request.email(), person.getCompany().getDomain());

        try {
            identityClient.updateEmail(person.getEmail(), request.email());
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("User {} not found in identity-service, skipping sync", person.getEmail());
        }

        person.updateEmail(request.email());
        return PersonResponse.from(personRepository.save(person));
    }

    private Person getPersonOrThrow(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Person not found: " + id));
    }

    private void validateEmailDomain(String email, String companyDomain) {
        String emailDomain = email.substring(email.indexOf('@') + 1);
        if (!emailDomain.equals(companyDomain)) {
            throw new DomainMismatchException(email, companyDomain);
        }
    }
}
