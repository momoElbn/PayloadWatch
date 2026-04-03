package mohammed.payloadwatch.controllers;

import mohammed.payloadwatch.entities.User;
import mohammed.payloadwatch.services.HealthLogService;
import mohammed.payloadwatch.services.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/health-logs")
public class HealthLogController {

    private final HealthLogService healthLogService;
    private final UserService userService;

    public HealthLogController(HealthLogService healthLogService, UserService userService) {
        this.healthLogService = healthLogService;
        this.userService = userService;
    }

    @GetMapping("/{monitorId}")
    public Object getMonitorHistory(@AuthenticationPrincipal Jwt jwt, @PathVariable Long monitorId, @RequestParam(required = false, defaultValue = "1h") String range) {

        User user = userService.getOrCreateUser(jwt);

        // Si le frontend n'envoie pas de paramètre de temps, on donne les dernières 24 heures par défaut
        Instant since = Instant.now().minus(1, ChronoUnit.HOURS);

        if ("24h".equals(range)) {
            since = Instant.now().minus(24, ChronoUnit.HOURS);
        } else if ("7d".equals(range)) {
            since = Instant.now().minus(7, ChronoUnit.DAYS);
        }

        return healthLogService.getMonitorHistory(monitorId, since);
    }
}
