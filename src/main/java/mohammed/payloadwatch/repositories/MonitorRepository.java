package mohammed.payloadwatch.repositories;

import mohammed.payloadwatch.entities.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MonitorRepository extends JpaRepository<Monitor, Long> {
    //Find monitors by the user id
    List<Monitor> findByUserId(Long userId);

    //Find monitors by the user id and monitor id
    Monitor findByUserIdAndId(Long userId, Long monitorId);

    // Grabs monitors that are due for a ping
    @Query(
            value = "SELECT * FROM monitors WHERE is_active = true AND last_checked_at + (interval_minutes * interval '1 minute') <= CURRENT_TIMESTAMP",
            nativeQuery = true
    )
    List<Monitor> findMonitorsDueForPingAndIsActive();

    // Load monitor + contracts in one query so async workers can safely access contracts
    @Query("SELECT DISTINCT m FROM Monitor m LEFT JOIN FETCH m.contracts WHERE m.id = :monitorId")
    Optional<Monitor> findByIdWithContracts(@Param("monitorId") Long monitorId);

}
