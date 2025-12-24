package com.ryle.document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "profiles")
public class ProfileDocument {
    @Id
    private String id;
    private  String clerkId;
    private  String firstName;
    private  String lastName;

    @Indexed(unique = true)
    private  String email;
    private  Integer credits;
    private  String photoUrl;

    @CreatedDate
    private Instant createdAt;

}
