package com.ryle.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ProfileDto {
    private String id;
    private String clerkId;
    private String firstName;
    private String lastName;
    private String email;
    private Integer credits;
    private String photoUrl;
    private Instant createdAt;
}
