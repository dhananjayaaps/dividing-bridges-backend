package org.penpal.email.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.penpal.shared.MessageStatus;
import org.penpal.shared.MessageType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Data
@Document(collection = "email-threads")
@CompoundIndex(name = "message_sentAt_index", def = "{'messages.sentAt': 1}")
public class EmailThread {
    @Id
    private String threadId;
    private String subject;
    private List<String> participants;
    private List<Message> messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @Data
    public static class Message {
        @Id
        private String messageId;
        private String sender;
        private String body;
        private MessageStatus status;
        private LocalDateTime sentAt;
        private String language;
        private MessageType type;
        private List<Attachment> attachments;
        private ModerationRecord moderationRecord;
        private String isDraft;

        public Message(String messageId, String sender, String body, MessageStatus status, LocalDateTime now,
                       String language, MessageType type, String isDraft) {
            this.messageId = messageId;
            this.sender = sender;
            this.body = body;
            this.status = status;
            this.sentAt = now;
            this.language = language;
            this.type = type;
            this.isDraft = isDraft;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @Data
        public static class ModerationRecord {
            private String moderator;
            private MessageStatus action;
            private String reason;
            private LocalDateTime timestamp;

            public ModerationRecord(String moderator, MessageStatus action, String reason, LocalDateTime timestamp) {
                this.moderator = moderator;
                this.action = action;
                this.reason = reason;
                this.timestamp = timestamp;
            }
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @Data
        public static class Attachment {
            private String fileName;
            private String fileType;
            private byte[] fileData;

            public Attachment(String fileName, String fileType, byte[] fileData) {
                this.fileName = fileName;
                this.fileType = fileType;
                this.fileData = fileData;
            }
        }
    }

}
