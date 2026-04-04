package mohammed.payloadwatch.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
// health log response dto
public class HealthLogResponse {
    private Instant timestamp;
    private int statusCode;
    private int latency;
    private String errorMessage;

    public HealthLogResponse(Instant timestamp, int statusCode, int latency, String errorMessage) {
        this.timestamp = timestamp;
        this.statusCode = statusCode;
        this.latency = latency;
        this.errorMessage = errorMessage;
    }
}
