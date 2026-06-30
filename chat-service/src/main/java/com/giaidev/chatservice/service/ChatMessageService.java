package com.giaidev.chatservice.service;

import com.giaidev.chatservice.dto.request.ChatMessageRequest;
import com.giaidev.chatservice.dto.request.ConversationRequest;
import com.giaidev.chatservice.dto.response.ChatMessageResponse;
import com.giaidev.chatservice.dto.response.ConversationResponse;
import com.giaidev.chatservice.dto.response.UserProfileResponse;
import com.giaidev.chatservice.entity.ChatMessage;
import com.giaidev.chatservice.entity.Conversation;
import com.giaidev.chatservice.entity.ParticipantInfo;
import com.giaidev.chatservice.exception.AppException;
import com.giaidev.chatservice.exception.ErrorCode;
import com.giaidev.chatservice.mapper.ChatMessageMapper;
import com.giaidev.chatservice.mapper.ConversationMapper;
import com.giaidev.chatservice.repository.ChatMessageRepository;
import com.giaidev.chatservice.repository.ConversationRepository;
import com.giaidev.chatservice.repository.httpclient.ProfileClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageService {
    ChatMessageRepository chatMessageRepository;
    ConversationRepository conversationRepository;
    ProfileClient profileClient;
    ChatMessageMapper chatMessageMapper;

    public List<ChatMessageResponse> getMessages(String conversationId) {
        //Validate conversationId
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        var conversation = conversationRepository.findById(conversationId).orElseThrow(
                () -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        conversation.getParticipants().stream()
                .filter(participantInfo -> userId.equals(participantInfo.getUserId()))
                .findAny().orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        var messages = chatMessageRepository.findAllByConversationIdOrderByCreatedDateDesc(conversationId);

        return messages.stream()
                .map(this::toChatMessageResponse).toList();
//        return messages.stream()
//                .map(message -> toChatMessageResponse(message)).toList();

    }

    public ChatMessageResponse create(ChatMessageRequest request){

        //Validate conversationId
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        var conversation = conversationRepository.findById(request.getConversationId()).orElseThrow(
                () -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

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

        //Create chat message
        chatMessage = chatMessageRepository.save(chatMessage);

        //Convert to Response
        return toChatMessageResponse(chatMessage);
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage){

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        ChatMessageResponse chatMessageResponse;
        chatMessageResponse = chatMessageMapper.toChatMessageResponse(chatMessage);
        chatMessageResponse.setMe(userId.equals(chatMessage.getSender().getUserId())); //


        return chatMessageResponse;
    }

}