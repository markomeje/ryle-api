package com.ryle.service;
import com.ryle.document.ProfileDocument;
import com.ryle.dto.ProfileDto;
import com.ryle.exception.ApiException;
import com.ryle.exception.ResourceNotFoundException;
import com.ryle.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileDto createProfile(ProfileDto profileDto) {
        if (profileRepository.existsByClerkId(profileDto.getClerkId())) {
            throw new ApiException("User already exists");
        }

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

    public ProfileDto updateProfile(ProfileDto profileDto) {
        ProfileDocument existingProfile = profileRepository.findByClerkId(profileDto.getClerkId())
           .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        if(profileDto.getEmail() != null && !profileDto.getEmail().isEmpty()) {
            existingProfile.setEmail(profileDto.getEmail());
        }

        if(profileDto.getFirstName() != null && !profileDto.getFirstName().isEmpty()) {
            existingProfile.setFirstName(profileDto.getFirstName());
        }

        if(profileDto.getLastName() != null && !profileDto.getLastName().isEmpty()) {
            existingProfile.setLastName(profileDto.getLastName());
        }

        if(profileDto.getPhotoUrl() != null && !profileDto.getPhotoUrl().isEmpty()) {
            existingProfile.setPhotoUrl(profileDto.getPhotoUrl());
        }

        existingProfile.setCredits(profileDto.getCredits());
        profileRepository.save(existingProfile);

        return ProfileDto.builder()
            .id(existingProfile.getId())
            .email(existingProfile.getEmail())
            .firstName(existingProfile.getFirstName())
            .lastName(existingProfile.getLastName())
            .clerkId(existingProfile.getClerkId())
            .credits(existingProfile.getCredits())
            .photoUrl(existingProfile.getPhotoUrl())
            .createdAt(existingProfile.getCreatedAt())
            .build();
    }

    public void deleteProfile(String clerkId) {
        ProfileDocument existingProfile = profileRepository.findByClerkId(clerkId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        profileRepository.delete(existingProfile);
    }
}
