package com.giaidev.chatservice.mapper;


import com.giaidev.chatservice.dto.request.ChatMessageRequest;
import com.giaidev.chatservice.dto.response.ChatMessageResponse;
import com.giaidev.chatservice.entity.ChatMessage;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(componentModel = "spring")
public interface ChatMessageMapper {
    ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage);

    ChatMessage toChatMessage(ChatMessageRequest request);

    List<ChatMessageResponse> toListChatMessageResponses(List<ChatMessage> chatMessageList);
}
