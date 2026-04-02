package mohammed.payloadwatch.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(
        name = "health_logs",
        indexes = {
                @Index(name = "idx_health_logs_monitor_id", columnList = "monitor_id")
        }
)
@Getter
@Setter
public class HealthLog {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @ManyToOne
    @JoinColumn(
            name = "monitor_id",
            nullable = false,
            updatable = false
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Monitor monitor;

    @Column(
            name = "status_code",
            nullable = false,
            updatable = false
    )
    private int statusCode;

    @Column(
            name = "latency_ms",
            nullable = false,
            updatable = false
    )
    private int latencyMs;

    @Column(
            name = "is_success",
            nullable = false,
            updatable = false
    )
    private boolean isSuccess;

    @Column(
            name = "error_message",
            columnDefinition = "TEXT"
    )
    private String errorMessage;

    @Column(
            name = "timestamp",
            nullable = false,
            updatable = false
    )
    private Instant timestamp = Instant.now();

    public HealthLog() {
    }

    public HealthLog(Monitor monitor, int statusCode, int latencyMs, boolean isSuccess, String errorMessage) {
        this.monitor = monitor;
        this.statusCode = statusCode;
        this.latencyMs = latencyMs;
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
    }
}
