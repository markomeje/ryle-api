package com.ryle.controller;
import com.ryle.dto.ProfileDto;
import com.ryle.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/v1/profile")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerProfile(@RequestBody ProfileDto profileDto) {
        ProfileDto savedProfile = profileService.createProfile(profileDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProfile);
    }
}
