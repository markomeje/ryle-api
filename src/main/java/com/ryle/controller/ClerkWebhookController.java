package com.ryle.controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryle.component.ClerkComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${clerk.webhook.secret}")
    private String webhookSecret;
    private final ClerkComponent clerkComponent;

    @PostMapping("/handle")
    public ResponseEntity<?> handleWebhook(@RequestHeader("svix-id") String id,
        @RequestHeader("svix-timestamp") String timestamp, @RequestHeader("svix-signature") String signature,
        @RequestBody String payload) {
        try {
            boolean IsValid = clerkComponent.verifyWebhookSignature(id, timestamp, signature);
            if(!IsValid) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid webhook signature");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(payload);
            String type = rootNode.path("type").asText();
            JsonNode data = rootNode.path("data");

            switch (type) {
                case "user.created":
                    clerkComponent.handleUserCreatedEvent(data);
                case "user.updated":
                    clerkComponent.handleUserUpdatedEvent(data);
                case "user.delete":
                    clerkComponent.handleUserDeletedEvent(data);
            }
        } catch (JsonProcessingException j) {
            throw new RuntimeException(j);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,  e.getMessage());
        }

        return ResponseEntity.ok(webhookSecret);
    }


}
