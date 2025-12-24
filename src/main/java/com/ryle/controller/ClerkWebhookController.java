package com.ryle.controller;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryle.component.ClerkComponent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhook/clerk")
public class ClerkWebhookController {
    private final ClerkComponent clerkComponent;
    private static final Logger logger = LoggerFactory.getLogger(ClerkWebhookController.class);

    @PostMapping("/handle")
    public ResponseEntity<?> handleWebhook(@RequestHeader("svix-id") String id,
        @RequestHeader("svix-timestamp") String timestamp, @RequestHeader("svix-signature") String signature,
        @RequestBody String payload) {
        try {
            boolean isValid = clerkComponent.verifyWebhookSignature(id, timestamp, signature);
            if(!isValid) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid webhook signature");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode webhookPayload = mapper.readTree(payload);

            String type = webhookPayload.path("type").asText();
            JsonNode data = webhookPayload.path("data");

            switch (type) {
                case "user.created":
                    clerkComponent.handleUserCreatedEvent(data);
                case "user.updated":
                    clerkComponent.handleUserUpdatedEvent(data);
                case "user.delete":
                    clerkComponent.handleUserDeletedEvent(data);
            }

            return ResponseEntity.status(HttpStatus.OK).body("Clerk webhook handled successfully");
        } catch (Exception e) {
            logger.error("Unauthorised Clerk webhook recieved: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,  e.getMessage());
        }
    }


}
