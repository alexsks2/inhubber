package com.inhubber.main.exception;

public class DomainMismatchException extends RuntimeException {
    public DomainMismatchException(String email, String companyDomain) {
        super("Email domain of '" + email + "' does not match company domain '" + companyDomain + "'");
    }
}
