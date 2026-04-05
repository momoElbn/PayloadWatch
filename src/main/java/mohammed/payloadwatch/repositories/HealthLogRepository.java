package mohammed.payloadwatch.repositories;

import mohammed.payloadwatch.entities.HealthLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.Instant;

public interface HealthLogRepository extends JpaRepository<HealthLog, Long> {

    // get recent health logs scoped to the owning user
    List<HealthLog> findByMonitorIdAndMonitorUserCognitoSubAndTimestampAfter(Long monitorId, String cognitoSub, Instant since);

    // get latest health log
    HealthLog findFirstByMonitorIdOrderByTimestampDesc(Long monitorId);

    // MAINTENANCE: Deletes logs older than a specific date to prevent database bloat
    void deleteByTimestampBefore(Instant cutoffDate);
}
