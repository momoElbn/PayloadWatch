package mohammed.payloadwatch.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

import java.time.Instant;

@Entity
@Table(name = "monitors")
@Getter
@Setter
public class Monitor {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(
            name = "name",
            nullable = false,
            length = 100
    )
    private String name;

    @Column(
            name = "url",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String url;

    @Column(
            name = "http_method",
            nullable = false,
            length = 10
    )
    private String httpMethod = "GET";

    @Column(
            name = "interval_minutes",
            nullable = false
    )
    private int intervalInMinutes;

    @Column(
            name = "is_active",
            nullable = false
    )
    private boolean isActive;

    @Column(
            name = "current_status",
            nullable = true,
            length = 10
    )
    private String currentStatus;

    @Column(
            name = "last_checked_at",
            updatable = true,
            nullable = false
    )
    private Instant lastCheckedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "monitor", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Contract> contracts;

    public Monitor() {
    }

    public Monitor(User user, String name, String url, HttpMethod httpMethod, int intervalInMinutes) {
        this.user = user;
        this.name = name;
        this.url = url;
        this.httpMethod = httpMethod.name();
        this.intervalInMinutes = intervalInMinutes;
        this.isActive = true; // Default to active when created
        this.lastCheckedAt = Instant.now(); // Initialize to current time
    }

    // Helper method so the rest of your Java code can still use the Spring HttpMethod class
    public HttpMethod getHttpMethodAsObject() {
        return HttpMethod.valueOf(this.httpMethod);
    }

    public void setHttpMethodFromObject(HttpMethod httpMethod) {
        this.httpMethod = httpMethod.name();
    }
}
