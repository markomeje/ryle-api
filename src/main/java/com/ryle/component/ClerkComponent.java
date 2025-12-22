package com.ryle.component;
import com.fasterxml.jackson.databind.JsonNode;
import com.ryle.dto.ProfileDto;
import com.ryle.service.ProfileService;
import org.springframework.stereotype.Component;

@Component
public class ClerkComponent {
    private final ProfileService profileService;

    public ClerkComponent(ProfileService profileService) {
        this.profileService = profileService;
    }

    private ProfileDto updateOrCreateUser(JsonNode data) {
        String clerkId = data.path("id").asText();

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
        ProfileDto newProfileDto = updateOrCreateUser(data);
        profileService.createProfile(newProfileDto);
    }

    public void handleUserUpdatedEvent(JsonNode data) {
        ProfileDto updateProfileDto = updateOrCreateUser(data);
        ProfileDto updatedProfile = profileService.updateProfile(updateProfileDto);
        if(updatedProfile == null) {
            handleUserCreatedEvent(data);
        }
    }

    public void handleUserDeletedEvent(JsonNode data) {
        String clerkId = data.path("id").asText();
        profileService.deleteProfile(clerkId);
    }

    public boolean verifyWebhookSignature(String id, String timestamp, String signature) {
        return true;
    }
}
