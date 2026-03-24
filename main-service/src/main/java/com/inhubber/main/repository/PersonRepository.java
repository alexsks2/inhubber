package com.inhubber.main.repository;

import com.inhubber.main.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Person> findAllByOrderByNameAsc();
}
