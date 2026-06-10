package com.giaidev.gateway.service;

import com.giaidev.gateway.dto.ApiResponse;
import com.giaidev.gateway.dto.request.IntrospectRequest;
import com.giaidev.gateway.dto.response.IntrospectResponse;
import com.giaidev.gateway.repository.IdentityClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdentityService {
    IdentityClient identityClient;
    //Reuse Identity Service (Introspect to verify token)
    public Mono<ApiResponse<IntrospectResponse>> introspect(String token){
        return identityClient.introspect(IntrospectRequest.builder()
                        .token(token)
                .build());
    }
}
