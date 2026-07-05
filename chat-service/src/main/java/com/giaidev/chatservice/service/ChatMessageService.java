package com.giaidev.chatservice.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giaidev.chatservice.dto.request.ChatMessageRequest;
import com.giaidev.chatservice.dto.request.ConversationRequest;
import com.giaidev.chatservice.dto.response.ChatMessageResponse;
import com.giaidev.chatservice.dto.response.ConversationResponse;
import com.giaidev.chatservice.dto.response.UserProfileResponse;
import com.giaidev.chatservice.entity.ChatMessage;
import com.giaidev.chatservice.entity.Conversation;
import com.giaidev.chatservice.entity.ParticipantInfo;
import com.giaidev.chatservice.entity.WebSocketSession;
import com.giaidev.chatservice.exception.AppException;
import com.giaidev.chatservice.exception.ErrorCode;
import com.giaidev.chatservice.mapper.ChatMessageMapper;
import com.giaidev.chatservice.mapper.ConversationMapper;
import com.giaidev.chatservice.repository.ChatMessageRepository;
import com.giaidev.chatservice.repository.ConversationRepository;
import com.giaidev.chatservice.repository.WebSocketSessionRepository;
import com.giaidev.chatservice.repository.httpclient.ProfileClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageService {
    ChatMessageRepository chatMessageRepository;
    ConversationRepository conversationRepository;
    ProfileClient profileClient;
    ChatMessageMapper chatMessageMapper;
    SocketIOServer socketIOServer;
    WebSocketSessionRepository webSocketSessionRepository;
    ObjectMapper objectMapper;

    public List<ChatMessageResponse> getMessages(String conversationId) {
        //Validate conversationId
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //Get Conversations of User
        var conversation = conversationRepository.findById(conversationId).orElseThrow(
                () -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        //Check if user is the member of the conversation, else => exception CONVERSATION_NOT_FOUND
        conversation.getParticipants().stream()
                .filter(participantInfo -> userId.equals(participantInfo.getUserId()))
                .findAny().orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        //Get List Messages of db
        var messages = chatMessageRepository.findAllByConversationIdOrderByCreatedDateDesc(conversationId);

        //Return List<ChatMessageResponse>
        return messages.stream()
                .map(this::toChatMessageResponse).toList();
//        return messages.stream()
//                .map(message -> toChatMessageResponse(message)).toList();

    }

    public ChatMessageResponse create(ChatMessageRequest request) throws JsonProcessingException {

        //Validate conversationId
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        var conversation = conversationRepository.findById(request.getConversationId()).orElseThrow(
                () -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        //Check if user is the member of the conversation, else => exception CONVERSATION_NOT_FOUND
        conversation.getParticipants().stream()
                .filter(participantInfo -> userId.equals(participantInfo.getUserId()))
                .findAny().orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        //Get UserInfo from ProfileService
        var userResponse = profileClient.getProfile(userId);
        if(Objects.isNull(userResponse)){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        UserProfileResponse userInfo = userResponse.getResult();

        //Build ChatMessage Info
        ChatMessage chatMessage = chatMessageMapper.toChatMessage(request);
        chatMessage.setSender(ParticipantInfo.builder()
                        .userId(userInfo.getUserId())
                        .username(userInfo.getUsername())
                        .firstName(userInfo.getFirstName())
                        .lastName(userInfo.getLastName())
                        .avatar(userInfo.getAvatar())
                .build());
        chatMessage.setCreatedDate(Instant.now());

        //Create chat message - Save to DB
        chatMessage = chatMessageRepository.save(chatMessage);


        //Publish socket event to clients is conversation
        //Get participants userIds
        List<String> userIds = conversation.getParticipants().stream()
                .map(ParticipantInfo::getUserId).toList();

        //When use Set help us reduce the waiting time => faster than List
        Map<String, WebSocketSession> webSocketSessions =
                webSocketSessionRepository
                        .findAllByUserIdIn(userIds).stream()
                        .collect(Collectors.toMap(
                                WebSocketSession::getSocketSessionId,
                                Function.identity()));


        ChatMessageResponse chatMessageResponse = chatMessageMapper.toChatMessageResponse(chatMessage);
        //Publish socket event to clients
        socketIOServer.getAllClients().forEach(client -> {
            var webSocketSession = webSocketSessions.get(client.getSessionId().toString());
            if (Objects.nonNull(webSocketSession)){
                //Convert from Chat Message Entity to String
                String message = null;
                try {
                    //Set me in ChatMessage Response
                    chatMessageResponse.setMe(webSocketSession.getUserId().equals(userId));
                    message = objectMapper.writeValueAsString(chatMessageResponse);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                client.sendEvent("message", message);
            }
        });

        //Convert to Response
        return toChatMessageResponse(chatMessage);
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage){

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        ChatMessageResponse chatMessageResponse;
        chatMessageResponse = chatMessageMapper.toChatMessageResponse(chatMessage);
        chatMessageResponse.setMe(userId.equals(chatMessage.getSender().getUserId()));


        return chatMessageResponse;
    }

}