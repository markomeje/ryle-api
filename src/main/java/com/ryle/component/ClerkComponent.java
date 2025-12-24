package com.ryle.component;
import com.fasterxml.jackson.databind.JsonNode;
import com.ryle.dto.ProfileDto;
import com.ryle.service.ProfileService;
import com.ryle.service.UserCreditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClerkComponent {
    private final ProfileService profileService;
    private final UserCreditService userCreditService;
    private static final Logger logger = LoggerFactory.getLogger(ClerkComponent.class);

    public ClerkComponent(ProfileService profileService, UserCreditService userCreditService) {
        this.profileService = profileService;
        this.userCreditService = userCreditService;
    }

    private ProfileDto prepareProfileData(JsonNode data, String clerkId) {
        String email = "";
        JsonNode emails = data.path("email_addresses");
        if(emails.isArray() && !emails.isEmpty()) {
            email = emails.get(0).path("email_addresses").asText();
        }

        String firstName = data.path("first_name").asText("");
        String lastName = data.path("last_name").asText("");
        String photoUrl = data.path("image_url").asText("");

        return ProfileDto.builder()
            .clerkId(clerkId)
            .email(email)
            .firstName(firstName)
            .lastName(lastName)
            .photoUrl(photoUrl)
            .build();
    }

    public void handleUserCreatedEvent(JsonNode data) {
        String clerkId = data.path("id").asText();
        ProfileDto newProfileDto = prepareProfileData(data, clerkId);

        profileService.createProfile(newProfileDto);
        userCreditService.createInitialCredits(clerkId);
    }

    public void handleUserUpdatedEvent(JsonNode data) {
        String clerkId = data.path("id").asText();
        ProfileDto updateProfileDto = prepareProfileData(data, clerkId);
        profileService.updateProfile(updateProfileDto);
    }

    public void handleUserDeletedEvent(JsonNode data) {
        String clerkId = data.path("id").asText();
        profileService.deleteProfile(clerkId);
    }

    public boolean verifyWebhookSignature(String id, String timestamp, String signature) {
        return true;
    }
}
