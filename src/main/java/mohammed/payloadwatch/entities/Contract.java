package mohammed.payloadwatch.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "contracts")
@Getter
@Setter
public class Contract {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "monitor_id",
            nullable = false,
            updatable = false
    )
    @JsonIgnore
    private Monitor monitor;

    @Column(
            name = "expected_key",
            nullable = false,
            length = 100
    )
    private String expectedKey;

    @Column(
            name = "expected_type",
            nullable = false,
            length = 50
    )
    private String expectedType = "ANY";

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt = Instant.now();

    public Contract() {

    }

    public Contract(Monitor monitor, String expectedKey, String expectedType) {
        this.monitor = monitor;
        this.expectedKey = expectedKey;
        this.expectedType = expectedType;
    }
}
