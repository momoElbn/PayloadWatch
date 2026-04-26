package mohammed.payloadwatch.controllers;

import mohammed.payloadwatch.entities.User;
import mohammed.payloadwatch.services.HealthLogService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/health-logs")
public class HealthLogController {

    private final HealthLogService healthLogService;

    public HealthLogController(HealthLogService healthLogService) {
        this.healthLogService = healthLogService;
    }

    @GetMapping("/{monitorId}")
    public Object getMonitorHistory(@AuthenticationPrincipal User user, @PathVariable Long monitorId, @RequestParam(required = false, defaultValue = "1h") String range) {

        // dafault to last 1 hour
        Instant since = Instant.now().minus(1, ChronoUnit.HOURS);

        if ("24h".equals(range)) {
            since = Instant.now().minus(24, ChronoUnit.HOURS);
        } else if ("7d".equals(range)) {
            since = Instant.now().minus(7, ChronoUnit.DAYS);
        }

        return healthLogService.getMonitorHistory(monitorId, user.getId(), since);
    }
}
