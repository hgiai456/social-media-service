package com.giaidev.chatservice.entity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_message")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessage {
    @MongoId
    String id; //Chat Message id

    @Indexed
    String conversationId; //This chat message belong to what conversation

    String message; //Content of message

    ParticipantInfo sender; //Object save participants join to the conversation

    @Indexed
    Instant createdDate; //Created Date

}
