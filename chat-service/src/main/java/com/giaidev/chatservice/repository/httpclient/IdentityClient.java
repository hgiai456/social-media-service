package com.giaidev.chatservice.repository.httpclient;

import com.giaidev.chatservice.dto.ApiResponse;
import com.giaidev.chatservice.dto.request.IntrospectRequest;
import com.giaidev.chatservice.dto.response.IntrospectResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "identity-client", url = "${app.services.identity.url}")
public interface IdentityClient {
    @PostMapping("/auth/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request);
}
