package mohammed.payloadwatch.services;

import mohammed.payloadwatch.dto.HealthLogResponse;
import mohammed.payloadwatch.entities.HealthLog;
import mohammed.payloadwatch.repositories.HealthLogRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class HealthLogService {

    private final HealthLogRepository healthLogRepository;

    public HealthLogService(HealthLogRepository healthLogRepository) {
        this.healthLogRepository = healthLogRepository;
    }

    // Get monitor history
    public List<HealthLogResponse> getMonitorHistory(Long monitorId, String cognitoSub, Instant since) {
        // Fetch recent logs for the authenticated owner only
        List<HealthLog> logs = healthLogRepository.findByMonitorIdAndMonitorUserCognitoSubAndTimestampAfter(monitorId, cognitoSub, since);

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

    // Daily cleanup task to remove logs older than 7 days
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldLogs() {
        System.out.println("Running daily cleanup task to delete old health logs...");
        Instant cutoffDate = Instant.now().minus(7, ChronoUnit.DAYS);
        healthLogRepository.deleteByTimestampBefore(cutoffDate);
    }
}
