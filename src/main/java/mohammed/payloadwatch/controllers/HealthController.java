package mohammed.payloadwatch.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        // Simulating some dynamic metrics
        int activeThreads = (int) (Math.random() * 10) + 1;

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "PayloadWatch-Engine",
                "timestamp", Instant.now().toString(),
                "activeThreads", activeThreads,
                "databaseConnected", true
        ));
    }
}
