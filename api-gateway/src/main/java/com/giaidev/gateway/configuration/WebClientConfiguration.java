package com.giaidev.gateway.configuration;

import com.giaidev.gateway.repository.IdentityClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class WebClientConfiguration {
    @Bean
    WebClient webClient(){
        return WebClient.builder()
                .baseUrl("http://localhost:8080/identity")
                .build();
    }

    @Bean
    IdentityClient identityClient(WebClient webClient){ //Building Identity Service with Proxy => Request to api follow initial information by myself
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.
                builderFor(WebClientAdapter.create(webClient)).build();

        return httpServiceProxyFactory.createClient(IdentityClient.class);

    }

}
