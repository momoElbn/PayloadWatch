package mohammed.payloadwatch.repositories;

import mohammed.payloadwatch.entities.HealthLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

public interface HealthLogRepository extends JpaRepository<HealthLog, Long> {

    // Fetch all health logs for a specific monitor since a given timestamp (used for the "History" tab)
    List<HealthLog> findByMonitorIdAndTimestampAfter(Long monitorId, Instant since);

    // Used to show the "Current Status" on the dashboard
    Optional<HealthLog> findFirstByMonitorIdOrderByTimestampDesc(Long monitorId);

    // MAINTENANCE: Deletes logs older than a specific date to prevent database bloat
    void deleteByTimestampBefore(Instant cutoffDate);
}
