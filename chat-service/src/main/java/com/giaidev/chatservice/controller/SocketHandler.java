package com.giaidev.chatservice.controller;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.giaidev.chatservice.dto.request.IntrospectRequest;
import com.giaidev.chatservice.service.IdentityService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SocketHandler {
    SocketIOServer server;
    IdentityService identityService;
    @OnConnect
    public void clientConnected(SocketIOClient client){
        //Get Token from request param
        String token = client.getHandshakeData().getSingleUrlParam("token");
        //Verify token
        var introspect = identityService.introspect(IntrospectRequest.builder()
                        .token(token)
                .build());
        if(introspect.isValid()){
            log.info("Client connected: {}, {}", client.getSessionId(), token);
        }else {
            log.error("Authentication fail: {}", client.getSessionId());
        }
        //If token is invalid disconnect

    }

    @OnDisconnect
    public void clientDisconnected(SocketIOClient client){
        log.info("Client disconnected: {}", client.getSessionId());
    }

    @PostConstruct
    public void startServer(){
        server.start();
        server.addListeners(this);
        log.info("Socket server started");
    }

    @PreDestroy
    public void stopServer(){
        server.stop();
        log.info("Socket server stopped");
    }
}
