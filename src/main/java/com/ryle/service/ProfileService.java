package com.ryle.service;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoWriteException;
import com.ryle.document.ProfileDocument;
import com.ryle.dto.ProfileDto;
import com.ryle.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileDto createProfile(ProfileDto profileDto) {
        ProfileDocument profile = ProfileDocument.builder()
            .clerkId(profileDto.getClerkId())
            .email(profileDto.getEmail())
            .firstName(profileDto.getFirstName())
            .lastName(profileDto.getLastName())
            .photoUrl(profileDto.getPhotoUrl())
            .createdAt(Instant.now())
            .credits(5)
            .build();

        profile = profileRepository.save(profile);
        return ProfileDto.builder()
             .id(profile.getId())
           .clerkId(profile.getClerkId())
           .email(profile.getEmail())
           .firstName(profile.getFirstName())
           .lastName(profile.getLastName())
           .photoUrl(profile.getPhotoUrl())
           .credits(profile.getCredits())
           .createdAt(Instant.now())
           .build();
    }
}
