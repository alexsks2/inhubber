package com.inhubber.main.client;

import com.inhubber.main.dto.SyncEmailRequest;
import com.inhubber.main.dto.ValidateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IdentityClient {

    private final RestClient restClient;

    public IdentityClient(@Value("${services.identity-url}") String identityUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(identityUrl)
                .build();
    }

    public ValidateResponse validate(String authHeader) {
        return restClient.get()
                .uri("/auth/validate")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .body(ValidateResponse.class);
    }

    public void updateEmail(String oldEmail, String newEmail) {
        restClient.patch()
                .uri("/auth/users/email")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SyncEmailRequest(oldEmail, newEmail))
                .retrieve()
                .toBodilessEntity();
    }
}
