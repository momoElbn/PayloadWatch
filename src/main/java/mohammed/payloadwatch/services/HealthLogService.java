package mohammed.payloadwatch.services;

import mohammed.payloadwatch.dto.HealthLogResponse;
import mohammed.payloadwatch.entities.HealthLog;
import mohammed.payloadwatch.repositories.HealthLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class HealthLogService {

    private final HealthLogRepository healthLogRepository;

    public HealthLogService(HealthLogRepository healthLogRepository) {
        this.healthLogRepository = healthLogRepository;
    }

    // Get monitor history
    public List<HealthLogResponse> getMonitorHistory(Long monitorId, Instant since) {
        // Fetch recent logs
        List<HealthLog> logs = healthLogRepository.findByMonitorIdAndTimestampAfter(monitorId, since);

        // Map logs to DTOs for frontend
        List<HealthLogResponse> responses = new ArrayList<>();
        for (HealthLog log : logs) {
            responses.add(new HealthLogResponse(
                    log.getTimestamp(),
                    log.getStatusCode(),
                    log.getLatencyMs(),
                    log.getErrorMessage()
            ));
        }
        return responses;
    }

}
