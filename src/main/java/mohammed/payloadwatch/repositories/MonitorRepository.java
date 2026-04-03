package mohammed.payloadwatch.repositories;

import mohammed.payloadwatch.entities.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.Instant;
import java.util.Optional;

public interface MonitorRepository extends JpaRepository<Monitor, Long> {
    // SECURITY: Ensures a user can only fetch, edit, or delete their own monitors
    Monitor findByIdAndUserId(Long id, Long userid);

    // Fetches all monitors for a specific user (used in the dashboard)
    List<Monitor> findByUserId(Long userId);

    // Grabs monitors that are due for a ping
    @Query(
            value = "SELECT * FROM monitors WHERE is_active = true AND last_checked_at + (interval_minutes * interval '1 minute') <= CURRENT_TIMESTAMP",
            nativeQuery = true
    )
    List<Monitor> findMonitorsDueForPingAndIsActive();

}
