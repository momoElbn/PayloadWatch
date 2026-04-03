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

    // Add this to your MonitorService (or a new HealthLogService)
    public List<HealthLogResponse> getMonitorHistory(Long monitorId, Instant since) {
        // 1. Fetch the top 50 most recent logs from the repository
        List<HealthLog> logs = healthLogRepository.findByMonitorIdAndTimestampAfter(monitorId, since);

        // 2. Map them to your DTO
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
