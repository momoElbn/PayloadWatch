package mohammed.payloadwatch.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
// This DTO represents the expected structure of the response for the frontend when fetching health logs
public class HealthLogResponse {
    private Instant timestamp;
    private int statusCode;
    private int latency;

    public HealthLogResponse(Instant timestamp, int statusCode, int latency) {
        this.timestamp = timestamp;
        this.statusCode = statusCode;
        this.latency = latency;
    }
}
