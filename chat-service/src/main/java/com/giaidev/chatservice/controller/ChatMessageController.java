package com.giaidev.chatservice.controller;

import com.giaidev.chatservice.dto.ApiResponse;
import com.giaidev.chatservice.dto.request.ChatMessageRequest;
import com.giaidev.chatservice.dto.response.ChatMessageResponse;
import com.giaidev.chatservice.service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("messages")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageController {
    ChatMessageService chatMessageService;

    @GetMapping
    ApiResponse<List<ChatMessageResponse>> createMessage(@RequestParam("conversationId") String conversationId) {
        return ApiResponse.<List<ChatMessageResponse>>builder()
                .result(chatMessageService.getMessages(conversationId))
                .build();
    }

    @PostMapping("/create")
    ApiResponse<ChatMessageResponse> createMessage(@RequestBody @Valid ChatMessageRequest request) {
        return ApiResponse.<ChatMessageResponse>builder()
                .result(chatMessageService.create(request))
                .build();
    }


}