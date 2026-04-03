package mohammed.payloadwatch.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @Column(
            name = "cognito_sub",
            nullable = false,
            unique = true
    )
    private String cognitoSub;

    @Column(
            name = "email",
            nullable = false,
            unique = true
    )
    private String email;

    @Column(
            name = "email_alerts_enabled",
            nullable = false
    )
    private boolean emailAlertsEnabled = true;

    @Column(
            name = "plan_tier",
            nullable = false,
            length = 20
    )
    private String planTier = "free";

    @Column(
            name = "theme_preference",
            nullable = false,
            length = 20
    )
    private String themePreference = "light";

    @Column(
            name = "time_zone_preference",
            nullable = false,
            length = 20
    )
    private String timeZonePreference = "UTC";

    @CreatedDate
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Monitor> monitors;

    public User() {
    }

    public User(String cognito_sub, String email) {
        this.cognitoSub = cognito_sub;
        this.email = email;
    }
}
