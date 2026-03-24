package com.inhubber.main.service;

import com.inhubber.main.client.IdentityClient;
import com.inhubber.main.domain.Company;
import com.inhubber.main.domain.Person;
import com.inhubber.main.domain.Role;
import com.inhubber.main.dto.PersonRequest;
import com.inhubber.main.dto.PersonResponse;
import com.inhubber.main.dto.UpdateEmailRequest;
import com.inhubber.main.dto.UpdateNameRequest;
import com.inhubber.main.exception.DomainMismatchException;
import com.inhubber.main.exception.ForbiddenException;
import com.inhubber.main.repository.CompanyRepository;
import com.inhubber.main.repository.PersonRepository;
import com.inhubber.main.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private IdentityClient identityClient;

    @InjectMocks
    private PersonService personService;

    private Company acme;

    @BeforeEach
    void setUp() {
        acme = new Company("Acme Corp", "acme.com");
    }

    @Test
    void create_emailDomainMatchesCompany_success() {
        PersonRequest request = new PersonRequest("John", "john@acme.com", Role.USER, 1L);
        when(personRepository.existsByEmail("john@acme.com")).thenReturn(false);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(acme));
        when(personRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PersonResponse response = personService.create(request);

        assertThat(response.email()).isEqualTo("john@acme.com");
        assertThat(response.companyName()).isEqualTo("Acme Corp");
    }

    @Test
    void create_emailDomainMismatch_throwsDomainMismatchException() {
        PersonRequest request = new PersonRequest("John", "john@gmail.com", Role.USER, 1L);
        when(personRepository.existsByEmail("john@gmail.com")).thenReturn(false);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(acme));

        assertThatThrownBy(() -> personService.create(request))
                .isInstanceOf(DomainMismatchException.class)
                .hasMessageContaining("acme.com");
    }

    @Test
    void findAll_returnsSortedByName() {
        List<Person> sorted = List.of(
                new Person("Alice", "alice@acme.com", Role.USER, acme),
                new Person("Bob", "bob@acme.com", Role.USER, acme),
                new Person("Charlie", "charlie@acme.com", Role.ADMIN, acme)
        );
        when(personRepository.findAllByOrderByNameAsc()).thenReturn(sorted);

        List<PersonResponse> result = personService.findAll();

        assertThat(result).extracting(PersonResponse::name)
                .containsExactly("Alice", "Bob", "Charlie");
    }

    @Test
    void updateEmail_adminUpdatesOwnEmail_throwsForbiddenException() {
        Person person = new Person("Admin", "admin@acme.com", Role.ADMIN, acme);
        UserPrincipal admin = new UserPrincipal("admin@acme.com", Role.ADMIN);
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));

        assertThatThrownBy(() -> personService.updateEmail(1L, new UpdateEmailRequest("new@acme.com"), admin))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Cannot update own email");
    }

    @Test
    void updateEmail_adminUpdatesAnotherUserEmail_success() {
        Person person = new Person("John", "john@acme.com", Role.USER, acme);
        UserPrincipal admin = new UserPrincipal("admin@acme.com", Role.ADMIN);
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(personRepository.existsByEmail("john.new@acme.com")).thenReturn(false);
        when(personRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PersonResponse response = personService.updateEmail(1L, new UpdateEmailRequest("john.new@acme.com"), admin);

        assertThat(response.email()).isEqualTo("john.new@acme.com");
    }

    @Test
    void updateEmail_newEmailDomainMismatch_throwsDomainMismatchException() {
        Person person = new Person("John", "john@acme.com", Role.USER, acme);
        UserPrincipal admin = new UserPrincipal("admin@acme.com", Role.ADMIN);
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(personRepository.existsByEmail("john@gmail.com")).thenReturn(false);

        assertThatThrownBy(() -> personService.updateEmail(1L, new UpdateEmailRequest("john@gmail.com"), admin))
                .isInstanceOf(DomainMismatchException.class);
    }

    @Test
    void updateName_userUpdatesOwnName_success() {
        Person person = new Person("John", "john@acme.com", Role.USER, acme);
        UserPrincipal user = new UserPrincipal("john@acme.com", Role.USER);
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(personRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PersonResponse response = personService.updateName(1L, new UpdateNameRequest("John Updated"), user);

        assertThat(response.name()).isEqualTo("John Updated");
    }

    @Test
    void updateName_userUpdatesAnotherUserName_throwsForbiddenException() {
        Person person = new Person("John", "john@acme.com", Role.USER, acme);
        UserPrincipal otherUser = new UserPrincipal("other@acme.com", Role.USER);
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));

        assertThatThrownBy(() -> personService.updateName(1L, new UpdateNameRequest("Hacked Name"), otherUser))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Users can only update their own name");
    }
}
