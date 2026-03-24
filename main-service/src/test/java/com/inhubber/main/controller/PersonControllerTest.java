package com.inhubber.main.controller;

import com.inhubber.main.domain.Role;
import com.inhubber.main.dto.PersonResponse;
import com.inhubber.main.dto.UpdateEmailRequest;
import com.inhubber.main.dto.UpdateNameRequest;
import com.inhubber.main.security.UserPrincipal;
import com.inhubber.main.service.PersonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonControllerTest {

    @Mock
    private PersonService personService;

    @InjectMocks
    private PersonController personController;

    private final UserPrincipal admin = new UserPrincipal("admin@acme.com", Role.ADMIN);
    private final UserPrincipal user = new UserPrincipal("john@acme.com", Role.USER);

    @Test
    void findAll_delegatesToService() {
        PersonResponse p1 = new PersonResponse(1L, "Alice", "alice@acme.com", Role.USER, 1L, "Acme");
        PersonResponse p2 = new PersonResponse(2L, "Bob", "bob@acme.com", Role.USER, 1L, "Acme");
        when(personService.findAll()).thenReturn(List.of(p1, p2));

        ResponseEntity<List<PersonResponse>> response = personController.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(p1, p2);
    }

    @Test
    void updateEmail_delegatesToServiceWithPrincipal() {
        UpdateEmailRequest request = new UpdateEmailRequest("new@acme.com");
        PersonResponse updated = new PersonResponse(1L, "John", "new@acme.com", Role.USER, 1L, "Acme");
        when(personService.updateEmail(1L, request, admin)).thenReturn(updated);

        ResponseEntity<PersonResponse> response = personController.updateEmail(1L, request, admin);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(updated);
        verify(personService).updateEmail(1L, request, admin);
    }

    @Test
    void updateName_delegatesToServiceWithPrincipal() {
        UpdateNameRequest request = new UpdateNameRequest("John Updated");
        PersonResponse updated = new PersonResponse(1L, "John Updated", "john@acme.com", Role.USER, 1L, "Acme");
        when(personService.updateName(1L, request, user)).thenReturn(updated);

        ResponseEntity<PersonResponse> response = personController.updateName(1L, request, user);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(updated);
        verify(personService).updateName(1L, request, user);
    }
}
