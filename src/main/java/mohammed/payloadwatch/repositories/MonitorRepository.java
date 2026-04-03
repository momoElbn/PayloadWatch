package mohammed.payloadwatch.repositories;

import mohammed.payloadwatch.entities.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.Instant;
import java.util.Optional;

public interface MonitorRepository extends JpaRepository<Monitor, Long> {
    //Find monitors by the Cognito sub of the user
    @Query("SELECT m FROM Monitor m WHERE m.user.cognitoSub = :cognitoSub")
    List<Monitor> findByUserCognitoSub(@Param("cognitoSub") String cognitoSub);

    //Find monitors by the Cognito sub of the user and monitor id
    @Query("SELECT m FROM Monitor m WHERE m.user.cognitoSub = :cognitoSub AND m.id = :monitorId")
    Monitor findByUserCognitoSubAndMonitorId(@Param("cognitoSub") String cognitoSub, @Param("monitorId") Long monitorId);

    // Grabs monitors that are due for a ping
    @Query(
            value = "SELECT * FROM monitors WHERE is_active = true AND last_checked_at + (interval_minutes * interval '1 minute') <= CURRENT_TIMESTAMP",
            nativeQuery = true
    )
    List<Monitor> findMonitorsDueForPingAndIsActive();

}
