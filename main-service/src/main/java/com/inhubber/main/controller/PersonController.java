package com.inhubber.main.controller;

import com.inhubber.main.dto.PersonRequest;
import com.inhubber.main.dto.PersonResponse;
import com.inhubber.main.dto.UpdateEmailRequest;
import com.inhubber.main.dto.UpdateNameRequest;
import com.inhubber.main.security.UserPrincipal;
import com.inhubber.main.service.PersonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PersonResponse> create(@RequestBody @Valid PersonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PersonResponse>> findAll() {
        return ResponseEntity.ok(personService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponse> findById(@PathVariable Long id,
                                                   @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(personService.findById(id, user));
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<PersonResponse> updateName(@PathVariable Long id,
                                                     @RequestBody @Valid UpdateNameRequest request,
                                                     @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(personService.updateName(id, request, user));
    }

    @PatchMapping("/{id}/email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PersonResponse> updateEmail(@PathVariable Long id,
                                                      @RequestBody @Valid UpdateEmailRequest request,
                                                      @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(personService.updateEmail(id, request, user));
    }
}
