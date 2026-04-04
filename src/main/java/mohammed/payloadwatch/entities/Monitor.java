package mohammed.payloadwatch.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
    @JsonIgnore
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
    private boolean isActive = true;

    @Column(
            name = "current_status",
            nullable = true,
            length = 10
    )
    private String currentStatus = "UNKNOWN";

    @Column(
            name = "last_checked_at",
            updatable = true,
            nullable = false
    )
    private Instant lastCheckedAt = Instant.now();

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "monitor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contract> contracts = new ArrayList<>();

    public Monitor() {
    }

    public Monitor(User user, String name, String url, String httpMethod, int intervalInMinutes, List<Contract> contracts) {
        this.user = user;
        this.name = name;
        this.url = url;
        this.httpMethod = httpMethod;
        this.intervalInMinutes = intervalInMinutes;
        this.isActive = true; // default active
        this.lastCheckedAt = Instant.now(); // default timestamp
        this.createdAt = Instant.now();
        this.contracts = contracts;
    }
}
